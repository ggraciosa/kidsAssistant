package com.example.graciosa.kidsassistant;

import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

/*****
 * TODO:
 * 1. Replace PreferenceFragment with PreferenceFragmentCompat
 * 2. done - Icons
 * 3. done - Theme
 * 4. Remove logs
 * 5. Pizza chart in main activity
 * 6. internationalize and localize
 * 7. git hub
 * history (provider)
 * 9. define constants in string resources to use same value in .java and .xml
 */

public class MainActivity extends AppCompatActivity {

    final String TAG = MainActivity.class.getSimpleName();

    private SettingsFragment mSettingsFragment;
    private boolean mShowOptionMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mShowOptionMenu = true;
        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mShowOptionMenu = true;
    }

    // Called only during activity creation
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    // invoked by system in response to invalidateOptionsMenu()
    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        return mShowOptionMenu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle menu item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                // handles action bar back button
                getFragmentManager().beginTransaction().remove(mSettingsFragment).commit();
                // Remove back button from action bar
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                // Trigger redisplay of options menu
                mShowOptionMenu = true;
                invalidateOptionsMenu();
                return true;
            case R.id.settings_id:
                MyLog.d(TAG,"Settings menu item selected");
                // Display the fragment as the main content.
                mSettingsFragment = new SettingsFragment();
                getFragmentManager().beginTransaction()
                        .replace(R.id.main_activity_container, mSettingsFragment)
                        .commit();
                // Set back button in action bar
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                // Trigger removal of options menu
                mShowOptionMenu = false;
                invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
