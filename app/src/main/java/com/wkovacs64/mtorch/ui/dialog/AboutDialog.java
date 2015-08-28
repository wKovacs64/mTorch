package com.wkovacs64.mtorch.ui.dialog;

import android.app.Dialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.wkovacs64.mtorch.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public final class AboutDialog extends DialogFragment {

    public static final String TAG = AboutDialog.class.getSimpleName();

    @Bind(R.id.about_description)
    TextView mAboutDesc;
    @Bind(R.id.about_version_number)
    TextView mAboutVersion;

    public static AboutDialog newInstance() {
        return new AboutDialog();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.dialog_about, container, false);
        ButterKnife.bind(this, view);

        // Configure the dialog
        Dialog dialog = getDialog();
        dialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
        dialog.setContentView(R.layout.dialog_about);
        dialog.setTitle(getString(R.string.app_name));
        dialog.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_launcher);

        // Find the version number
        String versionName;
        try {
            PackageInfo pkgInfo = getContext().getPackageManager()
                    .getPackageInfo(getContext().getPackageName(), 0);
            versionName = pkgInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = getString(R.string.unknown);
        }

        // Populate data
        mAboutDesc.setText(getString(R.string.app_name) + " " + mAboutDesc.getText());
        mAboutVersion.setText(versionName);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
