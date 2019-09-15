package com.example.graciosa.kidsassistant;

import android.preference.PreferenceManager;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.graciosa.kidsassistant.fragments.HistoryFragment;
import com.example.graciosa.kidsassistant.fragments.PieChartFragment;
import com.example.graciosa.kidsassistant.fragments.SettingsFragment;

import java.util.List;

/*****
 * TODO:
 * 1. done - Replace PreferenceFragment with PreferenceFragmentCompat
 * 2. done - Icons
 * 3. done - Theme
 * 4. Clean: logs, commented code, not used code, not used resources, etc.
 * 5. done - Pie chart (https://jitpack.io/com/github/PhilJay/MPAndroidChart/v3.0.3/javadoc/)
 * 6. Internationalize and localize
 * 7. done - git hub
 * 8. done - store daily played time in database
 * 9. done - Fix orientation change bug
 * 10. Add pause button in notification and a setting to display it or not
 * 11. Pie chart: center text; handle overtime (colors, layout); consider replacing with stacked bar chart.
 * 12. done - handle clock changes e.g. entering and exiting daylight saving time
 * 13. done - execute receiver in a background thread
 * 14. done - Heads up notif does not work in M
 * 15. Display selected values in preference
 * 16. Bug: settings action bar back navigation arrow is lost upon rotation
 * 17. done - Display played time statistics
 * 18. Bug: invalid default shared preference (computePlayingTime=true, maxPlayingTime=120) is brought
 *     when install App in my Z3 Play P multi user; cleaning app data from Global Settings solves the
 *     issue; issue does not happen in Z2 Play N single user neither G2 M single user.
 * 19. done - Set new data and reset time played upon midnight, even if device is no active.
 * 20. done - Replace Fragment class deprecated in API level 28
 * 21. done - Make bar value color follow bar color.
 * 22. done - Set chart legend to start and end date of that chart.
 * 23. Add action to bar touch e.g. display data, playedTime and limit?
 * 24. done - Display an explanation message in history when there is no data to be displayed.
 * 25. done - Set history chart yAxis minimum value to 0.
 * 26. Support light/dark/follow-system modes.
 * 27. done - In notification, replace bottom "Kids Assistant" string with progress e.g. 23/60.
 * 28. Update database and notification when daily limit is changed in settings.
 * 29. done - Update pie chart dynamically upon change via TimeStepReceiver.
 * 30. done - Make notification icon colored (https://stackoverflow.com/questions/45874742/android-color-notification-icon).
 * 31. Remove icons from barChart legend.
 * 32. done - Update history charts dynamically via LiveData upon change via TimeStepReceiver.
 * 33. done - Always have 12 bars per chart in history.
 * 34. done - Replace 0-n numbers with played day under each history bar chart xAxis.
 * 35. Use navigation for fragments management.
 * 36. Use stacked bars to have in bars an indication of allowed and over played time.
 * 37. done - Start computing time and launch 1st notification when feature is turned on.
 * 38. Security: broadcasts permissions, etc.
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
    final String TAG_FRAG_HISTORY = "frag_history";

    /***************
     *** FIELDS ***
     ***************/

    private boolean mShowOptionMenu;
    private FragmentManager mFragMgr;
    private SettingsFragment mSettingsFragment;
    private PieChartFragment mPieChartFragment;
    private HistoryFragment mHistoryFragment;

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
        mHistoryFragment = new HistoryFragment();
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
                case TAG_FRAG_HISTORY:
                    addFragHistory();
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

    private void addFragHistory(){
        mFragMgr.beginTransaction()
                .add(R.id.main_activity_container, mHistoryFragment, TAG_FRAG_HISTORY)
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

            case R.id.history_id:
                MyLog.d(TAG, "History menu item selected");
                // Display the fragment as the main content.
                mFragMgr.beginTransaction()
                        .replace(R.id.main_activity_container, mHistoryFragment, TAG_FRAG_HISTORY)
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
