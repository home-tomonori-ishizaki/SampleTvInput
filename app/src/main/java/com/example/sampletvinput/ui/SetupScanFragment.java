package com.example.sampletvinput.ui;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.media.tv.TvContract;
import android.media.tv.TvInputInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class SetupScanFragment extends Fragment {
    private static final String TAG = SetupScanFragment.class.getSimpleName();

    private static final int SPINNER_WIDTH = 100;
    private static final int SPINNER_HEIGHT = 100;

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
    public void onResume() {
        new ScanTask().execute(getActivity().getIntent().getStringExtra(TvInputInfo.EXTRA_INPUT_ID));
        super.onResume();
    }

    private static final Map<String, String> CHANNEL_MAP = Collections.unmodifiableMap(new LinkedHashMap<String, String>()
    {
        {
            put("g1", "ＮＨＫ総合１");
            put("e1", "ＮＨＫＥテレ１");
            put("e4", "ＮＨＫワンセグ２");
            put("s1", "ＮＨＫＢＳ１");
            put("s3", "ＮＨＫＢＳプレミアム");
            put("r1", "ＮＨＫラジオ第1");
            put("r2", "ＮＨＫラジオ第2");
            put("r3", "ＮＨＫＦＭ");
        }
    });

    private class ScanTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(String... params) {
            String inputId = params[0];
            Log.i(TAG, "inputId:" + inputId);

            ContentResolver resolver = getActivity().getContentResolver();

            // delete old channels
            resolver.delete(TvContract.buildChannelsUriForInput(inputId), null, null);

            int channelNumber = 1;
            for (Map.Entry<String, String> entry : CHANNEL_MAP.entrySet()) {
                ContentValues values = new ContentValues();
                values.put(TvContract.Channels.COLUMN_INPUT_ID, inputId);
                values.put(TvContract.Channels.COLUMN_DISPLAY_NUMBER, String.valueOf(channelNumber));
                values.put(TvContract.Channels.COLUMN_DISPLAY_NAME, entry.getValue());
                values.put(TvContract.Channels.COLUMN_SERVICE_ID, entry.getKey());
                resolver.insert(TvContract.Channels.CONTENT_URI, values);

                ++channelNumber;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.i(TAG, "completed");
            ((SampleInputSetupActivity)getActivity()).scanChannelsCompleted();
        }
    }
}

