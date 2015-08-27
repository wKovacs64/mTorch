package com.wkovacs64.mtorch.ui.activity;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Process;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import com.wkovacs64.mtorch.Constants;
import com.wkovacs64.mtorch.R;
import com.wkovacs64.mtorch.service.TorchService;
import com.wkovacs64.mtorch.ui.dialog.AboutDialog;

import java.util.List;
import java.util.Set;

import butterknife.Bind;
import timber.log.Timber;

public class MainActivity extends BaseActivity
        implements View.OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private boolean mAutoOn;
    private boolean mPersist;
    private boolean mTorchEnabled;

    private AboutDialog mAboutDialog;
    private BroadcastReceiver mBroadcastReceiver;
    private SharedPreferences mPrefs;

    @Bind(R.id.torch_image_button)
    ImageButton mImageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("********** onCreate **********");

        // Check for flash capability
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            Timber.e(getString(R.string.error_no_flash));
            Toast.makeText(this, R.string.error_no_flash, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        Timber.d("DEBUG: flash capability detected!");

        // Set the content
        setContentView(R.layout.activity_main);

        // Read preferences
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mAutoOn = mPrefs.getBoolean(Constants.SETTINGS_KEY_AUTO_ON, false);
        mPersist = mPrefs.getBoolean(Constants.SETTINGS_KEY_PERSISTENCE, false);

        // Set up the About dialog box
        mAboutDialog = new AboutDialog(this);

        // Register to receive broadcasts from the service
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Timber.d("DEBUG: broadcast received...");
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    Set<String> ks = extras.keySet();

                    if (ks.contains(Constants.EXTRA_UPDATE_UI)) {
                        // Update the UI according to the command from the service
                        mTorchEnabled = extras.getBoolean(Constants.EXTRA_UPDATE_UI);
                        updateUi();
                    } else if (ks.contains(Constants.EXTRA_DEATH_THREAT)) {
                        Timber.d("DEBUG: received death threat from service... shutting down!");
                        Toast.makeText(MainActivity.this,
                                intent.getStringExtra(Constants.EXTRA_DEATH_THREAT),
                                Toast.LENGTH_LONG)
                                .show();
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

        // Listen for command intents from the service
        IntentFilter command = new IntentFilter(Constants.INTENT_COMMAND);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, command);

        // Listen for toggle image clicks
        mImageButton.setOnClickListener(this);

        // Listen for preference changes so we can react if necessary
        mPrefs.registerOnSharedPreferenceChangeListener(this);

        // Pass the service our preferences as extras on the startup intent
        Intent startItUp = new Intent(this, TorchService.class);
        startItUp.putExtra(Constants.SETTINGS_KEY_PERSISTENCE, mPersist);

        // Start the service that will control the torch
        if (mAutoOn) startItUp.putExtra(Constants.EXTRA_START_TORCH, true);
        startService(startItUp);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Timber.d("********** onStop **********");

        // Stop listening for toggle image clicks
        mImageButton.setOnClickListener(null);

        // Stop listening for preference changes
        mPrefs.unregisterOnSharedPreferenceChangeListener(this);

        // Stop listening for broadcasts from the service
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);

        // Close the About dialog if we're stopping anyway
        if (mAboutDialog.isShowing()) mAboutDialog.dismiss();

        // If no persistence or if the torch is off, stop the service
        if (!mPersist || !mTorchEnabled) {
            killService(getPackageName() + getString(R.string.service_process));
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        Timber.d("DEBUG: SharedPreferences: " + key + " has changed");

        // Settings have changed, observe the new value
        if (key.equals(Constants.SETTINGS_KEY_AUTO_ON)) {
            mAutoOn = prefs.getBoolean(key, false);
        } else if (key.equals(Constants.SETTINGS_KEY_PERSISTENCE)) {
            mPersist = prefs.getBoolean(key, false);

            // Create intent to notify the service of the changed setting
            Intent settingsChangedIntent = new Intent(this, TorchService.class);
            settingsChangedIntent.putExtra(Constants.SETTINGS_KEY_PERSISTENCE, mPersist);
            startService(settingsChangedIntent);
        }
    }

    private void updateUi() {
        Timber.d("DEBUG: updating UI...");

        // Set the corresponding toggle image
        Timber.d("DEBUG: mTorchEnabled = " + mTorchEnabled + "; setting image accordingly");
        mImageButton.setImageResource(mTorchEnabled ? R.drawable.torch_on : R.drawable.torch_off);

        // Keep the screen on while the app is open and the torch is on
        if (mTorchEnabled) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private void toggleTorch() {
        Timber.d("DEBUG: toggleTorch | mTorchEnabled was " + mTorchEnabled
                + " when image was pressed; changing to " + !mTorchEnabled);

        // Use the service to start/stop the torch (start = on, stop = off)
        Intent toggleIntent = new Intent(this, TorchService.class);
        toggleIntent.putExtra(mTorchEnabled
                ? Constants.EXTRA_STOP_TORCH : Constants.EXTRA_START_TORCH, true);
        startService(toggleIntent);
    }

    private void killService(String processName) {
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses
                = am.getRunningAppProcesses();

        for (ActivityManager.RunningAppProcessInfo next : runningAppProcesses) {
            if (next.processName.equals(processName)) {
                Process.killProcess(next.pid);
                break;
            }
        }
    }
}
