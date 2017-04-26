package com.jcedar.xratecurrencyconverter.helper;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.RequestFuture;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.jcedar.xratecurrencyconverter.provider.CurrencyContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by OLUWAPHEMMY on 1/17/2017.
 */

public class CurrencyJobService extends JobService {


    @Override
    public boolean onStartJob(JobParameters job) {

        new CurrencyTask(this).execute(job);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
    }


    private static class CurrencyTask extends AsyncTask<JobParameters, Void, JobParameters> {

        CurrencyJobService mService;
        static Context mContext;
        private static final String TAG = CurrencyJobService.CurrencyTask.class.getName();
        public static final String KEY_CURRENCY_UPDATE_RATE = "rate";
        public static final String KEY_CURRENCY_UPDATE_CODE = "currency_code";
        public static final String KEY_UPDATE_URL= "http://www.mycurrency.net/service/rates";
        private static ContentResolver contentResolver;

        CurrencyTask(CurrencyJobService service) {

            mContext = service;
            contentResolver = service.getContentResolver();
            this.mService = service;

        }

        /**send broadcast to mainactivity after update*/
        static void notifyActivity(String data){
            Intent intent = new Intent("my-event");
            intent.putExtra("message", data);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        }

        @Override
        protected JobParameters doInBackground(JobParameters... jobParameterses) {
            JSONArray updateArray = updateRateAndTimeVolley(mContext);
            getUpdatedRates(updateArray, mContext);

            return jobParameterses[0];
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(JobParameters jobParameters) {
            super.onPostExecute(jobParameters);

            /**broadcastmanager intent data*/
            String data = "TRUE";
            notifyActivity(data);
            Toast.makeText(mContext, "Currency Rates Successfully updated", Toast.LENGTH_LONG).show();
            // TODO: 12/31/2016  i altered jobfinished boolean to false
            mService.jobFinished(jobParameters, false);
        }


        public static JSONArray updateRateAndTimeVolley(final Context context) {
            mContext = context;
            String url = KEY_UPDATE_URL;
            JSONArray response = null;
            RequestFuture<JSONArray> future = RequestFuture.newFuture();
            JsonArrayRequest reqName = new JsonArrayRequest(url, future, future);
            AppSingleton.getInstance(context).addToRequestQueue(reqName, TAG);

            try {
                response = future.get(30000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
            }
            return response;
        }

        public static void getUpdatedRates(JSONArray response, Context context) {

            HashMap<String, String> updatedHash = codeNameHash(response);
            updateDbRate(updatedHash, context);

        }

        private static HashMap<String, String> codeNameHash(JSONArray response) {

            HashMap<String, String> updateCode = new HashMap<>();

            if (response != null) {
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject mObject = response.getJSONObject(i);
                        String currencyUpdateCode = mObject.getString(KEY_CURRENCY_UPDATE_CODE);
                        String currencyUpdateRate = mObject.getString(KEY_CURRENCY_UPDATE_RATE);

                        updateCode.put(currencyUpdateCode, currencyUpdateRate);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            return updateCode;
        }

        private static void updateDbRate(HashMap<String, String> code, Context context) {
            Uri uri = CurrencyContract.Currency.CONTENT_URI;
            Cursor c = context.getContentResolver().query(uri, new String[]{CurrencyContract.Currency.CURRENCY_SYMBOL}, null, null, null);
            String ff;
            assert c != null;
            while (c.moveToNext()) {
                ff = c.getString(c.getColumnIndex(CurrencyContract.Currency.CURRENCY_SYMBOL));

                if (code.containsKey(ff)) {
                    String newRate = code.get(ff);
                    Log.e(TAG, "Compared value with rate " + ff + " new rate " + newRate);

                    String updateTime = String.valueOf(System.currentTimeMillis() / 1000L);
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
        }
    }


}
