package com.example.sampletvinput.service;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.tv.TvContract;
import android.media.tv.TvInputManager;
import android.media.tv.TvInputService;
import android.net.Uri;
import android.view.Surface;

import java.io.IOException;

public class SampleInputService extends TvInputService {
    @Override
    public Session onCreateSession(String inputId) {
        return new SampleSessionImpl(this);
    }

    /**
     * Simple session implementation which plays local videos on the application's tune request.
     */
    private class SampleSessionImpl extends TvInputService.Session {

        private MediaPlayer mPlayer;
        private float mVolume;
        private Surface mSurface;

        SampleSessionImpl(Context context) {
            super(context);
        }

        @Override
        public void onRelease() {
            if (mPlayer != null) {
                mPlayer.release();
            }
        }

        @Override
        public boolean onSetSurface(Surface surface) {
            if (mPlayer != null) {
                mPlayer.setSurface(surface);
            }
            mSurface = surface;
            return true;
        }

        @Override
        public void onSetStreamVolume(float volume) {
            if (mPlayer != null) {
                mPlayer.setVolume(volume, volume);
            }
            mVolume = volume;
        }

        @Override
        public boolean onTune(Uri channelUri) {
            return false;
            //return startPlayback(0);
        }

        @Override
        public void onSetCaptionEnabled(boolean enabled) {
            // The sample content does not have caption. Nothing to do in this sample input.
            // NOTE: If the channel has caption, the implementation should turn on/off the caption
            // based on {@code enabled}.
            // For the example implementation for the case, please see {@link RichTvInputService}.
        }

        private boolean startPlayback(int resource) {
            if (mPlayer == null) {
                mPlayer = new MediaPlayer();
                mPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                    @Override
                    public boolean onInfo(MediaPlayer player, int what, int arg) {
                        // NOTE: TV input should notify the video playback state by using
                        // {@code notifyVideoAvailable()} and {@code notifyVideoUnavailable() so
                        // that the application can display back screen or spinner properly.
                        if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                            notifyVideoUnavailable(
                                    TvInputManager.VIDEO_UNAVAILABLE_REASON_BUFFERING);
                            return true;
                        } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END
                                || what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                            notifyVideoAvailable();
                            return true;
                        }
                        return false;
                    }
                });
                mPlayer.setSurface(mSurface);
                mPlayer.setVolume(mVolume, mVolume);
            } else {
                mPlayer.reset();
            }
            mPlayer.setLooping(true);
            AssetFileDescriptor afd = getResources().openRawResourceFd(resource);
            if (afd == null) {
                return false;
            }
            try {
                mPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(),
                        afd.getDeclaredLength());
                mPlayer.prepare();
                mPlayer.start();
            } catch (IOException e) {
                return false;
            } finally {
                try {
                    afd.close();
                } catch (IOException e) {
                    // Do nothing.
                }
            }
            // The sample content does not have rating information. Just allow the content here.
            // NOTE: If the content might include problematic scenes, it should not be allowed.
            // Also, if the content has rating information, the implementation should allow the
            // content based on the current rating settings by using
            // {@link android.media.tv.TvInputManager#isRatingBlocked()}.
            // For the example implementation for the case, please see {@link RichTvInputService}.
            notifyContentAllowed();
            return true;
        }
    }
}
