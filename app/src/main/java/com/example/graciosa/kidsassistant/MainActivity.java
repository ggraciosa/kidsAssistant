package com.example.graciosa.kidsassistant;

import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
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
 * 8. history (provider)
 * 9. done - Fix orientation change bug
 * 10. Pause button in notification?
 * 11. Pie chart: center text; handle overtime (colors, layout); change notification overtime colors to match it
 * 12. done - handle clock changes e.g. entering and exiting daylight saving time
 * 13. execute receive in a background thread
 * 14. done - Heads up notif does not work in M
 * 15. display selected values in preference
 * 16. Settings action bar back navigation arrow is lost upon rotation
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

        // init saved state independent data
        mFragMgr = getSupportFragmentManager();
        mPieChartFragment = new PieChartFragment();
        mSettingsFragment = new SettingsFragment();
        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);

        if (savedInstanceState == null) {
            // app being started with no previous state
            // init data
            mShowOptionMenu = true;
            // init UI
            addFragPieChart();
        } else {
            // system is restarting app for instance due to orientation change and will resume previous state
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
                // handles action bar back button
                mFragMgr.beginTransaction().remove(mSettingsFragment).commit();
                // Remove back button from action bar
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                // Trigger redisplay of options menu
                mShowOptionMenu = true;
                invalidateOptionsMenu();
                // redisplay pie chart
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
