package com.example.sampletvinput.ui;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.example.sampletvinput.R;
import com.example.sampletvinput.util.PreferenceUtils;

public class SetupApiKeyFragment extends Fragment {
    private static final String TAG = SetupApiKeyFragment.class.getSimpleName();
    private EditText mApiKeyEdit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setup_apikey, null);

        mApiKeyEdit = (EditText)view.findViewById(R.id.setup_api_key);
        mApiKeyEdit.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && keyCode == KeyEvent.KEYCODE_ENTER) {

                    // close keyboard
                    InputMethodManager inputMethodManager
                            = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);

                    doSetup();

                    return true;
                }
                return false;
            }
        });
        mApiKeyEdit.setText(PreferenceUtils.getApiKey(getActivity()));

        return view;
    }

    private void doSetup() {
        String apiKey = mApiKeyEdit.getText().toString();
        PreferenceUtils.storeApiKey(getActivity(), apiKey);

        ((SampleInputSetupActivity) getActivity()).scanChannels();
    }
}

