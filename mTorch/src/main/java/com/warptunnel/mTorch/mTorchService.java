package com.warptunnel.mTorch;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Binder;
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
    private final IBinder mBinder = new LocalBinder();
    private CameraDevice mCameraDevice;
    private SurfaceView mOverlayPreview;
    private SurfaceHolder mSurfaceHolder;
    private LinearLayout mOverlayLayout;

    public mTorchService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        mTorchService getService() {
            return mTorchService.this;
        }
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "********** onCreate **********");
        super.onCreate();

        // Don't think we want this in the service anymore
/*
        // Check for flash capability
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            Log.e(TAG, getString(R.string.error_no_flash));
            stopSelf();
        }
        Log.d(TAG, getString(R.string.debug_flash_found));
*/

/*
        // Get the Camera device
        mCameraDevice = new CameraDevice();

        if (!mCameraDevice.acquireCamera()) {
            Log.e(TAG, getString(R.string.error_camera_unavailable));
        }

        // Dynamically create the overlay layout and surface preview contained within
        createOverlay();

        // Create the holder for the preview
        SurfaceHolder localHolder = mOverlayPreview.getHolder();
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

    private void createOverlay() {
        Log.d(TAG, "********** createOverlay **********");
        if (mOverlayLayout == null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(1, 1,
                    WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
            layoutParams.gravity = Gravity.BOTTOM;

            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            mOverlayLayout = (LinearLayout) inflater.inflate(R.layout.overlay, null);
            mOverlayPreview = (SurfaceView) mOverlayLayout.findViewById(R.id.overlay_preview);

            WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            wm.addView(mOverlayLayout, layoutParams);
        } else {
            Log.e(TAG, "ERROR: mOverlayLayout already had a value");
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

        // Remove the overlay layout/preview
        if (mOverlayLayout != null) {
            WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            wm.removeView(mOverlayLayout);
            mOverlayLayout = null;
        }
        mOverlayPreview = null;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "********** (overlay) surfaceCreated **********");

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
        Log.d(TAG, "********** (overlay) surfaceChanged **********");

        // I don't think there's anything interesting we need to do in this method,
        // but it's required to implement.
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "********** (overlay) surfaceDestroyed **********");

        // I don't think there's anything interesting we need to do in this method,
        // but it's required to implement.
    }
}
