package com.example.sampletvinput.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Program {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    private String mName;
    private long mStartTime;
    private long mEndTime;
    private String mGenre;

    public Program setName(String name) {
        mName = name;
        return this;
    }

    public String getName() {
        return mName;
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
}
