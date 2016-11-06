package com.example.sampletvinput.ui;

import android.os.Bundle;
import android.app.Activity;

public class SampleInputSetupActivity extends Activity {

    public static final String MODE = "mode";
    public static final int   MODE_NONE   = 0;                 // update all
    public static final int   MODE_UPDATE = 1;                 // update only programs
    public static final int   MODE_UPDATE_ONLY_CURRENT = 2;  // update only current programs
    public static final int   MODE_UPDATE_SOME_PROGRAMS = 3;    // update some programs

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int mode = getIntent().getIntExtra(MODE, MODE_NONE);
        if (mode == MODE_NONE) {
            getFragmentManager().beginTransaction()
                    .add(android.R.id.content, new SetupApiKeyFragment())
                    .commit();
        } else {
            getFragmentManager().beginTransaction()
                    .add(android.R.id.content, SetupScanFragment.newInstance(mode))
                    .commit();
        }
    }

    public void scanChannels() {
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, SetupScanFragment.newInstance(MODE_NONE))
                .commit();
    }

    public void scanChannelsCompleted() {
        setResult(Activity.RESULT_OK);
        finish();
    }
}
