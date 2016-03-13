package com.example.sampletvinput.presenter;

import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter;

import com.example.sampletvinput.R;
import com.example.sampletvinput.model.Program;

import java.text.SimpleDateFormat;

public class ProgramDetailsDescriptionPresenter extends AbstractDetailsDescriptionPresenter {

    private static SimpleDateFormat DATE_FORMAT_START = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    private static SimpleDateFormat DATE_FORMAT_END   = new SimpleDateFormat("HH:mm");

    @Override
    protected void onBindDescription(ViewHolder vh, Object item) {
        if (item == null) {
            return;
        }

        Program program = (Program)item;

        String name = program.getName();
        if (name == null) {
            name = vh.view.getContext().getString(R.string.no_title);
        }
        vh.getTitle().setText(name);

        long startTime = program.getStartTime();
        long endTime = program.getEndTime();
        if (startTime != 0 && endTime != 0) {
            String content = DATE_FORMAT_START.format(startTime)
                    + " - " + DATE_FORMAT_END.format(endTime);
            vh.getSubtitle().setText(content);
        }

        vh.getBody().setText(program.getDescription());
    }
}
