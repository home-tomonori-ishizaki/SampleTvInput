package com.example.sampletvinput.model;

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
}
