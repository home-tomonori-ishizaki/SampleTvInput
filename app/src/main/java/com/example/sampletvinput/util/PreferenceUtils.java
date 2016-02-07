package com.example.sampletvinput.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

public class PreferenceUtils {

    public static void storeApiKey(@NonNull Context context, @NonNull String apiKey) {
        SharedPreferences pref = context.getSharedPreferences("user_data", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = pref.edit();
        editor.putString("api_key", apiKey);
        editor.commit();
    }

    @NonNull
    public static String getApiKey(Context context) {
        SharedPreferences pref = context.getSharedPreferences("user_data", Context.MODE_PRIVATE);

        return  pref.getString("api_key", "");
    }
}
