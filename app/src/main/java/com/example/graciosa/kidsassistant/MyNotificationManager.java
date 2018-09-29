package com.example.graciosa.kidsassistant;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

public class MyNotificationManager {

    /*****************
     *** CONSTANTS ***
     *****************/

    final int NOTIF_ID = 0;
    final String HIGH_IMPORTANCE_CHANNEL_ID = "KIDS_ASSISTANT_HIGH_IMPORTANCE_NOTIF_CHANNEL";
    final String LOW_IMPORTANCE_CHANNEL_ID = "KIDS_ASSISTANT_LOW_IMPORTANCE_NOTIF_CHANNEL";

    /***************
     *** METHODS ***
     ***************/

    public MyNotificationManager(Context context){
        // For API level 26 (Oreo) and above
        createNotificationChannels(context);
    }

    /* Posts regular notification in status bar*/
    public void postProgressMediumImportance(Context context, int minutesRemaining, int progress_max, int progress_current){
        NotificationCompat.Builder builder = buildBaseNotif(context, LOW_IMPORTANCE_CHANNEL_ID);
        // Customize the notification
        builder.setContentText(Long.toString(minutesRemaining) + " minutes left");
        builder.setProgress(progress_max, progress_current, false);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            // For API level < 26 there is no notif channel so importance attached to channel will
            // not be considered. Need to set priority here.
            builder.setPriority(Notification.PRIORITY_DEFAULT);
        }
        post(context, builder);
    }

    /* Posts the intrusive heads up notification */
    public void postProgressHighImportance(Context context, int minutesRemaining, int progress_max, int progress_current){
        NotificationCompat.Builder builder = buildBaseNotif(context, HIGH_IMPORTANCE_CHANNEL_ID);
        // Customize the notification
        builder.setContentText(Long.toString(minutesRemaining) + " minutes left");
        builder.setProgress(progress_max, progress_current, false);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            // For API level < 26 there is no notif channel so importance attached to channel will
            // not be considered. Need to set priority here.
            builder.setPriority(Notification.PRIORITY_HIGH);
        }
        post(context, builder);
    }

    public void postTimeout(Context context, int overtimeMinutes){
        NotificationCompat.Builder builder = buildBaseNotif(context, HIGH_IMPORTANCE_CHANNEL_ID);
        // Customize the notification
        builder.setContentText("Timeout! " + overtimeMinutes + " min over");
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setProgress(100, 100, false);
        post(context, builder);
    }

    /* Creates a notification channel that show notif in status, do not make noise neither interrupt */
    private void createNotificationChannels(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

            // Create channel to show notifs in status bar, make noise and interrupts
            String name = "Low importance";
            NotificationChannel channel = new NotificationChannel(HIGH_IMPORTANCE_CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Progress notifications");
            notificationManager.createNotificationChannel(channel);

            // Create channel to show notifs in status bar but do not make noise neither interrupt
            name = "High importance";
            channel = new NotificationChannel(LOW_IMPORTANCE_CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Near and post timeout notifications");
            notificationManager.createNotificationChannel(channel);
        }
    }

    private NotificationCompat.Builder buildBaseNotif(Context context, String channelId){
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_notif_hourglass)
                .setContentTitle("Kids Assistant")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        return builder;
    }

    private void post(Context context, NotificationCompat.Builder builder){
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIF_ID, builder.build());
    }
}