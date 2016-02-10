package com.example.sampletvinput.ui;

import android.os.Bundle;
import android.app.Activity;

public class SampleInputSetupActivity extends Activity {

    public static final String MODE = "mode";
    public static final int   MODE_NONE   = 0;
    public static final int   MODE_UPDATE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int mode = getIntent().getIntExtra(MODE, MODE_NONE);
        if (mode == MODE_UPDATE) {
            getFragmentManager().beginTransaction()
                    .add(android.R.id.content, SetupScanFragment.newInstance(false))
                    .commit();
        } else {
            getFragmentManager().beginTransaction()
                    .add(android.R.id.content, new SetupApiKeyFragment())
                    .commit();
        }
    }

    public void scanChannels() {
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, SetupScanFragment.newInstance(true))
                .commit();
    }

    public void scanChannelsCompleted() {
        setResult(Activity.RESULT_OK);
        finish();
    }
}
