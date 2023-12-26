package com.example.ntasks;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;

public class NotificationScheduler {

    private static final int NOTIFICATION_ID = 7;

    public static void scheduleNotification(Context context, Date notificationDate, String title, String message) {
        // Create an Intent to be triggered by the alarm
        Toast.makeText(context, "1166", Toast.LENGTH_SHORT).show();
        Intent notificationIntent = new Intent(context, NotificationReceiver.class);
        notificationIntent.putExtra("title", title);
        notificationIntent.putExtra("message", message);

        // Create a PendingIntent for the Intent
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                NOTIFICATION_ID,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
        );

        // Get the AlarmManager service
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Set the alarm to trigger at the specified time
        if (alarmManager != null) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, notificationDate.getTime(), pendingIntent);
        }
    }

    public static void sendImmediateNotification(Context context, String title, String message) {
        try {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            Notification.Builder builder = new Notification.Builder(context)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setAutoCancel(true);
            Toast.makeText(context, "Immediate Notification Sent", Toast.LENGTH_SHORT).show();
            notificationManager.notify(0, builder.build());
        } catch (Exception e) {
            // Log the exception for debugging
            Log.e("NotificationScheduler", "Error sending immediate notification", e);
        }
    }

}
