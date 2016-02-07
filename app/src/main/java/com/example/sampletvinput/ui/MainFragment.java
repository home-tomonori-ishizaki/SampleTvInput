package com.example.sampletvinput.ui;

import android.content.ContentResolver;
import android.database.Cursor;
import android.media.tv.TvContract;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.util.Log;

import com.example.sampletvinput.presenter.ProgramItemPresenter;

import java.util.Map;

public class MainFragment extends BrowseFragment {
    private static final String TAG = BrowseFragment.class.getSimpleName();
    private ArrayObjectAdapter mRowAdapter;

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
                int idxDisplayName = cursor.getColumnIndexOrThrow(TvContract.Channels.COLUMN_DISPLAY_NAME);
                int idxServiceId = cursor.getColumnIndexOrThrow(TvContract.Channels.COLUMN_SERVICE_ID);

                while (cursor.moveToNext()) {
                    ArrayObjectAdapter programAdapter = new ArrayObjectAdapter(new ProgramItemPresenter());

                    String channelName = cursor.getString(idxDisplayName);
                    mRowAdapter.add(new ListRow(new HeaderItem(0, channelName), programAdapter));
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
        }
    }
}
