package com.jcedar.xratecurrencyconverter.helper;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.util.Log;

import com.jcedar.xratecurrencyconverter.provider.CurrencyContract;

import java.util.ArrayList;

/**
 * Created by OLUWAPHEMMY on 1/17/2017.
 */

public class MyCustomListPreference extends ListPreference {

    Context mContext;

    public MyCustomListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setEntries(entries());
        setEntryValues(entries());
        setValueIndex(initializeIndex());
    }
    public MyCustomListPreference(Context context) {
        this(context, null);
        this.mContext = context;
    }

    private CharSequence[] entries() {
        //action to provide entry data in char sequence array for list
         String[]myEntries = new String[dbSymbol().size()];
        if (dbSymbol().size() != 0) {
            for (int i = 0; i < dbSymbol().size(); i++){
                myEntries[i] = dbSymbol().get(i);
            }
        }
        return myEntries;
    }

    private CharSequence[] entryValues() {
        //action to provide value data for list
        String myEntryValues[] = {"ten", "twenty", "thirty", "forty", "fifty"};

        return myEntryValues;
    }

    private int initializeIndex() {
        //here you can provide the value to set (typically retrieved from the SharedPreferences)        //...
        int i = 2;
        return i;
    }

    private ArrayList<String> dbSymbol () {
        ArrayList<String> symbolList = new ArrayList<>();

        Uri uri = CurrencyContract.Currency.CONTENT_URI;
        Cursor c = getContext().getContentResolver().query(uri, new String[]{CurrencyContract.Currency.CURRENCY_SYMBOL}, null, null, null);
        String cc;

        assert c != null;
        while (c.moveToNext()) {
            cc = c.getString(c.getColumnIndex(CurrencyContract.Currency.CURRENCY_SYMBOL));
            symbolList.add(cc);
        } c.close();

        return symbolList;
    }

}