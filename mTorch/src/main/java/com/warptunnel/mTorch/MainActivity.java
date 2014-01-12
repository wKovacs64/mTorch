package com.warptunnel.mTorch;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private AboutDialog mAboutDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MainFragment())
                    .commit();
        }

        mAboutDialog = new AboutDialog(this);
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
    protected void onPause() {
        super.onPause();

        if (mAboutDialog.isShowing()) mAboutDialog.dismiss();
    }

    public static class MainFragment extends Fragment implements View.OnClickListener {

        private static final String TAG = MainFragment.class.getSimpleName();
        private ImageButton mImageButton;
        private Context mContext;
        private FragmentActivity mActivity;
        private boolean mTorchEnabled;

        public MainFragment() {
            mTorchEnabled = false;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Log.d(TAG, "********** onCreateView **********");

            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @SuppressWarnings("deprecation")
        @Override
        public void onStart() {
            super.onStart();
            Log.d(TAG, "********** onStart **********");

            // Obtain Activity and Context for communicating with UI later
            mActivity = getActivity();
            mContext = mActivity.getApplicationContext();

            // Check for flash capability
            if (!mContext.getPackageManager().hasSystemFeature(PackageManager
                    .FEATURE_CAMERA_FLASH)) {
                Log.e(TAG, getString(R.string.error_no_flash));
                Toast.makeText(mContext, R.string.error_no_flash, Toast.LENGTH_LONG).show();
                mActivity.finish();
                return;
            }

            Log.d(TAG, getString(R.string.debug_flash_found));

            // Set up the clickable toggle image
            mImageButton = (ImageButton) mActivity.findViewById(R.id.torch_image_button);
            if (mImageButton == null) Log.e(TAG, "mImageButton was NULL");
            else {
                mImageButton.setImageResource(R.drawable.torch_off);
                mImageButton.setOnClickListener(this);
                mImageButton.setEnabled(false);

                // Allow the toggle image to be clicked
                mImageButton.setEnabled(true);

                // Keep the screen on while the app is open
                mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            Log.d(TAG, "********** onResume **********");

            mTorchEnabled = mTorchService.isRunning();
            updateImageButton();
        }

        @Override
        public void onPause() {
            super.onPause();
            Log.d(TAG, "********** onPause **********");

        }

        @Override
        public void onStop() {
            super.onStop();
            Log.d(TAG, "********** onStop **********");

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
            boolean persist = sharedPref.getBoolean(getString(R.string.persistence), false);

            if (!persist) mContext.stopService(new Intent(mContext, mTorchService.class));
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            Log.d(TAG, "********** onDestroy **********");

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
                mContext.stopService(new Intent(mContext, mTorchService.class));
            }
            else {
                mContext.startService(new Intent(mContext, mTorchService.class).putExtra
                        (getString(R.string.persistence), persist));
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

}
