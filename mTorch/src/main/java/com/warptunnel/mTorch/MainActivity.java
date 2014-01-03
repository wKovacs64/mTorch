package com.warptunnel.mTorch;

import android.content.Context;
import android.content.pm.PackageManager;
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

import com.swijaya.galaxytorch.CameraDevice;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
        private final Lock mSurfaceLock = new ReentrantLock();
        private final Condition mSurfaceHolderIsSet = mSurfaceLock.newCondition();
        private ImageButton mImageButton;
        private CameraDevice mCameraDevice;
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
                mImageButton.setImageResource(R.drawable.torch_off);
                mImageButton.setOnClickListener(this);
                mImageButton.setEnabled(false);

                // Get the Camera device
                mCameraDevice = new CameraDevice();

                // Get the camera preview SurfaceView
                mCameraPreview = (SurfaceView) mActivity.findViewById(R.id.camera_preview);
                /**
                 * Get a throw-away SurfaceHolder and add a callback to it. We'll get and store
                 * the result in mSurfaceHolder and start the camera preview in the callback once
                 * we know the SurfaceHolder has been created successfully.
                 */
                SurfaceHolder localHolder = mCameraPreview.getHolder();
                if (localHolder != null) {
                    localHolder.addCallback(this);
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                        localHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                    }
                } else {
                    Log.e(TAG, getString(R.string.error_holder_failed));
                    Toast.makeText(mContext, R.string.error_holder_failed,
                            Toast.LENGTH_LONG).show();
                    mActivity.finish();
                    return;
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

            // when we get here from onPause(), the camera would have been released and
            // now re-acquired, but that means the camera has now no surface holder
            // to flush to! so remember the state of the surface holder, and reset
            // it immediately after re-acquiring
            if (!mCameraDevice.acquireCamera()) {
                // bail fast if we cannot acquire the camera device to begin with - perhaps some
                // background service outside of our control is holding it hostage
                Log.e(TAG, getString(R.string.error_camera_unavailable));
                Toast.makeText(mContext, R.string.error_camera_unavailable,
                        Toast.LENGTH_LONG).show();
                mActivity.finish();
            }
            if (mSurfaceHolder != null) {
                mCameraDevice.setPreviewDisplayAndStartPreview(mSurfaceHolder);
            }
        }

        @Override
        public void onPause() {
            super.onPause();
            Log.d(TAG, "********** onPause **********");

            // toggle the torch if it is on
            if (mCameraDevice.isFlashlightOn()) {
                if (!mCameraDevice.toggleCameraLED(false)) {
                    Log.e(TAG, getString(R.string.error_toggle_failed));
                    return;
                }
                mImageButton.setSelected(false);
            }
        }

        @Override
        public void onStop() {
            super.onStop();
            Log.d(TAG, "********** onStop **********");

            // don't stop preview too early; releaseCamera() does it anyway and it might need the
            // preview to toggle the torch off cleanly
            mCameraDevice.releaseCamera();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            Log.d(TAG, "********** onDestroy **********");

            // toggle the torch if it is on
            if (mCameraDevice.isFlashlightOn()) {
                if (!mCameraDevice.toggleCameraLED(false)) {
                    Log.e(TAG, getString(R.string.error_toggle_failed));
                }
            }

            mCameraDevice.releaseCamera();
            mImageButton.setSelected(false);
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
            Log.d(TAG, "********** surfaceCreated **********");

            // atomically set the surface holder and start camera preview
            mSurfaceLock.lock();
            try {
                mSurfaceHolder = holder;
                mCameraDevice.setPreviewDisplayAndStartPreview(mSurfaceHolder);
                mSurfaceHolderIsSet.signalAll();
            } finally {
                mSurfaceLock.unlock();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.d(TAG, "********** surfaceChanged **********");

            // I don't think there's anything interesting we need to do in this method,
            // but it's required to implement.
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.d(TAG, "********** surfaceDestroyed **********");

            mCameraDevice.stopPreview();
            mSurfaceHolder = null;
        }

        private boolean toggleTorch() {
            Log.d(TAG, "toggleTorch | mFlashOn was " + mFlashOn + " when image was pressed");

            return mCameraDevice.toggleCameraLED(!mFlashOn);
        }

        private void toggleImage() {
            //Log.d(TAG, "toggleImage | mFlashOn = " + mFlashOn);

            if (mFlashOn) mImageButton.setImageResource(R.drawable.torch_off);
            else mImageButton.setImageResource(R.drawable.torch_on);

            mFlashOn = !mFlashOn;
        }

    }

}
