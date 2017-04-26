package com.jcedar.xratecurrencyconverter.io;

import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.jcedar.xratecurrencyconverter.helper.AppSingleton;
import com.jcedar.xratecurrencyconverter.helper.DataUtils;
import com.jcedar.xratecurrencyconverter.helper.FormatUtils;
import com.jcedar.xratecurrencyconverter.helper.Lists;
import com.jcedar.xratecurrencyconverter.provider.CurrencyContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

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
    private static final String KEY_CURRENCY_TIMESTAMP = "timestamp";
    private static Context mContext;
    private static ProgressDialog mDialog;
    static HashMap<String, String> baseSixtyFour = new HashMap<>();

    public static void fetchCurrencyFromJson (final Context context) {
        mContext = context;
        mDialog = new ProgressDialog(context);
        mDialog.setMessage("Updating Rate");
        mDialog.setTitle("Please wait");
        mDialog.setIndeterminate(true);
        mDialog.setCancelable(false);
        mDialog.show();
        fetchCurrencyVolley(context);
    }

        private static void fetchCurrencyVolley (final Context context) {
        String url = KEY_CURRENCY_URL;
        JsonObjectRequest req = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                getCurrencySymbol(jsonObject, context);
                DataUtils.setCurrencyFirstRun(false, context);
                fetchCurrencyNamesVolley(context);
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (mDialog != null) {
                    mDialog.dismiss();
                }
                Toast.makeText(context, "An error occured : " + volleyError, Toast.LENGTH_LONG).show();
            }
        });

        AppSingleton.getInstance(context).addToRequestQueue(req, TAG);
    }


    public static   void fetchCurrencyNamesVolley(final Context context) {
        mContext = context;
        String url = KEY_CURRENCY_NAME_URL;
        JsonObjectRequest reqName = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                getCurrencyName(jsonObject, context);
                DataUtils.setCurrencyNameFirstRun(false, context);
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

                if (mDialog.isShowing()){
                    mDialog.dismiss();
                }
                Toast.makeText(context, "An error occured : " + volleyError, Toast.LENGTH_LONG).show();
            }
        });
        AppSingleton.getInstance(context).addToRequestQueue(reqName, TAG);
    }

    public static void getCurrencyName(JSONObject currency, Context context){
        try {
            JSONObject curr = currency.getJSONObject("currencies");
            if (curr == null) {
                return;
            }
            ArrayList<ContentProviderOperation> operations = parseCurrencyName(curr);
            if (operations.size() > 0) {
                ContentResolver resolver = context.getContentResolver();
                resolver.applyBatch(CurrencyContract.CONTENT_AUTHORITY, operations);
            }
            DataUtils.setCurrencyFirstRun(false, context);/*
            if (mDialog != null && mDialog.isShowing()){
                mDialog.dismiss();
            }*/
        }catch (Exception e){
            Log.e(TAG, "getCurrency error " + e);
        }
    }

    private static ArrayList<ContentProviderOperation> parseCurrencyName  (JSONObject curr){
        ArrayList<ContentProviderOperation> batch = Lists.newArrayList();
        HashMap<String, String> codes = new HashMap<>();
        try {
            for (int i = 0; i < curr.length(); i++) {
                JSONArray nameArray = curr.names();
                String rawCurrency = nameArray.get(i).toString();
                String nameObj = curr.getString(rawCurrency);

                codes.put(rawCurrency, nameObj);

                Uri uri = CurrencyContract.addCallerIsSyncAdapterParameter(CurrencyContract.CurrencyName.CONTENT_URI);
                ContentProviderOperation.Builder builder = ContentProviderOperation
                        .newInsert(uri)
                        .withValue(CurrencyContract.CurrencyName.CURRENCY_SYMBOL, rawCurrency)
                        .withValue(CurrencyContract.CurrencyName.CURRENCY_NAME, nameObj);

                batch.add(builder.build());
            }
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
                SharedPreferences.Editor editor = sp.edit();
                for (String s : codes.keySet()) {
                    editor.putString(s, codes.get(s));
                }
                editor.apply();

//                insertCurrencyNameInDb(nameObj, rawCurrency);

        } catch (Exception e){
            e.printStackTrace();
        }
        return batch;
    }

    public static void getCurrencySymbol(JSONObject symbol, Context context){
        try {
            JSONObject quo = symbol.getJSONObject(KEY_CURRENCY_SYMBOL);
            String timeStamp = symbol.getString(KEY_CURRENCY_TIMESTAMP);
            if ((quo == null) && (timeStamp ==null)){
                return;
            }
            ArrayList<ContentProviderOperation> operations = parseCurrencyQuotes(quo, timeStamp);
            if (operations.size() > 0){
                ContentResolver resolver =  context.getContentResolver();
                resolver.applyBatch(CurrencyContract.CONTENT_AUTHORITY, operations);
            }
        } catch (JSONException | RemoteException | OperationApplicationException e) {
            e.printStackTrace();
        }
        if (mDialog != null && mDialog.isShowing()){
            mDialog.dismiss();
        }

        Toast.makeText(mContext, "Rates Successfully Updated after parse", Toast.LENGTH_LONG).show();
    }

    private static ArrayList<ContentProviderOperation> parseCurrencyQuotes (JSONObject quo, String timeStamp){
        final ArrayList<ContentProviderOperation> batch = Lists.newArrayList();
        try {
            for (int i = 0; i < quo.length(); i++) {
                JSONArray mObject = quo.names();
                String rawSymbol = mObject.getString(i);
                String currencySymbol = rawSymbol.substring(rawSymbol.length() - 3);
                String baseCurrencySymbol = rawSymbol.substring(0, 3);
                String rawRate = quo.getString(rawSymbol);
                String currencyRate = String.valueOf(DataUtils.formatCurrencyDp(rawRate));
                final String currencyCode = rawSymbol.substring(3, 5);
                String inverseValue = String.valueOf(DataUtils.invertedRate(rawRate));

                Uri uri = CurrencyContract.addCallerIsSyncAdapterParameter(CurrencyContract.Currency.CONTENT_URI);
                ContentProviderOperation.Builder builder = ContentProviderOperation
                        .newInsert(uri)
                        .withValue(CurrencyContract.Currency.BASE_CURRENCY_SYMBOL, baseCurrencySymbol)
                        .withValue(CurrencyContract.Currency.CURRENCY_SYMBOL, currencySymbol)
                        .withValue(CurrencyContract.Currency.CURRENCY_CODE, currencyCode)
                        .withValue(CurrencyContract.Currency.BASE_RATE, currencyRate)
                        .withValue(CurrencyContract.Currency.INVERTED_RATE, inverseValue)
                        .withValue(CurrencyContract.Currency.TIME_STAMP, timeStamp);
                batch.add(builder.build());
                }
        } catch (Exception e){
            e.printStackTrace();
        }

        return batch;
    }

    public static void fetchTimeForTv (Context context, TextView tv){
        Cursor c = context.getContentResolver().query(CurrencyContract.Currency.CONTENT_URI,
                new String[] {CurrencyContract.Currency.TIME_STAMP}, null, null,null);
//        assert c != null;
        String ff = null;
        if (c != null) {
            c.moveToLast();
            long timeBase = Long.parseLong(c.getString(c.getColumnIndex(CurrencyContract.Currency.TIME_STAMP)));
            Log.e(TAG, "Returned time Stamp " + timeBase);
            ff = FormatUtils.getReadableDate(timeBase);
            tv.setText(ff);
        } c.close();
//        return ff;
    }

}
