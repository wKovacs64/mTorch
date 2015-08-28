package com.wkovacs64.mtorch.ui.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.wkovacs64.mtorch.ui.fragment.SettingsFragment;

import timber.log.Timber;

public final class SettingsActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("********** Settings **********");
        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
