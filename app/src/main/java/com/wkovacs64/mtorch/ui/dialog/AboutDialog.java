package com.wkovacs64.mtorch.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.wkovacs64.mtorch.R;

public final class AboutDialog extends DialogFragment {

  public static final String TAG = AboutDialog.class.getSimpleName();

  private Unbinder unbinder;

  @BindView(R.id.about_version_number) TextView aboutVersion;
  @BindString(R.string.app_name) String appName;
  @BindString(R.string.unknown) String unknown;

  public static AboutDialog newInstance() {
    return new AboutDialog();
  }

  @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    final View view = inflater.inflate(R.layout.dialog_about, container, false);
    unbinder = ButterKnife.bind(this, view);

    // Configure the dialog
    Dialog dialog = getDialog();
    dialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
    dialog.setContentView(R.layout.dialog_about);
    dialog.setTitle(appName);
    dialog.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.mipmap.ic_launcher);

    // Find the version number
    final Activity activity = getActivity();
    String versionName;
    try {
      PackageInfo pkgInfo =
          activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
      versionName = pkgInfo.versionName;
    } catch (PackageManager.NameNotFoundException e) {
      versionName = unknown;
    }

    // Populate data
    aboutVersion.setText(versionName);

    return view;
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    unbinder.unbind();
  }
}
