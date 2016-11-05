package com.example.sampletvinput.model;

import android.database.Cursor;
import android.media.tv.TvContract;
import android.net.Uri;

public class Channel {
    private long mId;
    private String mDisplayName;
    private String mDisplayNumber;
    private String mServiceId;

    public Channel setId(long id) {
        mId = id;
        return this;
    }

    public long getId() {
        return mId;
    }

    public Channel setDisplayName(String displayName) {
        mDisplayName = displayName;
        return this;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public Channel setDisplayNumber(String displayNumber) {
        mDisplayNumber = displayNumber;
        return this;
    }

    public String getDisplayNumber() {
        return mDisplayNumber;
    }

    public Channel setServiceId(String serviceId) {
        mServiceId = serviceId;
        return this;
    }

    public String getServiceId() {
        return mServiceId;
    }

    public Uri getLogoUri() {
        return TvContract.buildChannelLogoUri(getId());
    }

    public static Channel fromCursor(Cursor cursor) {
        Channel channel = new Channel();

        try {
            int idxChannelId = cursor.getColumnIndexOrThrow(TvContract.Channels._ID);
            int idxDisplayName = cursor.getColumnIndexOrThrow(TvContract.Channels.COLUMN_DISPLAY_NAME);
            int idxDisplayNumber = cursor.getColumnIndexOrThrow(TvContract.Channels.COLUMN_DISPLAY_NUMBER);
            int idxServiceId = cursor.getColumnIndexOrThrow(TvContract.Channels.COLUMN_NETWORK_AFFILIATION);

            channel.setId(cursor.getLong(idxChannelId))
                    .setDisplayName(cursor.getString(idxDisplayName))
                    .setDisplayNumber(cursor.getString(idxDisplayNumber))
                    .setServiceId(cursor.getString(idxServiceId));
        } catch (Exception e ) {
            e.printStackTrace();
        }

        return channel;
    }


}
