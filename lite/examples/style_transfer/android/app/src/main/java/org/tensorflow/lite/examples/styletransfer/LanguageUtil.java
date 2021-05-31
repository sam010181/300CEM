package org.tensorflow.lite.examples.styletransfer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import java.util.HashMap;
import java.util.Locale;

public class LanguageUtil {
    public static final String ENGLISH = "en";
    public static final String TRADITIONAL_CHINESE = "zh_rHK";

    private static HashMap<String, Locale> languagesList = new HashMap<String, Locale>(2) {{
        put(ENGLISH, Locale.ENGLISH);
        put(TRADITIONAL_CHINESE, Locale.TRADITIONAL_CHINESE);
    }};

    public static void changeAppLanguage(Activity activity, String language, Class<?> cls) {
        Resources resources = activity.getResources();
        Configuration configuration = resources.getConfiguration();

        Locale locale = getLocaleByLanguage(language.equals("") ? "en" : language);
        configuration.setLocale(locale);
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        Intent intent = new Intent(activity, cls);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
    }

    private static Locale getLocaleByLanguage(String language) {
        if (isContainsKeyLanguage(language)) {
            return languagesList.get(language);
        } else {
            Locale locale = Locale.getDefault();
            for (String key : languagesList.keySet()) {
                if (TextUtils.equals(languagesList.get(key).getLanguage(), locale.getLanguage())) {
                    return locale;
                }
            }
        }
        return Locale.ENGLISH;
    }

    private static boolean isContainsKeyLanguage(String language) {
        return languagesList.containsKey(language);
    }
}