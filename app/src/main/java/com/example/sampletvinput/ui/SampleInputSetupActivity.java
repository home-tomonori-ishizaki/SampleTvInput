package com.example.sampletvinput.ui;

import android.os.Bundle;
import android.app.Activity;

public class SampleInputSetupActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .add(android.R.id.content, new SetupApiKeyFragment())
                .commit();
    }

    public void scanChannels() {
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SetupScanFragment())
                .commit();
    }

    public void scanChannelsCompleted() {
        setResult(Activity.RESULT_OK);
        finish();
    }
}
