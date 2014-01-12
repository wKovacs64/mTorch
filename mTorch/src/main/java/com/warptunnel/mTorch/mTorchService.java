package com.warptunnel.mTorch;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.swijaya.galaxytorch.CameraDevice;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class mTorchService extends Service implements SurfaceHolder.Callback {

    private static final String TAG = mTorchService.class.getSimpleName();
    private final Lock mSurfaceLock = new ReentrantLock();
    private final Condition mSurfaceHolderIsSet = mSurfaceLock.newCondition();
    private CameraDevice mCameraDevice;
    private SurfaceView mHiddenPreview;
    private SurfaceHolder mSurfaceHolder;
    private LinearLayout mHiddenLayout;

    public mTorchService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // This is a Started Service (unbound).
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "********** onCreate **********");
        super.onCreate();

        // Check for flash capability
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            Log.e(TAG, getString(R.string.error_no_flash));
            stopSelf();
        }
        Log.d(TAG, getString(R.string.debug_flash_found));

        // Don't want this in onCreate(), will want wherever we get notified it's time to enable
        // the torch
/*
        // Get the Camera device
        mCameraDevice = new CameraDevice();

        if (!mCameraDevice.acquireCamera()) {
            Log.e(TAG, getString(R.string.error_camera_unavailable));
        }

        // Dynamically create the hidden layout and hidden surface preview contained within
        createHiddenPreview();

        // Create the holder for the hidden preview
        SurfaceHolder localHolder = mHiddenPreview.getHolder();
        if (localHolder != null) {
            localHolder.addCallback(this);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                localHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            }
        } else {
            Log.e(TAG, getString(R.string.error_holder_failed));
            stopSelf(); // ?
        }
*/
    }

    private void createHiddenPreview() {
        Log.d(TAG, "********** createHiddenPreview **********");
        if (mHiddenLayout == null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(1, 1,
                    WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
            layoutParams.gravity = Gravity.BOTTOM;

            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            mHiddenLayout = (LinearLayout) inflater.inflate(R.layout.hidden, null);
            mHiddenPreview = (SurfaceView) mHiddenLayout.findViewById(R.id.hidden_preview);

            WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            wm.addView(mHiddenLayout, layoutParams);
        } else {
            Log.e(TAG, "ERROR: mHiddenLayout already had a value");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "********** onStartCommand **********");
        return Service.START_NOT_STICKY; // not sure if this is really what we want
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "********** onDestroy **********");
        super.onDestroy();

        // Shut the torch off if it was on when we got shut down
        if (mCameraDevice != null && mCameraDevice.isFlashlightOn()) {
            Log.w(TAG, "Torch still on, better shut it off");
            if (!mCameraDevice.toggleCameraLED(false)) {
                Log.e(TAG, "ERROR: could not toggle torch");
            }
        }

        // Release the camera
        if (mCameraDevice != null) mCameraDevice.releaseCamera();
        mCameraDevice = null;

        // Remove the hidden layout/preview
        if (mHiddenLayout != null) {
            WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            wm.removeView(mHiddenLayout);
            mHiddenLayout = null;
        }
        mHiddenPreview = null;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "********** (hidden) surfaceCreated **********");

        if (mCameraDevice == null) {
            Log.w(TAG, "WARN: mCameraDevice is null");
            return;
        }

        // atomically set the surface holder and start camera preview
        mSurfaceLock.lock();
        try {
            mSurfaceHolder = holder;
            mCameraDevice.setPreviewDisplayAndStartPreview(holder);
            mSurfaceHolderIsSet.signalAll();
        }
        finally {
            mSurfaceLock.unlock();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "********** (hidden) surfaceChanged **********");

        // I don't think there's anything interesting we need to do in this method,
        // but it's required to implement.
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "********** (hidden) surfaceDestroyed **********");

        // I don't think there's anything interesting we need to do in this method,
        // but it's required to implement.
    }
}
