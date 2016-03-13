package com.example.sampletvinput.util;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.tv.TvContract;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.sampletvinput.model.Channel;
import com.example.sampletvinput.model.Program;
import com.example.sampletvinput.service.SampleInputService;

import java.util.ArrayList;
import java.util.List;

public class TvContractUtils {
    private static final String TAG = TvContract.class.getSimpleName();

    @NonNull
    public static List<Channel> getChannels(ContentResolver resolver) {
        List<Channel> channels = new ArrayList<>();
        try (Cursor cursor = resolver.query(TvContract.Channels.CONTENT_URI, null, null, null, null)) {
            if (cursor == null || cursor.getCount() == 0) {
                return channels;
            }
            while (cursor.moveToNext()) {
                channels.add(Channel.fromCursor(cursor));
            }
        } catch (Exception e) {
            Log.w(TAG, "Unable to get channels", e);
        }
        return channels;
    }

    @NonNull
    public static List<Program> getPrograms(ContentResolver resolver, Channel channel) {
        return getProgramsInternal(resolver, channel, TvContract.buildProgramsUriForChannel(channel.getId()));
    };

    @Nullable
    public static Program getCurrentProgram(ContentResolver resolver, Channel channel) {
        long current = System.currentTimeMillis();
        Uri programUri = TvContract.buildProgramsUriForChannel(channel.getId(), current, current);
        List<Program> programs = getProgramsInternal(resolver, channel, programUri);
        if (programs.size() > 0) {
            return programs.get(0);
        }
        return null;
    }

    @NonNull
    private static List<Program> getProgramsInternal(ContentResolver resolver, Channel channel,
                                                     Uri programUri) {
        List<Program> programs = new ArrayList<>();
        try (Cursor cursor = resolver.query(programUri, null, null, null, null)) {

            if (cursor == null || cursor.getCount() == 0) {
                return programs;
            }

            while (cursor.moveToNext()) {
                programs.add(Program.fromCursor(cursor, channel.getServiceId()));
            }
        } catch (Exception e) {
            Log.w(TAG, "Unable to get programs", e);
        }
        return programs;
    }


    public static String getInputId(Context context) {
        ComponentName component = new ComponentName(context, SampleInputService.class);
        return TvContract.buildInputId(component);
    }

}
