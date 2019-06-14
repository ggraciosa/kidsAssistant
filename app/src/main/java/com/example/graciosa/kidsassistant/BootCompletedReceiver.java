package com.example.graciosa.kidsassistant;

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

import static com.example.graciosa.kidsassistant.Constants.INTERVAL;


public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        final String TAG = BootCompletedReceiver.class.getSimpleName();

        StringBuilder sb = new StringBuilder();

        sb.append("Action: " + intent.getAction());
        String log = sb.toString();
        MyLog.d(TAG, log);

        /*
        // Create job to compute elapsed time
        JobInfo job = new JobInfo.Builder(Constants.TIME_STEP_JOB_ID,
                new ComponentName(context, TimeStepJobService.class))
                .setPeriodic(Constants.INTERVAL)
                .build();
        JobScheduler js = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        js.schedule(job);
        */

        MySharedPrefManager sp = new MySharedPrefManager(context);

        if (!sp.isComputingPlayingTime()){
            // Computing time is off in settings: do not compute playing time
            MyLog.d(TAG, "Time computation is switched off: do not turn on time step");
            return;
        }

        // override default playing time with today's day of the week max playing time
        sp.setWeekdayMaxPlayingTime();

        // Set alarm to compute elapsed time
        MyAlarmManager.enableOrUpdate(context, INTERVAL);

    }
}
