package com.warptunnel.mTorch;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MainFragment())
                    .commit();
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
                return true;
            case R.id.menu_settings:
                // show Settings
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class MainFragment extends Fragment implements View.OnClickListener,
            SurfaceHolder.Callback {

        private static final String TAG = MainFragment.class.getSimpleName();
        private ImageButton mImageButton;
        private Camera mCamera;
        private boolean mHasFlash;
        private boolean mFlashOn;
        private Context mContext;
        private FragmentActivity mActivity;
        private SurfaceView mCameraPreview;
        private SurfaceHolder mSurfaceHolder;

        public MainFragment() {
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

            // Initially, the flash will be off.   ...right?
            mFlashOn = false;

            // Check for flash capability
            mHasFlash = mContext.getPackageManager().hasSystemFeature(PackageManager
                    .FEATURE_CAMERA_FLASH);
            if (!mHasFlash) {
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
                //mImageButton.setImageResource(R.drawable.torch_off);
                mImageButton.setOnClickListener(this);
                mImageButton.setEnabled(false);

                // Get the Camera device
                // mCamera = new CameraDevice();

                // Get the SurfaceView and the SurfaceHolder for it
                mCameraPreview = (SurfaceView) mActivity.findViewById(R.id.camera_preview);
                mSurfaceHolder = mCameraPreview.getHolder();
                mSurfaceHolder.addCallback(this);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                    mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                }

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
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            Log.d(TAG, "********** onDestroy **********");
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG, "********** onClick **********");

            // Toggle torch and image
            if (toggleTorch()) toggleImage();
            else {
                Log.e(TAG, getString(R.string.error_toggle_failed));
                Toast.makeText(mContext, R.string.error_toggle_failed, Toast.LENGTH_LONG).show();
                mActivity.finish();
            }
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {

        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }

        private boolean toggleTorch() {
            Log.d(TAG, "toggleTorch | mFlashOn = " + mFlashOn);

            // Toggle torch here

            return true;
        }

        private void toggleImage() {
            Log.d(TAG, "toggleImage | mFlashOn = " + mFlashOn);

            if (mFlashOn) mImageButton.setImageResource(R.drawable.torch_off);
            else mImageButton.setImageResource(R.drawable.torch_on);

            mFlashOn = !mFlashOn;
        }

    }

}
