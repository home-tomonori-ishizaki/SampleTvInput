package com.example.sampletvinput.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.tv.TvContract;
import android.media.tv.TvInputInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.PresenterSelector;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.util.Log;

import com.example.sampletvinput.model.IconHeaderItem;
import com.example.sampletvinput.model.Program;
import com.example.sampletvinput.presenter.IconHeaderItemPresenter;
import com.example.sampletvinput.presenter.ProgramItemPresenter;
import com.example.sampletvinput.presenter.StringItemPresenter;
import com.example.sampletvinput.service.SampleInputService;

import java.util.LinkedList;
import java.util.List;

public class MainFragment extends BrowseFragment {
    private static final String TAG = BrowseFragment.class.getSimpleName();
    private ArrayObjectAdapter mRowAdapter;

    private Handler mHandler = new Handler();
    private int REQUEST_ID_UPDATE_PROGRAM = 0;

    private static final String SETTING_UPDATE_PROGRAMS = "Update programs";
    private static final String SETTING_UPDATE_CURRENT_PROGRAMS = "Update current programs";

    public MainFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        setupUIElements();

        loadRows();

        setupEventListeners();
    }

    private void setupUIElements() {
        setTitle("Sample TV Input");

        // over title
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);

        setHeaderPresenterSelector(new PresenterSelector() {
            @Override
            public Presenter getPresenter(Object o) {
                return new IconHeaderItemPresenter();
            }
        });
    }

    private void loadRows() {
        ClassPresenterSelector selector = new ClassPresenterSelector();
        selector.addClassPresenter(ListRow.class, new ListRowPresenter());

        mRowAdapter = new ArrayObjectAdapter(selector);

        loadSettingsRow();

        setAdapter(mRowAdapter);

        new LoadTask().execute();
    }

    private void loadSettingsRow() {
        ArrayObjectAdapter settingsAdapter = new ArrayObjectAdapter(new StringItemPresenter());
        settingsAdapter.add(SETTING_UPDATE_PROGRAMS);
        settingsAdapter.add(SETTING_UPDATE_CURRENT_PROGRAMS);
        mRowAdapter.add(new ListRow(new IconHeaderItem(0, "Settings"), settingsAdapter));
    }

    private class LoadTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... params) {
            ContentResolver resolver = getActivity().getContentResolver();


            try (Cursor cursor = resolver.query(TvContract.Channels.CONTENT_URI, null, null, null, null)) {
                int idxChannelId = cursor.getColumnIndexOrThrow(TvContract.Channels._ID);
                int idxDisplayName = cursor.getColumnIndexOrThrow(TvContract.Channels.COLUMN_DISPLAY_NAME);

                while (cursor.moveToNext()) {
                    final ArrayObjectAdapter programAdapter = new ArrayObjectAdapter(new ProgramItemPresenter());

                    long channelId = cursor.getLong(idxChannelId);
                    List<Program> programs = getPrograms(channelId);
                    if (programs != null) {
                        programAdapter.addAll(0, programs);
                    }

                    final String channelName = cursor.getString(idxDisplayName);
                    final Uri logoUri = TvContract.buildChannelLogoUri(channelId);

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mRowAdapter.add(new ListRow(new IconHeaderItem(0, channelName, logoUri), programAdapter));
                        }
                    });
                }
            }

            return null;
        }

        private List<Program> getPrograms(long channelId) {
            try (Cursor cursor = getActivity().getContentResolver().query(
                    TvContract.buildProgramsUriForChannel(channelId),
                    null, null, null, null)) {
                int idxTitle = cursor.getColumnIndexOrThrow(TvContract.Programs.COLUMN_TITLE);
                int idxStartTime = cursor.getColumnIndexOrThrow(TvContract.Programs.COLUMN_START_TIME_UTC_MILLIS);
                int idxEndTime = cursor.getColumnIndexOrThrow(TvContract.Programs.COLUMN_END_TIME_UTC_MILLIS);
                int idxGenre = cursor.getColumnIndexOrThrow(TvContract.Programs.COLUMN_CANONICAL_GENRE);

                List<Program> programs = new LinkedList<>();
                while (cursor.moveToNext()) {
                    Program program = new Program()
                            .setName(cursor.getString(idxTitle))
                            .setStartTime(cursor.getLong(idxStartTime))
                            .setEndTime(cursor.getLong(idxEndTime))
                            .setGenre(cursor.getString(idxGenre));
                    programs.add(program);
                }
                return programs;
            }
        };

        @Override
        protected void onPostExecute(Void aVoid) {
        }
    }

    private void setupEventListeners() {
        setOnItemViewClickedListener(new OnItemViewClickedListener() {
            @Override
            public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                      RowPresenter.ViewHolder rowViewHolder, Row row) {
                if (item instanceof String) {
                    if (item == SETTING_UPDATE_PROGRAMS) {
                        updatePrograms(SampleInputSetupActivity.MODE_UPDATE);
                    } else if (item == SETTING_UPDATE_CURRENT_PROGRAMS) {
                        updatePrograms(SampleInputSetupActivity.MODE_UPDATE_ONLY_CURRENT);
                    }
                }
            }
        });
    }

    private void updatePrograms(int mode) {
        Intent intent = new Intent(getActivity(), SampleInputSetupActivity.class);
        intent.putExtra(SampleInputSetupActivity.MODE, mode);
        // ComponentName is needed by TV Input Service
        ComponentName component = new ComponentName(getActivity().getApplicationContext(), SampleInputService.class);
        intent.putExtra(TvInputInfo.EXTRA_INPUT_ID, TvContract.buildInputId(component));
        startActivityForResult(intent, REQUEST_ID_UPDATE_PROGRAM);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        Log.i(TAG, "request:" + requestCode + " result:" + resultCode);
        if(requestCode == REQUEST_ID_UPDATE_PROGRAM && resultCode == Activity.RESULT_OK){
            // come back from update programs
            mRowAdapter.clear();
            loadSettingsRow();
            new LoadTask().execute();
        }
    }
}
