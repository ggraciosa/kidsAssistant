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
 * 4. Clean: logs, commented code, not used code, not used resources, etc.
 * 5. done - Pie chart (https://jitpack.io/com/github/PhilJay/MPAndroidChart/v3.0.3/javadoc/)
 * 6. internationalize and localize
 * 7. done - git hub
 * 8. history (provider)
 * 9. Fix orientation change bug
 * 10. Pause button in notification?
 * 11. Pie chart: center text; handle overtime (colors, layout); change notification overtime colors to match it
 * 12. done: handle clock changes e.g. entering and exiting daylight saving time
 * 13. execute receive in a background thread
 * 14. done - Heads up notif does not work in M
 * 15. display selected values in preference
 */

public class MainActivity extends AppCompatActivity {

    final String TAG = MainActivity.class.getSimpleName();

    private SettingsFragment mSettingsFragment;
    private PieChartFragment mPieChartFragment;
    private boolean mShowOptionMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // init data
        mShowOptionMenu = true;
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);

        // init UI
        setContentView(R.layout.activity_main);
        mPieChartFragment = new PieChartFragment();
        getFragmentManager().beginTransaction()
                .add(R.id.main_activity_container, mPieChartFragment)
                .commit();
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

    // Invoked by system in response to invalidateOptionsMenu()
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
                // redisplay pie chart
                getFragmentManager().beginTransaction()
                        .replace(R.id.main_activity_container, mPieChartFragment)
                        .commit();
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
