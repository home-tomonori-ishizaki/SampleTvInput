package com.example.sampletvinput.model;

import android.database.Cursor;
import android.media.tv.TvContract;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Program implements Serializable {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    private long mId;
    private String mName;
    private String mDescription;
    private long mStartTime;
    private long mEndTime;
    private String mGenre;
    private String mThumbnailUrl;
    private String mLinkUrl;
    private String mServiceId;
    private List<String> mHashTags;

    public Program setId(long id) {
        mId = id;
        return this;
    }

    public long getId() {
        return mId;
    }

    public Program setName(String name) {
        mName = name;
        return this;
    }

    public String getName() {
        return mName;
    }

    public Program setDescription(String description) {
        mDescription = description;
        return this;
    }

    public String getDescription() {
        return mDescription;
    }

    public Program setStartTime(String startTime) {
        mStartTime = convertTime(startTime);
        return this;
    }
    public Program setStartTime(long startTime) {
        mStartTime = startTime;
        return this;
    }

    public long getStartTime() {
        return mStartTime;
    }

    public Program setEndTime(String endTime) {
        mEndTime = convertTime(endTime);
        return this;
    }

    public Program setEndTime(long endTime) {
        mEndTime = endTime;
        return this;
    }

    public long getEndTime() {
        return mEndTime;
    }

    private static long convertTime(String time) {
        long timeStamp = 0;
        try {
            Date date = DATE_FORMAT.parse(time);
            timeStamp = date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timeStamp;
    }

    public Program setGenre(String genre) {
        mGenre = genre;
        return this;
    }

    public String getGenre() {
        return mGenre;
    }

    public Program setThumbnailUrl(String url) {
        mThumbnailUrl = url;
        return this;
    }

    public String getThumbnailUrl() {
        return mThumbnailUrl;
    }

    public Program setLinkUrl(String url) {
        mLinkUrl = url;
        return this;
    }

    public String getLinkUrl() {
        return mLinkUrl;
    }

    public Program setServiceId(String serviceId) {
        mServiceId = serviceId;
        return this;
    }

    public String getServiceId() {
        return mServiceId;
    }

    public Program setHashTags(List<String> tags) {
        mHashTags = tags;
        return this;
    }

    public List<String> getHasTangs() {
        return mHashTags;
    }

    public static Program fromCursor(Cursor cursor, String serviceId) {
        Program program = new Program();

        try {
            int idxTitle = cursor.getColumnIndexOrThrow(TvContract.Programs.COLUMN_TITLE);
            int idxDescription = cursor.getColumnIndexOrThrow(TvContract.Programs.COLUMN_SHORT_DESCRIPTION);
            int idxStartTime = cursor.getColumnIndexOrThrow(TvContract.Programs.COLUMN_START_TIME_UTC_MILLIS);
            int idxEndTime = cursor.getColumnIndexOrThrow(TvContract.Programs.COLUMN_END_TIME_UTC_MILLIS);
            int idxGenre = cursor.getColumnIndexOrThrow(TvContract.Programs.COLUMN_CANONICAL_GENRE);
            int idxVersion = cursor.getColumnIndexOrThrow(TvContract.Programs.COLUMN_VERSION_NUMBER);

            program.setId(cursor.getLong(idxVersion))
                    .setName(cursor.getString(idxTitle))
                    .setDescription(cursor.getString(idxDescription))
                    .setStartTime(cursor.getLong(idxStartTime))
                    .setEndTime(cursor.getLong(idxEndTime))
                    .setGenre(cursor.getString(idxGenre))
                    .setServiceId(serviceId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return program;
    }
}
