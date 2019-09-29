package com.example.graciosa.kidsassistant;

import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/*****
 * TODO:
 * 01. DONE - Replace PreferenceFragment with PreferenceFragmentCompat
 * 02. DONE - Icons
 * 03. DONE - Theme
 * 04. DONE - Pie chart (https://jitpack.io/com/github/PhilJay/MPAndroidChart/v3.0.3/javadoc/)
 * 05. DONE - git hub
 * 06. DONE - store daily played time in database
 * 07. DONE - Fix orientation change bug
 * 08. DONE - Add pause/resume actions in notification
 * 09. DONE - Display overtime in pie
 * 10. DONE - handle clock changes e.g. entering and exiting daylight saving time
 * 11. DONE - execute receiver in a background thread
 * 12. DONE - Heads up notif does not work in M
 * 13. DONE - Bug: settings action bar back navigation arrow is lost upon rotation
 * 14. DONE - Display played time statistics
 * 15. DONE - Set new data and reset time played upon midnight, even if device is no active.
 * 26. DONE - Replace Fragment class deprecated in API level 28
 * 17. DONE - Make bar value color follow bar color.
 * 18. DONE - Set chart legend to start and end date of that chart.
 * 19. DONE - Display an explanation message in history when there is no data to be displayed.
 * 20. DONE - Set history chart yAxis minimum value to 0.
 * 21. DONE - In notification, replace bottom "Kids Assistant" string with progress e.g. 23/60.
 * 22. DONE - Update pie chart dynamically upon change via TimeStepReceiver.
 * 23. DONE - Make notification icon colored (https://stackoverflow.com/questions/45874742/android-color-notification-icon).
 * 24. DROP - Remove icons from barChart legend.
 * 25. DONE - Update history charts dynamically via LiveData upon change via TimeStepReceiver.
 * 26. DONE - Always have 12 bars per chart in history.
 * 27. DONE - Replace 0-n numbers with played day under each history bar chart xAxis.
 * 28. DONE - Use navigation for fragments management (https://codelabs.developers.google.com).
 * 29. DONE - Start computing time and launch 1st notification when feature is turned on.
 * 30. Use stacked bars in history to have in bars an indication of allowed and over played time.
 * 31. Security: broadcasts permissions, export, etc.
 * 32. Review typography adherence to material design (https://codelabs.developers.google.com).
 * 33. Replace expanded notification action with collapsed notification action (pause/play buttons)?
 * 34. Create a settings to enable/disable notification actions buttons?
 * 35. Internationalize and localize
 * 36. Clean: logs, commented code, not used code, not used resources, etc.
 * 37. Display selected values in preference
 * 38. Bug: invalid default shared preference (computePlayingTime=true, maxPlayingTime=120) is brought
 *     when install App in my Z3 Play P multi user; cleaning app data from Global Settings solves the
 *     issue; issue does not happen in Z2 Play N single user neither G2 M single user.
 * 39. Add action to bar touch e.g. display data, playedTime and limit?
 * 40. Support light/dark/follow-system modes (https://developer.android.com/guide/topics/ui/look-and-feel/darktheme).
 * 41. Update database and notification when daily limit is changed in settings.
 */

public class MainActivity extends AppCompatActivity {

    /*****************
     *** CONSTANTS ***
     *****************/

    final String TAG = MainActivity.class.getSimpleName();
    final String INST_DATA_KEY_SHOW_OPTIONS_MENU = "key_show_options_menu";
    final String INST_DATA_KEY_FRAG = "key_frag";

    /***************
     *** FIELDS ***
     ***************/

    private boolean mShowOptionMenu;
    private NavController mNavController;

    /***************
     *** METHODS ***
     ***************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MyLog.d(TAG, "onCreate");

        // Set preference default values
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);

        // Override default preference play time limit with today's day of the week play time limit,
        // but only if it is the first time in the day, in order to do avoid override a user choice
        MySharedPrefManager sp = new MySharedPrefManager(getApplicationContext());
        sp.setWeekdayPlayTimeLimitOnce();

        setContentView(R.layout.activity_main);

        mNavController = Navigation.findNavController(this, R.id.nav_host_fragment);
        mNavController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(NavController controller,
                                             NavDestination destination,
                                             Bundle arguments) {
                // Navigation is its way to move to a new destination (fragment).
                if(destination.getId() == R.id.pieChartFragment) {
                    // Triggers that fall here:
                    // 1. App creation with not previous state
                    // 2. App re-creation with previous state e.g. due to orientation change.
                    // 3. Pressing back in the bottom navigation bar when in the other fragments.
                    // OBS: Pressing back in the action bar does not fall here. See onOptionsItemSelected.
                    MyLog.d(TAG, "addOnDestinationChangedListener: dest = pie chart");
                    mShowOptionMenu = true;
                } else {
                    MyLog.d(TAG, "addOnDestinationChangedListener: dest = settings or hist");
                    mShowOptionMenu = false;
                }
                // Trigger redisplay of options menu
                invalidateOptionsMenu();
            }
        });

        // Request NavigationUI to manage the action bar
        NavigationUI.setupActionBarWithNavController(this, mNavController);

        // Handle saved state independent data
        if (savedInstanceState == null) {
            MyLog.d(TAG,"OnCreate: savedInstanceState == null");
            // App being started with no previous state
            mShowOptionMenu = true;
        } else {
            // System restarted app e.g. due to orientation change: resume fragment displayed at restart
            mShowOptionMenu = savedInstanceState.getBoolean(INST_DATA_KEY_SHOW_OPTIONS_MENU);
            String frag = savedInstanceState.getString(INST_DATA_KEY_FRAG);
            // Decide which fragment to navigate to
            if (frag.equals(getResources().getString(R.string.pie_chart_frag_label))){
                // Nothing to do since Navigation controller will display PieChart as the start/home navigation fragment
            } else if (frag.equals(getResources().getString(R.string.settings_frag_label))){
                mNavController.navigate(R.id.action_pieChartFragment_to_settingsFragment);
            } else if (frag.equals(getResources().getString(R.string.history_frag_label))){
                mNavController.navigate(R.id.action_pieChartFragment_to_historyFragment);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        outState.putBoolean(INST_DATA_KEY_SHOW_OPTIONS_MENU, mShowOptionMenu);
        outState.putString(INST_DATA_KEY_FRAG, getVisibleFrag());
        MyLog.d(TAG, "onSaveInstanceState = " + getVisibleFrag());
    }

    private String getVisibleFrag(){
        return mNavController.getCurrentDestination().getLabel().toString();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyLog.d(TAG, "onResume");
        //TODO: may unnecessary after navigation
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
                MyLog.d(TAG, "onOptionsItemSelected: Action bar back button selected");
                // Action bar back button pressed
                // Navigation controller will display PieChart as the start/home navigation fragment
                // TODO: there must be a way for Navigation to pop the stack itself.
                mNavController.popBackStack();
                return true;

            case R.id.settings_id:
                MyLog.d(TAG, "onOptionsItemSelected: Settings menu item selected");
                mNavController.navigate(R.id.action_pieChartFragment_to_settingsFragment);
                return true;

            case R.id.history_id:
                MyLog.d(TAG, "onOptionsItemSelected: History menu item selected");
                mNavController.navigate(R.id.action_pieChartFragment_to_historyFragment);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
