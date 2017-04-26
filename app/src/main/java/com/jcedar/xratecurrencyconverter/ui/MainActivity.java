package com.jcedar.xratecurrencyconverter.ui;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.evernote.android.job.JobManager;
import com.jcedar.xratecurrencyconverter.R;
import com.jcedar.xratecurrencyconverter.adapter.ViewPagerAdapter;
import com.jcedar.xratecurrencyconverter.fragment.ConvertFragment;
import com.jcedar.xratecurrencyconverter.fragment.RateFragment;
import com.jcedar.xratecurrencyconverter.helper.AppSingleton;
import com.jcedar.xratecurrencyconverter.helper.DataUtils;
import com.jcedar.xratecurrencyconverter.helper.Lists;
import com.jcedar.xratecurrencyconverter.provider.CurrencyContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MainActivity extends AppCompatActivity implements ConvertFragment.OnFragmentInteractionListener,
        RateFragment.OnFragmentInteractionListener {


    private ConvertFragment convertFragment;
    private RateFragment rateFragment;
    private static final String TAG = MainActivity.class.getSimpleName();


    @Override
    protected void onStart() {
        super.onStart();

      /*  if (DataUtils.isCurrencyFirstRun(this)) {
            checkConnection();
        }*/

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        convertFragment = new ConvertFragment();
        rateFragment = new RateFragment();

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        setupViewPager(viewPager);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tabLayout.setupWithViewPager(viewPager);

        tabLayout.setTabTextColors(ContextCompat.getColorStateList(this, R.color.tab_selector));
        tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.indicator));

        JobManager.create(this);

//        CurrencySyncAdapter.initializeSyncAdapter(this);
        if ((DataUtils.isCurrencyFirstRun(this)) && (checkConnection())) {
        new firstRunFetch(MainActivity.this).execute();
        }

    }


    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), this);
        adapter.addFragment(convertFragment, "Converter");
        adapter.addFragment(rateFragment, "Rates");
        viewPager.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settingIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }


    public void refreshRate(){
        Fragment frg = getSupportFragmentManager().findFragmentByTag("rate_frag");
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.detach(frg);
        ft.attach(frg);
        ft.commit();
        getSupportFragmentManager().executePendingTransactions();
    }


    public class firstRunFetch extends AsyncTask<Void, Void, Void> {


        public Context mContext;
        private final String TAG = firstRunFetch.class.getName();
        public static final String KEY_CURRENCY_SYMBOL = "quotes";
        private static final String KEY_CURRENCY_TIMESTAMP = "timestamp";

        public ProgressDialog mDialog;
        public firstRunFetch (Context ctx) {
            this.mContext = ctx;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mDialog = new ProgressDialog(mContext);
            mDialog.setMessage("Initializing...");
            mDialog.setTitle("Please wait");
            mDialog.setIndeterminate(true);
            mDialog.setCancelable(false);
            mDialog.show();


        }

        @Override
        protected Void doInBackground(Void... voids) {

            JSONObject symbol = fetchCurrencyVolley(mContext);
            if (symbol != null) {
                getCurrencySymbol(symbol, mContext);
            }
            JSONObject nameResponse = fetchCurrencyNamesVolley(mContext);
            getCurrencyName(nameResponse, mContext);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if ((mDialog.isShowing()) && (mDialog != null)) {
                reloadRate();
                mDialog.dismiss();
            }
            Toast.makeText(MainActivity.this, "Currency Rates Successfully downloaded", Toast.LENGTH_LONG).show();

        }



        private JSONObject fetchCurrencyVolley(final Context context) {
            String url = DataUtils.currencyUrl();
            JSONObject response = null;
            RequestFuture<JSONObject> requestFuture = RequestFuture.newFuture();

            JsonObjectRequest req = new JsonObjectRequest(url, null, requestFuture, requestFuture);

            AppSingleton.getInstance(context).addToRequestQueue(req, TAG);
            try {
                response = requestFuture.get(30000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
            }
            return response;
        }


        public JSONObject fetchCurrencyNamesVolley(final Context context) {
            mContext = context;
            String url = DataUtils.currencyNameUrl();
            JSONObject response = null;
            RequestFuture<JSONObject> future = RequestFuture.newFuture();
            JsonObjectRequest reqName = new JsonObjectRequest(url, null, future, future);
            AppSingleton.getInstance(context).addToRequestQueue(reqName, TAG);

            try {
                response = future.get(30000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
            }
            Log.e(TAG, "name response: " + response);
            return response;
        }

        public void getCurrencyName(JSONObject currency, Context context) {
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
                DataUtils.setCurrencyFirstRun(false, context);

            } catch (Exception e) {
                Log.e(TAG, "getCurrency error " + e);
            }
        }

        private ArrayList<ContentProviderOperation> parseCurrencyName(JSONObject curr) {
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

        public void getCurrencySymbol(JSONObject symbol, Context context) {
            try {
                JSONObject quo = symbol.getJSONObject(KEY_CURRENCY_SYMBOL);
                String timeStamp = String.valueOf((System.currentTimeMillis()/1000L));
                if (quo == null) {
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

        private ArrayList<ContentProviderOperation> parseCurrencyQuotes(JSONObject quo, String timeStamp) {
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


    }

    private boolean checkConnection () {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = (activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting());
        if (!isConnected) {

            networkDialog();
            return false;
        } else

            return true;
    }

    private void networkDialog (){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("No Internet Connectivity detected! Check your Internet Connectivity settings")
                .setCancelable(false)
                .setPositiveButton("Check Settings", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(Settings.ACTION_SETTINGS));
                    }
                })
                .setNegativeButton("Quit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            if (message.equalsIgnoreCase("TRUE")){
                reloadRate();
            }
            Log.e(TAG, "broadcasted message : " + message);
        }
    };


    /**refresh rate fragment after db population*/
    public void reloadRate(){
        Fragment frg = getSupportFragmentManager().findFragmentByTag("rate_frag");
        RateFragment ratee = (RateFragment) frg;
        @SuppressLint("CommitTransaction")
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.detach(ratee).commitNowAllowingStateLoss();
        @SuppressLint("CommitTransaction")
        FragmentTransaction fts = getSupportFragmentManager().beginTransaction();
        fts.attach(ratee).commitNowAllowingStateLoss();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("my-event"));

 /*       //Todo: there is still bug with this dialog when network is not connected
        if ((DataUtils.isCurrencyFirstRun(this)) && (checkConnection())) {
            new firstRunFetch(this).execute();
        }*/
    }


    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }
}
