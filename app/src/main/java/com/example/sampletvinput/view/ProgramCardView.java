package com.example.sampletvinput.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.BaseCardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sampletvinput.R;

public class ProgramCardView extends BaseCardView {
    private View mInfoArea;
    private TextView mTitleView;
    private TextView mContentView;
    private ImageView mBadgeImage;
    private boolean mAttachedToWindow;

    public ProgramCardView(Context context) {
        this(context, null);
    }

    public ProgramCardView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.imageCardViewStyle);
    }


    public ProgramCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.program_card_view, this);

        mInfoArea = v.findViewById(R.id.info_field);
        mTitleView = (TextView) v.findViewById(R.id.title_text);
        mContentView = (TextView) v.findViewById(R.id.content_text);
        mBadgeImage = (ImageView) v.findViewById(R.id.extra_badge);

        if (mInfoArea != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.lbImageCardView,
                    defStyle, 0);
            try {
                setInfoAreaBackground(
                        a.getDrawable(R.styleable.lbImageCardView_infoAreaBackground));
            } finally {
                a.recycle();
            }
        }
    }

    /**
     * Returns the info area background drawable.
     */
    public Drawable getInfoAreaBackground() {
        if (mInfoArea != null) {
            return mInfoArea.getBackground();
        }
        return null;
    }

    /**
     * Sets the info area background drawable.
     */
    public void setInfoAreaBackground(Drawable drawable) {
        if (mInfoArea != null) {
            mInfoArea.setBackground(drawable);
        }
    }

    /**
     * Sets the info area background color.
     */
    public void setInfoAreaBackgroundColor(int color) {
        if (mInfoArea != null) {
            mInfoArea.setBackgroundColor(color);
        }
    }

    /**
     * Sets the title text.
     */
    public void setTitleText(CharSequence text) {
        if (mTitleView == null) {
            return;
        }

        mTitleView.setText(text);
    }

    /**
     * Returns the title text.
     */
    public CharSequence getTitleText() {
        if (mTitleView == null) {
            return null;
        }

        return mTitleView.getText();
    }

    /**
     * Sets the content text.
     */
    public void setContentText(CharSequence text) {
        if (mContentView == null) {
            return;
        }

        mContentView.setText(text);
    }

    /**
     * Returns the content text.
     */
    public CharSequence getContentText() {
        if (mContentView == null) {
            return null;
        }

        return mContentView.getText();
    }

    /**
     * Sets the badge image drawable.
     */
    public void setBadgeImage(Drawable drawable) {
        if (mBadgeImage == null) {
            return;
        }
        mBadgeImage.setImageDrawable(drawable);
        if (drawable != null && mContentView!= null && mContentView.getVisibility() != GONE) {
            mBadgeImage.setVisibility(View.VISIBLE);
        } else {
            mBadgeImage.setVisibility(View.GONE);
        }
    }

    /**
     * Returns the badge image drawable.
     */
    public Drawable getBadgeImage() {
        if (mBadgeImage == null) {
            return null;
        }

        return mBadgeImage.getDrawable();
    }

    private void fadeIn() {
        mTitleView.setAlpha(0f);
        if (mAttachedToWindow) {
            mTitleView.animate().alpha(1f).setDuration(mTitleView.getResources().getInteger(
                    android.R.integer.config_shortAnimTime));
        }
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mAttachedToWindow = true;
        if (mTitleView.getAlpha() == 0) {
            fadeIn();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        mAttachedToWindow = false;
        mTitleView.animate().cancel();
        mTitleView.setAlpha(1f);
        super.onDetachedFromWindow();
    }
}
