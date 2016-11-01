package com.jcedar.xratecurrencyconverter.io;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.RemoteException;

import com.jcedar.xratecurrencyconverter.provider.CurrencyContract;

import java.io.IOException;
import java.util.ArrayList;


/**
 * Created by user1 on 13/08/2015.
 */
public abstract class JSONHandler {

    protected static Context mContext;

    public JSONHandler(Context context){
        mContext = context;
    }

    public abstract ArrayList<ContentProviderOperation> parse(String json) throws IOException;


    public final void parseAndApply(String json) throws IOException {
        final ContentResolver resolver = mContext.getContentResolver();

        try {
            ArrayList<ContentProviderOperation> batch = parse(json);
            resolver.applyBatch(CurrencyContract.CONTENT_AUTHORITY, batch);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

}
