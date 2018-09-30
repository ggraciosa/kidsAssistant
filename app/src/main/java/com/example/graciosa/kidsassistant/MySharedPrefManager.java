package com.example.graciosa.kidsassistant;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.example.graciosa.kidsassistant.Constants;

import static com.example.graciosa.kidsassistant.Constants.DEFAULT_PROGRESS_THRESHOLD_PERCENTAGE;

/**
 *  Manages all shared preferences making int transparent to other classes which shared preference
 *  contains each key.
 **/

public class MySharedPrefManager {

    /*****************
     *** CONSTANTS ***
     *****************/

    public static final String TAG = MySharedPrefManager.class.getSimpleName();

    // SHARED PREFERENCE: DEFAULT (SETTINGS)
    // Default (settings) shared preferences constants
    // Settings preference switch to enable/disable playing time computation
    public static final String SHARED_PREF_SETTINGS_COMPUTE_PLAYING_TIME_KEY = "computePlayingTime";
    public static final boolean SHARED_PREF_SETTINGS_COMPUTE_PLAYING_TIME_DEFAULT_VALUE = true;
    // Max allowed playing time in current day in minutes
    public static final String SHARED_PREF_SETTINGS_MAX_PLAYING_TIME_KEY = "maxPlayingTime";
    public static final String SHARED_PREF_SETTINGS_MAX_PLAYING_TIME_DEFAULT_VALUE = "120";

    // SHARED PREFERENCE: AUXILIAR
    // Auxiliar shared preference file name
    public static final String SHARED_PREF_FILENAME = "auxiliarSharedPref";
    // Auxiliar data to calculate daily usage (elapsed playing time)
    public static final String SHARED_PREF_LAST_ELAPSED_TIME_KEY = "elapsedTime";
    // Current day
    public static final String SHARED_PREF_PLAYING_DATE_KEY = "playingDate";
    // Total time kids have played in current day
    public static final String SHARED_PREF_PLAYED_TIME_KEY = "playedTime";
    // Playing time percentage (0 - 100%) to promote notification from default to high (heads up)
    public static final String SHARED_PREF_PROGRESS_THRESHOLD_PERCENTAGE_KEY = "progressThreshold";

    /**************
     *** FIELDS ***
     **************/

    private SharedPreferences mAuxSharedPref;
    private SharedPreferences mSettingsSharedPref;

    /***************
     *** METHODS ***
     ***************/

    public MySharedPrefManager(Context context){

        // Make sure default shared preferences is initialized
        PreferenceManager.setDefaultValues(context, R.xml.settings, false);
        mSettingsSharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        // Get non default shared preference
        mAuxSharedPref = context.getSharedPreferences(SHARED_PREF_FILENAME, Context.MODE_PRIVATE);
        // Initialize playing date if needed
        String lastPlayingDate = mAuxSharedPref.getString(SHARED_PREF_PLAYING_DATE_KEY, "EMPTY");
        if (lastPlayingDate.equals("EMPTY")){
            Editor editor = mAuxSharedPref.edit();
            editor.putString(SHARED_PREF_PLAYING_DATE_KEY, getCurrentDate());
            editor.commit();
        }
    }

    public void updatePlayingTime() {

        long elapsedTime = SystemClock.elapsedRealtime();
        long previousElapsedTime = mAuxSharedPref.getLong(SHARED_PREF_LAST_ELAPSED_TIME_KEY, 0);
        long previousPlayingTime = mAuxSharedPref.getLong(SHARED_PREF_PLAYED_TIME_KEY, 0);

        if (previousElapsedTime == 0) {
            // First time, no previous data.
            previousElapsedTime = elapsedTime;
        }

        // Compute total elapsed time
        long playingTime = elapsedTime - previousElapsedTime;

        Editor editor = mAuxSharedPref.edit();
        editor.putLong(SHARED_PREF_LAST_ELAPSED_TIME_KEY, elapsedTime);
        editor.putLong(SHARED_PREF_PLAYED_TIME_KEY, previousPlayingTime + playingTime);
        editor.commit();
    }

    public void resetElapsedPlayingTime(){
        Editor editor = mAuxSharedPref.edit();
        editor.putLong(SHARED_PREF_LAST_ELAPSED_TIME_KEY, 0);
        editor.commit();
    }

    public void resetPlayedTime(){
        Editor editor = mAuxSharedPref.edit();
        editor.putLong(SHARED_PREF_PLAYED_TIME_KEY, 0);
        editor.commit();
    }

    public long getPlayedTimeInMinutes() {
        long playingTime = mAuxSharedPref.getLong(SHARED_PREF_PLAYED_TIME_KEY, 0);
        return TimeUnit.MILLISECONDS.toMinutes(playingTime);
    }

    public long getMaxPlayingTimeInMinutes() {
        long pt = Long.parseLong(mSettingsSharedPref.getString(SHARED_PREF_SETTINGS_MAX_PLAYING_TIME_KEY,
                SHARED_PREF_SETTINGS_MAX_PLAYING_TIME_DEFAULT_VALUE));
        MyLog.d(TAG, "getMaxPlayingTimeInMinutes: " + String.valueOf(pt));
        return pt;
    }

    public boolean isComputingPlayingTime() {
        return mSettingsSharedPref.getBoolean(
                SHARED_PREF_SETTINGS_COMPUTE_PLAYING_TIME_KEY,
                SHARED_PREF_SETTINGS_COMPUTE_PLAYING_TIME_DEFAULT_VALUE);
    }

    public String getLastPlayingDate(){
        String playingDate = mAuxSharedPref.getString(SHARED_PREF_PLAYING_DATE_KEY, getCurrentDate());
        return playingDate;
    }

    public boolean updatePlayingDateIfNeeded(){

        String lastDate = getLastPlayingDate();
        String currentDate = getCurrentDate();

        MyLog.d(TAG, "updatePlayingDateIfNeeded: lastDate = " + lastDate);
        MyLog.d(TAG, "updatePlayingDateIfNeeded: currentDate = " + currentDate);

        if (!currentDate.equals(lastDate)){
            // New day: update
            Editor editor = mAuxSharedPref.edit();
            editor.putString(SHARED_PREF_PLAYING_DATE_KEY, currentDate);
            editor.commit();
            MyLog.d(TAG, "updatePlayingDateIfNeeded: return true");
            return true;
        } else {
            // Same day: no need to change
            MyLog.d(TAG, "updatePlayingDateIfNeeded: return false");
            return false;
        }
    }

    public void setProgressThresholdInMinutes(int progressThreshold){
        Editor editor = mAuxSharedPref.edit();
        editor.putLong(SHARED_PREF_PROGRESS_THRESHOLD_PERCENTAGE_KEY, progressThreshold);
        editor.commit();
    }

    public int getProgressThresholdInMinutes(){
        int progresThreshold = mAuxSharedPref.getInt(SHARED_PREF_PROGRESS_THRESHOLD_PERCENTAGE_KEY,
                        DEFAULT_PROGRESS_THRESHOLD_PERCENTAGE);
        return progresThreshold;
    }

    private String getCurrentDate(){
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String currentDate = df.format(c);
        return currentDate;
    }

}
