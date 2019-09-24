package com.example.graciosa.kidsassistant.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
//import android.app.job.JobInfo;
//import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
//import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
//import android.content.IntentFilter;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.example.graciosa.kidsassistant.Constants;
//import androidx.work.PeriodicWorkRequest;
//import androidx.work.WorkManager;

import com.example.graciosa.kidsassistant.Constants;
import com.example.graciosa.kidsassistant.MyAlarmManager;
import com.example.graciosa.kidsassistant.MyLog;
import com.example.graciosa.kidsassistant.MySharedPrefManager;

import static com.example.graciosa.kidsassistant.Constants.INTERVAL;


public class BootCompletedReceiver extends BroadcastReceiver {

    final String TAG = BootCompletedReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        if ("android.intent.action.BOOT_COMPLETED".equals(action)) {

            MyLog.d(TAG, "onReceive");

            StringBuilder sb = new StringBuilder();
            sb.append("Action: ");
            sb.append(intent.getAction());
            MyLog.d(TAG, sb.toString());

            MySharedPrefManager sp = new MySharedPrefManager(context);

            if (!sp.getComputePlayingTime()) {
                // Computing time is off in settings: do not compute playing time
                MyLog.d(TAG, "Time computation is switched off: do not turn on time step");
                return;
            }

            // Override default playing time with today's day of the week max playing time
            sp.setWeekdayPlayTimeLimitOnce();

            // Set alarm to compute elapsed time
            MyAlarmManager.enableOrUpdate(context, INTERVAL);

        }
    }
}
