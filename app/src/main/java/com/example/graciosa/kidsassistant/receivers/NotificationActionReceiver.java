package com.example.graciosa.kidsassistant.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.graciosa.kidsassistant.MyLog;
import com.example.graciosa.kidsassistant.MySharedPrefManager;


public class NotificationActionReceiver extends BroadcastReceiver {

    final String TAG = NotificationActionReceiver.class.getSimpleName();

    // Intent action to indicate "PAUSE" button/action was pressed in notification
    public static final String ACTION_PAUSE =
            "com.example.graciosa.kidsassistant.receivers.notification.action.PAUSE";
    // Intent action to indicate "RESUME" button/action was pressed in notification
    public static final String ACTION_RESUME =
            "com.example.graciosa.kidsassistant.receivers.notification.action.RESUME";

    @Override
    public void onReceive(Context context, Intent intent) {


        String action = intent.getAction();

        if (ACTION_PAUSE.equals(action) || ACTION_RESUME.equals(action)){

            MyLog.d(TAG, "onReceive: action=" + action);

            MySharedPrefManager sp = new MySharedPrefManager(context);
            Intent i = new Intent(context, TimeStepReceiver.class);

            if (ACTION_PAUSE.equals(action)){
                // Turn off played time computation
                sp.setComputePlayingTime(false);
                // Request TimeStepReceiver to capture the elapsed time since last step and to
                // update the notification including its action label PAUSE -> RESUME.
                // Need to skip the value of "ComputePlayingTime" preference since deliberately
                // set to off in above command.
                i.setAction(TimeStepReceiver.COMPUTE_TIME_SKIP_PREFERENCE);

            } else {
                // Turn on played time computation
                sp.setComputePlayingTime(true);
                // Request TimeStepReceiver to capture the elapsed time since above command and to
                // update the notification including its action label RESUME -> PAUSE.
                i.setAction(TimeStepReceiver.COMPUTE_TIME);
            }

            context.sendBroadcast(i);

        }
    }
}
