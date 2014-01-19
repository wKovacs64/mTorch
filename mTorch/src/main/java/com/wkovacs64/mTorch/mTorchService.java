package com.wkovacs64.mTorch;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.swijaya.galaxytorch.CameraDevice;

public class mTorchService extends Service implements SurfaceHolder.Callback {

    private static final String TAG = mTorchService.class.getSimpleName();
    private static final int ONGOING_NOTIFICATION_ID = 1;
    private static boolean mIsRunning;
    private static boolean mPersist;
    private static boolean mSurfaceCreated;
    private static boolean mIsTorchOn;
    private static boolean mAutoOn;
    private CameraDevice mCameraDevice;
    private SurfaceView mOverlayPreview;
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

    public static boolean isTorchOn() {
        return mIsTorchOn;
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

        // Initializations
        mSurfaceCreated = false;
        mPersist = false;
        mIsRunning = true;
        mIsTorchOn = false;
        mAutoOn = false;
    }

    private void goForeground() {
        Log.d(TAG, "********** goForeground **********");

        // Enter foreground mode to keep the service running and provide a notification to return
        // to the app
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

        // Create an overlay to hold the camera preview and add it to the Window Manager
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

        // Check if this is really a call to start the torch or just the service starting up
        if (intent.hasExtra(getString(R.string.start_torch))) {

            // Do we have the surface? We should, unless the user was impossibly quick to press the
            // toggle and the surfaceCreated callback wasn't reached yet.
            if (mSurfaceCreated) {
                // Let's light this candle!
                startTorch();

                // Check for persistence user setting, enter foreground mode if present
                if (intent.hasExtra(getString(R.string.settings_persistence))) {
                    mPersist = intent.getBooleanExtra(getString(R.string.settings_persistence),
                            false);
                }
                if (mPersist) goForeground();
            }
            else {
                Log.e(TAG, "ERROR: tried to call startTorch but mSurfaceCreated = false");
                stopSelf();
            }
        } else if (intent.hasExtra(getString(R.string.stop_torch))) {
            // Stop the torch
            mCameraDevice.toggleCameraLED(false);
            mIsTorchOn = mCameraDevice.isFlashlightOn();
            if (mPersist) stopForeground(true);
        } else if (intent.hasExtra(getString(R.string.settings_auto_on))) {
            // Take note of the state of the Auto On feature so the SurfaceHolder callback will
            // know to tell MainActivity to toggle the torch properly
            mAutoOn = true;
        }

        return Service.START_NOT_STICKY;
    }

    private void startTorch() {
        Log.d(TAG, "startTorch | mCameraDevice.isFlashlightOn() was " +
                mCameraDevice.isFlashlightOn() + " when image was pressed");

        // Assuming we have a valid CameraDevice, fire it up
        if (mCameraDevice != null) {
            mCameraDevice.toggleCameraLED(true);
            mIsTorchOn = mCameraDevice.isFlashlightOn();
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "********** onDestroy **********");
        super.onDestroy();

        mIsRunning = false;

        // If this service was told to stop for some reason and persistence was enabled,
        // stop running in foreground mode
        if (mPersist) stopForeground(true);

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

        // Start the preview
        mCameraDevice.setPreviewDisplayAndStartPreview(holder);
        mSurfaceCreated = true;

        // If the Auto On feature is enabled, broadcast an intent back to MainActivity to toggle
        // the torch and update the UI accordingly
        if (mAutoOn) {
            Log.d(TAG, "Broadcasting toggleIntent...");

            // send intent back to MainActivity to call toggleTorch();
            Intent toggleIntent = new Intent(MainActivity.INTERNAL_INTENT);
            toggleIntent.putExtra(getString(R.string.settings_auto_on), true);
            LocalBroadcastManager.getInstance(this).sendBroadcast(toggleIntent);
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

        // Housekeeping
        mSurfaceCreated = false;
        mIsTorchOn = false;
    }
}
