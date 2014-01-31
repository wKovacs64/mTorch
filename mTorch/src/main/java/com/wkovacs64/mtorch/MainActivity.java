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
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends ActionBarActivity implements View.OnClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    public final static String INTERNAL_INTENT = MainActivity.class.getPackage().getName() +
            "INTERNAL_INTENT";
    private static final String TAG = MainActivity.class.getSimpleName();
    private boolean mTorchEnabled;
    private AboutDialog mAboutDialog;
    private Context mContext;
    private ImageButton mImageButton;
    private BroadcastReceiver mBroadcastReceiver;
    private SharedPreferences prefs;
    private boolean mAutoOn;
    private boolean mPersist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "********** onCreate **********");
        setContentView(R.layout.activity_main);

        // Read preferences
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mAutoOn = prefs.getBoolean(getString(R.string.settings_auto_on_key), false);
        mPersist = prefs.getBoolean(getString(R.string.settings_persistence_key), false);

        // Assume flash off on launch (certainly true the first time)
        mTorchEnabled = false;

        // Set up the About dialog box
        mAboutDialog = new AboutDialog(this);

        // Track this Context for potentially passing it around later
        mContext = getApplicationContext();

        // Check for flash capability
        if (!mContext.getPackageManager().hasSystemFeature(PackageManager
                .FEATURE_CAMERA_FLASH)) {
            Log.e(TAG, getString(R.string.error_no_flash));
            Toast.makeText(mContext, R.string.error_no_flash, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        Log.d(TAG, getString(R.string.debug_flash_found));

        // Set up the clickable toggle image
        mImageButton = (ImageButton) findViewById(R.id.torch_image_button);
        if (mImageButton == null) Log.e(TAG, "mImageButton was NULL");
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
                Log.d(TAG, "Broadcast received...");
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    Set<String> ks = extras.keySet();

                    if (ks.contains(getString(R.string.settings_auto_on_key))) {
                        Log.d(TAG, "Intent included Auto On extra, toggling torch...");
                        toggleTorch();
                    }
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "********** onStart **********");

        Intent startItUp = new Intent(mContext, mTorchService.class);
        IntentFilter toggleIntent = new IntentFilter(INTERNAL_INTENT);

        // Listen for intents from the service
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, toggleIntent);

        // Listen for preference changes so we can react if necessary
        prefs.registerOnSharedPreferenceChangeListener(this);

        // Pass the service our preferences as extras on the startup intent
        startItUp.putExtra(getString(R.string.settings_auto_on_key), mAutoOn);
        startItUp.putExtra(getString(R.string.settings_persistence_key), mPersist);

        // Start the service that will handle the camera
        mContext.startService(startItUp);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "********** onResume **********");

        mTorchEnabled = mTorchService.isTorchOn();
        updateImageButton();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "********** onPause **********");

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "********** onStop **********");

        // Stop listening for broadcasts from the service
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);

        // Stop listening for preference changes
        prefs.unregisterOnSharedPreferenceChangeListener(this);

        // Close the About dialog if we're stopping anyway
        if (mAboutDialog.isShowing()) mAboutDialog.dismiss();

        // If no persistence or if the torch is off, stop the service
        if (!mPersist || !mTorchEnabled) {
            mContext.stopService(new Intent(mContext, mTorchService.class));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "********** onDestroy **********");

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
        Log.d(TAG, "********** onClick **********");

        toggleTorch();
    }

    private void toggleTorch() {
        Log.d(TAG, "toggleTorch | mTorchEnabled was " + mTorchEnabled + " when image was " +
                "pressed; changing to " + !mTorchEnabled);

        // Use the service to start/stop the torch (start = on, stop = off)
        Intent toggleIntent = new Intent(mContext, mTorchService.class);
        if (mTorchEnabled) toggleIntent.putExtra(getString(R.string.stop_torch), true);
        else toggleIntent.putExtra(getString(R.string.start_torch), true);
        mContext.startService(toggleIntent);

        mTorchEnabled = !mTorchEnabled;
        updateImageButton();
    }

    private void updateImageButton() {
        Log.d(TAG, "updateImageButton | mTorchEnabled = " + mTorchEnabled + "; setting image " +
                "accordingly");

        if (mTorchEnabled) mImageButton.setImageResource(R.drawable.torch_on);
        else mImageButton.setImageResource(R.drawable.torch_off);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        Log.d(TAG, "SharedPreferences: " + key + " has changed");

        Intent settingsChangedIntent = new Intent(mContext, mTorchService.class);

        // Settings have changed, observe the new value
        if (key.equals(getString(R.string.settings_auto_on_key))) {
            mAutoOn = prefs.getBoolean(key, false);
            settingsChangedIntent.putExtra(key, mAutoOn);
        } else if (key.equals(getString(R.string.settings_persistence_key))) {
            mPersist = prefs.getBoolean(key, false);
            settingsChangedIntent.putExtra(key, mPersist);
        }

        // Notify the service
        mContext.startService(settingsChangedIntent);
    }
}
