package com.wkovacs64.mtorch.ui.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.wkovacs64.mtorch.R;
import com.wkovacs64.mtorch.service.TorchService;

import timber.log.Timber;

import static com.wkovacs64.mtorch.util.PermissionUtils.hasCameraPermissions;

public final class LaunchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check for flash capability
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            Timber.e(getString(R.string.error_no_flash));
            Toast.makeText(this, R.string.error_no_flash, Toast.LENGTH_LONG).show();
            finish();
        }
        Timber.d("Flash capability detected!");

        // Start the service if we have the appropriate permissions, otherwise it will be started
        // after a manual toggle attempt (which prompts for permissions)
        if (hasCameraPermissions(this)) {
            // Start the service
            Intent torchService = new Intent(this, TorchService.class);
            startService(torchService);
        }

        // Move on to MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
