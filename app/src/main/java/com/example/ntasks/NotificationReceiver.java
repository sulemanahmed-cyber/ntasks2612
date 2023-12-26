package com.example.ntasks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("Notification", "Notification received!");
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");

        // Handle the notification, for example, by displaying it
        NotificationHelper.showNotification(context, title, message);
    }
}
