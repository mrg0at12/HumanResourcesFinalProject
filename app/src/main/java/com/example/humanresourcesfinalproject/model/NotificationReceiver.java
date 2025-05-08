package com.example.humanresourcesfinalproject.model;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.humanresourcesfinalproject.R;

public class NotificationReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID = "COURSE_SIGNUP_CHANNEL";

    @Override
    public void onReceive(Context context, Intent intent) {
        String username = intent.getStringExtra("username");
        String courseName = intent.getStringExtra("courseName");

        createNotificationChannel(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo) // make sure this icon exists
                .setContentTitle("Course Sign-Up Confirmation")
                .setContentText("Dear " + username + ", welcome to " + courseName)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1001, builder.build());
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Course Signup Channel";
            String description = "Channel for course sign-up notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}