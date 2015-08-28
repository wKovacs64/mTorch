package com.wkovacs64.mtorch.ui.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.wkovacs64.mtorch.R;

import timber.log.Timber;

public final class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("********** Settings **********");

        //noinspection deprecation
        addPreferencesFromResource(R.xml.settings);
    }
}
