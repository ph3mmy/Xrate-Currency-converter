package com.jcedar.xratecurrencyconverter.app;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.evernote.android.job.JobManager;

/**
 * Created by Afolayan on 23/1/2016.
 */
public class CurrencyApp extends MultiDexApplication{


    public CurrencyApp() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        JobManager.create(this).addJobCreator(new CurrencyJobCreator());
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }


}
