package com.jcedar.xratecurrencyconverter.io;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.jcedar.xratecurrencyconverter.adapter.RateListAdapter;
import com.jcedar.xratecurrencyconverter.fragment.RateFragment;
import com.jcedar.xratecurrencyconverter.helper.AppSingleton;
import com.jcedar.xratecurrencyconverter.model.CurrencyModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by OLUWAPHEMMY on 10/19/2016.
 */
public class JsonVolleyHandler {

    public static String[] currencySymbol;
    public static String[] currencyRate;
    public static String[] currencyCode;
    public static String[] currencyName;
    public static Double[] inverseValue;
    public static String[] flag;

   public static List <CurrencyModel> currencyList = new ArrayList<>();

    public static final String KEY_CURRENCY_SYMBOL = "quotes";
    public static final String KEY_CURRENCY_RATE = "rate";
    public static final String KEY_CURRENCY_CODE = "code";
    public static final String KEY_CURRENCY_NAME = "name";
    public static final String KEY_FLAG_URL= "http://caches.space/flags/";


    final static String TAG = JsonVolleyHandler.class.getName();

    public static void showJsonResult(String url, final Context context){

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


/*        JsonArrayRequest request = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {
//                Toast.makeText(context, "Successssssssss : " + "\n" + jsonArray, Toast.LENGTH_LONG).show();
                *//*ProgressDialog progressDialog = new ProgressDialog(context);
                progressDialog.setMessage("Fetching data");
                progressDialog.setTitle("please wait");
                progressDialog.show();*//*
                getCurrencySymbol(jsonArray);
//                progressDialog.hide();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(context, "An error occured : " + volleyError, Toast.LENGTH_LONG).show();
            }
        });*/
        //add request to queue
        AppSingleton.getInstance(context).addToRequestQueue(req, TAG);

    }

    public  void currencyNamesResult(String url, final Context context) {

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

    public void getCurrencyName(JSONObject currency){


        try {
            JSONObject curr = currency.getJSONObject("currencies");
            currencyName = new String[curr.length()];
            for (int i = 0; i < curr.length(); i++){
                JSONArray nameArray = curr.names();
                String rawCurrency = nameArray.get(i).toString();

                currencyName[i] = curr.getString(rawCurrency);

//                CurrencyModel cModel = new CurrencyModel();

                /*cModel.setCurrencyName(currencyName[i]);
                currencyList.add(cModel);*/
            }

        }catch (Exception e){
            Log.e(TAG, "getCurrency error " + e);
        }
    }

    public static void getCurrencySymbol(JSONObject symbol){

        /*
         currencyName = new String[symbol.length()];*/

          final RateListAdapter mAdapter = RateFragment.adapter;


        try {

            JSONObject quo = symbol.getJSONObject(KEY_CURRENCY_SYMBOL);
            currencySymbol = new String[quo.length()];
            currencyRate = new String[quo.length()];
            currencyCode = new String[quo.length()];
            inverseValue = new Double[quo.length()];

            for (int i = 0; i < quo.length(); i++) {
//                JSONObject mObject = symbol.getJSONObject(i);

                JSONArray mObject = quo.names();
                String rawSymbol = mObject.getString(i);
                currencySymbol[i] = rawSymbol.substring(rawSymbol.length() - 3);
                currencyRate[i] = quo.getString(rawSymbol);
                currencyCode[i] = rawSymbol.substring(3, 5);
                inverseValue[i] = invertedRate(currencyRate[i]);

                /*currencySymbol[i] = mObject.getString(KEY_CURRENCY_SYMBOL);
                currencyRate[i] = mObject.getString(KEY_CURRENCY_RATE);
                currencyCode[i] = mObject.getString(KEY_CURRENCY_CODE);
                currencyName[i] = mObject.getString(KEY_CURRENCY_NAME);
                inverseValue[i] = invertedRate(mObject.getString(KEY_CURRENCY_RATE));*/
//                flag[i] = KEY_FLAG_URL+currencyCode[i]+".png";


             /*   Log.e(TAG, "NAME Trial " + mObject.names());
                Log.e(TAG, "KEY Trial " + mObject.keys());*/



                final CurrencyModel cModel = new CurrencyModel();
                cModel.setSymbol(currencySymbol[i]);
                cModel.setCurrencyName(currencyCode[i]);
                cModel.setBaseRate(currencyRate[i]);
//                cModel.setCurrencyName(currencyName[i]);
                cModel.setInvRate(inverseValue[i]);
                currencyList.add(cModel);

                /*Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                        currencyList.add(cModel);
                    }
                }, 3000);*/

//                Log.e(TAG, "Printed Symbols code : " + currencyList);

                String trial = "JSONSTUB";
//                String lastThree = trial.substring(trial.length()-3);
                String firstThree = trial.substring(0,3);
                String lastThree = trial.substring(2,4);/*
                Log.e(TAG, "Printed Symbols code : " + currencyCode);
                Log.e(TAG, "Printed Symbols symbol : " + currencySymbol);*/
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


      /*  for (String s : currencySymbol){
            Log.e(TAG, "Printed Symbols S : " + s);
        } */
    }



    public void loadImageVolley(String url, Context context){
        ImageLoader imageLoader = AppSingleton.getInstance(context).getImageLoader();
        imageLoader.get(url, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {

            }

            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });
    }
    public static double invertedRate (String base){
//        int inv = Integer.parseInt(base);
        double db = Double.parseDouble(base);
        double inv = (1/db);
        DecimalFormat threeDp = new DecimalFormat("#.0000");

        return Double.parseDouble(threeDp.format(inv));
    }

}
