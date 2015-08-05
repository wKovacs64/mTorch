package com.wkovacs64.mtorch;

import android.app.Application;
import android.os.Build;
import android.os.StrictMode;

import timber.log.Timber;

public class CustomApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            // Initialize Timber logging library
            Timber.plant(new Timber.DebugTree());

            // Enable StrictMode for debug builds
            enabledStrictMode();
        }
    }

    private void enabledStrictMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDialog()
                    .build());
        }
    }
}
