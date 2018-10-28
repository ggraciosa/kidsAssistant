package com.example.graciosa.kidsassistant;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.example.graciosa.kidsassistant.Constants;

import static com.example.graciosa.kidsassistant.Constants.INTERVAL;
import static com.example.graciosa.kidsassistant.MySharedPrefManager.SHARED_PREF_SETTINGS_COMPUTE_PLAYING_TIME_KEY;
import static com.example.graciosa.kidsassistant.MySharedPrefManager.SHARED_PREF_SETTINGS_MAX_PLAYING_TIME_KEY;


/**
 *  Handles the Settings menu items
 **/

public class SettingsFragment extends PreferenceFragment
        implements OnSharedPreferenceChangeListener {

    /*****************
     *** CONSTANTS ***
     *****************/

    /**************
     *** FIELDS ***
     **************/

    /***************
     *** METHODS ***
     ***************/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        MySharedPrefManager sp = new MySharedPrefManager(getActivity());
        if (key.equals(SHARED_PREF_SETTINGS_COMPUTE_PLAYING_TIME_KEY)) {
            if (sp.isComputingPlayingTime()) {
                // Switched on: set alarm to compute playing times
                MyAlarmManager.enableOrUpdate(getActivity(), INTERVAL);
            } else {
                // Switched off: cancel pending alarms
                MyAlarmManager.disable(getActivity());
                // Reset continuous elapsed playing time control in order to do not compute
                // the switched-off time when toggle is later switched-on.
                sp.resetElapsedPlayingTime();
            }
        } else if (key.equals(SHARED_PREF_SETTINGS_MAX_PLAYING_TIME_KEY)) {
            // no action needed here
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}

