package com.example.graciosa.kidsassistant.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;

import com.example.graciosa.kidsassistant.MyLog;
import com.example.graciosa.kidsassistant.MyNotificationManager;
import com.example.graciosa.kidsassistant.MySharedPrefManager;
import com.example.graciosa.kidsassistant.Utils;
import com.example.graciosa.kidsassistant.db.PlayedTimeDao;
import com.example.graciosa.kidsassistant.db.PlayedTimeDatabase;
import com.example.graciosa.kidsassistant.db.PlayedTimeDatabaseSingleton;
import com.example.graciosa.kidsassistant.db.PlayedTimeEntity;


public class TimeStepReceiver extends BroadcastReceiver {

    final String TAG = TimeStepReceiver.class.getSimpleName();

    private class ProcessTimeStepThread extends Thread {

        private Context mContext;
        private boolean mPlaying;
        private boolean mNewDay;

        /*
         * context: application context
         * newDay: date has changed, playing info must be reset
         * playing: kids are currently playing, time step must be computed
         */
        public ProcessTimeStepThread(Context context, boolean newDay, boolean playing) {
            mContext = context;
            mNewDay = newDay;
            mPlaying = playing;
        }

        @Override
        public void run() {

            MyLog.d(TAG, "ProcessTimeStepThread: newDay=" + mNewDay + ", playing=" + mPlaying);

            MySharedPrefManager sp = new MySharedPrefManager(mContext);

            if (mNewDay) {
                // It is a new day: change date and set max play time for the new date
                sp.setPlayingDate();
                sp.setWeekdayPlayTimeLimitOnce();
                // Reset elapsed playing time reference and played time
                sp.resetElapsedPlayedTime();
                sp.resetPlayedTime();
            }

            if (mPlaying) {
                // Kids are playing, compute playing time
                sp.updatePlayedTime();
            }

            long minutesPlayed = sp.getPlayedTimeInMinutes();
            long minutesLimit = sp.getPlayTimeLimitInMinutes();

            int progress_max = (int) minutesLimit;
            int progress_current = Math.min((int) minutesPlayed, (int) minutesLimit);

            // Decide notification importance and post the notification
            MyNotificationManager notif = new MyNotificationManager(mContext);
            if (progress_current < progress_max) {
                // There is still some time to play
                int minutesRemaining = progress_max - progress_current;
                int progressThreshold = sp.getProgressThresholdInMinutes();
                float percent = (float) progress_current / progress_max;
                if (Math.round(100 * percent) < progressThreshold) {
                    MyLog.d(TAG, "Notif priority: medium");
                    notif.postProgressMediumImportance(mContext, minutesRemaining, progress_max, progress_current);
                } else {
                    MyLog.d(TAG, "Notif priority: high");
                    notif.postProgressHighImportance(mContext, minutesRemaining, progress_max, progress_current);
                }
            } else {
                // Playing time is over
                long minutesOvertime = minutesPlayed - minutesLimit;
                notif.postTimeout(mContext, (int) minutesOvertime);
            }

            if (mPlaying) {
                // Save playing time update in database
                PlayedTimeDatabase db = PlayedTimeDatabaseSingleton.getInstance(mContext).getDatabase();
                PlayedTimeDao dao = db.playedTimeDao();
                PlayedTimeEntity entity = new PlayedTimeEntity();
                String date = Utils.getCurrentDate();
                entity.setDate(date);
                entity.setPlayed((int) minutesPlayed);
                entity.setLimit((int) minutesLimit);
                if (dao.countByDate(date) == 0) {
                    // No today record yet, add it
                    MyLog.d(TAG, "ProcessTimeStepThread: insert");
                    dao.insert(entity);
                } else {
                    MyLog.d(TAG, "ProcessTimeStepThread: update");
                    dao.update(entity);
                }

            }
        }

    }

    @Override
    public void onReceive(Context context, Intent intent) {

        MySharedPrefManager sp = new MySharedPrefManager(context);
        boolean newDay = sp.hasDateChanged();
        boolean playing = true;

        if (!isInteractive(context)) {
            // Kids are not interacting with the device
            // Reset elapsed playing time reference
            sp.resetElapsedPlayedTime();
            MyLog.d(TAG, "Kids are not interacting");
            playing = false;
        }

        if (!sp.isComputingPlayingTime()) {
            // Computing time is off in settings
            // Reset elapsed playing time reference
            sp.resetElapsedPlayedTime();
            MyLog.d(TAG, "Time computation setting is switched off");
            playing = false;
        }

        if (newDay || playing){
            // Kids are interacting with the device, perform remaining processing in background
            new ProcessTimeStepThread(context, newDay, playing).start();
        }

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
