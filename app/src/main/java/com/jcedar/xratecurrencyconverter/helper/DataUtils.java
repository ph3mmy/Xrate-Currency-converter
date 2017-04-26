package com.jcedar.xratecurrencyconverter.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import com.jcedar.xratecurrencyconverter.R;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;

/**
 * Created by Afolayan Oluwaseyi on 02/11/2016.
 */
public class DataUtils {


    private static final String TAG = DataUtils.class.getSimpleName();
    private static final String CURRENCY_FIRST_RUN = "currency_first_run";
    private static final String CURRENCY_NAME_FIRST_RUN = "currency_name_first_run";
    private static final String CURRENCY_NAME_MAP = "currency_name";
    private static final String PREF_PROFILE_PIC = "profile_pic";
    private static final String UPDATE_TIME_KEY = "update_time_key";

    public static String getUpdateFrequency(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(
                context.getString(R.string.pref_key_frequency),
                context.getString(R.string.pref_frequency_default));
    }

    public static String getBaseCurrency(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(
                context.getString(R.string.pref_key_currency),
                context.getString(R.string.pref_currency_default));
    }

    public static void setCurrencyFirstRun(final boolean isFirst, final Context context){
        Log.d(TAG, "Set currency first run to" + Boolean.toString(isFirst));
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(CURRENCY_FIRST_RUN, isFirst).apply();
    }

    public static boolean isCurrencyFirstRun(final Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return  sp.getBoolean(CURRENCY_FIRST_RUN, true);
    }

    public static void setCurrencyNameFirstRun(final boolean isFirst, final Context context){
        Log.d(TAG, "Set currency name first run to" + Boolean.toString(isFirst));
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(CURRENCY_NAME_FIRST_RUN, isFirst).apply();
    }

    public static boolean isCurrencyNameFirstRun(final Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return  sp.getBoolean(CURRENCY_NAME_FIRST_RUN, true);
    }

    public static void setUpdateTime(Context context, String key){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString(UPDATE_TIME_KEY, key).apply();
    }

    public static String getUpdateTime(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(UPDATE_TIME_KEY, "0");
    }


    public static double invertedRate (String base){
//        int inv = Integer.parseInt(base);
        double db = Double.parseDouble(base);
        double inv = (1/db);
        DecimalFormat threeDp = new DecimalFormat("#.000");

        return Double.parseDouble(threeDp.format(inv));
    }

    public static double formatCurrencyDp (String rate){
        double mRate = Double.parseDouble(rate);
        DecimalFormat fiveDp = new DecimalFormat("#.000");
        return Double.parseDouble(fiveDp.format(mRate));
    }

    public static String currencyUrl () {
        return "http://www.apilayer.net/api/live?access_key=3b0cacb7e3ade580c18a3a26f23427fd";
    }

    public static String currencyNameUrl () {
        return "http://apilayer.net/api/list?access_key=3b0cacb7e3ade580c18a3a26f23427fd";
    }

    // method for bitmap to base64
    public static String encodeTobase64(Bitmap image) {
        Bitmap image1 = image; //create a copy of the image
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image1.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

        Log.d("Image Log:", imageEncoded);
        return imageEncoded;
    }

    public static Bitmap decodeBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory
                .decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    public static void saveImage(final Context context, Bitmap userPic, String imageName){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(imageName, encodeTobase64(userPic)).apply();
    }

    public static Bitmap getImage(final Context context, String imageName) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String imageStr =  sp.getString(imageName, null);
        return decodeBase64(imageStr);
    }


    public static int imageId(String symbol, String country, Context mContext) {
        int id;
        String countryCode = country.toLowerCase();
        if (countryCode.equalsIgnoreCase("do")) {
            String mUri = "@drawable/do1";
            id = mContext.getResources().getIdentifier(mUri, "raw", mContext.getPackageName());
        } else if (symbol.equalsIgnoreCase("xaf")) {
            String mUri = "@drawable/xaf";
            id = mContext.getResources().getIdentifier(mUri, "raw", mContext.getPackageName());
        } else if (symbol.equalsIgnoreCase("xag")) {
            String mUri = "@drawable/xag";
            id = mContext.getResources().getIdentifier(mUri, "raw", mContext.getPackageName());
        } else if (symbol.equalsIgnoreCase("xau")) {
            String mUri = "@drawable/xau";
            id = mContext.getResources().getIdentifier(mUri, "raw", mContext.getPackageName());
        } else {
            String uri = "@drawable/" + countryCode;
            id = mContext.getResources().getIdentifier(uri, "raw", mContext.getPackageName());
        }

        return id;
    }

}
