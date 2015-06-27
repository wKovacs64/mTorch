package com.wkovacs64.mtorch;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.swijaya.galaxytorch.CameraDevice;

import timber.log.Timber;

public class TorchService extends Service implements SurfaceHolder.Callback {

    private static final String DEATH_THREAT = "die";
    private static final String SETTINGS_AUTO_ON_KEY = "auto_on";
    private static final String SETTINGS_PERSISTENCE_KEY = "persistence";
    private static final int ONGOING_NOTIFICATION_ID = 1;

    private static boolean mPersist;
    private static boolean mSurfaceCreated;
    private static boolean mIsTorchOn;
    private static boolean mAutoOn;

    private AsyncTask<Object, Void, Boolean> mStartPreviewTask;
    private CameraDevice mCameraDevice;
    private SurfaceView mOverlayPreview;
    private FrameLayout mOverlayLayout;

    public TorchService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // This is a "Started" service (not a "Bound" service)
        return null;
    }

    public static boolean isTorchOn() {
        return mIsTorchOn;
    }

    @Override
    public void onCreate() {
        Timber.d("********** onCreate **********");
        super.onCreate();

        // Get access to the camera
        mCameraDevice = new CameraDevice();
        if (mCameraDevice.acquireCamera()) {
            // Dynamically create the overlay layout and surface preview contained within
            createOverlay();

            // Create the holder for the preview, store it in the callback
            SurfaceHolder localHolder = mOverlayPreview.getHolder();
            if (localHolder != null) {
                localHolder.addCallback(this);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                    //noinspection deprecation
                    localHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                }
            } else {
                Timber.e("ERROR: unable to get SurfaceHolder");
                die(getString(R.string.error_camera_unavailable));
            }

            // Initializations
            mSurfaceCreated = false;
            mPersist = false;
            mIsTorchOn = false;
            mAutoOn = false;
        } else die(getString(R.string.error_camera_unavailable));
    }

    private void goForeground() {
        Timber.d("********** goForeground **********");

        // Enter foreground mode to keep the service running and provide a notification to return
        // to the app
        Intent launchActivity = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, launchActivity, 0);
        Notification notification = new NotificationCompat.Builder(this)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notification_text)).setContentIntent(pIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSmallIcon(R.drawable.ic_stat_notify).build();

        startForeground(ONGOING_NOTIFICATION_ID, notification);
    }

    @SuppressLint("InflateParams")
    private void createOverlay() {
        Timber.d("********** createOverlay **********");

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
            Timber.e("ERROR: mOverlayLayout already had a value");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.d("********** onStartCommand **********");

        // Check for 'auto on' user setting
        if (intent.hasExtra(SETTINGS_AUTO_ON_KEY)) {
            mAutoOn = intent.getBooleanExtra(SETTINGS_AUTO_ON_KEY, false);
        }

        // Check for persistence user setting
        if (intent.hasExtra(SETTINGS_PERSISTENCE_KEY)) {
            mPersist = intent.getBooleanExtra(SETTINGS_PERSISTENCE_KEY, false);

            // If the user enables persistence while the torch is already lit, goForeground
            // If the user disables persistence while the torch is already lit, stopForeground
            if (mIsTorchOn) {
                if (mPersist) goForeground();
                else stopForeground(true);
            }
        }

        // Check if this is really a call to start the torch or just the service starting up
        if (intent.hasExtra("start_torch")) {

            // Do we have the surface? We should, unless the user was impossibly quick to press the
            // toggle and the surfaceCreated callback wasn't reached yet.
            if (mSurfaceCreated) {
                // Let's light this candle!
                startTorch();

                // Check for persistence user setting, enter foreground mode if present
                if (mPersist) goForeground();
            } else die("ERROR: tried to call startTorch but mSurfaceCreated = false");
        } else if (intent.hasExtra("stop_torch")) {
            // Stop the torch
            mCameraDevice.toggleCameraLED(false);
            mIsTorchOn = mCameraDevice.isFlashlightOn();
            if (mPersist) stopForeground(true);
        }

        return Service.START_NOT_STICKY;
    }

    private void startTorch() {
        Timber.d("DEBUG: startTorch | mCameraDevice.isFlashlightOn() was " +
                mCameraDevice.isFlashlightOn() + " when image was pressed");

        // Fire it up
        mCameraDevice.toggleCameraLED(true);
        mIsTorchOn = mCameraDevice.isFlashlightOn();
    }

    @Override
    public void onDestroy() {
        Timber.d("********** onDestroy **********");
        super.onDestroy();

        // Cancel any currently running CameraDeviceStartPreview tasks (should never happen anyway)
        if (mStartPreviewTask != null) {
            Timber.wtf("Canceling mStartPreviewTask...");
            mStartPreviewTask.cancel(true);
        }

        // Set torch to off, in case the activity/service is restarted too quickly before the
        // SurfaceHolder has been destroyed
        mIsTorchOn = false;

        // If this service was told to stop for some reason and persistence was enabled,
        // stop running in foreground mode
        if (mPersist) stopForeground(true);

        // Shut the torch off if it was on when we got shut down
        if (mCameraDevice != null && mCameraDevice.isFlashlightOn()) {
            Timber.w("WARN: torch still on, better shut it off");
            if (!mCameraDevice.toggleCameraLED(false)) {
                Timber.e("ERROR: could not toggle torch");
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
        Timber.d("********** (overlay) surfaceCreated **********");

        if (mCameraDevice == null) {
            Timber.w("WARN: mCameraDevice is null");
            return;
        }

        // Start the preview
        mStartPreviewTask = new CameraDeviceStartPreview();
        mStartPreviewTask.execute(mCameraDevice, holder);
        mSurfaceCreated = true;

        // If the Auto On feature is enabled, broadcast an intent back to MainActivity to toggle
        // the torch and update the UI accordingly
        if (mAutoOn) {
            Timber.d("DEBUG: broadcasting toggleIntent...");

            // send intent back to MainActivity to call toggleTorch();
            Intent toggleIntent = new Intent(MainActivity.INTERNAL_INTENT);
            toggleIntent.putExtra(SETTINGS_AUTO_ON_KEY, true);
            LocalBroadcastManager.getInstance(this).sendBroadcast(toggleIntent);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Timber.d("********** (overlay) surfaceChanged **********");

        // I don't think there's anything interesting we need to do in this method,
        // but it's required to implement.
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Timber.d("********** (overlay) surfaceDestroyed **********");

        // Housekeeping
        mSurfaceCreated = false;
        mIsTorchOn = false;
    }

    private class CameraDeviceStartPreview extends AsyncTask<Object, Void, Boolean> {

        private final String TAG = CameraDeviceStartPreview.class.getSimpleName();

        @Override
        protected Boolean doInBackground(Object... params) {
            if (params == null || params.length != 2) {
                Timber.wtf("WTF: this task requires a CameraDevice and a SurfaceHolder");
                return false;
            }

            CameraDevice cameraDevice = (CameraDevice) params[0];
            SurfaceHolder holder = (SurfaceHolder) params[1];

            cameraDevice.setPreviewDisplayAndStartPreview(holder);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            mStartPreviewTask = null;
            if (!success) stopSelf();
        }
    }

    private void die(String errMsg) {
        Timber.e(errMsg);

        // send intent back to MainActivity to finish()
        Intent deathThreat = new Intent(MainActivity.INTERNAL_INTENT);
        deathThreat.putExtra(DEATH_THREAT, errMsg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(deathThreat);

        // Stop the service
        stopSelf();
    }
}
