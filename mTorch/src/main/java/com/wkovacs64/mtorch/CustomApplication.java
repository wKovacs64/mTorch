package com.wkovacs64.mtorch;

import android.app.Application;

import timber.log.Timber;

public class CustomApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            // Initialize Timber logging library
            Timber.plant(new Timber.DebugTree());
        }
    }
}
