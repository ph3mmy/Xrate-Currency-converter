package com.jcedar.xratecurrencyconverter.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by OLUWAPHEMMY on 11/11/2016.
 */
public class CurrencySyncAdapterService extends Service {

    private static final Object sSyncAdapterLock = new Object();
    private static CurrencySyncAdapter mCurrencySyncAdapter = null;

    @Override
    public void onCreate() {
        super.onCreate();
        synchronized (sSyncAdapterLock){
            if (mCurrencySyncAdapter == null) {
                mCurrencySyncAdapter = new CurrencySyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mCurrencySyncAdapter.getSyncAdapterBinder();
    }
}
