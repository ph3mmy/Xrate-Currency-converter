package com.jcedar.xratecurrencyconverter.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

import java.util.List;

/**
 * Created by OLUWAPHEMMY on 10/30/2016.
 */
public class CurrencyContract {

    //authority of data provider
    public static final String CONTENT_AUTHORITY = "com.jcedar.xratecurrencyconverter.provider";

    //authority of base content uri
    public static final Uri BASE_CONTENT_URI =  Uri.parse("content://" + CONTENT_AUTHORITY);

    //path or table names
    public static final String PATH_CURRENCY = "currency";
    public static final String PATH_CURRENCY_NAME = "currency_name";
    private static final String CALLER_IS_SYNCADAPTER = "caller_is_sync_adapter";
//    public static final String PATH_SEARCH = "search";


    public static class Currency implements CurrencyColumns, BaseColumns {
        /** Content URI for  students table */
        public static final Uri CONTENT_URI  =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CURRENCY).build();

        /** The mime type of a single item */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE
                        + "/vnd.com.jcedar.xratecurrencyconverter.provider.currency";

        /** The mime type of a single item */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE
                        + "/vnd.com.jcedar.xratecurrencyconverter.provider.currency";

        public static Uri buildCurrencyUri(long currencyId){
            return CONTENT_URI.buildUpon().appendPath(Long.toString(currencyId)).build();
        }


        /** A projection of all tables in students table */
        public static final String[] PROJECTION_ALL = {
                _ID,  CURRENCY_SYMBOL, CURRENCY_CODE, BASE_CURRENCY_SYMBOL, BASE_RATE, INVERTED_RATE, TIME_STAMP
                //, UPDATED

        };

        /** The default sort order for queries containing students */
        public static final String SORT_ORDER_DEFAULT = CURRENCY_SYMBOL +" ASC";

    }

    public static class CurrencyName implements CurrencyNamesColumns, BaseColumns {
        /** Content URI for  studentsChapter table */
        public static final Uri CONTENT_URI  =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CURRENCY_NAME).build();

        /** The mime type of a single item */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE
                        + "/vnd.com.jcedar.xratecurrencyconverter.provider.currency_name";

        /** The mime type of a single item */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE
                        + "/vnd.com.jcedar.xratecurrencyconverter.provider.currency_name";

        public static Uri buildCurrencyNameUri(long currencyNameId){
            return CONTENT_URI.buildUpon().appendPath(Long.toString(currencyNameId)).build();
        }


        /** A projection of all tables in studentsChapter table */
        public static final String[] PROJECTION_ALL = {
                _ID, CURRENCY_NAME, CURRENCY_SYMBOL //, UPDATED
        };

        /** The default sort order for queries containing students_chapter */
        public static final String SORT_ORDER_DEFAULT = CURRENCY_SYMBOL +" ASC";
    }



    public interface SyncColumns{
        String UPDATED = "updated";
    }

    interface CurrencyColumns{
        String CURRENCY_SYMBOL = "currency_symbol";
        String BASE_CURRENCY_SYMBOL = "base_currency_symbol";
        String CURRENCY_CODE = "currency_code";
        String BASE_RATE = "base_rate";
        String INVERTED_RATE = "inverted_rate";
        String TIME_STAMP = "time_stamp";
    }

    interface CurrencyNamesColumns {
        String CURRENCY_SYMBOL = "currency_symbol";
        String CURRENCY_NAME = "currency_name";
    }

    public static Uri addCallerIsSyncAdapterParameter(Uri uri) {
        return uri.buildUpon().appendQueryParameter(
                CurrencyContract.CALLER_IS_SYNCADAPTER, "true").build();
    }

    public static boolean hasCallerIsSyncAdapterParameter(Uri uri) {
        return TextUtils.equals("true",
                uri.getQueryParameter(CurrencyContract.CALLER_IS_SYNCADAPTER));
    }

}
