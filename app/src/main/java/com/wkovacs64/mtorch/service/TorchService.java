package com.wkovacs64.mtorch.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.wkovacs64.mtorch.Constants;
import com.wkovacs64.mtorch.R;
import com.wkovacs64.mtorch.core.Camera2Torch;
import com.wkovacs64.mtorch.core.CameraTorch;
import com.wkovacs64.mtorch.core.Torch;
import com.wkovacs64.mtorch.ui.activity.MainActivity;

import timber.log.Timber;

public final class TorchService extends Service {

    private static final int ONGOING_NOTIFICATION_ID = 1;

    private Torch mTorch;

    private boolean mPersist;

    public TorchService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // This is a "Started" service (not a "Bound" service)
        return null;
    }

    @Override
    public void onCreate() {
        Timber.d("########## onCreate ##########");
        super.onCreate();

        // Choose the appropriate Torch implementation based on OS version
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mTorch = new CameraTorch();
        } else {
            mTorch = new Camera2Torch(getApplicationContext());
        }

        // Initialize the torch
        try {
            mTorch.init();
        } catch (IllegalStateException e) {
            Timber.e("Unable to initialize torch.", e);
            // TODO: better error handling, possibly
            die(getString(R.string.error_camera_unavailable));
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.d("########## onStartCommand ##########");

        // Check for persistence user setting
        if (intent.hasExtra(Constants.SETTINGS_KEY_PERSISTENCE)) {
            mPersist = intent.getBooleanExtra(Constants.SETTINGS_KEY_PERSISTENCE, false);
            Timber.d("DEBUG: mPersist = " + mPersist);

            // If the user enables persistence while the torch is already lit, goForeground
            // If the user disables persistence while the torch is already lit, stopForeground
            if (mTorch.isOn()) {
                if (mPersist) {
                    goForeground();
                } else {
                    stopForeground(true);
                }
            }
        }

        if (intent.hasExtra(Constants.EXTRA_START_TORCH)) {
            Timber.d("DEBUG: startTorch | mTorch.isOn() was " + mTorch.isOn()
                    + " when image was pressed");

            // Let's light this candle!
            mTorch.toggle(true);

            // Check for persistence user setting, enter foreground mode if present
            if (mPersist) goForeground();
        } else if (intent.hasExtra(Constants.EXTRA_STOP_TORCH)) {
            Timber.d("DEBUG: stopTorch | mTorch.isOn() was " + mTorch.isOn()
                    + " when image was pressed");

            // Snuff out the torch
            mTorch.toggle(false);

            // Check for persistence user setting, exit foreground mode if present
            if (mPersist) stopForeground(true);
        }

        // Tell MainActivity to refresh the UI
        Timber.d("DEBUG: broadcasting toggleIntent...");
        Intent toggleIntent = new Intent(Constants.INTENT_COMMAND);
        toggleIntent.putExtra(Constants.EXTRA_UPDATE_UI, mTorch.isOn());
        sendBroadcast(toggleIntent);

        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Timber.d("########## onDestroy ##########");
        super.onDestroy();

        // If this service was told to stop for some reason and persistence was enabled,
        // stop running in foreground mode
        if (mPersist) stopForeground(true);

        // Shut the torch off if it was on when we got shut down
        if (mTorch != null && mTorch.isOn()) {
            Timber.w("WARN: torch still on, shutting it off...");
            mTorch.toggle(false);
        }

        // Release the camera
        if (mTorch != null) {
            mTorch.tearDown();
            mTorch = null;
        }
    }

    /**
     * Creates a {@link Notification} and makes this service run in the foreground via {@link
     * Service#startForeground}.
     */
    private void goForeground() {
        Timber.d("########## goForeground ##########");

        // Create a notification with pending intent to return to the app
        Intent launchActivity = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, launchActivity, 0);
        Notification notification = new NotificationCompat.Builder(this)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notification_text))
                .setContentIntent(pIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSmallIcon(R.drawable.ic_stat_notify).build();

        // Enter foreground mode to keep the service running
        startForeground(ONGOING_NOTIFICATION_ID, notification);
    }

    /**
     * Stops the service and broadcasts a "death threat" intent to {@link MainActivity}.
     *
     * @param errMsg the error message to include in the death threat
     */
    private void die(String errMsg) {
        Timber.e(errMsg);

        // Send intent back to MainActivity to finish()
        Intent deathThreat = new Intent(Constants.INTENT_COMMAND);
        deathThreat.putExtra(Constants.EXTRA_DEATH_THREAT, errMsg);
        sendBroadcast(deathThreat);

        // Stop the service
        stopSelf();
    }
}
