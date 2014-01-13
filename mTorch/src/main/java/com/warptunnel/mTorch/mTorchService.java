package com.warptunnel.mTorch;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.swijaya.galaxytorch.CameraDevice;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class mTorchService extends Service implements SurfaceHolder.Callback {

    private static final String TAG = mTorchService.class.getSimpleName();
    private static final int ONGOING_NOTIFICATION_ID = 1;
    private static boolean mIsRunning;
    private static boolean mPersist;
    private final Lock mSurfaceLock = new ReentrantLock();
    private final Condition mSurfaceHolderIsSet = mSurfaceLock.newCondition();
    private CameraDevice mCameraDevice;
    private SurfaceView mOverlayPreview;
    private SurfaceHolder mSurfaceHolder;
    private FrameLayout mOverlayLayout;

    public mTorchService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // This is a "Started" service (not a "Bound" service)
        return null;
    }

    public static boolean isRunning() {
        return mIsRunning;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "********** onCreate **********");
        super.onCreate();

        // Get access to the camera
        mCameraDevice = new CameraDevice();
        if (!mCameraDevice.acquireCamera()) {
            Log.e(TAG, getString(R.string.error_camera_unavailable));
            stopSelf();
        }

        // Dynamically create the overlay layout and surface preview contained within
        createOverlay();

        // Create the holder for the preview, store it in the callback
        SurfaceHolder localHolder = mOverlayPreview.getHolder();
        if (localHolder != null) {
            localHolder.addCallback(this);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                localHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            }
        } else {
            Log.e(TAG, getString(R.string.error_holder_failed));
            stopSelf();
        }

        mPersist = false;
        mIsRunning = true;
    }

    private void goForeground() {
        Log.d(TAG, "********** goForeground **********");

        Intent launchActivity = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, launchActivity, 0);
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notification_text)).setContentIntent(pIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSmallIcon(R.drawable.ic_launcher).build();

        startForeground(ONGOING_NOTIFICATION_ID, notification);
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
            mOverlayLayout = (FrameLayout) inflater.inflate(R.layout.overlay, null);
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

        if (intent.hasExtra(getString(R.string.persistence))) {
            mPersist = intent.getBooleanExtra(getString(R.string.persistence), false);
        }
        if (mPersist) goForeground();

        return Service.START_NOT_STICKY;
    }

    private void startTorch() {
        Log.d(TAG, "startTorch | mCameraDevice.isFlashlightOn() was " +
                mCameraDevice.isFlashlightOn() + " when image was pressed");

        mSurfaceLock.lock();
        try {
            while (mSurfaceHolder == null) {
                mSurfaceHolderIsSet.await();
            }
        }
        catch (InterruptedException e) {
            Log.e(TAG, "ERROR: " + e.getLocalizedMessage());
            // possible Notification here to alert the user something bad happened?
            stopSelf();
        }
        finally {
            mSurfaceLock.unlock();
        }

        if (mCameraDevice != null) mCameraDevice.toggleCameraLED(true);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "********** onDestroy **********");
        super.onDestroy();

        if (mPersist) stopForeground(true);
        mIsRunning = false;

        // Shut the torch off if it was on when we got shut down
        if (mCameraDevice != null && mCameraDevice.isFlashlightOn()) {
            Log.w(TAG, "Torch still on, better shut it off");
            if (!mCameraDevice.toggleCameraLED(false)) {
                Log.e(TAG, "ERROR: could not toggle torch");
            }
        }

        // Release the camera
        if (mCameraDevice != null) {
            mCameraDevice.releaseCamera();
            mCameraDevice = null;
        }

        // Remove the overlay layout and preview
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

        startTorch();
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
