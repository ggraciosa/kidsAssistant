package com.example.graciosa.kidsassistant;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import java.text.SimpleDateFormat;import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;


import static com.example.graciosa.kidsassistant.Constants.DEFAULT_PROGRESS_THRESHOLD_PERCENTAGE;
import static com.example.graciosa.kidsassistant.Constants.INTERVAL;

/**
 *  Manages all shared preferences making it transparent to other classes which shared preference
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
    public static final String SHARED_PREF_SETTINGS_MAX_PLAYING_TIME_SUNDAY_VALUE = "90";
    public static final String SHARED_PREF_SETTINGS_MAX_PLAYING_TIME_MONDAY_VALUE = "60";
    public static final String SHARED_PREF_SETTINGS_MAX_PLAYING_TIME_TUESDAY_VALUE = "60";
    public static final String SHARED_PREF_SETTINGS_MAX_PLAYING_TIME_WEDNESDAY_VALUE = "60";
    public static final String SHARED_PREF_SETTINGS_MAX_PLAYING_TIME_THURSDAY_VALUE = "60";
    public static final String SHARED_PREF_SETTINGS_MAX_PLAYING_TIME_FRIDAY_VALUE = "90";
    public static final String SHARED_PREF_SETTINGS_MAX_PLAYING_TIME_SATURDAY_VALUE = "120";

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
        if ((playingTime < 0) // clock changed backwards e.g. exiting daylight saving time
            || (playingTime > 5 * INTERVAL)) // // clock changed fwd e.g. entering summer time
        {
            MyLog.d(TAG, "updatePlayingTime: clock changed, taking fixed interaval as time step");
            playingTime = INTERVAL;
        }

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
                getWeekdayMaxPlayingTime()));
        MyLog.d(TAG, "getMaxPlayingTimeInMinutes: " + String.valueOf(pt));
        return pt;
    }

    public boolean isComputingPlayingTime() {
        return mSettingsSharedPref.getBoolean(
                SHARED_PREF_SETTINGS_COMPUTE_PLAYING_TIME_KEY,
                SHARED_PREF_SETTINGS_COMPUTE_PLAYING_TIME_DEFAULT_VALUE);
    }

    // true if a new day
    public boolean hasDateChanged(){
        String lastDate = mAuxSharedPref.getString(SHARED_PREF_PLAYING_DATE_KEY, getCurrentDate());
        String currentDate = getCurrentDate();
        return !currentDate.equals(lastDate);
    }

    // Set today as playing date
    public void setPlayingDate(){
        String currentDate = getCurrentDate();
        Editor editor = mAuxSharedPref.edit();
        editor.putString(SHARED_PREF_PLAYING_DATE_KEY, currentDate);
        editor.commit();
        MyLog.d(TAG, "setPlayingDate: " + currentDate);
    }

    // Set max playing time for today given which day of the week is today
    public void setWeekdayMaxPlayingTime(){
        String weekdayMaxPlayingTime = getWeekdayMaxPlayingTime();
        Editor ed = mSettingsSharedPref.edit();
        ed.putString(SHARED_PREF_SETTINGS_MAX_PLAYING_TIME_KEY, weekdayMaxPlayingTime);
        ed.commit();
        MyLog.d(TAG, "setWeekdayMaxPlayingTime: weekday=" + weekdayMaxPlayingTime +
                ", maxPlayTime=" + weekdayMaxPlayingTime);
    }

    private String getWeekdayMaxPlayingTime(){

        Calendar calendar = Calendar.getInstance();
        int weekday = calendar.get(Calendar.DAY_OF_WEEK);
        String weekdayMaxPlayingTime = "";
        switch (weekday){
            case 1:
                weekdayMaxPlayingTime = SHARED_PREF_SETTINGS_MAX_PLAYING_TIME_SUNDAY_VALUE;
                break;
            case 2:
                weekdayMaxPlayingTime = SHARED_PREF_SETTINGS_MAX_PLAYING_TIME_MONDAY_VALUE;
                break;
            case 3:
                weekdayMaxPlayingTime = SHARED_PREF_SETTINGS_MAX_PLAYING_TIME_TUESDAY_VALUE;
                break;
            case 4:
                weekdayMaxPlayingTime = SHARED_PREF_SETTINGS_MAX_PLAYING_TIME_WEDNESDAY_VALUE;
                break;
            case 5:
                weekdayMaxPlayingTime = SHARED_PREF_SETTINGS_MAX_PLAYING_TIME_THURSDAY_VALUE;
                break;
            case 6:
                weekdayMaxPlayingTime = SHARED_PREF_SETTINGS_MAX_PLAYING_TIME_FRIDAY_VALUE;
                break;
            case 7:
                weekdayMaxPlayingTime = SHARED_PREF_SETTINGS_MAX_PLAYING_TIME_SATURDAY_VALUE;
                break;
        }
        return weekdayMaxPlayingTime;
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
