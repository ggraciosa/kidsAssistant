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
 * 01. DONE - Replace PreferenceFragment with PreferenceFragmentCompat
 * 02. DONE - Icons
 * 03. done - Theme
 * 04. Clean: logs, commented code, not used code, not used resources, etc.
 * 05. DONE - Pie chart (https://jitpack.io/com/github/PhilJay/MPAndroidChart/v3.0.3/javadoc/)
 * 06. Internationalize and localize
 * 07. DONE - git hub
 * 08. DONE - store daily played time in database
 * 09. DONE - Fix orientation change bug
 * 10. DONE - Add pause/resume actions in notification
 * 11. Pie chart: replace with stacked bar chart.
 * 12. DONE - handle clock changes e.g. entering and exiting daylight saving time
 * 13. DONE - execute receiver in a background thread
 * 14. DONE - Heads up notif does not work in M
 * 15. Display selected values in preference
 * 16. Bug: settings action bar back navigation arrow is lost upon rotation
 * 17. DONE - Display played time statistics
 * 18. Bug: invalid default shared preference (computePlayingTime=true, maxPlayingTime=120) is brought
 *     when install App in my Z3 Play P multi user; cleaning app data from Global Settings solves the
 *     issue; issue does not happen in Z2 Play N single user neither G2 M single user.
 * 19. DONE - Set new data and reset time played upon midnight, even if device is no active.
 * 20. DONE - Replace Fragment class deprecated in API level 28
 * 21. DONE - Make bar value color follow bar color.
 * 22. DONE - Set chart legend to start and end date of that chart.
 * 23. Add action to bar touch e.g. display data, playedTime and limit?
 * 24. DONE - Display an explanation message in history when there is no data to be displayed.
 * 25. DONE - Set history chart yAxis minimum value to 0.
 * 26. Support light/dark/follow-system modes.
 * 27. DONE - In notification, replace bottom "Kids Assistant" string with progress e.g. 23/60.
 * 28. Update database and notification when daily limit is changed in settings.
 * 29. DONE - Update pie chart dynamically upon change via TimeStepReceiver.
 * 30. DONE - Make notification icon colored (https://stackoverflow.com/questions/45874742/android-color-notification-icon).
 * 31. Remove icons from barChart legend.
 * 32. DONE - Update history charts dynamically via LiveData upon change via TimeStepReceiver.
 * 33. DONE - Always have 12 bars per chart in history.
 * 34. DONE - Replace 0-n numbers with played day under each history bar chart xAxis.
 * 35. Use navigation for fragments management.
 * 36. Use stacked bars in history to have in bars an indication of allowed and over played time.
 * 37. DONE - Start computing time and launch 1st notification when feature is turned on.
 * 38. Security: broadcasts permissions, export, etc.
 * 39. Review typography adherence to material design.
 * 40. Replace expanded notification action with collapsed notification action (pause/play buttons)
 * 41. Create a settings to enable/disable notification actions buttons.
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
