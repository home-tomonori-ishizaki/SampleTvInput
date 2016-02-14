package com.example.sampletvinput.presenter;

import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter;

import com.example.sampletvinput.model.Program;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ProgramDetailsDescriptionPresenter extends AbstractDetailsDescriptionPresenter {

    private static SimpleDateFormat DATE_FORMAT_START = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    private static SimpleDateFormat DATE_FORMAT_END   = new SimpleDateFormat("HH:mm");

    @Override
    protected void onBindDescription(ViewHolder vh, Object item) {
        if (item == null) {
            return;
        }

        Program program = (Program)item;
        vh.getTitle().setText(program.getName());

        String content = DATE_FORMAT_START.format(new Date(program.getStartTime()))
                + " - " + DATE_FORMAT_END.format(new Date(program.getEndTime()));
        vh.getSubtitle().setText(content);

        vh.getBody().setText(program.getDescription());
    }
}
