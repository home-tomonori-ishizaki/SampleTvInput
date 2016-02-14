package com.example.sampletvinput.ui;

import android.os.Bundle;
import android.app.Activity;

import com.example.sampletvinput.R;

public class ProgramDetailsActivity extends Activity {

    public static final String EXTRA_PROGRAM = "program";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_program_details);
    }

}
