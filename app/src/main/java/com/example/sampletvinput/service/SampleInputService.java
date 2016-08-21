package com.example.sampletvinput.service;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.media.MediaPlayer;
import android.media.tv.TvContract;
import android.media.tv.TvInputManager;
import android.media.tv.TvInputService;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.Surface;

import com.example.sampletvinput.R;

import java.io.IOException;

public class SampleInputService extends TvInputService {
    private static String TAG = SampleInputService.class.getSimpleName();

    @Override
    public Session onCreateSession(String inputId) {
        return new SampleSessionImpl(this);
    }

    @Override
    public RecordingSession onCreateRecordingSession(String inputId) {
        Log.i(TAG, "onCreateRecordingSession(" + inputId + ")");
        return new SampleRecordingSession(this, inputId);
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
            //return false;
            Log.i(TAG, "onTune(" + channelUri + ")");
            return startPlayback(R.raw.video);
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
                e.printStackTrace();
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

            // enable time shift feature(need to enable for recording feature)
            if (Build.VERSION.SDK_INT >= 23) {
                notifyTimeShiftStatusChanged(TvInputManager.TIME_SHIFT_STATUS_AVAILABLE);
            }

            return true;
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    private class SampleRecordingSession extends RecordingSession {

        private String mInputId;
        private Uri mChannelUir;
        private Uri mProgramUri;
        private long mStartTime;
        private long mEndTime;

        public SampleRecordingSession(Context context, String inputId) {
            super(context);
            mInputId = inputId;
        }

        @Override
        public void onTune(Uri uri) {
            Log.i(TAG, "[DVR] onTune(" + uri + ")");
            mChannelUir = uri;
            notifyTuned(uri);
        }

        @Override
        public void onStartRecording(Uri uri) {
            mProgramUri = uri;
            mStartTime = System.currentTimeMillis();
            Log.i(TAG, "[DVR] onStartRecording(" + uri + ")");
        }

        @Override
        public void onStopRecording() {
            Log.i(TAG, "[DVR] onStopRecording(" + mProgramUri + ")");
            mEndTime = System.currentTimeMillis();

            Uri programUri = mProgramUri;
            if (mProgramUri == null) {
                // OTR case
                long channelId = Long.valueOf(mChannelUir.getLastPathSegment());
                programUri = TvContract.buildProgramsUriForChannel(channelId, mStartTime, mEndTime);
            }

            // get program info
            Cursor cursor = getContentResolver().query(programUri,
                    new String[]{
                            TvContract.Programs.COLUMN_CHANNEL_ID,            // 0
                            TvContract.Programs.COLUMN_TITLE,                 // 1
                            TvContract.Programs.COLUMN_SHORT_DESCRIPTION,     // 2
                            TvContract.Programs.COLUMN_START_TIME_UTC_MILLIS, // 3
                            TvContract.Programs.COLUMN_END_TIME_UTC_MILLIS,   // 4
                            TvContract.Programs.COLUMN_CANONICAL_GENRE,       // 5
                            TvContract.Programs.COLUMN_POSTER_ART_URI         // 6
                    }, null, null, null);
            //DatabaseUtils.dumpCursor(cursor);

            ContentValues values = new ContentValues();
            if (cursor != null && cursor.moveToFirst()) {
                values.put(TvContract.RecordedPrograms.COLUMN_CHANNEL_ID, cursor.getLong(0));
                values.put(TvContract.RecordedPrograms.COLUMN_TITLE, cursor.getString(1));
                values.put(TvContract.RecordedPrograms.COLUMN_SHORT_DESCRIPTION, cursor.getString(2));
                values.put(TvContract.RecordedPrograms.COLUMN_START_TIME_UTC_MILLIS, mStartTime);
                values.put(TvContract.RecordedPrograms.COLUMN_END_TIME_UTC_MILLIS, mEndTime);
                //values.put(TvContract.RecordedPrograms.COLUMN_START_TIME_UTC_MILLIS, cursor.getLong(3));
                //values.put(TvContract.RecordedPrograms.COLUMN_END_TIME_UTC_MILLIS, cursor.getLong(4));
                values.put(TvContract.RecordedPrograms.COLUMN_CANONICAL_GENRE, cursor.getString(5));
                values.put(TvContract.RecordedPrograms.COLUMN_POSTER_ART_URI, cursor.getString(6));
            }

            values.put(TvContract.RecordedPrograms.COLUMN_INPUT_ID, mInputId);
            values.put(TvContract.RecordedPrograms.COLUMN_SEARCHABLE, 1);

            getContentResolver().insert(TvContract.RecordedPrograms.CONTENT_URI, values);

            notifyRecordingStopped(mProgramUri);
        }

        @Override
        public void onRelease() {
            Log.i(TAG, "[DVR] onRelease(" + mChannelUir + "," + mProgramUri + ")");
        }
    }
}
