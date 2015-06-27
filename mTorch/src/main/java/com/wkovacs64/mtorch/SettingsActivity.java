package com.wkovacs64.mtorch;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import timber.log.Timber;

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("********** Settings **********");

        //noinspection deprecation
        addPreferencesFromResource(R.xml.settings);
    }
}
