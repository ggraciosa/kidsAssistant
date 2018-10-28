package com.example.graciosa.kidsassistant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.PowerManager;
import android.preference.PreferenceManager;


public class TimeStepReceiver extends BroadcastReceiver {

    final String TAG = TimeStepReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        MySharedPrefManager sp = new MySharedPrefManager(context);

        if (!isInteractive(context)) {
            // Kids are not interacting with the device: do not compute playing time.
            // Reset elapsed playing time reference
            sp.resetElapsedPlayingTime();
            MyLog.d(TAG, "Kids are not interacting: do not compute playing time");
            return;
        }

        if (!sp.isComputingPlayingTime()){
            // Computing time is off in settings: do not compute playing time
            // Reset elapsed playing time reference
            sp.resetElapsedPlayingTime();
            MyLog.d(TAG, "Time computation is switched off: do not compute playing time");
            return;
        }

        // Kids are interacting with the device.

        if (sp.updatePlayingDateIfNeeded()) {
            // It is a new day: reset elapsed playing time reference and played time
            sp.resetElapsedPlayingTime();
            sp.resetPlayedTime();
        }
        sp.updatePlayingTime();

        long minutesPlaying = sp.getPlayedTimeInMinutes();
        long minutesMaxPlaying = sp.getMaxPlayingTimeInMinutes();

        int progress_max = (int) minutesMaxPlaying;
        int progress_current = Math.min((int) minutesPlaying, (int) minutesMaxPlaying);

        // Decide notification importance and post the notif
        MyNotificationManager notif = new MyNotificationManager(context);
        if (progress_current < progress_max) {
            // There is still some time to play
            int minutesRemaining = progress_max - progress_current;
            int progressThreshold = sp.getProgressThresholdInMinutes();
            float percent = (float) progress_current / progress_max;
            if (Math.round(100 * percent) < progressThreshold) {
                MyLog.d(TAG, "Notif priority: medium");
                notif.postProgressMediumImportance(context, minutesRemaining, progress_max, progress_current);
            } else {
                MyLog.d(TAG, "Notif priority: high");
                notif.postProgressHighImportance(context, minutesRemaining, progress_max, progress_current);
            }
        } else {
            // Playing time is over
            long minutesOvertime = minutesPlaying - minutesMaxPlaying;
            notif.postTimeout(context, (int) minutesOvertime);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("onReceive: max minutes: " + minutesMaxPlaying);
        sb.append("; minutes playing: " + minutesPlaying);
        MyLog.d(TAG, sb.toString());
    }

    private boolean isInteractive(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            // API level >= 20
            return pm.isInteractive();
        } else {
            // API level < 20
            return pm.isScreenOn();
        }
    }

}
