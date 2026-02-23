package com.example.note.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.note.MainActivity;
import com.example.note.R;

public class NotificationHelper {
    
    private static final String TAG = "NotificationHelper";
    private static final String CHANNEL_ID = "todo_reminder_channel";
    private static final String CHANNEL_NAME = "待办事项提醒";
    private static final String CHANNEL_DESC = "用于待办事项定时提醒的通知";
    private static final int NOTIFICATION_ID = 1001;
    
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build();
                
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription(CHANNEL_DESC);
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{0, 500, 200, 500});
                channel.setShowBadge(true);
                channel.setSound(defaultSoundUri, audioAttributes);
                channel.enableLights(true);
                
                NotificationManager manager = context.getSystemService(NotificationManager.class);
                if (manager != null) {
                    manager.createNotificationChannel(channel);
                    Log.d(TAG, "Notification channel created successfully");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error creating notification channel", e);
            }
        }
    }
    
    public static void showNotification(Context context, String title, int todoId) {
        try {
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("todo_id", todoId);
            
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    todoId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("待办事项提醒")
                    .setContentText(title)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setVibrate(new long[]{0, 500, 200, 500})
                    .setSound(defaultSoundUri)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setOngoing(false)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setDefaults(NotificationCompat.DEFAULT_ALL);
            
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.notify(NOTIFICATION_ID + todoId, builder.build());
                Log.d(TAG, "Notification shown for todo: " + title + ", id: " + todoId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing notification", e);
        }
    }
    
    public static void cancelNotification(Context context, int todoId) {
        try {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.cancel(NOTIFICATION_ID + todoId);
                Log.d(TAG, "Notification cancelled for todo id: " + todoId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error cancelling notification", e);
        }
    }
    
    public static void cancelAllNotifications(Context context) {
        try {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.cancelAll();
                Log.d(TAG, "All notifications cancelled");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error cancelling all notifications", e);
        }
    }
}
