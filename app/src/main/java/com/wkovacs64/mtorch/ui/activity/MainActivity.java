package com.wkovacs64.mtorch.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;
import com.wkovacs64.mtorch.Constants;
import com.wkovacs64.mtorch.R;
import com.wkovacs64.mtorch.bus.BusProvider;
import com.wkovacs64.mtorch.bus.PersistenceChangeEvent;
import com.wkovacs64.mtorch.bus.ShutdownEvent;
import com.wkovacs64.mtorch.bus.StateRequestEvent;
import com.wkovacs64.mtorch.bus.ToggleRequestEvent;
import com.wkovacs64.mtorch.bus.TorchStateEvent;
import com.wkovacs64.mtorch.service.TorchService;
import com.wkovacs64.mtorch.ui.dialog.AboutDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static com.wkovacs64.mtorch.util.PermissionUtils.hasCameraPermissions;
import static com.wkovacs64.mtorch.util.PermissionUtils.requestCameraPermissions;

public final class MainActivity extends AppCompatActivity
    implements View.OnClickListener,
    SharedPreferences.OnSharedPreferenceChangeListener,
    ActivityCompat.OnRequestPermissionsResultCallback {

  private final Bus bus = BusProvider.getBus();
  private AboutDialog aboutDialog;
  private SharedPreferences prefs;
  private boolean autoOn;
  private boolean persist;
  private boolean torchEnabled;
  /**
   * Indicates whether Camera permissions were actively denied by the user upon being prompted.
   */
  private boolean cameraPermissionDenied;
  /**
   * Indicates whether Camera permissions were actively granted by the user upon being prompted.
   */
  private boolean cameraPermissionGranted;

  @BindView(R.id.container)
  LinearLayout rootView;
  @BindView(R.id.app_bar)
  Toolbar appBar;
  @BindView(R.id.torch_image_button)
  ImageButton imageButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    Timber.d("********** onCreate **********");

    // Check for flash capability
    if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
      Timber.e(getString(R.string.error_no_flash));
      Toast.makeText(this, R.string.error_no_flash, Toast.LENGTH_LONG).show();
      finish();
    }
    Timber.d("Flash capability detected!");

    // Start the service if we have the appropriate permissions, otherwise it will be started
    // after a manual toggle attempt (which prompts for permissions)
    if (hasCameraPermissions(this)) {
      // Start the service
      Intent torchService = new Intent(this, TorchService.class);
      startService(torchService);
    }

    // Set the content
    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Initialize Butter Knife bindings
    ButterKnife.bind(this);

    // Initialize the app bar
    if (appBar != null) {
      setSupportActionBar(appBar);
    }

    // Read preferences
    prefs = PreferenceManager.getDefaultSharedPreferences(this);
    autoOn = prefs.getBoolean(Constants.SETTINGS_KEY_AUTO_ON, false);
    persist = prefs.getBoolean(Constants.SETTINGS_KEY_PERSISTENCE, false);

    // Instantiate the About dialog box
    aboutDialog = AboutDialog.newInstance();
  }

  @Override
  protected void onStart() {
    super.onStart();
    Timber.d("********** onStart **********");

    // Listen for preference changes so we can react if necessary
    prefs.registerOnSharedPreferenceChangeListener(this);

    // Start the service
    if (hasCameraPermissions(this)) {
      Intent torchService = new Intent(this, TorchService.class);
      if (autoOn) torchService.putExtra(Constants.SETTINGS_KEY_AUTO_ON, true);
      torchService.putExtra(Constants.SETTINGS_KEY_PERSISTENCE, persist);
      startService(torchService);
    }
  }

  /*
   * This callback appears to occur prior to the UI being ready, so no UI code can exist here.
   */
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
    Timber.d("********** onRequestPermissionsResult **********");
    switch (requestCode) {
      case Constants.RESULT_PERMISSION_CAMERA:
        if (grantResults.length == 1
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          Timber.d("Permission granted: CAMERA");
          cameraPermissionDenied = false;
          cameraPermissionGranted = true;
        } else {
          Timber.d("Permission denied: CAMERA");
          cameraPermissionDenied = true;
          cameraPermissionGranted = false;
        }
        break;
      default:
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        break;
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    Timber.d("********** onResume **********");

    // Register with the event bus
    Timber.d("Registering with the event bus.");
    bus.register(this);

    // Show the appropriate feedback based on permission results now that the UI is available
    // (or do nothing if the user has not been prompted yet).
    processPermissionResults();

    // Request the current state of the torch, according to the service
    Timber.d("Requesting torch state.");
    bus.post(new StateRequestEvent());

    // Listen for toggle image clicks
    imageButton.setOnClickListener(this);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    // Close the About dialog if we're stopping anyway
    if (aboutDialog.isVisible()) aboutDialog.dismiss();

    super.onSaveInstanceState(outState);
  }

  @Override
  protected void onPause() {
    super.onPause();
    Timber.d("********** onPause **********");

    // Stop listening for toggle image clicks
    imageButton.setOnClickListener(null);

    // Unregister from the event bus
    Timber.d("Unregistering from the event bus.");
    bus.unregister(this);
  }

  @Override
  protected void onStop() {
    super.onStop();
    Timber.d("********** onStop **********");

    // Stop listening for preference changes
    prefs.unregisterOnSharedPreferenceChangeListener(this);

    // If no persistence or if the torch is off, stop the service
    if (!persist || !torchEnabled) {
      stopService(new Intent(this, TorchService.class));
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    switch (item.getItemId()) {
      case R.id.menu_about:
        // show About dialog
        aboutDialog.show(getFragmentManager(), AboutDialog.TAG);
        return true;
      case R.id.menu_settings:
        // show Settings
        startActivity(new Intent(this, SettingsActivity.class));
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onClick(View v) {
    Timber.d("********** onClick **********");

    // Check for the necessary permissions prior to toggling, request if missing
    if (hasCameraPermissions(this)) {
      onToggleClicked();
    } else {
      requestCameraPermissions(this, rootView);
    }
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
    Timber.d("SharedPreferences: %s has changed", key);

    // Settings have changed, observe the new value
    if (key.equals(Constants.SETTINGS_KEY_AUTO_ON)) {
      autoOn = prefs.getBoolean(key, false);
    } else if (key.equals(Constants.SETTINGS_KEY_PERSISTENCE)) {
      persist = prefs.getBoolean(key, false);

      // Notify the service of the setting change
      Timber.d("Posting a new PersistenceChangeEvent to the bus: %s", persist);
      bus.post(new PersistenceChangeEvent(persist));
    }
  }

  /**
   * Shows the appropriate dialog based on results from {@link
   * ActivityCompat.OnRequestPermissionsResultCallback#onRequestPermissionsResult}.
   */
  private void processPermissionResults() {
    if (cameraPermissionDenied
        && !ActivityCompat
        .shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
      Timber.d("Instructing user to grant permissions manually.");
      Snackbar.make(rootView, R.string.content_camera_permission_denied,
          Snackbar.LENGTH_INDEFINITE)
          .setActionTextColor(ContextCompat.getColor(this, R.color.accent))
          .setAction(R.string.action_settings, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
            }
          })
          .show();
    } else if (cameraPermissionGranted) {
      onToggleClicked();
    }

    // Reset
    cameraPermissionDenied = false;
    cameraPermissionGranted = false;
  }

  /**
   * Starts the service (in rare circumstances where it was killed) and toggles the torch.
   */
  private void onToggleClicked() {
    // Start the service (in case it was still on its way down when we fired up a new
    // instance of MainActivity). In normal circumstances where the service is already
    // running, this does nothing.
    startService(new Intent(this, TorchService.class));

    // Toggle the torch
    toggleTorch();
  }

  /**
   * Updates the toggle image to match the on/off state of the torch. Also toggles {@link
   * android.view.WindowManager.LayoutParams#FLAG_KEEP_SCREEN_ON} to keep the screen on while the
   * torch is lit (but not otherwise).
   */
  private void updateUi() {
    Timber.d("Updating UI, torchEnabled = %s", torchEnabled);

    // Set the corresponding toggle image
    imageButton.setImageResource(torchEnabled ? R.drawable.torch_on : R.drawable.torch_off);

    // Keep the screen on while the app is open and the torch is on
    if (torchEnabled) {
      getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    } else {
      getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
  }

  /**
   * Toggles the on/off state of the torch. Checks for the necessary permissions prior to
   * attempting the toggle, prompting for them if necessary.
   */
  private void toggleTorch() {
    // Use the service to start/stop the torch (start = on, stop = off)
    torchEnabled = !torchEnabled;
    Timber.d("Posting a new ToggleRequestEvent to the bus.");
    bus.post(new ToggleRequestEvent(torchEnabled, persist));
  }

  /**
   * Subscribes to receive ShutdownEvent notifications from the bus.
   *
   * @param event the ShutdownEvent
   */
  @Subscribe
  public void onShutdownEvent(ShutdownEvent event) {
    Timber.d("ShutdownEvent detected on the bus.");
    Toast.makeText(MainActivity.this, event.getError(), Toast.LENGTH_LONG).show();
    finish();
  }

  /**
   * Subscribes to receive TorchStateEvent notifications from the bus.
   *
   * @param event the TorchStateEvent
   */
  @Subscribe
  public void onTorchStateEvent(TorchStateEvent event) {
    Timber.d("TorchStateEvent detected on the bus.");
    torchEnabled = event.getState();
    updateUi();
  }

  /**
   * Posts a new ToggleRequestEvent to the bus when new subscribers are registered.
   *
   * @return a new ToggleRequestEvent constructed with the current values
   */
  @Produce
  public ToggleRequestEvent produceToggleRequestEvent() {
    Timber.d("Producing a new ToggleRequestEvent.");
    ToggleRequestEvent producedEvent = new ToggleRequestEvent(torchEnabled, persist);
    producedEvent.setProduced(true);
    return producedEvent;
  }
}
