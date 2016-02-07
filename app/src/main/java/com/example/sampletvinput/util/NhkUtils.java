package com.example.sampletvinput.util;

import android.media.tv.TvContract;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.example.sampletvinput.data.NhkProgramList;
import com.example.sampletvinput.data.Program;
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
    private static final String AREA_ID_TOKYO = "130";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public static List<Program> getPrograms(@NonNull String serviceId, @NonNull String apiKey) {
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

            List<NhkProgramList.NhkProgram> programList = nhkPrograms.list.get(serviceId);
            if (programList == null) {
                Log.d(TAG, "can not found programs of " + serviceId);
                return null;
            }

            Log.d(TAG, "program size: " + programList.size());
            List<Program> programs = new LinkedList<>();
            for (NhkProgramList.NhkProgram p : programList) {
                programs.add(createProgram(p));
            }
            return programs;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Program createProgram(NhkProgramList.NhkProgram nhk) {
        return new Program()
                .setName(nhk.title)
                .setStartTime(nhk.start_time)
                .setEndTime(nhk.end_time)
                .setGenre(getGenre());
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
}
