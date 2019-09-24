package com.example.graciosa.kidsassistant;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.graciosa.kidsassistant.receivers.NotificationActionReceiver;

public class MyNotificationManager {

    /*****************
     *** CONSTANTS ***
     *****************/

    final String TAG = MyNotificationManager.class.getSimpleName();

    final int NOTIF_ID = 0;
    final String HIGH_IMPORTANCE_CHANNEL_ID = "HIGH_IMPORTANCE_NOTIFICATION_CHANNEL_ID";
    final String MID_IMPORTANCE_CHANNEL_ID = "MID_IMPORTANCE_NOTIFICATION_CHANNEL_ID";

    // Notification action label to pause played time computation
    private final String ACTION_LABEL_PAUSE = "PAUSE";
    // Notification action label to resume played time computation
    private final String ACTION_LABEL_RESUME = "RESUME";

    /**************
     *** FIELDS ***
     **************/

    Context mContext;

    /***************
     *** METHODS ***
     ***************/

    public MyNotificationManager(Context context){

        mContext = context;
        // For API level 26 (Oreo) and above
        createNotificationChannels(context);
    }

    /* Posts regular notification in status bar */
    public void postProgressMediumImportance(Context context, int limit, int played){
        NotificationCompat.Builder builder =
                buildBaseNotif(context, MID_IMPORTANCE_CHANNEL_ID, limit, played);
        // Customize the notification
        int minutesRemaining = limit - played;
        builder.setContentText(Long.toString(minutesRemaining) + " minutes left");
        builder.setProgress(limit, played, false);
        builder.setColor(context.getResources().getColor(R.color.colorAccent));
        builder.setColorized(true);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            // For API level < 26 there is no notif channel so importance attached to channel will
            // not be considered. Need to set priority here.
            builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        }

        //builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0));
        // TODO: below code displays "PAUSE" string underneath notif. To have a pause button to the
        //  right of the progress bar need to create the layout of a custom view and pass it to
        //  builder.setCustomContentView().
        int icon;
        String actionText;
        Intent intent = new Intent(mContext, NotificationActionReceiver.class);
        MySharedPrefManager sp = new MySharedPrefManager(mContext);
        if (sp.getComputePlayingTime()) {
            // Play time is being computed: offer the action to pause.
            MyLog.d(TAG,"postProgressMediumImportance: update notif action to PAUSE");
            icon = R.drawable.ic_pause;
            actionText = ACTION_LABEL_PAUSE;
            intent.setAction(NotificationActionReceiver.ACTION_PAUSE);
        } else {
            // Play time computation has been already paused by user: offer the action to resume.
            MyLog.d(TAG,"postProgressMediumImportance: update notif action to RESUME");
            icon = R.drawable.ic_play;
            actionText = ACTION_LABEL_RESUME;
            intent.setAction(NotificationActionReceiver.ACTION_RESUME);
        }
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);
        builder.addAction(icon, actionText, pendingIntent);

        post(context, builder);
    }

    /* Posts the intrusive heads up notification */
    public void postProgressHighImportance(Context context, int limit, int played){
        NotificationCompat.Builder builder = buildBaseNotif(context, HIGH_IMPORTANCE_CHANNEL_ID,
                limit, played);
        // Customize the notification
        int minutesRemaining = limit - played;
        builder.setContentText(Long.toString(minutesRemaining) + " minutes left");
        builder.setProgress(limit, played, false);
        builder.setColor(context.getResources().getColor(R.color.colorAccent));
        builder.setColorized(true);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            // For API level < 26 there is no notif channel so importance attached to channel will not be considered.
            // For system to consider it high importance and display as heads up, both vibe and high priority must be set.
            builder.setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(Notification.DEFAULT_VIBRATE);

        }
        post(context, builder);
    }

    private NotificationCompat.Builder buildBaseNotif(Context context, String channelId,
                                                      int limit, int played){
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_notif_hourglass)
                .setContentTitle(played + "/" + limit)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        return builder;
    }

    public void postTimeout(Context context, int limit, int played, long overtimeMinutes){
        NotificationCompat.Builder builder =
                buildBaseNotif(context, HIGH_IMPORTANCE_CHANNEL_ID, limit, played);
        // Customize the notification
        builder.setContentText("Timeout! " + overtimeMinutes + " min over");
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        long[] v = {500,1000};
        builder.setVibrate(v);
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(uri);
        builder.setProgress(100, 100, false);
        builder.setColor(context.getResources().getColor(R.color.colorOrange));
        builder.setColorized(true);
        post(context, builder);
    }

    /*
     * Clean all notifications of this App from the status bar.
     */
    public static void cancelNotification(Context context){
        NotificationManagerCompat.from(context).cancelAll();
    }

    /* Creates a notification channel that show notif in status, do not make noise neither interrupt */
    private void createNotificationChannels(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

            // Create channel to show notifs in status bar but do not make noise neither interrupt
            String name = "Low importance";
            NotificationChannel channel = new NotificationChannel(MID_IMPORTANCE_CHANNEL_ID, name,
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Progress notifications");
            notificationManager.createNotificationChannel(channel);

            // Create channel to show notifs in status bar, make noise and interrupts
            name = "High importance";
            channel = new NotificationChannel(HIGH_IMPORTANCE_CHANNEL_ID, name,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Near and post timeout notifications");
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void post(Context context, NotificationCompat.Builder builder){
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIF_ID, builder.build());
    }
}
