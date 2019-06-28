package com.example.graciosa.kidsassistant;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import java.util.Calendar;
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

    // SHARED PREFERENCE: SettingsSharedPref (default settings)
    // Default (settings) shared preferences constants
    // Settings preference switch to enable/disable playing time computation
    public static final String SHARED_PREF_SETTINGS_COMPUTE_PLAYING_TIME_KEY = "computePlayingTime";
    public static final boolean SHARED_PREF_SETTINGS_COMPUTE_PLAYING_TIME_DEFAULT_VALUE = true;
    // Max allowed playing time in current day in minutes
    public static final String SHARED_PREF_SETTINGS_PLAY_TIME_LIMIT_KEY = "playTimeLimit";
    public static final String SHARED_PREF_SETTINGS_PLAY_TIME_LIMIT_SUNDAY_VALUE = "90";
    public static final String SHARED_PREF_SETTINGS_PLAY_TIME_LIMIT_MONDAY_VALUE = "60";
    public static final String SHARED_PREF_SETTINGS_PLAY_TIME_LIMIT_TUESDAY_VALUE = "60";
    public static final String SHARED_PREF_SETTINGS_PLAY_TIME_LIMIT_WEDNESDAY_VALUE = "60";
    public static final String SHARED_PREF_SETTINGS_PLAY_TIME_LIMIT_THURSDAY_VALUE = "60";
    public static final String SHARED_PREF_SETTINGS_PLAY_TIME_LIMIT_FRIDAY_VALUE = "60";
    public static final String SHARED_PREF_SETTINGS_PLAY_TIME_LIMIT_SATURDAY_VALUE = "90";

    // SHARED PREFERENCE: PlayedTimeSharedPref (played time and associated data)
    // File name
    public static final String SHARED_PREF_PLAYED_FILENAME = "auxiliarSharedPref";
    // Auxiliar data to calculate daily usage (elapsed playing time)
    public static final String SHARED_PREF_PLAYED_LAST_ELAPSED_TIME_KEY = "elapsedTime";
    // Current day
    public static final String SHARED_PREF_PLAYED_PLAYING_DATE_KEY = "playingDate";
    // Total time kids have played in current day
    public static final String SHARED_PREF_PLAYED_TIME_KEY = "playedTime";
    // Playing time percentage (0 - 100%) to promote notification from default to high (heads up)
    public static final String SHARED_PREF_PLAYED_PROGRESS_THRESHOLD_PERCENTAGE_KEY = "progressThreshold";
    // Date to control weekday default play time updates
    public static final String SHARED_PREF_PLAYED_WEEKDAY_UPDATE_CONTROL_DATE = "weekdayControlDate";

    /**************
     *** FIELDS ***
     **************/

    private SharedPreferences mPlayedSharedPref;
    private SharedPreferences mSettingsSharedPref;

    /***************
     *** METHODS ***
     ***************/

    public SharedPreferences getSettingsSharedPref(){
        return mSettingsSharedPref;
    }

    public SharedPreferences getPlayedSharedPref(){
        return mPlayedSharedPref;
    }

    public MySharedPrefManager(Context context){

        // Make sure default shared preferences is initialized
        PreferenceManager.setDefaultValues(context, R.xml.settings, false);
        mSettingsSharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        // Get non default shared preference
        mPlayedSharedPref = context.getSharedPreferences(SHARED_PREF_PLAYED_FILENAME, Context.MODE_PRIVATE);
        // Initialize playing date if needed
        String lastPlayingDate = mPlayedSharedPref.getString(SHARED_PREF_PLAYED_PLAYING_DATE_KEY, "EMPTY");
        if (lastPlayingDate.equals("EMPTY")){
            Editor editor = mPlayedSharedPref.edit();
            editor.putString(SHARED_PREF_PLAYED_PLAYING_DATE_KEY, Utils.getCurrentDate());
            editor.commit();
        }
    }

    public void updatePlayedTime() {

        long elapsedTime = SystemClock.elapsedRealtime();
        long previousElapsedTime = mPlayedSharedPref.getLong(SHARED_PREF_PLAYED_LAST_ELAPSED_TIME_KEY, 0);
        long previousPlayingTime = mPlayedSharedPref.getLong(SHARED_PREF_PLAYED_TIME_KEY, 0);

        if (previousElapsedTime == 0) {
            // First time, no previous data.
            previousElapsedTime = elapsedTime;
        }

        // Compute total elapsed time
        long playingTime = elapsedTime - previousElapsedTime;
        if ((playingTime < 0) // clock changed backwards e.g. exiting daylight saving time
            || (playingTime > 5 * INTERVAL)) // clock changed fwd e.g. entering summer time
        {
            MyLog.d(TAG, "updatePlayingTime: clock changed, taking fixed interaval as time step");
            playingTime = INTERVAL;
        }

        Editor editor = mPlayedSharedPref.edit();
        editor.putLong(SHARED_PREF_PLAYED_LAST_ELAPSED_TIME_KEY, elapsedTime);
        editor.putLong(SHARED_PREF_PLAYED_TIME_KEY, previousPlayingTime + playingTime);
        editor.commit();
    }

    public void resetElapsedPlayedTime(){
        Editor editor = mPlayedSharedPref.edit();
        editor.putLong(SHARED_PREF_PLAYED_LAST_ELAPSED_TIME_KEY, 0);
        editor.commit();
    }

    public void resetPlayedTime(){
        Editor editor = mPlayedSharedPref.edit();
        editor.putLong(SHARED_PREF_PLAYED_TIME_KEY, 0);
        editor.commit();
    }

    public long getPlayedTimeInMinutes() {
        long playingTime = mPlayedSharedPref.getLong(SHARED_PREF_PLAYED_TIME_KEY, 0);
        return TimeUnit.MILLISECONDS.toMinutes(playingTime);
    }

    public long getPlayTimeLimitInMinutes() {
        long pt = Long.parseLong(mSettingsSharedPref.getString(SHARED_PREF_SETTINGS_PLAY_TIME_LIMIT_KEY,
                getWeekdayPlayTimeLimit()));
        MyLog.d(TAG, "getPlayTimeLimitInMinutes: " + String.valueOf(pt));
        return pt;
    }

    public boolean isComputingPlayingTime() {
        return mSettingsSharedPref.getBoolean(
                SHARED_PREF_SETTINGS_COMPUTE_PLAYING_TIME_KEY,
                SHARED_PREF_SETTINGS_COMPUTE_PLAYING_TIME_DEFAULT_VALUE);
    }

    // true if a new day
    public boolean hasDateChanged(){
        String lastDate = mPlayedSharedPref.getString(SHARED_PREF_PLAYED_PLAYING_DATE_KEY, "EMPTY");
        String currentDate = Utils.getCurrentDate();
        return !currentDate.equals(lastDate);
    }

    // Set today as playing date
    public void setPlayingDate(){
        String currentDate = Utils.getCurrentDate();
        Editor editor = mPlayedSharedPref.edit();
        editor.putString(SHARED_PREF_PLAYED_PLAYING_DATE_KEY, currentDate);
        editor.commit();
        MyLog.d(TAG, "setPlayingDate: " + currentDate);
    }

    // Set play time limit for today given which day of the week is today.
    // Do it only once a day because if user overrode default in settings, user choice should hold.
    public void setWeekdayPlayTimeLimitOnce(){

        String previousDate = mPlayedSharedPref.getString(SHARED_PREF_PLAYED_WEEKDAY_UPDATE_CONTROL_DATE, "EMPTY");
        String currentDate = Utils.getCurrentDate();
        boolean firstCall = !currentDate.equals(previousDate);
        if (firstCall){
            // First time this method is called today
            // Update control date
            Editor edPlayed = mPlayedSharedPref.edit();
            edPlayed.putString(SHARED_PREF_PLAYED_WEEKDAY_UPDATE_CONTROL_DATE, currentDate);
            edPlayed.commit();
            // Update playtime limit given today's day of the week
            String weekdayPlayTimeLimit = getWeekdayPlayTimeLimit();
            Editor edDef = mSettingsSharedPref.edit();
            edDef.putString(SHARED_PREF_SETTINGS_PLAY_TIME_LIMIT_KEY, weekdayPlayTimeLimit);
            edDef.commit();
            MyLog.d(TAG, "setWeekdayPlayTimeLimitOnce: playTimeLimit = " + weekdayPlayTimeLimit +
                    ", updated=" + firstCall);
        }
    }

    private String getWeekdayPlayTimeLimit(){

        Calendar calendar = Calendar.getInstance();
        int weekday = calendar.get(Calendar.DAY_OF_WEEK);
        String weekdayPlayTimeLimit = "";
        switch (weekday){
            case 1:
                weekdayPlayTimeLimit = SHARED_PREF_SETTINGS_PLAY_TIME_LIMIT_SUNDAY_VALUE;
                break;
            case 2:
                weekdayPlayTimeLimit = SHARED_PREF_SETTINGS_PLAY_TIME_LIMIT_MONDAY_VALUE;
                break;
            case 3:
                weekdayPlayTimeLimit = SHARED_PREF_SETTINGS_PLAY_TIME_LIMIT_TUESDAY_VALUE;
                break;
            case 4:
                weekdayPlayTimeLimit = SHARED_PREF_SETTINGS_PLAY_TIME_LIMIT_WEDNESDAY_VALUE;
                break;
            case 5:
                weekdayPlayTimeLimit = SHARED_PREF_SETTINGS_PLAY_TIME_LIMIT_THURSDAY_VALUE;
                break;
            case 6:
                weekdayPlayTimeLimit = SHARED_PREF_SETTINGS_PLAY_TIME_LIMIT_FRIDAY_VALUE;
                break;
            case 7:
                weekdayPlayTimeLimit = SHARED_PREF_SETTINGS_PLAY_TIME_LIMIT_SATURDAY_VALUE;
                break;
        }
        return weekdayPlayTimeLimit;
    }

    public void setProgressThresholdInMinutes(int progressThreshold){
        Editor editor = mPlayedSharedPref.edit();
        editor.putLong(SHARED_PREF_PLAYED_PROGRESS_THRESHOLD_PERCENTAGE_KEY, progressThreshold);
        editor.commit();
    }

    public int getProgressThresholdInMinutes(){
        int progresThreshold = mPlayedSharedPref.getInt(SHARED_PREF_PLAYED_PROGRESS_THRESHOLD_PERCENTAGE_KEY,
                        DEFAULT_PROGRESS_THRESHOLD_PERCENTAGE);
        return progresThreshold;
    }
}
