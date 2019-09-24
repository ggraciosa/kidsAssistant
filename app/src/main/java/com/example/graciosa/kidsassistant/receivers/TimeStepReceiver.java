package com.example.graciosa.kidsassistant.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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

    /*****************
     *** CONSTANTS ***
     *****************/

    final String TAG = TimeStepReceiver.class.getSimpleName();

    // Intent action to request to process the elapsed time since last step.
    public static final String COMPUTE_TIME =
            "com.example.graciosa.kidsassistant.receivers.timestep.action.COMPUTE_TIME";
    // Intent action to request to forcely process the elapsed time since last step, i.e., even
    // if the compute playing time preference in App settings is off.
    public static final String COMPUTE_TIME_SKIP_PREFERENCE =
            "com.example.graciosa.kidsassistant.receivers.timestep.action.COMPUTE_TIME_SKIP_PREFERENCE";

    /*********************
     *** INNER CLASSES ***
     *********************/

    private class UpdateDbAndPostNotifThread extends Thread {

        private Context mContext;
        private boolean mPlaying;
        private boolean mNewDay;

        /*
         * context: application context
         * newDay: date has changed, playing info must be reset
         * playing: kids are currently playing, time step must be computed
         */
        public UpdateDbAndPostNotifThread(Context context, boolean newDay, boolean playing) {
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

            int played = (int) sp.getPlayedTimeInMinutes();
            int limit = (int) sp.getPlayTimeLimitInMinutes();

            int progress_max = limit;
            int progress_current = Math.min(played, limit);

            // Decide notification importance and post the notification
            MyNotificationManager notif = new MyNotificationManager(mContext);
            if (progress_current < progress_max) {
                // There is still some time to play
                int progressThreshold = sp.getProgressThresholdInMinutes();
                float percent = (float) progress_current / progress_max;
                if (Math.round(100 * percent) < progressThreshold) {
                    MyLog.d(TAG, "Notif priority: medium");
                    notif.postProgressMediumImportance(mContext, progress_max, progress_current);
                } else {
                    MyLog.d(TAG, "Notif priority: high");
                    notif.postProgressHighImportance(mContext, progress_max, progress_current);
                }
            } else {
                // Playing time is over
                int minutesOvertime = played - limit;
                notif.postTimeout(mContext, limit, played, minutesOvertime);
            }

            if (mPlaying) {
                // Save playing time update in database
                PlayedTimeDatabase db = PlayedTimeDatabaseSingleton.getInstance(mContext).getDatabase();
                PlayedTimeDao dao = db.playedTimeDao();
                PlayedTimeEntity entity = new PlayedTimeEntity();
                String date = Utils.getCurrentDate();
                entity.setDate(date);
                entity.setPlayed((int) played);
                entity.setLimit((int) limit);
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

    /***************
     *** METHODS ***
     ***************/

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        if (COMPUTE_TIME.equals(action) || COMPUTE_TIME_SKIP_PREFERENCE.equals(action)) {

            MyLog.d(TAG, "onReceive: action=" + action);

            MySharedPrefManager sp = new MySharedPrefManager(context);
            boolean newDay = sp.hasDateChanged();
            boolean playing = true;

            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (!pm.isInteractive()) {
                // Kids are not interacting with the device
                // Reset elapsed playing time reference
                sp.resetElapsedPlayedTime();
                MyLog.d(TAG, "Kids are not interacting");
                playing = false;
            }

            if (!COMPUTE_TIME_SKIP_PREFERENCE.equals(action)) {
                if (!sp.getComputePlayingTime()) {
                    // Computing time is off in settings
                    // Reset elapsed playing time reference
                    sp.resetElapsedPlayedTime();
                    MyLog.d(TAG, "Time computation setting is switched off");
                    playing = false;
                }
            }

            if (newDay || playing) {
                // Day changed or Kids are interacting with the device
                // Perform remaining processing in background
                new UpdateDbAndPostNotifThread(context, newDay, playing).start();
            }
        }
    }

}
