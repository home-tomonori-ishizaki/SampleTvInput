package com.example.sampletvinput.ui;

import android.os.Bundle;
import android.support.v17.leanback.app.DetailsFragment;
import android.util.Log;

import com.example.sampletvinput.model.Program;

public class ProgramDetailsFragment extends DetailsFragment {

    private static final String TAG = ProgramDetailsFragment.class.getSimpleName();
    private Program mProgram;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate DetailsFragment");
        super.onCreate(savedInstanceState);

        mProgram = (Program) getActivity().getIntent()
                .getSerializableExtra(ProgramDetailsActivity.EXTRA_PROGRAM);

        if (mProgram == null) {
            return;
        }

        Log.i(TAG, "title:" + mProgram.getName());
    }
}

