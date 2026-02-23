package com.example.note.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.note.receiver.TodoReminderReceiver;

public class AlarmHelper {
    
    private static final String TAG = "AlarmHelper";
    private static final String ACTION_TODO_REMINDER = "com.example.note.TODO_REMINDER";
    private static final String EXTRA_TODO_ID = "todo_id";
    private static final String EXTRA_TODO_TITLE = "todo_title";

    //设置闹铃
    public static void setAlarm(Context context, int todoId, String title, long reminderTime) {
        //拿到闹钟对象
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager is null");
            return;
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Log.e(TAG, "Cannot schedule exact alarms - permission not granted");
            return;
        }
        
        if (reminderTime <= System.currentTimeMillis()) {
            Log.e(TAG, "Reminder time is in the past: " + reminderTime);
            return;
        }
        //发广播
        Intent intent = new Intent(context, TodoReminderReceiver.class);
        intent.setAction(ACTION_TODO_REMINDER);
        intent.putExtra(EXTRA_TODO_ID, todoId);//哪一条Todo
        intent.putExtra(EXTRA_TODO_TITLE, title);//标题文本
        intent.setFlags(Intent.FLAG_RECEIVER_FOREGROUND);//当作广播处理
        
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getBroadcast(
                    context,
                    todoId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getBroadcast(
                    context,
                    todoId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
        } else {
            pendingIntent = PendingIntent.getBroadcast(
                    context,
                    todoId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
        }
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminderTime,
                        pendingIntent
                );
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminderTime,
                        pendingIntent
                );
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        reminderTime,
                        pendingIntent
                );
            } else {
                alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        reminderTime,
                        pendingIntent
                );
            }
            Log.d(TAG, "Alarm set successfully for todoId: " + todoId + 
                    " at: " + reminderTime + " (" + formatTime(reminderTime) + ")");
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException while setting alarm", e);
        } catch (Exception e) {
            Log.e(TAG, "Exception while setting alarm", e);
        }
    }
    
    public static void cancelAlarm(Context context, int todoId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager is null");
            return;
        }
        
        Intent intent = new Intent(context, TodoReminderReceiver.class);
        intent.setAction(ACTION_TODO_REMINDER);
        
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getBroadcast(
                    context,
                    todoId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
        } else {
            pendingIntent = PendingIntent.getBroadcast(
                    context,
                    todoId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
        }
        
        try {
            alarmManager.cancel(pendingIntent);
            Log.d(TAG, "Alarm cancelled successfully for todoId: " + todoId);
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException while cancelling alarm", e);
        } catch (Exception e) {
            Log.e(TAG, "Exception while cancelling alarm", e);
        }
    }
    
    public static boolean canScheduleExactAlarms(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            boolean canSchedule = alarmManager != null && alarmManager.canScheduleExactAlarms();
            Log.d(TAG, "Can schedule exact alarms: " + canSchedule);
            return canSchedule;
        }
        return true;
    }
    
    private static String formatTime(long timestamp) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date(timestamp));
    }
}
