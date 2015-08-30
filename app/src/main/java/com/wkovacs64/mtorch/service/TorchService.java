package com.wkovacs64.mtorch.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.wkovacs64.mtorch.Constants;
import com.wkovacs64.mtorch.R;
import com.wkovacs64.mtorch.bus.BusProvider;
import com.wkovacs64.mtorch.bus.PersistenceChangeEvent;
import com.wkovacs64.mtorch.bus.ShutdownEvent;
import com.wkovacs64.mtorch.bus.StateRequestEvent;
import com.wkovacs64.mtorch.bus.StateResponseEvent;
import com.wkovacs64.mtorch.bus.ToggleRequestEvent;
import com.wkovacs64.mtorch.bus.ToggleResponseEvent;
import com.wkovacs64.mtorch.core.Camera2Torch;
import com.wkovacs64.mtorch.core.CameraTorch;
import com.wkovacs64.mtorch.core.Torch;
import com.wkovacs64.mtorch.ui.activity.MainActivity;

import timber.log.Timber;

public final class TorchService extends Service {

    private static final int ONGOING_NOTIFICATION_ID = 1;

    private final Bus mBus = BusProvider.getBus();

    private Torch mTorch;

    private boolean mPersist;
    private boolean mForeground;

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
            Timber.d("DEBUG: Pre-M Android OS detected, using deprecated CameraTorch");
            mTorch = new CameraTorch();
        } else {
            Timber.d("DEBUG: Android M or later detected, using Camera2Torch");
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

        // Register with the event bus
        Timber.d("Registering with the event bus.");
        mBus.register(this);

        // Notify subscribers of initial torch state
        Timber.d("Notifying subscribers of initial torch state.");
        mBus.post(new StateResponseEvent(mTorch.isOn()));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.d("########## onStartCommand ##########");

        // Check for Auto On
        if (intent.hasExtra(Constants.SETTINGS_KEY_AUTO_ON)) {
            mPersist = intent.getBooleanExtra(Constants.SETTINGS_KEY_PERSISTENCE, false);
            toggleTorch(true, mPersist);

            // Post a ToggleResponseEvent to the bus to update the UI
            Timber.d("Posting a new ToggleResponseEvent to the bus.");
            mBus.post(new ToggleResponseEvent(mTorch.isOn()));
        }

        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Timber.d("########## onDestroy ##########");
        super.onDestroy();

        // If this service was told to stop for some reason and persistence was enabled,
        // stop running in foreground mode
        if (mPersist) exitForeground();

        // Shut the torch off if it was on when we got shut down
        if (mTorch != null && mTorch.isOn()) {
            Timber.w("Torch is still on, shutting it off...");
            try {
                mTorch.toggle(false);

                // Post a ToggleResponseEvent to the bus to update the UI
                Timber.d("Posting a new ToggleResponseEvent to the bus.");
                mBus.post(new ToggleResponseEvent(false));
            } catch (IllegalStateException e) {
                Timber.e("Failed to toggle torch off during service destruction!", e);
            }
        }

        // Release the camera
        if (mTorch != null) {
            mTorch.tearDown();
            mTorch = null;
        }

        // Unregister from the event bus
        Timber.d("Unregistering from the event bus.");
        mBus.unregister(this);
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
        mForeground = true;
        startForeground(ONGOING_NOTIFICATION_ID, notification);
    }

    /**
     * Leaves foreground mode via {@link Service#stopForeground}.
     */
    private void exitForeground() {
        mForeground = false;
        stopForeground(true);
    }

    /**
     * Toggles the persistence feature.
     *
     * @param persist true to enable persistence, false to disable
     */
    private void togglePersistence(boolean persist) {
        // Track the current state of persistence
        mPersist = persist;

        // If the user enables persistence while the torch is already lit, goForeground
        // If the user disables persistence while the torch is already lit, stopForeground
        if (mTorch.isOn()) {
            if (persist) {
                goForeground();
            } else {
                exitForeground();
            }
        }
    }

    /**
     * Toggles the torch state.
     *
     * @param requestedState the requested state of the torch (true for on, false for off)
     * @param persistence    the current state of the persistence feature (true for on, false for
     *                       off)
     */
    private void toggleTorch(boolean requestedState, boolean persistence) {
        // Try to toggle the torch to the requested state
        try {
            mTorch.toggle(requestedState);
        } catch (IllegalStateException e) {
            Timber.e("Failed to turn on the torch!", e);
            die(getString(R.string.error_flash_unavailable));
        }

        // If turning torch off while in foreground mode (persistence enabled), exit foreground
        if (mForeground && !requestedState) {
            Timber.d("Disabling torch while in foreground mode. Exiting foreground mode.");
            exitForeground();
        }

        // Toggle persistence if applicable
        togglePersistence(persistence);
    }

    /**
     * Stops the service and posts a new {@link ShutdownEvent} to the bus.
     *
     * @param error the error message to include in the shutdown event
     */
    private void die(@NonNull String error) {
        Timber.e(error);

        // Post a new ShutdownEvent to the bus with the included error message
        Timber.d("Posting a new ShutdownEvent to the bus.");
        mBus.post(new ShutdownEvent(error));

        // Stop the service
        stopSelf();
    }

    /**
     * Subscribes to receive ToggleRequestEvent notifications from the bus.
     *
     * @param event the ToggleRequestEvent
     */
    @Subscribe
    public void onToggleRequestEvent(ToggleRequestEvent event) {
        Timber.d("ToggleRequestEvent detected on the bus.");

        if (mForeground && event.isProduced()) {
            // Disregard the initial launch toggle request if already in the foreground
            Timber.d("ToggleRequestEvent was produced while service in foreground. Disregarding.");
        } else {
            // Toggle the torch
            toggleTorch(event.getRequestedState(), event.getPersistence());
        }

        // Post a ToggleResponseEvent to the bus to update the UI
        Timber.d("Posting a new ToggleResponseEvent to the bus.");
        mBus.post(new ToggleResponseEvent(mTorch.isOn()));
    }

    /**
     * Subscribes to receive PersistenceChangeEvent notifications from the bus.
     *
     * @param event the PersistenceChangeEvent
     */
    @Subscribe
    public void onPersistenceChangeEvent(PersistenceChangeEvent event) {
        Timber.d("PersistenceChangeEvent detected on the bus.");

        // Toggle the persistence feature accordingly
        togglePersistence(event.getState());
    }

    /**
     * Subscribes to receive StateRequestEvent notifications from the bus.
     *
     * @param event the StateRequestEvent
     */
    @Subscribe
    public void onStateRequestEvent(StateRequestEvent event) {
        Timber.d("StateRequestEvent detected on the bus.");
        Timber.d("Notifying subscribers of current torch state.");
        mBus.post(new StateResponseEvent(mTorch.isOn()));
    }
}
