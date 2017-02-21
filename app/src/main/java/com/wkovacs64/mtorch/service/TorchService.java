package com.wkovacs64.mtorch.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
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
import com.wkovacs64.mtorch.bus.ToggleRequestEvent;
import com.wkovacs64.mtorch.bus.TorchStateEvent;
import com.wkovacs64.mtorch.core.CameraTorch;
import com.wkovacs64.mtorch.core.Torch;
import com.wkovacs64.mtorch.ui.activity.MainActivity;
import timber.log.Timber;

public final class TorchService extends Service {

  private static final int ONGOING_NOTIFICATION_ID = 1;

  private final Bus bus = BusProvider.getBus();
  private Torch torch;
  private boolean persist;
  private boolean foreground;

  public TorchService() {
  }

  @Override public IBinder onBind(Intent intent) {
    // This is a "Started" service (not a "Bound" service)
    return null;
  }

  @Override public void onCreate() {
    Timber.d("########## onCreate ##########");
    super.onCreate();

        /*
        // Choose the appropriate Torch implementation based on OS version
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Timber.d("DEBUG: Pre-M Android OS detected, using deprecated CameraTorch");
            torch = new CameraTorch();
        } else {
            Timber.d("DEBUG: Android M or later detected, using Camera2Torch");
            torch = new Camera2Torch(getApplicationContext());
        }
        */

    // TODO: replace with OS version-specific Torch implementation
    // Use the default Torch implementation
    Timber.d("DEBUG: Instantiating deprecated CameraTorch");
    torch = new CameraTorch();

    // Initialize the torch
    try {
      torch.init();
    } catch (IllegalStateException e) {
      Timber.e(e, "Unable to initialize torch.");
      // TODO: better error handling, possibly
      die(getString(R.string.error_camera_unavailable));
    }

    // Register with the event bus
    Timber.d("Registering with the event bus.");
    bus.register(this);

    // Notify subscribers of initial torch state
    updateUi(torch.isOn());
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    Timber.d("########## onStartCommand ##########");

    // Check for Auto On
    if (intent.hasExtra(Constants.SETTINGS_KEY_AUTO_ON)) {
      persist = intent.getBooleanExtra(Constants.SETTINGS_KEY_PERSISTENCE, false);
      toggleTorch(true, persist);

      // Update the UI
      updateUi(torch.isOn());
    }

    return Service.START_NOT_STICKY;
  }

  @Override public void onDestroy() {
    Timber.d("########## onDestroy ##########");
    super.onDestroy();

    // If this service was told to stop for some reason and persistence was enabled,
    // stop running in foreground mode
    if (persist) exitForeground();

    // Shut the torch off if it was on when we got shut down
    if (torch != null && torch.isOn()) {
      Timber.w("Torch is still on, shutting it off...");
      try {
        // Turn off the torch
        torch.toggle(false);

        // Update the UI
        updateUi(false);
      } catch (IllegalStateException e) {
        Timber.e(e, "Failed to toggle torch off during service destruction!");
      }
    }

    // Release the camera
    if (torch != null) {
      torch.tearDown();
      torch = null;
    }

    // Unregister from the event bus
    Timber.d("Unregistering from the event bus.");
    bus.unregister(this);
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
    Notification notification =
        new NotificationCompat.Builder(this).setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.notification_text))
            .setContentIntent(pIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSmallIcon(R.drawable.ic_stat_notify)
            .build();

    // Enter foreground mode to keep the service running
    foreground = true;
    startForeground(ONGOING_NOTIFICATION_ID, notification);
  }

  /**
   * Leaves foreground mode via {@link Service#stopForeground}.
   */
  private void exitForeground() {
    foreground = false;
    stopForeground(true);
  }

  /**
   * Toggles the persistence feature.
   *
   * @param persist true to enable persistence, false to disable
   */
  private void togglePersistence(boolean persist) {
    // Track the current state of persistence
    this.persist = persist;

    // If the user enables persistence while the torch is already lit, goForeground
    // If the user disables persistence while the torch is already lit, stopForeground
    if (torch.isOn()) {
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
   * @param persistence the current state of the persistence feature (true for on, false for
   * off)
   */
  private void toggleTorch(boolean requestedState, boolean persistence) {
    // Try to toggle the torch to the requested state
    try {
      torch.toggle(requestedState);
    } catch (IllegalStateException e) {
      Timber.e(e, "Failed to turn on the torch!");
      die(getString(R.string.error_flash_unavailable));
    }

    // If turning torch off while in foreground mode (persistence enabled), exit foreground
    if (foreground && !requestedState) {
      Timber.d("Disabling torch while in foreground mode. Exiting foreground mode.");
      exitForeground();
    }

    // Toggle persistence if applicable
    togglePersistence(persistence);
  }

  /**
   * Posts a TorchStateEvent to the bus to update the UI.
   *
   * @param state the current state of the torch (true for on, false for off)
   */
  private void updateUi(boolean state) {
    Timber.d("Posting a new TorchStateEvent to the bus.");
    bus.post(new TorchStateEvent(state));
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
    bus.post(new ShutdownEvent(error));

    // Stop the service
    stopSelf();
  }

  /**
   * Subscribes to receive ToggleRequestEvent notifications from the bus.
   *
   * @param event the ToggleRequestEvent
   */
  @Subscribe public void onToggleRequestEvent(ToggleRequestEvent event) {
    Timber.d("ToggleRequestEvent detected on the bus.");

    if (foreground && event.isProduced()) {
      // Disregard the initial launch toggle request if already in the foreground
      Timber.d("ToggleRequestEvent was produced while service in foreground. Disregarding.");
    } else {
      // Toggle the torch
      toggleTorch(event.getRequestedState(), event.getPersistence());
    }

    // Update the UI
    updateUi(torch.isOn());
  }

  /**
   * Subscribes to receive PersistenceChangeEvent notifications from the bus.
   *
   * @param event the PersistenceChangeEvent
   */
  @Subscribe public void onPersistenceChangeEvent(PersistenceChangeEvent event) {
    Timber.d("PersistenceChangeEvent detected on the bus.");

    // Toggle the persistence feature accordingly
    togglePersistence(event.getState());
  }

  /**
   * Subscribes to receive StateRequestEvent notifications from the bus.
   *
   * @param event the StateRequestEvent
   */
  @Subscribe public void onStateRequestEvent(StateRequestEvent event) {
    Timber.d("StateRequestEvent detected on the bus.");
    updateUi(torch.isOn());
  }
}
