package com.example.sampletvinput.ui;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v17.leanback.app.DetailsFragment;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.DetailsOverviewRow;
import android.support.v17.leanback.widget.FullWidthDetailsOverviewRowPresenter;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.SparseArrayObjectAdapter;
import android.util.Log;

import com.example.sampletvinput.model.Program;
import com.example.sampletvinput.presenter.ProgramDetailsDescriptionPresenter;
import com.example.sampletvinput.util.NhkUtils;
import com.example.sampletvinput.util.PreferenceUtils;
import com.squareup.picasso.Picasso;

public class ProgramDetailsFragment extends DetailsFragment {

    private static final String TAG = ProgramDetailsFragment.class.getSimpleName();
    private Program mProgram;
    private ArrayObjectAdapter mAdapter;
    private static final long TYPE_OPEN_LINK = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate DetailsFragment");
        super.onCreate(savedInstanceState);

        mProgram = (Program) getActivity().getIntent()
                .getSerializableExtra(ProgramDetailsActivity.EXTRA_PROGRAM);

        if (mProgram == null) {
            return;
        }

        setupAdapter();
        setupDetailRow();
        setOnItemViewClickedListener(new ItemViewClickedListener() );
    }

    private void setupAdapter() {
        ClassPresenterSelector classPresenterSelector = new ClassPresenterSelector();

        FullWidthDetailsOverviewRowPresenter detailPresenter =
                new FullWidthDetailsOverviewRowPresenter(new ProgramDetailsDescriptionPresenter());
        //detailPresenter.setInitialState(FullWidthDetailsOverviewRowPresenter.STATE_HALF);

        classPresenterSelector.addClassPresenter(DetailsOverviewRow.class, detailPresenter);
        classPresenterSelector.addClassPresenter(ListRow.class, new ListRowPresenter());

        mAdapter = new ArrayObjectAdapter(classPresenterSelector);
        setAdapter(mAdapter);
    }

    private void setupDetailRow() {
        DetailsOverviewRow row = new DetailsOverviewRow(mProgram);
        new DetailsLoaderTask(row).execute();

        mAdapter.add(row);
    }

    private class DetailsLoaderTask extends AsyncTask<Void, Void, Program> {

        private final DetailsOverviewRow mRow;

        DetailsLoaderTask(DetailsOverviewRow row) {
            mRow = row;
        }

        @Override
        protected Program doInBackground(Void... params) {
            try {
                Context context = getActivity().getApplicationContext();

                // get program details
                Program program = NhkUtils.getProgram(
                        mProgram.getId(),
                        mProgram.getServiceId(),
                        PreferenceUtils.getApiKey(context));

                // set logo
                String thumbnailUrl = program.getThumbnailUrl();
                Log.i(TAG, "thumb url:" + thumbnailUrl);
                if (thumbnailUrl != null) {
                    Bitmap thumb = Picasso.with(context)
                            .load(thumbnailUrl)
                            .resize(250, 250)
                            .get();
                    mRow.setImageBitmap(context, thumb);
                }
                return program;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Program program) {
            if (program == null) {
                return;
            }
            mProgram = program;

            SparseArrayObjectAdapter sparseArrayObjectAdapter = new SparseArrayObjectAdapter();

            String linkUrl = program.getLinkUrl();
            Log.i(TAG, "link url:" + linkUrl);
            if (linkUrl != null) {
                sparseArrayObjectAdapter.set(0, new Action(0, "Open link", ""));
            }

            mRow.setActionsAdapter(sparseArrayObjectAdapter);
        }
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof Action) {
                Action action = (Action)item;
                if (action.getId() == TYPE_OPEN_LINK) {
                    openLink(mProgram.getLinkUrl());
                }
            }
        }
    }

    private void openLink(String linkUrl) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(linkUrl));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            intent = new Intent(getActivity(), WebActivity.class);
            intent.setData(Uri.parse(linkUrl));
            startActivity(intent);
        }
    }
}

