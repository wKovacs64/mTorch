package com.warptunnel.mTorch;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private AboutDialog mAboutDialog;
    private Context mContext;
    private ImageButton mImageButton;
    private boolean mTorchEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "********** onCreate **********");
        setContentView(R.layout.activity_main);

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
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "********** onStart **********");

        // Start the service that will handle the camera
        mContext.startService(new Intent(mContext, mTorchService.class));
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

        // Close the About dialog if we're stopping anyway
        if (mAboutDialog.isShowing()) mAboutDialog.dismiss();

        // Check if the user enabled persistence and stop the service if not
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean persist = sharedPref.getBoolean(getString(R.string.persistence), false);

        // If no persistence or if the torch is off, stop the service
        if (!persist || !mTorchEnabled) {
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

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean persist = sharedPref.getBoolean(getString(R.string.persistence), false);

        // Use the service to start/stop the torch (start = on, stop = off)
        if (mTorchEnabled) {
            mContext.startService(new Intent(mContext, mTorchService.class)
                    .putExtra(getString(R.string.stop_torch), true));
        }
        else {
            mContext.startService(new Intent(mContext, mTorchService.class)
                    .putExtra(getString(R.string.persistence), persist)
                    .putExtra(getString(R.string.start_torch), true));
        }

        mTorchEnabled = !mTorchEnabled;
        updateImageButton();
    }

    private void updateImageButton() {
        Log.d(TAG, "updateImageButton | mTorchEnabled = " + mTorchEnabled + "; setting image " +
                "accordingly");

        if (mTorchEnabled) mImageButton.setImageResource(R.drawable.torch_on);
        else mImageButton.setImageResource(R.drawable.torch_off);
    }

}
