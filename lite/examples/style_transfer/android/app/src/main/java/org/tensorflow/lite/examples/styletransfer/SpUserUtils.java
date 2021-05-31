package org.tensorflow.lite.examples.styletransfer;

import android.content.Context;
import android.content.SharedPreferences;

public class SpUserUtils {
    private static SharedPreferences sp;

    private static SharedPreferences getSp(Context context) {
        if (sp == null) {
            sp = context.getSharedPreferences("SpUtil", Context.MODE_PRIVATE);
        }
        return sp;
    }

    public static void putString(Context context, String key, String value) {
        SharedPreferences preferences = getSp(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getString(Context context, String key) {
        SharedPreferences preferences = getSp(context);
        return preferences.getString(key, "");
    }

    public static void clearString(Context context, String key) {
        SharedPreferences sp = getSp(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(key);
        editor.apply();
    }
}
