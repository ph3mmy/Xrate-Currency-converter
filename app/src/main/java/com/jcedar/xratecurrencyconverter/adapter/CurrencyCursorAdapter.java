package com.jcedar.xratecurrencyconverter.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.v4.widget.CursorAdapter;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jcedar.xratecurrencyconverter.R;
import com.jcedar.xratecurrencyconverter.provider.CurrencyContract;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by OLUWAPHEMMY on 11/2/2016.
 */
public class CurrencyCursorAdapter extends CursorAdapter {

    private int mLayoutRes;
    Context mContext;
    private String userId;
    Map countryCodes;
    private static final String TAG = CurrencyCursorAdapter.class.getName();

    public CurrencyCursorAdapter(Context context, Cursor c, int layoutRes) {
        super(context, c, layoutRes);
        this.mLayoutRes = layoutRes;
        this.mContext = context;
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(mLayoutRes, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        Typeface tf = Typeface.createFromAsset(mContext.getAssets(), "fonts/proxima-nova-semibold.ttf");
        Typeface tfSym= Typeface.createFromAsset(mContext.getAssets(), "fonts/proxima-nova-cond-semibold.ttf");

        ImageView images = (ImageView) view.findViewById(R.id.iv_rate_country);
        TextView tvSymbol = (TextView) view.findViewById(R.id.tv_rate_currency_symbol);
        tvSymbol.setTypeface(tfSym);
        TextView tvCurrencyName = (TextView) view.findViewById(R.id.tv_rate_currency_name);
        tvCurrencyName.setTypeface(tf);
        TextView tvBaseRate = (TextView) view.findViewById(R.id.tv_base_rate);
        TextView tvInvRate = (TextView) view.findViewById(R.id.tv_inv_rate);
//        TextView timeTextView = (TextView) view.findViewById(R.id.tv_time_stamp);

        userId = cursor.getString(
                cursor.getColumnIndex(CurrencyContract.Currency._ID));

        final String country = cursor.getString(cursor.getColumnIndex(CurrencyContract.Currency.CURRENCY_CODE));
        String symbol = cursor.getString(cursor.getColumnIndex(CurrencyContract.Currency.CURRENCY_SYMBOL));

        String name = cursor.getString(cursor.getColumnIndex(CurrencyContract.Currency.BASE_CURRENCY_SYMBOL));
        String base_rate = cursor.getString(cursor.getColumnIndex(CurrencyContract.Currency.BASE_RATE));
        String inv_rate = cursor.getString(cursor.getColumnIndex(CurrencyContract.Currency.INVERTED_RATE));
        String timeBase = cursor.getString(cursor.getColumnIndex(CurrencyContract.Currency.TIME_STAMP));

        String codeName = getCountryCode(symbol);

        tvSymbol.setText(symbol.toUpperCase());
        tvCurrencyName.setText(codeName);
        tvBaseRate.setText(formatBaseRate(symbol, base_rate));
        tvInvRate.setText(formatInvRate(symbol, inv_rate));

        loadCountryImage(images, country.toLowerCase(), symbol);
    }

    private void loadCountryImage (ImageView imageView, String countryCode, String symbol){
        Context context = imageView.getContext();
        int id;
        if (countryCode.equalsIgnoreCase("do")){
            String mUri = "@drawable/do1";
            id = context.getResources().getIdentifier(mUri, "raw", context.getPackageName());
        } else  if (symbol.equalsIgnoreCase("xaf")){
            String mUri = "@drawable/xaf";
            id = context.getResources().getIdentifier(mUri, "raw", context.getPackageName());
        } else  if (symbol.equalsIgnoreCase("xag")){
            String mUri = "@drawable/xag";
            id = context.getResources().getIdentifier(mUri, "raw", context.getPackageName());
        } else if (symbol.equalsIgnoreCase("xau")){
            String mUri = "@drawable/xau";
            id = context.getResources().getIdentifier(mUri, "raw", context.getPackageName());
        } else {
            String uri = "@drawable/" + countryCode;
            id = context.getResources().getIdentifier(uri, "raw", context.getPackageName());
        }
        if (id != 0){
        Drawable res =context.getResources().getDrawable(id);
            imageView.setImageDrawable(res);
        }
        else
            imageView.setImageResource(R.mipmap.ic_launcher);
    }


    public String getCountryCode(String code){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        return sp.getString(code, "");
    }

    public Map getCountryCodes(){
        HashMap<String, String> codes = new HashMap<>();
        Cursor cursor = mContext.getContentResolver()
                .query(CurrencyContract.CurrencyName.CONTENT_URI, null, null, null, null);
        while ( cursor.moveToFirst() ){
            String code = cursor.getString( cursor.getColumnIndex(CurrencyContract.CurrencyName.CURRENCY_SYMBOL));
            String name = cursor.getString( cursor.getColumnIndex(CurrencyContract.CurrencyName.CURRENCY_NAME));

            codes.put(code, name);
            cursor.moveToNext();
        }
        cursor.close();

        return codes;
    }

    public  void encodeTobase64(Bitmap image, String cCode) {

        Log.e(TAG, "inside image encoder");
        Bitmap immage = image;
//        int i = 0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immage.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        byte[] b = baos.toByteArray();
        String encImage = Base64.encodeToString(b, Base64.DEFAULT);
        Log.e(TAG, "encoded image at " + cCode + " is " + encImage);

        updateDbImage(encImage, cCode);

    }

    private  void updateDbImage(String encodedImage, String currencyCode){

//        ArrayList<ContentValues> mValue = new ArrayList<ContentValues>();
        //first check if currency with this name already exist
        Cursor cursor = mContext.getContentResolver().query(
                CurrencyContract.Currency.CONTENT_URI,
                new String[]{CurrencyContract.Currency._ID},
                CurrencyContract.Currency.CURRENCY_CODE + "=?",
                new String[]{encodedImage},
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {

        } else {

            ContentValues nameVal = new ContentValues();
            nameVal.put(CurrencyContract.Currency.TIME_STAMP, encodedImage);

            mContext.getContentResolver().update(CurrencyContract.Currency.CONTENT_URI, nameVal, CurrencyContract.Currency.CURRENCY_CODE +
            "=?", new String[]{currencyCode});
//            mContext.getContentResolver().insert(CurrencyContract.CurrencyName.CONTENT_URI, nameVal);
        }

        if (cursor != null){
            cursor.close();
        }
    }

    // method for base64 to bitmap
    public static Bitmap decodeBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory
                .decodeByteArray(decodedByte, 0, decodedByte.length);
    }

private String formatInvRate(String currencySym, String inverseRate) {
    return ("1 " +currencySym + " = " + inverseRate + " USD");
}

    private String formatBaseRate(String currencySym, String baseRate) {
        return ("1 USD = " + baseRate + " " +currencySym);
    }
}

