package com.warptunnel.mTorch;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.swijaya.galaxytorch.CameraDevice;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class mTorchService extends Service {

    private static final String TAG = mTorchService.class.getSimpleName();
    private final Lock mSurfaceLock = new ReentrantLock();
    private final Condition mSurfaceHolderIsSet = mSurfaceLock.newCondition();
    private CameraDevice mCameraDevice;
    private SurfaceView mCameraPreview;
    private SurfaceHolder mSurfaceHolder;

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

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
