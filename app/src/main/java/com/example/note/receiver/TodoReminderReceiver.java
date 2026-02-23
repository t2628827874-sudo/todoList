package com.example.note.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import com.example.note.AlarmActivity;
import com.example.note.entity.Todo;
import com.example.note.repository.TodoRepository;
import com.example.note.util.NotificationHelper;
//广播接收器
public class TodoReminderReceiver extends BroadcastReceiver {
    
    private static final String TAG = "TodoReminderReceiver";
    private static final String WAKE_LOCK_TAG = "Note:TodoReminderWakeLock";
    private static PowerManager.WakeLock wakeLock;

    //处理收到的消息
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            Log.e(TAG, "Intent is null");
            return;
        }
        
        acquireWakeLock(context);
        
        int todoId = intent.getIntExtra("todo_id", -1);
        String todoTitle = intent.getStringExtra("todo_title");
        
        Log.d(TAG, "Received reminder for todoId: " + todoId + ", title: " + todoTitle);
        
        if (todoId == -1 || todoTitle == null) {
            Log.e(TAG, "Invalid todoId or todoTitle");
            releaseWakeLock();
            return;
        }
        
        TodoRepository repository = new TodoRepository(context);
        new Thread(() -> {
            try {
                Todo todo = repository.getTodoById(todoId);
                if (todo != null) {
                    Log.d(TAG, "Todo found: " + todo.getTitle() + 
                            ", isReminderTriggered: " + todo.isReminderTriggered() + 
                            ", isCompleted: " + todo.isCompleted());
                    
                    if (!todo.isReminderTriggered() && !todo.isCompleted()) {
                        todo.setReminderTriggered(true);
                        repository.update(todo);
                        
                        Log.d(TAG, "Triggering alarm for todo: " + todoTitle);
                        
                        Intent alarmIntent = AlarmActivity.createIntent(
                                context, 
                                todo.getId(), 
                                todo.getTitle(), 
                                todo.getReminderTime()
                        );
                        context.startActivity(alarmIntent);
                        
                        NotificationHelper.showNotification(context, todoTitle, todoId);
                    } else {
                        Log.d(TAG, "Todo already triggered or completed, skipping alarm");
                    }
                } else {
                    Log.e(TAG, "Todo not found with id: " + todoId);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing reminder", e);
            } finally {
                releaseWakeLock();
            }
        }).start();
    }
    
    private void acquireWakeLock(Context context) {
        try {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (powerManager != null) {
                if (wakeLock == null) {
                    wakeLock = powerManager.newWakeLock(
                            PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                            WAKE_LOCK_TAG
                    );
                    wakeLock.setReferenceCounted(false);
                }
                if (!wakeLock.isHeld()) {
                    wakeLock.acquire(10000L);
                    Log.d(TAG, "WakeLock acquired");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error acquiring WakeLock", e);
        }
    }
    
    private void releaseWakeLock() {
        try {
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
                Log.d(TAG, "WakeLock released");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error releasing WakeLock", e);
        }
    }
}
