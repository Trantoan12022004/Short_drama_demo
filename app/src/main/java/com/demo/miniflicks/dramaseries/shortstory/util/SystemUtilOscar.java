package com.demo.miniflicks.dramaseries.shortstory.util;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import java.util.Locale;



public class SystemUtilOscar {

    public static void saveLocale(Context context, String lang) {
        setPreLanguage(context, lang);
    }

    public static void setLocale(Context context) {
        String languageOscar = getPreLanguage(context);
        if (languageOscar.equals("")) {
            Configuration configOscar = new Configuration();
            Locale localeOscar = Locale.getDefault();
            Locale.setDefault(localeOscar);
            configOscar.locale = localeOscar;
            context.getResources().updateConfiguration(configOscar, context.getResources().getDisplayMetrics());
        } else {
            changeLang(context, languageOscar);
        }
    }

    public static void changeLang(Context context, String lang) {
        if (lang.equalsIgnoreCase("")) return;

        String[] localePartsOscar = lang.split("-");
        Configuration configOscar = new Configuration();

        if (localePartsOscar.length > 1) {
            configOscar.locale = new Locale(localePartsOscar[0], localePartsOscar[1]);
        } else {
            configOscar.locale = new Locale(lang);
        }

        context.getResources().updateConfiguration(configOscar, context.getResources().getDisplayMetrics());
    }

    public static String getPreLanguage(Context mContext) {
        SharedPreferences preferencesOscar = mContext.getSharedPreferences("data", Context.MODE_PRIVATE);
        return preferencesOscar.getString("KEY_LANGUAGE", "");
    }

    public static void setPreLanguage(Context context, String languageOscar) {
        if (languageOscar == null || languageOscar.equals("")) {
        } else {
            SharedPreferences preferencesOscar = context.getSharedPreferences("data", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferencesOscar.edit();
            editor.putString("KEY_LANGUAGE", languageOscar);
            editor.apply();
        }
    }
}