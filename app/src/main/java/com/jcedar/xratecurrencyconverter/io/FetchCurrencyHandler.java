package com.jcedar.xratecurrencyconverter.io;

import android.content.ContentProviderOperation;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.jcedar.xratecurrencyconverter.adapter.RateListAdapter;
import com.jcedar.xratecurrencyconverter.fragment.RateFragment;
import com.jcedar.xratecurrencyconverter.helper.AppSingleton;
import com.jcedar.xratecurrencyconverter.helper.Lists;
import com.jcedar.xratecurrencyconverter.model.CurrencyModel;
import com.jcedar.xratecurrencyconverter.provider.CurrencyContract;
import com.jcedar.xratecurrencyconverter.provider.DataProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by OLUWAPHEMMY on 10/30/2016.
 */
public class FetchCurrencyHandler {

    final static String TAG = FetchCurrencyHandler.class.getName();
    public static final String KEY_CURRENCY_SYMBOL = "quotes";
    public static final String KEY_CURRENCY_RATE = "rate";
    public static final String KEY_CURRENCY_CODE = "code";
    public static final String KEY_CURRENCY_NAME = "name";
    public static final String KEY_CURRENCY_NAME_URL= "http://apilayer.net/api/list?access_key=3b0cacb7e3ade580c18a3a26f23427fd";
    public static final String KEY_CURRENCY_URL= "http://www.apilayer.net/api/live?access_key=3b0cacb7e3ade580c18a3a26f23427fd";
    public static final String KEY_FLAG_URL= "http://caches.space/flags/";
    private static Context mContext;

    static String[] fromFetch;

    public static void fetchCurrencyFromJson (final Context context){
        mContext = context;

        String url = KEY_CURRENCY_URL;
        JsonObjectRequest req = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                getCurrencySymbol(jsonObject);
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

                Toast.makeText(context, "An error occured : " + volleyError, Toast.LENGTH_LONG).show();
            }
        });

        AppSingleton.getInstance(context).addToRequestQueue(req, TAG);
    }


    public static   void currencyNamesResult(final Context context) {
        mContext = context;

        String url = KEY_CURRENCY_NAME_URL;
        JsonObjectRequest reqName = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                getCurrencyName(jsonObject);
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

                Toast.makeText(context, "An error occured : " + volleyError, Toast.LENGTH_LONG).show();
            }
        });
        AppSingleton.getInstance(context).addToRequestQueue(reqName, TAG);
    }

    public static void getCurrencyName(JSONObject currency){

        try {
            JSONObject curr = currency.getJSONObject("currencies");
//            currencyName = new String[curr.length()];
            for (int i = 0; i < curr.length(); i++){
                JSONArray nameArray = curr.names();
                String rawCurrency = nameArray.get(i).toString();
                String nameObj = curr.getString(rawCurrency);

                Log.e(TAG, " Gotten Names: " + nameObj);
                insertCurrencyNameInDb(nameObj, rawCurrency);
            }

        }catch (Exception e){
            Log.e(TAG, "getCurrency error " + e);
        }
    }


    public static void getCurrencySymbol(JSONObject symbol){
        try {
            JSONObject quo = symbol.getJSONObject(KEY_CURRENCY_SYMBOL);
            Vector<ContentValues> cVVector = new Vector<ContentValues>(quo.length());
            for (int i = 0; i < quo.length(); i++) {
//                JSONObject mObject = symbol.getJSONObject(i);

                JSONArray mObject = quo.names();
                String rawSymbol = mObject.getString(i);
                String currencySymbol  = rawSymbol.substring(rawSymbol.length() - 3);
                String baseCurrencySymbol  = rawSymbol.substring(0,3);
                String rawRate  = quo.getString(rawSymbol);
                String currencyRate = String.valueOf(formatCurrencyDp(rawRate));
                String currencyCode  = rawSymbol.substring(3, 5);
                String inverseValue  = String.valueOf(invertedRate(rawRate));

//                for (String s : currencySymbol){

                insertCurrencyInDatabase(currencySymbol, currencyCode, baseCurrencySymbol, currencyRate, inverseValue);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void insertCurrencyNameInDb(String currencyName, String currencySymbol){

        ArrayList<ContentValues> mValue = new ArrayList<ContentValues>();
        //first check if currency with this name already exist
        Cursor cursor = mContext.getContentResolver().query(
                CurrencyContract.CurrencyName.CONTENT_URI,
                new String[] {CurrencyContract.CurrencyName._ID},
                CurrencyContract.CurrencyName.CURRENCY_SYMBOL+"=?",
                new String[] {currencyName},
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {

        } else {

            ContentValues nameVal = new ContentValues();
            nameVal.put(CurrencyContract.CurrencyName.CURRENCY_SYMBOL, currencySymbol);
            nameVal.put(CurrencyContract.CurrencyName.CURRENCY_NAME, currencyName);

            mContext.getContentResolver().insert(CurrencyContract.CurrencyName.CONTENT_URI, nameVal);
        }

        if (cursor != null){
            cursor.close();
        }
    }

    private static void insertCurrencyInDatabase(String symbol, String code, String baseCurrencySymbol, String baseRate, String inverseRate) {
        long rowId = 0;

        // First check if the location with this city name exists in the db
        Cursor cursor = mContext.getContentResolver().query(
                CurrencyContract.Currency.CONTENT_URI,
                new String[] {CurrencyContract.Currency._ID},
                CurrencyContract.Currency.CURRENCY_SYMBOL + " = ?",
                new String[] {symbol},
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {

            mContext.getContentResolver().delete(CurrencyContract.Currency.CONTENT_URI, null, null);
         /*   int locationIdIndex =  cursor.getColumnIndex(LocationEntry._ID);
            rowId = cursor.getLong(locationIdIndex);*/


            ContentValues currValues = new ContentValues();
            currValues.put(CurrencyContract.Currency.BASE_CURRENCY_SYMBOL, baseCurrencySymbol);
            currValues.put(CurrencyContract.Currency.CURRENCY_SYMBOL, symbol);
            currValues.put(CurrencyContract.Currency.CURRENCY_CODE, code);
            currValues.put(CurrencyContract.Currency.BASE_RATE, baseRate);
            currValues.put(CurrencyContract.Currency.INVERTED_RATE, inverseRate);

            mContext.getContentResolver().insert(CurrencyContract.Currency.CONTENT_URI, currValues);

        } else {

            ContentValues currValues = new ContentValues();
            currValues.put(CurrencyContract.Currency.BASE_CURRENCY_SYMBOL, baseCurrencySymbol);
            currValues.put(CurrencyContract.Currency.CURRENCY_SYMBOL, symbol);
            currValues.put(CurrencyContract.Currency.CURRENCY_CODE, code);
            currValues.put(CurrencyContract.Currency.BASE_RATE, baseRate);
            currValues.put(CurrencyContract.Currency.INVERTED_RATE, inverseRate);

            mContext.getContentResolver().insert(CurrencyContract.Currency.CONTENT_URI, currValues);
//            rowId = ContentUris.parseId(uri);

        }

        if (cursor != null) {
            cursor.close();
        }
//        return rowId;
    }

    public static double invertedRate (String base){
//        int inv = Integer.parseInt(base);
        double db = Double.parseDouble(base);
        double inv = (1/db);
        DecimalFormat threeDp = new DecimalFormat("#.000");

        return Double.parseDouble(threeDp.format(inv));
    }

    private static double formatCurrencyDp (String rate){
        double mRate = Double.parseDouble(rate);
        DecimalFormat fiveDp = new DecimalFormat("#.000");
        return Double.parseDouble(fiveDp.format(mRate));
    }
}
