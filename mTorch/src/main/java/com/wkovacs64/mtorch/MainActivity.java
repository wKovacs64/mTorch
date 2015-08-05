package com.wkovacs64.mtorch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.Set;

import timber.log.Timber;

public class MainActivity extends BaseActivity implements View.OnClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String INTERNAL_INTENT = MainActivity.class.getPackage().getName() +
            "INTERNAL_INTENT";
    private static final String DEATH_THREAT = "die";
    private static final String SETTINGS_AUTO_ON_KEY = "auto_on";
    private static final String SETTINGS_PERSISTENCE_KEY = "persistence";

    private boolean mAutoOn;
    private boolean mPersist;
    private boolean mTorchEnabled;

    private AboutDialog mAboutDialog;
    private ImageButton mImageButton;
    private BroadcastReceiver mBroadcastReceiver;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("********** onCreate **********");
        setContentView(R.layout.activity_main);

        // Read preferences
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mAutoOn = prefs.getBoolean(SETTINGS_AUTO_ON_KEY, false);
        mPersist = prefs.getBoolean(SETTINGS_PERSISTENCE_KEY, false);

        // Assume flash off on launch (certainly true the first time)
        mTorchEnabled = false;

        // Set up the About dialog box
        mAboutDialog = new AboutDialog(this);

        // Check for flash capability
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            Timber.d(getString(R.string.error_no_flash));
            Toast.makeText(this, R.string.error_no_flash, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        Timber.d("DEBUG: flash capability detected!");

        // Set up the clickable toggle image
        mImageButton = (ImageButton) findViewById(R.id.torch_image_button);
        if (mImageButton == null) Timber.e("ERROR: mImageButton was NULL");
        else {
            mImageButton.setImageResource(R.drawable.torch_off);
            mImageButton.setOnClickListener(this);
            mImageButton.setEnabled(true);

            // Keep the screen on while the app is open
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        // Register to receive broadcasts from the service
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Timber.d("DEBUG: broadcast received...");
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    Set<String> ks = extras.keySet();

                    if (ks.contains(SETTINGS_AUTO_ON_KEY)) {
                        Timber.d("DEBUG: intent included Auto On extra, toggling torch...");
                        toggleTorch();
                    } else if (ks.contains(DEATH_THREAT)) {
                        Timber.d("DEBUG: received death threat from service... shutting down!");
                        Toast.makeText(MainActivity.this, intent.getStringExtra(DEATH_THREAT),
                                Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        Timber.d("********** onStart **********");

        Intent startItUp = new Intent(this, TorchService.class);
        IntentFilter toggleIntent = new IntentFilter(INTERNAL_INTENT);

        // Listen for intents from the service
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, toggleIntent);

        // Listen for preference changes so we can react if necessary
        prefs.registerOnSharedPreferenceChangeListener(this);

        // Pass the service our preferences as extras on the startup intent
        startItUp.putExtra(SETTINGS_AUTO_ON_KEY, mAutoOn);
        startItUp.putExtra(SETTINGS_PERSISTENCE_KEY, mPersist);

        // Start the service that will handle the camera
        startService(startItUp);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Timber.d("********** onResume **********");

        mTorchEnabled = TorchService.isTorchOn();
        updateImageButton();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Timber.d("********** onStop **********");

        // Stop listening for broadcasts from the service
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);

        // Stop listening for preference changes
        prefs.unregisterOnSharedPreferenceChangeListener(this);

        // Close the About dialog if we're stopping anyway
        if (mAboutDialog.isShowing()) mAboutDialog.dismiss();

        // If no persistence or if the torch is off, stop the service
        if (!mPersist || !mTorchEnabled) {
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
                mAboutDialog.show();
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

        toggleTorch();
    }

    private void toggleTorch() {
        Timber.d("DEBUG: toggleTorch | mTorchEnabled was " + mTorchEnabled + " when image was " +
                "pressed; changing to " + !mTorchEnabled);

        // Use the service to start/stop the torch (start = on, stop = off)
        Intent toggleIntent = new Intent(this, TorchService.class);
        if (mTorchEnabled) toggleIntent.putExtra("stop_torch", true);
        else toggleIntent.putExtra("start_torch", true);
        startService(toggleIntent);

        mTorchEnabled = !mTorchEnabled;
        updateImageButton();
    }

    private void updateImageButton() {
        Timber.d("DEBUG: updateImageButton | mTorchEnabled = " + mTorchEnabled + "; setting " +
                "image accordingly");

        if (mTorchEnabled) mImageButton.setImageResource(R.drawable.torch_on);
        else mImageButton.setImageResource(R.drawable.torch_off);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        Timber.d("DEBUG: SharedPreferences: " + key + " has changed");

        Intent settingsChangedIntent = new Intent(this, TorchService.class);

        // Settings have changed, observe the new value
        if (key.equals(SETTINGS_AUTO_ON_KEY)) {
            mAutoOn = prefs.getBoolean(key, false);
            settingsChangedIntent.putExtra(key, mAutoOn);
        } else if (key.equals(SETTINGS_PERSISTENCE_KEY)) {
            mPersist = prefs.getBoolean(key, false);
            settingsChangedIntent.putExtra(key, mPersist);
        }

        // Notify the service
        startService(settingsChangedIntent);
    }
}
