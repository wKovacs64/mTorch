package com.wkovacs64.mtorch.core;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.List;

/**
 * A {@link Torch} implementation built on the deprecated {@link Camera} API for use on Android
 * Honeycomb (API 11) through Android Lollipop (API 22).
 */
public class CameraTorch implements Torch {

    private final SurfaceTexture mDummySurface = new SurfaceTexture(0);

    private Camera mCamera;
    private boolean mTorchEnabled;

    @Override
    public void init() {
        try {
            // Acquire the Camera device
            mCamera = Camera.open();
        } catch (RuntimeException e) {
            throw new IllegalStateException("Failed to acquire the camera device!", e);
        }

        // Test to make sure it supports torch mode
        if (mCamera == null || !supportsTorchMode(mCamera)) {
            throw new IllegalStateException("No back-facing camera that supports torch mode!");
        }

        // Start the preview required to enable the flash
        mCamera.startPreview();
        try {
            mCamera.setPreviewTexture(mDummySurface);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to set preview texture!", e);
        }
    }

    @Override
    public void toggle(boolean enabled) {
        // Toggle the torch
        Camera.Parameters params = mCamera.getParameters();
        params.setFlashMode(enabled
                ? Camera.Parameters.FLASH_MODE_TORCH
                : Camera.Parameters.FLASH_MODE_OFF);
        mCamera.setParameters(params);
        mTorchEnabled = enabled;
    }

    @Override
    public boolean isOn() {
        return mTorchEnabled;
    }

    @Override
    public void tearDown() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            mTorchEnabled = false;
        }
    }

    /**
     * Determines whether or not the supplied Camera supports torch mode.
     *
     * @param camera the Camera to test
     * @return true if the Camera supports torch mode, false if not
     */
    private boolean supportsTorchMode(@NonNull Camera camera) {
        Camera.Parameters params = camera.getParameters();
        List<String> flashModes = params.getSupportedFlashModes();
        return (flashModes != null && flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH));
    }
}
