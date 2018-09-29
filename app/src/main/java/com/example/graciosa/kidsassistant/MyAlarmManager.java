package com.example.graciosa.kidsassistant;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import static com.example.graciosa.kidsassistant.Constants.INTERVAL;

public class MyAlarmManager {

    static public void enableOrUpdate(Context context, long interval){

        AlarmManager am = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        PendingIntent pendingIntent = buildPendingIntent(context);

        // Cancel pending alarms with this pending intent, if any;
        am.cancel(pendingIntent);

        // Set a new alarm with same pending intent to compute elapsed time
        long trigger = SystemClock.elapsedRealtime() + interval;
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, trigger, interval, pendingIntent);
    }

    static public void disable(Context context){
        AlarmManager am = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        am.cancel(buildPendingIntent(context));
    }

    static private PendingIntent buildPendingIntent(Context context){
        Intent alarmIntent = new Intent(context, TimeStepReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
        return pendingIntent;
    }

}
