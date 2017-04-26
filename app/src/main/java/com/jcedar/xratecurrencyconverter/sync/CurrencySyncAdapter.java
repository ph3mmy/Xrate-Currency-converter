package com.jcedar.xratecurrencyconverter.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.jcedar.xratecurrencyconverter.R;
import com.jcedar.xratecurrencyconverter.adapter.CurrencyCursorAdapter;
import com.jcedar.xratecurrencyconverter.helper.AppSingleton;
import com.jcedar.xratecurrencyconverter.helper.DataUtils;
import com.jcedar.xratecurrencyconverter.helper.FormatUtils;
import com.jcedar.xratecurrencyconverter.helper.Lists;
import com.jcedar.xratecurrencyconverter.provider.CurrencyContract;
import com.jcedar.xratecurrencyconverter.ui.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by OLUWAPHEMMY on 11/11/2016.
 */
public class CurrencySyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = CurrencySyncAdapter.class.getName();
    public static final String KEY_CURRENCY_SYMBOL = "quotes";
    public static final String KEY_CURRENCY_UPDATE_RATE = "rate";
    public static final String KEY_CURRENCY_UPDATE_CODE = "currency_code";
    public static final String SYNC_ACCOUNT_TYPE = "com.jcedar.xratecurrencyconverter";
    public static final String KEY_CURRENCY_NAME_URL= "http://apilayer.net/api/list?access_key=3b0cacb7e3ade580c18a3a26f23427fd";
    public static final String KEY_CURRENCY_URL= "http://www.apilayer.net/api/live?access_key=3b0cacb7e3ade580c18a3a26f23427fd";
    public static final String KEY_UPDATE_URL= "http://www.mycurrency.net/service/rates";
    public static final String KEY_FLAG_URL= "http://caches.space/flags/";
    private static final String KEY_CURRENCY_TIMESTAMP = "timestamp";
    private static Context mContext;
    static ContentResolver contentResolver;
    static CurrencyCursorAdapter currencyCursorAdapter;
    private static ProgressDialog mDialog;

    // Interval at which to sync with the weather, in seconds
    // 60 seconds (1min) * 180 = 3 hours
//    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_INTERVAL = 60 * 3;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;

    public CurrencySyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
        contentResolver = context.getContentResolver();
//        currencyCursorAdapter = new CurrencyCursorAdapter(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {


        Log.d(TAG, "onPerformSync");
        if (DataUtils.isCurrencyFirstRun(mContext)) {
            fetchCurrencyVolley(mContext);
        } else {
            updateRateAndTimeVolley(mContext);
        }
    }
    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {

        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder()
                    .syncPeriodic(syncInterval, flexTime)
                    .setSyncAdapter(account, authority)
                    .setExtras(new Bundle())
                    .build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        // Since we've created an account
        CurrencySyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        // Without calling setSyncAutomatically, our periodic sync will not be enabled
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        // Finally, do a sync to get things started
//        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context An app context
     */
    public static void syncImmediately(Context context) {
        Log.e(TAG, "inside syncImme");
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);

        Account account = getSyncAccount(context);
        ContentResolver.requestSync(account,
                context.getString(R.string.content_authority), bundle);
    }

    private static Account getSyncAccount(Context context) {
        //Get an instance of Android acct manager
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        //Create the Account type and default acct
        Account newAccount = new Account(context.getString(R.string.app_name), SYNC_ACCOUNT_TYPE);
//        Account newAccount = new Account(context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If password doesn't exist, the account doesn't exist
        if (accountManager.getPassword(newAccount) == null) {
            // If not successful
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            // If you don't set android:syncable="true" in your <provider> element in the manifest
            // then call context.setIsSyncable(account, AUTHORITY, 1) here
            onAccountCreated(newAccount, context);
        }

        return newAccount;
    }

    private static void fetchCurrencyVolley (final Context context) {
        String url = KEY_CURRENCY_URL;
        JsonObjectRequest req = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                getCurrencySymbol(jsonObject, context);
                fetchCurrencyNamesVolley(context);
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
               /* if (mDialog != null) {
                    mDialog.dismiss();
                }*/
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
                /*
                if (mDialog != null && mDialog.isShowing()){
                    mDialog.dismiss();
                }*/
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

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
                resolver.notifyChange(CurrencyContract.CurrencyName.CONTENT_URI, null, false);
            }
            Toast.makeText(mContext, "Currency Rates Successfully downloaded", Toast.LENGTH_LONG).show();
            DataUtils.setCurrencyFirstRun(false, context);
/*
            String tt = fetchTimeForTv(mContext);
            DataUtils.setUpdateTime(mContext, tt);*/

            /*
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
                resolver.notifyChange(CurrencyContract.Currency.CONTENT_URI, null, false);
                DataUtils.setCurrencyFirstRun(false, context);
            }
        } catch (JSONException | RemoteException | OperationApplicationException e) {
            e.printStackTrace();
        }
       /* if (mDialog != null && mDialog.isShowing()){
            mDialog.dismiss();
        }*/

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

           /* if (mDialog != null && mDialog.isShowing()){
                mDialog.dismiss();
            }*/
        } catch (Exception e){
            e.printStackTrace();
        }

        return batch;
    }



    public static   void updateRateAndTimeVolley(final Context context) {
        mContext = context;
        String url = KEY_UPDATE_URL;
        JsonArrayRequest reqName = new JsonArrayRequest(url,  new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                getUpdatedRates(response, context);
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

                Toast.makeText(context, "An error occured : " + volleyError, Toast.LENGTH_LONG).show();
            }
        });
        AppSingleton.getInstance(context).addToRequestQueue(reqName, TAG);
    }

    public static void getUpdatedRates (JSONArray response, Context context){

        HashMap<String, String> updatedHash = codeNameHash(response);
        updateDbRate(updatedHash, context);

    }

    private static HashMap<String, String> codeNameHash (JSONArray response) {

        HashMap<String, String> updateCode = new HashMap<>();
        for (int i = 0; i < response.length(); i++){
            try {
                JSONObject mObject = response.getJSONObject(i);
                String currencyUpdateCode = mObject.getString(KEY_CURRENCY_UPDATE_CODE);
                String currencyUpdateRate = mObject.getString(KEY_CURRENCY_UPDATE_RATE);

                updateCode.put(currencyUpdateCode, currencyUpdateRate);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return updateCode;
    }

    private static void updateDbRate (HashMap<String,String> code, Context context) {
        Uri uri = CurrencyContract.Currency.CONTENT_URI;
        Cursor c = context.getContentResolver().query(uri, new String[]{CurrencyContract.Currency.CURRENCY_SYMBOL}, null, null, null);
        String ff;
        assert c != null;
        while (c.moveToNext()) {
            ff = c.getString(c.getColumnIndex(CurrencyContract.Currency.CURRENCY_SYMBOL));

            if (code.containsKey(ff)) {
                String newRate = code.get(ff);
                Log.e(TAG, "Compared value with rate " + ff + " new rate " + newRate);

                String updateTime = String.valueOf(System.currentTimeMillis()/1000L);
                Log.e(TAG, " Updated time Stamp new rate  " + updateTime);
                ContentValues updateValues = new ContentValues();
                updateValues.put(CurrencyContract.Currency.BASE_RATE, DataUtils.formatCurrencyDp(newRate));
                updateValues.put(CurrencyContract.Currency.INVERTED_RATE, DataUtils.invertedRate(newRate));
                updateValues.put(CurrencyContract.Currency.TIME_STAMP, updateTime);

                contentResolver.update(uri, updateValues, CurrencyContract.Currency.CURRENCY_SYMBOL +
                        "=?", new String[]{ff});

            }
            contentResolver.notifyChange(uri, null);
//            mContext.getContentResolver().notifyChange(uri, null);

        }
        c.close();
        Toast.makeText(mContext, "Rates Successfully Updated", Toast.LENGTH_LONG).show();
    }

}