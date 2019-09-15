package com.example.graciosa.kidsassistant.fragments;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceFragmentCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;

import com.example.graciosa.kidsassistant.MyAlarmManager;
import com.example.graciosa.kidsassistant.MyNotificationManager;
import com.example.graciosa.kidsassistant.MySharedPrefManager;
import com.example.graciosa.kidsassistant.R;
import com.example.graciosa.kidsassistant.receivers.TimeStepReceiver;

import static com.example.graciosa.kidsassistant.Constants.INTERVAL;
import static com.example.graciosa.kidsassistant.MySharedPrefManager.SHARED_PREF_SETTINGS_COMPUTE_PLAYING_TIME_KEY;
import static com.example.graciosa.kidsassistant.MySharedPrefManager.SHARED_PREF_SETTINGS_PLAY_TIME_LIMIT_KEY;


/**
 *  Handles the Settings menu items
 **/

public class SettingsFragment extends PreferenceFragmentCompat
        implements OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.settings, rootKey);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        MySharedPrefManager sp = new MySharedPrefManager(getActivity());
        if (key.equals(SHARED_PREF_SETTINGS_COMPUTE_PLAYING_TIME_KEY)) {
            if (sp.isComputingPlayingTime()) {
                // Switched on: set alarm to compute playing times
                MyAlarmManager.enableOrUpdate(getActivity(), INTERVAL);
                // Post notification of played time status
                Context context = getActivity().getApplicationContext();
                Intent intent = new Intent(context, TimeStepReceiver.class);
                context.sendBroadcast(intent);
            } else {
                // Switched off: cancel pending alarms
                MyAlarmManager.disable(getActivity());
                // Reset continuous elapsed playing time control in order to do not compute
                // the switched-off time when toggle is later switched-on.
                sp.resetElapsedPlayedTime();
                // Clear notifications from the status bar
                MyNotificationManager.cancelNotification(getActivity().getApplicationContext());
            }
        } else if (key.equals(SHARED_PREF_SETTINGS_PLAY_TIME_LIMIT_KEY)) {
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

