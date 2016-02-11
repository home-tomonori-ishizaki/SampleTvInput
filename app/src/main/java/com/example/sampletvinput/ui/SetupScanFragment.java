package com.example.sampletvinput.ui;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.media.tv.TvContract;
import android.media.tv.TvInputInfo;
import android.net.Uri;
import android.net.http.HttpResponseCache;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.sampletvinput.data.NhkService;
import com.example.sampletvinput.data.Program;
import com.example.sampletvinput.util.HttpUtils;
import com.example.sampletvinput.util.NhkUtils;
import com.example.sampletvinput.util.PreferenceUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SetupScanFragment extends Fragment {
    private static final String TAG = SetupScanFragment.class.getSimpleName();

    private static final int SPINNER_WIDTH = 100;
    private static final int SPINNER_HEIGHT = 100;

    public static SetupScanFragment newInstance(boolean isInitialScan) {
        SetupScanFragment fragment = new SetupScanFragment();
        Bundle args = new Bundle();
        args.putBoolean("mode", isInitialScan);
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
        boolean isInitialScan = args.getBoolean("mode");
        new ScanTask(isInitialScan).execute(getActivity().getIntent().getStringExtra(TvInputInfo.EXTRA_INPUT_ID));
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

        private boolean mIsInitialScan = false;

        public ScanTask(boolean isInitialScan) {
            mIsInitialScan = isInitialScan;
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

            // delete programs for old channels
            deletePrograms(resolver, channelUri);

            if (mIsInitialScan) {
                // delete old channels and add new channels
                resolver.delete(channelUri, null, null);
                addChannels(resolver, channelUri, inputId, apiKey);
            }

            addPrograms(resolver, channelUri, apiKey);

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

        private void addChannels(ContentResolver resolver, Uri channelUri, String inputId, String apiKey) {
            Map<String, String> logoMap = new HashMap<>();

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

        private void addPrograms(ContentResolver resolver,Uri channelUri, String apiKey) {
            // add new programs
            try (Cursor cursor = resolver.query(channelUri, null, null, null, null)) {
                // COLUMN_SEARCHABLE is supported above api-level 23(M)
                boolean hasSearchable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;

                int idxChannelId = cursor.getColumnIndexOrThrow(TvContract.Channels._ID);
                int idxServiceId = cursor.getColumnIndexOrThrow(TvContract.Channels.COLUMN_SERVICE_ID);
                while (cursor.moveToNext()) {
                    long channelId = cursor.getLong(idxChannelId);
                    String serviceId = cursor.getString(idxServiceId);
                    List<Program> programs = NhkUtils.getPrograms(serviceId, apiKey);
                    if (programs == null) {
                        continue;
                    }
                    ContentValues  values = new ContentValues();
                    values.put(TvContract.Programs.COLUMN_CHANNEL_ID, channelId);
                    if (hasSearchable) {
                        values.put(TvContract.Programs.COLUMN_SEARCHABLE, 1);
                    }

                    for (Program program : programs) {
                        values.put(TvContract.Programs.COLUMN_TITLE, program.getName());
                        values.put(TvContract.Programs.COLUMN_START_TIME_UTC_MILLIS, program.getStartTime());
                        values.put(TvContract.Programs.COLUMN_END_TIME_UTC_MILLIS, program.getEndTime());
                        values.put(TvContract.Programs.COLUMN_CANONICAL_GENRE, program.getGenre());
                        resolver.insert(TvContract.Programs.CONTENT_URI, values);
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result != RESULT_SUCCESS) {
                String message = "Fail";
                if (result == RESULT_FAIL_REASON_API_KEY) {
                    message = "API Key is empty";
                }
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
            ((SampleInputSetupActivity)getActivity()).scanChannelsCompleted();
        }
    }
}

