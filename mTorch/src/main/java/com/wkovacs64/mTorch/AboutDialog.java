package com.wkovacs64.mtorch;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

public class AboutDialog extends Dialog {
    Context mContext;
    TextView mAboutDesc;
    TextView mAboutVersion;
    String mVersion;

    public AboutDialog(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.dialog_about);
        getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_launcher);
        setTitle(R.string.app_name);

        // Insert application name in front of existing description text
        mAboutDesc = (TextView) findViewById(R.id.about_description);
        mAboutDesc.setText(mContext.getString(R.string.app_name) + mAboutDesc.getText());

        // Find version number
        try {
            PackageInfo pkgInfo = mContext.getPackageManager().getPackageInfo(mContext
                    .getPackageName(), 0);
            mVersion = pkgInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            mVersion = mContext.getString(R.string.unknown);
        }

        // Populate version number
        mAboutVersion = (TextView) findViewById(R.id.about_version_number);
        mAboutVersion.setText(mVersion);
    }
}
