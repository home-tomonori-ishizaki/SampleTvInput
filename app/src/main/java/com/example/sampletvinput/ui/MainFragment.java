package com.example.sampletvinput.ui;

import android.content.ContentResolver;
import android.database.Cursor;
import android.media.tv.TvContract;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.util.Log;

import com.example.sampletvinput.data.Program;
import com.example.sampletvinput.presenter.ProgramItemPresenter;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MainFragment extends BrowseFragment {
    private static final String TAG = BrowseFragment.class.getSimpleName();
    private ArrayObjectAdapter mRowAdapter;

    private Handler mHandler = new Handler();

    public MainFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        setupUIElements();

        loadRows();
    }

    private void setupUIElements() {
        setTitle("Sample TV Input");

        // over title
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);
    }

    private void loadRows() {
        ClassPresenterSelector selector = new ClassPresenterSelector();
        selector.addClassPresenter(ListRow.class, new ListRowPresenter());

        mRowAdapter = new ArrayObjectAdapter(selector);
        setAdapter(mRowAdapter);

        new LoadTask().execute();
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

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mRowAdapter.add(new ListRow(new HeaderItem(0, channelName), programAdapter));
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
}
