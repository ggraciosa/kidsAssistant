package com.example.graciosa.kidsassistant;

import android.preference.PreferenceManager;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.List;

/*****
 * TODO:
 * 1. done - Replace PreferenceFragment with PreferenceFragmentCompat
 * 2. done - Icons
 * 3. done - Theme
 * 4. Clean: logs, commented code, not used code, not used resources, etc.
 * 5. done - Pie chart (https://jitpack.io/com/github/PhilJay/MPAndroidChart/v3.0.3/javadoc/)
 * 6. internationalize and localize
 * 7. done - git hub
 * 8. done - store daily played time in database
 * 9. done - Fix orientation change bug
 * 10. Pause button in notification?
 * 11. Pie chart: center text; handle overtime (colors, layout); change notification overtime colors to match it
 * 12. done - handle clock changes e.g. entering and exiting daylight saving time
 * 13. done - execute receiver in a background thread
 * 14. done - Heads up notif does not work in M
 * 15. display selected values in preference
 * 16. Settings action bar back navigation arrow is lost upon rotation
 * 17. Display played time statistics
 * 18. Bug: invalid default shared preference (computePlayingTime=true, maxPlayingTime=120) is brought
 *     when install App in my Z3 Play P multi user; cleaning app data from Global Settings solves the
 *     issue; issue does not happen in Z2 Play N single user neither G2 M single user.
 */

public class MainActivity extends AppCompatActivity {

    /*****************
     *** CONSTANTS ***
     *****************/

    final String TAG = MainActivity.class.getSimpleName();
    final String INST_DATA_KEY_SHOW_OPTIONS_MENU = "key_show_options_menu";
    final String INST_DATA_KEY_FRAG = "key_frag";
    final String TAG_FRAG_SETTINGS = "frag_settings";
    final String TAG_FRAG_PIE_CHART = "frag_pie_chart";

    /***************
     *** FIELDS ***
     ***************/

    private boolean mShowOptionMenu;
    private FragmentManager mFragMgr;
    private SettingsFragment mSettingsFragment;
    private PieChartFragment mPieChartFragment;

    /***************
     *** METHODS ***
     ***************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set preference default values
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);

        // Override default preference play time limit with today's day of the week play time limit,
        // but only if it is the first time in the day, in order to do avoid override a user choice
        MySharedPrefManager sp = new MySharedPrefManager(getApplicationContext());
        sp.setWeekdayPlayTimeLimitOnce();

        // Init saved state independent data
        mFragMgr = getSupportFragmentManager();
        mPieChartFragment = new PieChartFragment();
        mSettingsFragment = new SettingsFragment();
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            // App being started with no previous state
            // Init data
            mShowOptionMenu = true;
            // init UI
            addFragPieChart();
        } else {
            // System is restarting app for instance due to orientation change and will resume previous state
            mShowOptionMenu = savedInstanceState.getBoolean(INST_DATA_KEY_SHOW_OPTIONS_MENU);
            String frag = savedInstanceState.getString(INST_DATA_KEY_FRAG);
            switch (frag) {
                case TAG_FRAG_PIE_CHART:
                    addFragPieChart();
                    break;
                case TAG_FRAG_SETTINGS:
                    addFragSettings();
                    break;
            }
        }
    }

    private void addFragPieChart(){
        mFragMgr.beginTransaction()
                .add(R.id.main_activity_container, mPieChartFragment, TAG_FRAG_PIE_CHART)
                .commit();
    }

    private void addFragSettings(){
        mFragMgr.beginTransaction()
                .add(R.id.main_activity_container, mSettingsFragment, TAG_FRAG_SETTINGS)
                .commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        outState.putBoolean(INST_DATA_KEY_SHOW_OPTIONS_MENU, mShowOptionMenu);
        outState.putString(INST_DATA_KEY_FRAG, getVisibleFragmentTag());
    }

    // Gets the name of the fragment currently displayed
    private String getVisibleFragmentTag(){
        List<Fragment> fragments = mFragMgr.getFragments();
        if (fragments != null){
            for (Fragment fragment : fragments){
                if (fragment != null && fragment.isVisible()) {
                    MyLog.d(TAG, fragment.getTag());
                    return fragment.getTag();
                }
            }
        }
        return null;
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        return mShowOptionMenu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle menu item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                // Handles action bar back button
                mFragMgr.beginTransaction().remove(mSettingsFragment).commit();
                // Remove back button from action bar
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                // Trigger redisplay of options menu
                mShowOptionMenu = true;
                invalidateOptionsMenu();
                // Redisplay pie chart
                mFragMgr.beginTransaction()
                        .replace(R.id.main_activity_container, mPieChartFragment, TAG_FRAG_PIE_CHART)
                        .commit();
                return true;

            case R.id.settings_id:
                MyLog.d(TAG, "Settings menu item selected");
                // Display the fragment as the main content.
                mFragMgr.beginTransaction()
                        .replace(R.id.main_activity_container, mSettingsFragment, TAG_FRAG_SETTINGS)
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
