package com.example.sampletvinput.presenter;

import android.content.Context;
import android.media.tv.TvContract;
import android.support.v17.leanback.widget.BaseCardView;
import android.support.v17.leanback.widget.Presenter;
import android.view.ViewGroup;

import com.example.sampletvinput.R;
import com.example.sampletvinput.model.Program;
import com.example.sampletvinput.view.ProgramCardView;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

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

        int resId = GENRE_MAP.get(program.getGenre());
        if (resId > 0) {
            cardView.setBadgeImage(cardView.getResources().getDrawable(resId));
        }
    }

    private static Map<String, Integer> GENRE_MAP = Collections.unmodifiableMap(new LinkedHashMap<String, Integer>() {{
        put(TvContract.Programs.Genres.ANIMAL_WILDLIFE, R.drawable.genre_animal_wildlife);
        put(TvContract.Programs.Genres.ARTS, R.drawable.genre_art);
        put(TvContract.Programs.Genres.COMEDY, R.drawable.genre_comedy);
        put(TvContract.Programs.Genres.DRAMA, R.drawable.genre_drama);
        put(TvContract.Programs.Genres.EDUCATION, R.drawable.genre_education);
        put(TvContract.Programs.Genres.ENTERTAINMENT, R.drawable.genre_entertainment);
        put(TvContract.Programs.Genres.FAMILY_KIDS, R.drawable.genre_family_kids);
        put(TvContract.Programs.Genres.GAMING, R.drawable.genre_game);
        put(TvContract.Programs.Genres.LIFE_STYLE, R.drawable.genre_life_style);
        put(TvContract.Programs.Genres.MOVIES, R.drawable.genre_movies);
        put(TvContract.Programs.Genres.MUSIC, R.drawable.genre_music);
        put(TvContract.Programs.Genres.NEWS, R.drawable.genre_news);
        put(TvContract.Programs.Genres.PREMIER, R.drawable.genre_premier);
        put(TvContract.Programs.Genres.SHOPPING, R.drawable.genre_shopping);
        put(TvContract.Programs.Genres.SPORTS, R.drawable.genre_sports);
        put(TvContract.Programs.Genres.TECH_SCIENCE, R.drawable.genre_tech_science);
        put(TvContract.Programs.Genres.TRAVEL, R.drawable.genre_travel);
    }});

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {

    }
}
