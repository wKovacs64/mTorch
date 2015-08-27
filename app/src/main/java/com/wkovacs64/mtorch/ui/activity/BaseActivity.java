package com.wkovacs64.mtorch.ui.activity;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.wkovacs64.mtorch.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public abstract class BaseActivity extends AppCompatActivity {

    @Bind(R.id.app_bar)
    Toolbar mAppBar;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        // Initialize Butter Knife bindings
        ButterKnife.bind(this);

        // Initialize the app bar
        if (mAppBar != null) {
            setSupportActionBar(mAppBar);
        }
    }
}
