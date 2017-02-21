package com.wkovacs64.mtorch.ui.activity;

import android.app.Activity;
import android.os.Bundle;

import com.wkovacs64.mtorch.ui.fragment.SettingsFragment;

import timber.log.Timber;

public final class SettingsActivity extends Activity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Timber.d("********** Settings **********");
    getFragmentManager()
        .beginTransaction()
        .replace(android.R.id.content, new SettingsFragment())
        .commit();
  }
}
