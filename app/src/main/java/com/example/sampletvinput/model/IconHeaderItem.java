package com.example.sampletvinput.model;

import android.net.Uri;
import android.support.v17.leanback.widget.HeaderItem;

public class IconHeaderItem extends HeaderItem {
    private Uri mIconUri;

    public IconHeaderItem(long id, String name, Uri iconUri) {
        super(id, name);
        mIconUri = iconUri;
    }

    public IconHeaderItem(long id, String name) {
        this(id, name, null);
    }

    public IconHeaderItem(String name) {
        super(name);
    }

    public Uri getIconUri() {
        return mIconUri;
    }
}
