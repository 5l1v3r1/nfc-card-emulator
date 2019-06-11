package com.example.nfc_card_reader;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;


import java.util.Locale;

public class LocaleChanger {

    /**
     *  Sets the current locale to the locale using the string sent in with parameter Language
     * @param Language is the language code of the language we want to change to
     * @return returns the new context with the switched language
     */
    @SuppressWarnings("deprecation")
    public static Context setLocale (Context context, String Language)
    {
        persist(context,Language);

        Locale locale = new Locale(Language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();

        Configuration config = resources.getConfiguration();
        config.locale = locale;
        resources.updateConfiguration(config,resources.getDisplayMetrics());

        return context;

    }

    /**
     * getPersistentData is used to get the current language stored in the Shared preferences
     * @param defaultLang is the language we should default to if we do not find a language
     * @return returns the language code of the language stored in the Shared Preferences, or defaultLang
     */
    public static String getPersistentData(Context context, String defaultLang) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString("Lang", defaultLang);
    }


    /**
     * persist adds a language to the persistent Data
     * @param language is the language that should be added to the Shared preferences
     */
    private static void persist(Context context, String language)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Lang",language);
        editor.apply();
    }
}
