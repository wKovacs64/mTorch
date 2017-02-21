package com.wkovacs64.mtorch.util;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Toast;

import com.wkovacs64.mtorch.Constants;
import com.wkovacs64.mtorch.R;

/**
 * Uninstantiable permission-related utility class.
 */
@TargetApi(Build.VERSION_CODES.M)
public final class PermissionUtils {

  // Pre-constructed String array for requestPermissions calls
  private static final String[] PERMISSION_CAMERA
      = new String[]{Manifest.permission.CAMERA};

  /**
   * Suppress default constructor to prevent instantiation.
   */
  private PermissionUtils() {
    throw new AssertionError();
  }

  /**
   * Checks to see if the required Camera permission has already been granted.
   *
   * @param context the Context to pass to {@link ContextCompat#checkSelfPermission}
   * @return true if the permissions have been granted, false if not
   */
  public static boolean hasCameraPermissions(@NonNull final Context context) {
    return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        == PackageManager.PERMISSION_GRANTED;
  }

  /**
   * Requests the Manifest.permission.CAMERA permission.
   *
   * @param activity the calling Activity in which to prompt for permissions
   * @param rootView the View to hold the Snackbar
   */
  public static void requestCameraPermissions(@NonNull final Activity activity,
                                              @NonNull final View rootView) {
    if (activity.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
      Snackbar.make(rootView,
          R.string.permission_rationale_camera,
          Snackbar.LENGTH_INDEFINITE)
          .setActionTextColor(ContextCompat.getColor(activity, R.color.accent))
          .setAction(R.string.ok, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              activity.requestPermissions(PERMISSION_CAMERA,
                  Constants.RESULT_PERMISSION_CAMERA);
            }
          })
          .show();
    } else {
      activity.requestPermissions(PERMISSION_CAMERA, Constants.RESULT_PERMISSION_CAMERA);
    }
  }

  /**
   * Requests the Manifest.permission.CAMERA permission for activities without a content view.
   *
   * @param activity the calling Activity in which to prompt for permissions
   */
  public static void requestCameraPermissions(@NonNull final Activity activity) {
    if (activity.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
      Toast.makeText(activity, R.string.permission_rationale_camera, Toast.LENGTH_LONG)
          .show();
    } else {
      activity.requestPermissions(PERMISSION_CAMERA, Constants.RESULT_PERMISSION_CAMERA);
    }
  }
}
