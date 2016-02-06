package com.example.sampletvinput.ui;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v17.leanback.app.GuidedStepFragment;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidedAction;
import android.support.v17.leanback.widget.GuidedActionsStylist;
import android.util.Log;

import com.example.sampletvinput.R;

import java.util.List;

public class SampleInputSetupActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_input_setup);
    }

}
