package com.example.sampletvinput.ui;

import android.app.Fragment;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.media.tv.TvContentRating;
import android.media.tv.TvContract;
import android.media.tv.TvInputInfo;
import android.net.Uri;
import android.net.http.HttpResponseCache;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.sampletvinput.R;
import com.example.sampletvinput.data.NhkService;
import com.example.sampletvinput.model.Program;
import com.example.sampletvinput.util.HttpUtils;
import com.example.sampletvinput.util.NhkUtils;
import com.example.sampletvinput.util.PreferenceUtils;
import com.example.sampletvinput.util.TvContractUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SetupScanFragment extends Fragment {
    private static final String TAG = SetupScanFragment.class.getSimpleName();

    private static final int SPINNER_WIDTH = 100;
    private static final int SPINNER_HEIGHT = 100;

    public static SetupScanFragment newInstance(int mode) {
        SetupScanFragment fragment = new SetupScanFragment();
        Bundle args = new Bundle();
        args.putInt("mode", mode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ProgressBar progressBar = new ProgressBar(container.getContext());
        if (container instanceof FrameLayout) {
            FrameLayout.LayoutParams layoutParams =
                    new FrameLayout.LayoutParams(SPINNER_WIDTH, SPINNER_HEIGHT, Gravity.CENTER);
            progressBar.setLayoutParams(layoutParams);
        }
        return progressBar;
    }

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            File httpCacheDir = new File(getActivity().getApplicationContext().getCacheDir(), "http");
            long httpCacheSize = 10 * 1024 * 1024; // 10 MiB
            HttpResponseCache.install(httpCacheDir, httpCacheSize);
        }  catch(IOException e){
            Log.i(TAG, "HTTP response cache installation failed:" + e);
        }
    }

    @Override
    public void onStop () {
        super.onStop();

        HttpResponseCache cache = HttpResponseCache.getInstalled();
        if (cache != null) {
            cache.flush();
        }
    }

    @Override
    public void onResume() {
        Bundle args = getArguments();
        int mode = args.getInt("mode");
        new ScanTask(mode).execute(getActivity().getIntent().getStringExtra(TvInputInfo.EXTRA_INPUT_ID));
        super.onResume();
    }

    private static final List<String> CHANNEL_LIST = Collections.unmodifiableList(new LinkedList<String>() {
        {
            add("g1"); // ＮＨＫ総合１
            add("e1"); // ＮＨＫＥテレ１
            add("e4"); // ＮＨＫワンセグ２
            add("s1"); // ＮＨＫＢＳ１
            add("s3"); // ＮＨＫＢＳプレミアム
            add("r1"); // ＨＫラジオ第1
            add("r2"); // ＮＨＫラジオ第2
            add("r3"); // ＮＨＫＦＭ
        }
    });

    private class ScanTask extends AsyncTask<String, Void, Integer> {

        private static final int RESULT_SUCCESS = 0;
        private static final int RESULT_FAIL_REASON_API_KEY = 1;
        private static final int RESULT_FAIL_REASON_OTHER = 2;

        private int mMode = SampleInputSetupActivity.MODE_NONE;

        public ScanTask(int mode) {
            mMode = mode;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Integer doInBackground(String... params) {
            // check api key
            String apiKey = PreferenceUtils.getApiKey(getActivity());
            if (TextUtils.isEmpty(apiKey)) {
                return RESULT_FAIL_REASON_API_KEY;
            }

            // check input id
            String inputId = params[0];
            if (TextUtils.isEmpty(inputId)) {
                return RESULT_FAIL_REASON_OTHER;
            }
            Log.i(TAG, "inputId:" + inputId);

            ContentResolver resolver = getActivity().getContentResolver();
            Uri channelUri = TvContract.buildChannelsUriForInput(inputId);

            try {
                if (mMode == SampleInputSetupActivity.MODE_NONE) {
                    // delete old programs and channels
                    deletePrograms(resolver, channelUri);
                    resolver.delete(channelUri, null, null);
                    // add channels and programs
                    addChannels(resolver, channelUri, inputId, apiKey);
                    addPrograms(resolver, channelUri, apiKey);
                } else if (mMode == SampleInputSetupActivity.MODE_UPDATE) {
                    // delete programs for old channels
                    deletePrograms(resolver, channelUri);
                    // add programs
                    addPrograms(resolver, channelUri, apiKey);
                } else if (mMode == SampleInputSetupActivity.MODE_UPDATE_ONLY_CURRENT) {
                    // add extra information for current programs
                    updateCurrentPrograms(resolver, inputId, apiKey, 0);
                } else if (mMode == SampleInputSetupActivity.MODE_UPDATE_ONLY_TODAY) {
                    // add extra information for current programs
                    updateCurrentPrograms(resolver, inputId, apiKey, 1000 * 60 * 60 * 24);
                }

            } catch (HttpUtils.BadRequestException e) {
                return RESULT_FAIL_REASON_OTHER;
            } catch (HttpUtils.UnauthorizedException e) {
                return RESULT_FAIL_REASON_API_KEY;
            }

            return RESULT_SUCCESS;
        }

        private void deletePrograms(ContentResolver resolver, Uri channelUri) {
            // delete old programs,
            // need to loop for each channel because SecurityException happens if selection is set
            try (Cursor cursor = resolver.query(channelUri, null, null, null, null)) {
                int idxChannelId = cursor.getColumnIndexOrThrow(TvContract.Channels._ID);
                while (cursor.moveToNext()) {
                    long channelId = cursor.getLong(idxChannelId);
                    Uri programUri = TvContract.buildProgramsUriForChannel(channelId);
                    resolver.delete(programUri, null, null);
                }
            }
        }

        private void addChannels(ContentResolver resolver, Uri channelUri, String inputId, String apiKey)
                throws HttpUtils.UnauthorizedException, HttpUtils.BadRequestException {
            Map<String, String> logoMap = new HashMap<>();

            // COLUMN_APP_LINK_XXX are supported above api-level 23(M)
            boolean hasAppLink = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;

            // add channels
            int channelNumber = 1;
            for (String serviceId : CHANNEL_LIST) {
                NhkService service = NhkUtils.getService(serviceId, apiKey);
                if (service == null || service.logo == null || service.logo.url == null) {
                    continue;
                }
                //Log.i(TAG, "name:" + service.name + " logo:" + service.logo.url);
                logoMap.put(serviceId, service.logo.url);

                // add channel
                ContentValues values = new ContentValues();
                values.put(TvContract.Channels.COLUMN_INPUT_ID, inputId);
                values.put(TvContract.Channels.COLUMN_DISPLAY_NUMBER, String.valueOf(channelNumber));
                values.put(TvContract.Channels.COLUMN_DISPLAY_NAME, service.name);
                values.put(TvContract.Channels.COLUMN_SERVICE_ID, serviceId);

                // update app link
                if (hasAppLink) {
                    Intent appLinkIntent = new Intent(Intent.ACTION_VIEW);
                    appLinkIntent.setClass(getActivity(), ProgramDetailsActivity.class);
                    appLinkIntent.putExtra(TvContract.Channels.COLUMN_DISPLAY_NUMBER, String.valueOf(channelNumber));
                    values.put(TvContract.Channels.COLUMN_APP_LINK_INTENT_URI, appLinkIntent.toUri(Intent.URI_INTENT_SCHEME));
                    values.put(TvContract.Channels.COLUMN_APP_LINK_POSTER_ART_URI, service.logo.url);
                    values.put(TvContract.Channels.COLUMN_APP_LINK_TEXT, getActivity().getString(R.string.app_link_text));
                }

                resolver.insert(TvContract.Channels.CONTENT_URI, values);

                ++channelNumber;
            }

            // add logo
            try (Cursor cursor = resolver.query(channelUri, null, null, null, null)) {
                int idxChannelId = cursor.getColumnIndexOrThrow(TvContract.Channels._ID);
                int idxServiceId = cursor.getColumnIndexOrThrow(TvContract.Channels.COLUMN_SERVICE_ID);
                while (cursor.moveToNext()) {
                    String serviceId = cursor.getString(idxServiceId);
                    String logoUrl = logoMap.get(serviceId);
                    Log.i(TAG, "serviceId:" + serviceId + " logo:" + logoUrl);
                    if (logoUrl != null) {
                        long channelId = cursor.getLong(idxChannelId);
                        byte[] logo = HttpUtils.getBytes(logoUrl);
                        Log.i(TAG, "logo size:" + logo.length);
                        writeChannelLogo(resolver, channelId, logo);
                    }
                }
            }
        }

        private void writeChannelLogo(ContentResolver resolver, long channelId, byte[] logo) {
            Uri channelLogoUri = TvContract.buildChannelLogoUri(channelId);
            try {
                AssetFileDescriptor fd = resolver.openAssetFileDescriptor(channelLogoUri, "rw");
                OutputStream os = fd.createOutputStream();
                os.write(logo);
                os.close();
                fd.close();
            } catch (IOException e) {
                // Handle error cases.
            }
        }

        private void addPrograms(ContentResolver resolver, Uri channelUri, String apiKey)
                throws HttpUtils.BadRequestException, HttpUtils.UnauthorizedException {

            // add new programs
            try (Cursor cursor = resolver.query(channelUri, null, null, null, null)) {
                // COLUMN_SEARCHABLE is supported above api-level 23(M)
                boolean hasSearchable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;

                int idxChannelId = cursor.getColumnIndexOrThrow(TvContract.Channels._ID);
                int idxServiceId = cursor.getColumnIndexOrThrow(TvContract.Channels.COLUMN_SERVICE_ID);

                int count = 0;
                ArrayList<ContentProviderOperation> ops = new ArrayList<>();

                while (cursor.moveToNext()) {
                    long channelId = cursor.getLong(idxChannelId);
                    String serviceId = cursor.getString(idxServiceId);
                    List<Program> programs = NhkUtils.getPrograms(serviceId, apiKey);
                    if (programs == null) {
                        continue;
                    }

                    for (Program program : programs) {

                        ContentProviderOperation.Builder builder =
                                ContentProviderOperation.newInsert(TvContract.Programs.CONTENT_URI)
                                        .withValue(TvContract.Programs.COLUMN_CHANNEL_ID, channelId)
                                        .withValue(TvContract.Programs.COLUMN_TITLE, program.getName())
                                        .withValue(TvContract.Programs.COLUMN_SHORT_DESCRIPTION, program.getDescription())
                                        .withValue(TvContract.Programs.COLUMN_START_TIME_UTC_MILLIS, program.getStartTime())
                                        .withValue(TvContract.Programs.COLUMN_END_TIME_UTC_MILLIS, program.getEndTime())
                                        .withValue(TvContract.Programs.COLUMN_CANONICAL_GENRE, program.getGenre())
                                        .withValue(TvContract.Programs.COLUMN_VERSION_NUMBER, program.getId());
                        if (hasSearchable) {
                            builder.withValue(TvContract.Programs.COLUMN_SEARCHABLE, 1);
                        }
                        ops.add(builder.build());

                        ++count;
                        if (count == 100) {
                            applyBatch(resolver, ops);
                            count = 0;
                        }
                    }
                }
                if (ops.size() > 0) {
                    applyBatch(resolver, ops);
                }
            }
        }

        private void applyBatch(ContentResolver resolver, ArrayList<ContentProviderOperation> ops) {
            try {
                resolver.applyBatch(TvContract.AUTHORITY, ops);
            } catch (RemoteException | OperationApplicationException e) {
                e.printStackTrace();
            }
            ops.clear();
        }

        private void updateCurrentPrograms(ContentResolver resolver, String inputId, String apiKey, long duration) {
            // get channel id list
            Map<Long, String> channelIdMap = TvContractUtils.getChannelIds(resolver, inputId);

            long current = System.currentTimeMillis();
            for (Map.Entry<Long, String> entry : channelIdMap.entrySet()) {
                long channelId = entry.getKey();
                String serviceId = entry.getValue();
                Uri programUri = TvContract.buildProgramsUriForChannel(channelId, current, current + duration);
                try (Cursor cursor = resolver.query(programUri, null, null, null, null)) {
                    if (cursor == null || !cursor.moveToFirst()) {
                        continue;
                    }

                    do  {
                        int idxId = cursor.getColumnIndexOrThrow(TvContract.Programs._ID);
                        int idxVersion = cursor.getColumnIndexOrThrow(TvContract.Programs.COLUMN_VERSION_NUMBER);

                        long id = cursor.getLong(idxId);
                        long programId = cursor.getLong(idxVersion);
                        Program program = NhkUtils.getProgram(programId, serviceId, apiKey);
                        if (program == null) {
                            continue;
                        }
                        String thumbnailUrl = program.getThumbnailUrl();

                        // update thumbnail url
                        if (thumbnailUrl != null) {
                            ContentValues values = new ContentValues();
                            values.put(TvContract.Programs.COLUMN_POSTER_ART_URI, thumbnailUrl);
                            resolver.update(TvContract.buildProgramUri(id), values, null, null);
                        }
                    } while (cursor.moveToNext());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result != RESULT_SUCCESS) {
                String message = "Fail, current time may be invalid";
                if (result == RESULT_FAIL_REASON_API_KEY) {
                    message = "API Key is empty or invalid";
                }
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
            ((SampleInputSetupActivity)getActivity()).scanChannelsCompleted();
        }
    }
}

