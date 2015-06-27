package com.wkovacs64.mtorch;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public abstract class BaseActivity extends AppCompatActivity {
    private Toolbar mAppBar;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        // Get the ActionBar/Toolbar widget (App Bar)
        mAppBar = getAppBar();
    }

    protected Toolbar getAppBar() {
        if (mAppBar == null) {
            mAppBar = (Toolbar) findViewById(R.id.app_bar);
            if (mAppBar != null) {
                setSupportActionBar(mAppBar);
            }
        }

        return mAppBar;
    }
}
