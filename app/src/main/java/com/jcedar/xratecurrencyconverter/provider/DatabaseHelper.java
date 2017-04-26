package com.jcedar.xratecurrencyconverter.provider;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by OLUWAPHEMMY on 10/30/2016.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = DatabaseHelper.class.getName();
    private static final String DATABASE_NAME = "currency.db";
    private static final int DATABASE_VERSION = 101;
    private Context mContext;


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(SQL_CREATE_CURRENCY_TABLE);
        db.execSQL(SQL_CREATE_CURRENCY_NAME_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Log.e(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + Tables.CURRENCY);
        onCreate(db);
    }

    final static String SQL_CREATE_CURRENCY_TABLE = "CREATE TABLE "
            + Tables.CURRENCY + "("
            + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + CurrencyContract.Currency.CURRENCY_SYMBOL + " VARCHAR NOT NULL, "
            + CurrencyContract.Currency.BASE_CURRENCY_SYMBOL + " VARCHAR NOT NULL, "
            + CurrencyContract.Currency.CURRENCY_CODE + " VARCHAR NOT NULL, "
            + CurrencyContract.Currency.BASE_RATE + " VARCHAR NOT NULL, "
            + CurrencyContract.Currency.INVERTED_RATE + " VARCHAR NOT NULL, "
            + CurrencyContract.Currency.TIME_STAMP + " VARCHAR NOT NULL "
            + ")";

    final static String SQL_CREATE_CURRENCY_NAME_TABLE = "CREATE TABLE "
            + Tables.CURRENCY_NAMES + "("
            + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + CurrencyContract.CurrencyName.CURRENCY_SYMBOL + " VARCHAR NOT NULL, "
            + CurrencyContract.CurrencyName.CURRENCY_NAME + " VARCHAR NOT NULL "
            + ")" ;


    public interface Tables {
        String CURRENCY = "currency";
        String CURRENCY_NAMES = "currency_names";
    }

    public static void deleteDatabase(Context context) {
        context.deleteDatabase(DATABASE_NAME);
    }


    public ArrayList<Cursor> getData(String Query){
        //get writable database
        SQLiteDatabase sqlDB = this.getWritableDatabase();
        String[] columns = new String[] { "mesage" };
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<>(2);
        MatrixCursor Cursor2= new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);


        try{
            String maxQuery = Query ;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);


            //add value to cursor2
            Cursor2.addRow(new Object[] { "Success" });

            alc.set(1,Cursor2);
            if (null != c && c.getCount() > 0) {


                alc.set(0,c);
                c.moveToFirst();

                return alc ;
            }
            return alc;
        } catch(SQLException sqlEx){
            Log.d("printing exception", sqlEx.getMessage());
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+sqlEx.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        } catch(Exception ex){

            Log.d("printing exception", ex.getMessage());

            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+ex.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        }


    }

}
