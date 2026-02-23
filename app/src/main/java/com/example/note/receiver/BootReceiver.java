package com.example.note.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.note.entity.Todo;
import com.example.note.repository.TodoRepository;
import com.example.note.util.AlarmHelper;

import java.util.List;

public class BootReceiver extends BroadcastReceiver {
    
    private static final String TAG = "BootReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }
        
        String action = intent.getAction();
        if (action == null) {
            return;
        }
        
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || 
            Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(action)) {
            Log.d(TAG, "Boot completed, rescheduling alarms");
            rescheduleAllAlarms(context);
        }
    }
    
    private void rescheduleAllAlarms(Context context) {
        new Thread(() -> {
            try {
                TodoRepository repository = new TodoRepository(context);
                List<Todo> todos = repository.getAllTodosSync();
                
                if (todos != null && !todos.isEmpty()) {
                    int rescheduledCount = 0;
                    long currentTime = System.currentTimeMillis();
                    
                    for (Todo todo : todos) {
                        if (todo.getReminderTime() != null && 
                            todo.getReminderTime() > currentTime &&
                            !todo.isReminderTriggered() &&
                            !todo.isCompleted()) {
                            
                            AlarmHelper.setAlarm(context, todo.getId(), todo.getTitle(), todo.getReminderTime());
                            rescheduledCount++;
                        }
                    }
                    
                    Log.d(TAG, "Rescheduled " + rescheduledCount + " alarms after boot");
                } else {
                    Log.d(TAG, "No todos to reschedule");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error rescheduling alarms after boot", e);
            }
        }).start();
    }
}