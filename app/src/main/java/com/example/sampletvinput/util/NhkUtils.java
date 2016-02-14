package com.example.sampletvinput.util;

import android.media.tv.TvContract;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.example.sampletvinput.data.NhkNowOnAirList;
import com.example.sampletvinput.data.NhkProgram;
import com.example.sampletvinput.data.NhkProgramList;
import com.example.sampletvinput.data.NhkService;
import com.example.sampletvinput.model.Program;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class NhkUtils {
    private static final String TAG = NhkUtils.class.getSimpleName();

    private static final String BASE_URL_PROGRAM_LIST = "http://api.nhk.or.jp/v1/pg/list";
    private static final String BASE_URL_ON_AIR = "http://api.nhk.or.jp/v1/pg/now";
    private static final String BASE_URL_PROGRAM_INFO = "http://api.nhk.or.jp/v1/pg/info";
    private static final String AREA_ID_TOKYO = "130";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public static List<Program> getPrograms(@NonNull String serviceId, @NonNull String apiKey)
            throws HttpUtils.BadRequestException, HttpUtils.UnauthorizedException {
        String today = DATE_FORMAT.format(new Date());
        String url = BASE_URL_PROGRAM_LIST + "/" + AREA_ID_TOKYO + "/" + serviceId + "/" + today+ ".json?key=" + apiKey;
        Log.d(TAG, "request url:" + url);
        String response = HttpUtils.get(url);

        if (TextUtils.isEmpty(response)) {
            return null;
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            NhkProgramList nhkPrograms = mapper.readValue(response, NhkProgramList.class);
            if (nhkPrograms == null || nhkPrograms.list == null || nhkPrograms.list.isEmpty()) {
                Log.d(TAG, "can not get list");
                return null;
            }

            List<NhkProgram> programList = nhkPrograms.list.get(serviceId);
            if (programList == null) {
                Log.d(TAG, "can not found programs of " + serviceId);
                return null;
            }

            Log.d(TAG, "program size: " + programList.size());
            List<Program> programs = new LinkedList<>();
            for (NhkProgram p : programList) {
                programs.add(createProgram(p));
            }
            return programs;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Program getProgram(long programId, String serviceId, String apiKey)
            throws HttpUtils.BadRequestException, HttpUtils.UnauthorizedException {
        String today = DATE_FORMAT.format(new Date());
        String url = BASE_URL_PROGRAM_INFO + "/" + AREA_ID_TOKYO + "/" + serviceId + "/" + programId + ".json?key=" + apiKey;
        Log.d(TAG, "request url:" + url);
        String response = HttpUtils.get(url);

        if (TextUtils.isEmpty(response)) {
            return null;
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            NhkProgramList nhkPrograms = mapper.readValue(response, NhkProgramList.class);
            if (nhkPrograms == null || nhkPrograms.list == null || nhkPrograms.list.isEmpty()) {
                Log.d(TAG, "can not get list");
                return null;
            }

            List<NhkProgram> programList = nhkPrograms.list.get(serviceId);
            if (programList == null) {
                Log.d(TAG, "can not found programs of " + serviceId);
                return null;
            }

            Log.d(TAG, "program size: " + programList.size());
            if (programList.size() > 0) {
                return createProgram(programList.get(0));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Program createProgram(NhkProgram nhk) {
        return new Program()
                .setId(nhk.id)
                .setName(nhk.title)
                .setDescription(nhk.subtitle)
                .setStartTime(nhk.start_time)
                .setEndTime(nhk.end_time)
                .setGenre(getGenre())
                .setThumbnailUrl(nhk.program_logo != null ? nhk.program_logo.url : null)
                .setLinkUrl(nhk.program_url)
                .setServiceId(nhk.service != null ? nhk.service.id : null);
    }

    private static final String[] SUPPORTED_GENRES = new String[] {
            TvContract.Programs.Genres.ANIMAL_WILDLIFE,
            TvContract.Programs.Genres.ARTS,
            TvContract.Programs.Genres.COMEDY,
            TvContract.Programs.Genres.DRAMA,
            TvContract.Programs.Genres.EDUCATION,
            TvContract.Programs.Genres.ENTERTAINMENT,
            TvContract.Programs.Genres.FAMILY_KIDS,
            TvContract.Programs.Genres.GAMING,
            TvContract.Programs.Genres.LIFE_STYLE,
            TvContract.Programs.Genres.MOVIES,
            TvContract.Programs.Genres.MUSIC,
            TvContract.Programs.Genres.NEWS,
            TvContract.Programs.Genres.PREMIER,
            TvContract.Programs.Genres.SHOPPING,
            TvContract.Programs.Genres.SPORTS,
            TvContract.Programs.Genres.TECH_SCIENCE,
            TvContract.Programs.Genres.TRAVEL,
    };

    private static int sGenreIndex = 0;
    private static String getGenre() {
         String genre = SUPPORTED_GENRES[sGenreIndex++];
        if (sGenreIndex == SUPPORTED_GENRES.length) {
            sGenreIndex = 0;
        }
        return genre;
    }

    public static NhkService getService(@NonNull String serviceId, @NonNull String apiKey)
            throws HttpUtils.BadRequestException, HttpUtils.UnauthorizedException {
        String url = BASE_URL_ON_AIR + "/" + AREA_ID_TOKYO + "/" + serviceId + ".json?key=" + apiKey;
        Log.d(TAG, "request url:" + url);
        String response = HttpUtils.get(url);

        if (TextUtils.isEmpty(response)) {
            return null;
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            NhkNowOnAirList onAirList = mapper.readValue(response, NhkNowOnAirList.class);
            if (onAirList == null || onAirList.nowonair_list == null) {
                Log.w(TAG, "can not get list");
                return null;
            }

            Map<String, NhkProgram> programs = onAirList.nowonair_list.get(serviceId);
            if (programs == null) {
                Log.w(TAG, "can not get programs") ;
                return null;
            }

            NhkProgram program = programs.get("present");
            if (program == null) {
                Log.w(TAG, "can not get present program");
                return null;
            }

            if (program.service == null) {
                Log.w(TAG, "can not get service");
                return null;
            }

            return program.service;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
