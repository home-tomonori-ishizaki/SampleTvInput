package com.example.sampletvinput.presenter;

import android.content.Context;
import android.support.v17.leanback.widget.BaseCardView;
import android.support.v17.leanback.widget.Presenter;
import android.view.ViewGroup;

import com.example.sampletvinput.R;
import com.example.sampletvinput.model.Program;
import com.example.sampletvinput.view.ProgramCardView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ProgramItemPresenter extends Presenter {
    private static SimpleDateFormat DATE_FORMAT_START = new SimpleDateFormat("MM/dd HH:mm");
    private static SimpleDateFormat DATE_FORMAT_END   = new SimpleDateFormat("HH:mm");

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        Context context = parent.getContext();
        ProgramCardView cardView = new ProgramCardView(context);
        cardView.setCardType(BaseCardView.CARD_TYPE_INFO_UNDER_WITH_EXTRA);
        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);

        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        ProgramCardView cardView = (ProgramCardView)viewHolder.view;
        Program program = (Program)item;

        cardView.setTitleText(program.getName());

        String content = DATE_FORMAT_START.format(new Date(program.getStartTime()))
                + " - " + DATE_FORMAT_END.format(new Date(program.getEndTime()));
        cardView.setContentText(content);
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {

    }
}
