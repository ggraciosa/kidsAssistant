package com.example.graciosa.kidsassistant.fragments;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;

import com.example.graciosa.kidsassistant.Constants;
import com.example.graciosa.kidsassistant.MyAlarmManager;
import com.example.graciosa.kidsassistant.MyLog;
import com.example.graciosa.kidsassistant.MyNotificationManager;
import com.example.graciosa.kidsassistant.MySharedPrefManager;
import com.example.graciosa.kidsassistant.R;
import com.example.graciosa.kidsassistant.receivers.TimeStepReceiver;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;
import static com.example.graciosa.kidsassistant.MySharedPrefManager.SHARED_PREF_SETTINGS_THEME_DARK_VALUE;
import static com.example.graciosa.kidsassistant.MySharedPrefManager.SHARED_PREF_SETTINGS_THEME_LIGHT_VALUE;
import static com.example.graciosa.kidsassistant.MySharedPrefManager.SHARED_PREF_SETTINGS_THEME_SYSTEM_DEFAULT_VALUE;


/**
 *  Handles the Settings menu items
 **/

public class SettingsFragment extends PreferenceFragmentCompat
        implements OnSharedPreferenceChangeListener {

    final String TAG = SettingsFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        MyLog.d(TAG, "onCreate");

        SwitchPreference computePlayTimePref = (SwitchPreference) findPreference("computePlayingTime");
        computePlayTimePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Boolean on = (Boolean) newValue;
                if (!on){
                    // Played time computing turned off by user from settings screen (not from notif).
                    // Clear notifications from the status bar.
                    MyNotificationManager.cancelNotification(getActivity().getApplicationContext());
                }
                return true;
            }
        });
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.settings, rootKey);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        MySharedPrefManager sp = new MySharedPrefManager(getActivity());
        if (key.equals(MySharedPrefManager.SHARED_PREF_SETTINGS_COMPUTE_PLAYING_TIME_KEY)) {
            if (sp.getComputePlayingTime()) {
                // Switched on: set alarm to compute playing times
                MyLog.d(TAG,"onSharedPreferenceChanged: compute playing time pref -> ON");
                MyAlarmManager.enableOrUpdate(getActivity(), Constants.INTERVAL);
                // Post notification of played time status
                Context context = getActivity().getApplicationContext();
                Intent intent = new Intent(context, TimeStepReceiver.class);
                intent.setAction(TimeStepReceiver.COMPUTE_TIME);
                context.sendBroadcast(intent);
            } else {
                // Switched off: cancel pending alarms
                MyLog.d(TAG,"onSharedPreferenceChanged: compute playing time pref -> OFF");
                MyAlarmManager.disable(getActivity());
                // Reset continuous elapsed playing time control in order to do not compute
                // the switched-off time when toggle is later switched-on.
                sp.resetElapsedPlayedTime();
            }
        } else if (key.equals(MySharedPrefManager.SHARED_PREF_SETTINGS_THEME_KEY)) {
            String theme = sp.getTheme();
            int mode;
            switch (theme){
                case SHARED_PREF_SETTINGS_THEME_LIGHT_VALUE:
                    mode = MODE_NIGHT_NO;
                    break;

                case SHARED_PREF_SETTINGS_THEME_DARK_VALUE:
                    mode = MODE_NIGHT_YES;
                    break;

                case SHARED_PREF_SETTINGS_THEME_SYSTEM_DEFAULT_VALUE:
                    mode = MODE_NIGHT_FOLLOW_SYSTEM;
                    break;

                 default:
                    MyLog.e(TAG, "onSharedPreferenceChanged: unknown theme. Set light theme.");
                    mode = MODE_NIGHT_NO;
            }
            // Update App mode.
            AppCompatDelegate.setDefaultNightMode(mode);
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

