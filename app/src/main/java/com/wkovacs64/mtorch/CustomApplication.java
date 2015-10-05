package com.wkovacs64.mtorch;

import android.app.Application;
import android.os.Build;
import android.os.StrictMode;

import timber.log.Timber;

public final class CustomApplication extends Application {

    @Override
    public void onCreate() {
        if (BuildConfig.DEBUG) {
            // Initialize Timber logging library
            Timber.plant(new Timber.DebugTree());

            // Enable StrictMode for debug builds
            enabledStrictMode();
        }

        super.onCreate();
    }

    private void enabledStrictMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDialog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
        }
    }
}
