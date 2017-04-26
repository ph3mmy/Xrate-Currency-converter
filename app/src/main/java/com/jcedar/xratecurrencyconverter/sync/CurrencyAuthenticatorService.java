package com.jcedar.xratecurrencyconverter.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by OLUWAPHEMMY on 11/11/2016.
 */
public class CurrencyAuthenticatorService extends Service {

    private CurrencyAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        super.onCreate();
        mAuthenticator = new CurrencyAuthenticator(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
