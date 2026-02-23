package com.example.note;

import android.app.Application;
import android.os.Build;

import com.example.note.util.NotificationHelper;

public class NoteApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        NotificationHelper.createNotificationChannel(this);
    }
}
