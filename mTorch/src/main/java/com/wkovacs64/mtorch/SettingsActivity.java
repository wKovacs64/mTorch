package com.wkovacs64.mtorch;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

public class SettingsActivity extends PreferenceActivity {

    private static final String TAG = SettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "********** Settings **********");

        //noinspection deprecation
        addPreferencesFromResource(R.xml.settings);
    }
}
