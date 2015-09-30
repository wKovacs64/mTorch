package com.wkovacs64.mtorch.ui.fragment;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.wkovacs64.mtorch.R;

public final class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }
}
