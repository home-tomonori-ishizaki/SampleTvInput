package com.example.sampletvinput.ui;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

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
        new ShowSpinnerTask().execute();
        Log.i(TAG, "onResume start");
        super.onResume();
    }


    private class ShowSpinnerTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Do some background process here.
            SystemClock.sleep(2000);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.i(TAG, "completed");
            ((SampleInputSetupActivity)getActivity()).scanChannelsCompleted();
        }
    }
}

