package com.warptunnel.mTorch;

import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MainFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.menu_about:
                // show About dialog
                return true;
            case R.id.menu_settings:
                // show Settings
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class MainFragment extends Fragment implements View.OnClickListener {

        private static final String TAG = MainFragment.class.getSimpleName();
        private ImageButton mImageButton;
        private Camera mCamera;
        private boolean mHasFlash;
        private boolean mFlashOn;

        public MainFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            Log.d(TAG, "********** onCreateView **********");

            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onStart() {
            super.onStart();
            Log.d(TAG, "********** onStart **********");

            // Check for flash capability
            mHasFlash = getActivity().getApplicationContext().getPackageManager()
                    .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
            if (!mHasFlash) {
                Log.e(TAG, getString(R.string.error_noflash));
                Toast mToast = Toast.makeText(getActivity().getApplicationContext(),
                        R.string.error_noflash, Toast.LENGTH_LONG);
                mToast.show();
                getActivity().finish();
                return;
            }

            Log.d(TAG, getString(R.string.debug_flashfound));

            mImageButton = (ImageButton) getActivity().findViewById(R.id.torch_imagebutton);
            if (mImageButton == null) Log.e(TAG, "mImageButton was NULL");
            else {
                //mImageButton.setImageResource(R.drawable.torch_off);
                mImageButton.setOnClickListener(this);
                mImageButton.setEnabled(false);
                // Get the Camera device and SurfaceView, then...
                mImageButton.setEnabled(true);
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            Log.d(TAG, "********** onResume **********");
        }

        @Override
        public void onPause() {
            super.onPause();
            Log.d(TAG, "********** onPause **********");
        }

        @Override
        public void onStop() {
            super.onStop();
            Log.d(TAG, "********** onStop **********");
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            Log.d(TAG, "********** onDestroy **********");
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG, "********** onClick **********");
            // Toggle torch here
        }

    }

}
