package com.jcedar.xratecurrencyconverter.service;

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
import com.jcedar.xratecurrencyconverter.helper.AppSingleton;
import com.jcedar.xratecurrencyconverter.helper.DataUtils;
import com.jcedar.xratecurrencyconverter.provider.CurrencyContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import me.tatarka.support.job.JobParameters;
import me.tatarka.support.job.JobService;

/**
 * Created by OLUWAPHEMMY on 11/17/2016.
 */
public class CurrencyService extends JobService {

    Context mContext;

    @Override
    public boolean onStartJob(JobParameters params) {

//        Toast.makeText(this, "onStart Job ", Toast.LENGTH_SHORT).show();
        new CurrencyTask(this).execute(params);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }





    private static class CurrencyTask extends AsyncTask<JobParameters, Void, JobParameters> {

        CurrencyService mService;
        static Context mContext;
        private static final String TAG = CurrencyTask.class.getName();
        public static final String KEY_CURRENCY_UPDATE_RATE = "rate";
        public static final String KEY_CURRENCY_UPDATE_CODE = "currency_code";
        public static final String KEY_UPDATE_URL= "http://www.mycurrency.net/service/rates";
        private static ContentResolver contentResolver;

        CurrencyTask(CurrencyService service) {

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
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JobParameters doInBackground(JobParameters... params) {

                JSONArray updateArray = updateRateAndTimeVolley(mContext);
                getUpdatedRates(updateArray, mContext);

            return params[0];
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




/*

        private static JSONObject fetchCurrencyVolley(final Context context) {
            String url = KEY_CURRENCY_URL;
            JSONObject response = null;
            RequestFuture <JSONObject> requestFuture = RequestFuture.newFuture();

            JsonObjectRequest req = new JsonObjectRequest(url, null, requestFuture, requestFuture);

            AppSingleton.getInstance(context).addToRequestQueue(req, TAG);
            try {
                response = requestFuture.get(30000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
            }

            return response;
        }


        public static JSONObject fetchCurrencyNamesVolley(final Context context) {
            mContext = context;
            String url = KEY_CURRENCY_NAME_URL;
            JSONObject response = null;
            RequestFuture<JSONObject> future = RequestFuture.newFuture();
            JsonObjectRequest reqName = new JsonObjectRequest(url, null, future, future);
            AppSingleton.getInstance(context).addToRequestQueue(reqName, TAG);

            try {
                response = future.get(30000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
            }
            return response;
        }

        public static void getCurrencyName(JSONObject currency, Context context) {
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

            } catch (Exception e) {
                Log.e(TAG, "getCurrency error " + e);
            }
        }

        private static ArrayList<ContentProviderOperation> parseCurrencyName(JSONObject curr) {
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

            } catch (Exception e) {
                e.printStackTrace();
            }
            return batch;
        }

        public static void getCurrencySymbol(JSONObject symbol, Context context) {
            try {
                JSONObject quo = symbol.getJSONObject(KEY_CURRENCY_SYMBOL);
                String timeStamp = symbol.getString(KEY_CURRENCY_TIMESTAMP);
                if ((quo == null) && (timeStamp == null)) {
                    return;
                }
                ArrayList<ContentProviderOperation> operations = parseCurrencyQuotes(quo, timeStamp);
                if (operations.size() > 0) {
                    ContentResolver resolver = context.getContentResolver();
                    resolver.applyBatch(CurrencyContract.CONTENT_AUTHORITY, operations);
                    resolver.notifyChange(CurrencyContract.Currency.CONTENT_URI, null, false);
                    DataUtils.setCurrencyFirstRun(false, context);
                }
            } catch (JSONException | RemoteException | OperationApplicationException e) {
                e.printStackTrace();
            }

        }

        private static ArrayList<ContentProviderOperation> parseCurrencyQuotes(JSONObject quo, String timeStamp) {
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

            } catch (Exception e) {
                e.printStackTrace();
            }

            return batch;
        }

        public static String fetchTimeForTv(Context context) {
            Cursor c = context.getContentResolver().query(CurrencyContract.Currency.CONTENT_URI,
                    new String[]{CurrencyContract.Currency.TIME_STAMP}, null, null, null);
//        assert c != null;
            String ff = null;
            if (c != null) {
                c.moveToFirst();
                long timeBase = Long.parseLong(c.getString(c.getColumnIndex(CurrencyContract.Currency.TIME_STAMP)));
                Log.e(TAG, "Returned time Stamp " + timeBase);
                ff = FormatUtils.getReadableDate(timeBase);
//            tv.setText(ff);
            }
            c.close();
            return ff;
        }*/

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


            /*new Thread(new Runnable() {
                @Override
                public void run() {

                    Toast.makeText(mContext, "Rates Successfully Updated", Toast.LENGTH_LONG).show();
                }
            }).start();*/
        }
    }

}
