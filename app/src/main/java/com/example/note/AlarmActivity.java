package com.example.note;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;

public class AlarmActivity extends AppCompatActivity {

    private static final String EXTRA_TODO_ID = "todo_id";
    private static final String EXTRA_TODO_TITLE = "todo_title";
    private static final String EXTRA_TODO_REMINDER_TIME = "todo_reminder_time";

    private TextView textViewTitle;
    private TextView textViewTime;
    private Button buttonDismiss;
    private MaterialCardView cardAlarm;
    private Ringtone ringtone;
    private Vibrator vibrator;
    private ValueAnimator pulseAnimator;
    private boolean isDismissing = false;

    public static Intent createIntent(Context context, int todoId, String title, long reminderTime) {
        Intent intent = new Intent(context, AlarmActivity.class);
        intent.putExtra(EXTRA_TODO_ID, todoId);
        intent.putExtra(EXTRA_TODO_TITLE, title);
        intent.putExtra(EXTRA_TODO_REMINDER_TIME, reminderTime);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setupFullScreen();
        setContentView(R.layout.activity_alarm);
        
        initViews();
        setupAlarm();
        startAnimations();
    }

    private void setupFullScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
        }
    }

    private void initViews() {
        textViewTitle = findViewById(R.id.textViewAlarmTitle);
        textViewTime = findViewById(R.id.textViewAlarmTime);
        buttonDismiss = findViewById(R.id.buttonDismiss);
        cardAlarm = findViewById(R.id.cardAlarm);

        int todoId = getIntent().getIntExtra(EXTRA_TODO_ID, -1);
        String title = getIntent().getStringExtra(EXTRA_TODO_TITLE);
        long reminderTime = getIntent().getLongExtra(EXTRA_TODO_REMINDER_TIME, 0);

        textViewTitle.setText(title);
        
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        textViewTime.setText(sdf.format(new java.util.Date(reminderTime)));

        buttonDismiss.setOnClickListener(v -> dismissAlarm());
    }

    private void setupAlarm() {
        startAlarmSound();
        startVibration();
    }

    private void startAlarmSound() {
        try {
            Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }

            ringtone = RingtoneManager.getRingtone(this, alarmUri);
            if (ringtone != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    AudioAttributes attributes = new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build();
                    ringtone.setAudioAttributes(attributes);
                }
                ringtone.play();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startVibration() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vibratorManager = (VibratorManager) getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            if (vibratorManager != null) {
                vibrator = vibratorManager.getDefaultVibrator();
            }
        } else {
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        }

        if (vibrator != null && vibrator.hasVibrator()) {
            long[] pattern = {0, 500, 200, 500, 200, 500, 200, 500};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                        android.os.VibrationEffect.createWaveform(pattern, 2),
                        new android.os.VibrationAttributes.Builder()
                                .setUsage(android.os.VibrationAttributes.USAGE_ALARM)
                                .build()
                );
            } else {
                vibrator.vibrate(pattern, 2);
            }
        }
    }

    private void startAnimations() {
        pulseAnimator = ValueAnimator.ofFloat(1f, 1.05f);
        pulseAnimator.setDuration(1000);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
        pulseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        pulseAnimator.addUpdateListener(animation -> {
            float scale = (float) animation.getAnimatedValue();
            cardAlarm.setScaleX(scale);
            cardAlarm.setScaleY(scale);
        });
        pulseAnimator.start();
    }

    private void dismissAlarm() {
        if (isDismissing) return;
        isDismissing = true;

        stopAlarm();
        
        ValueAnimator fadeOut = ValueAnimator.ofFloat(1f, 0f);
        fadeOut.setDuration(300);
        fadeOut.addUpdateListener(animation -> {
            float alpha = (float) animation.getAnimatedValue();
            cardAlarm.setAlpha(alpha);
        });
        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                finish();
                overridePendingTransition(0, R.anim.slide_out_bottom);
            }
        });
        fadeOut.start();
    }

    private void stopAlarm() {
        if (pulseAnimator != null && pulseAnimator.isRunning()) {
            pulseAnimator.cancel();
        }

        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
            ringtone = null;
        }

        if (vibrator != null) {
            vibrator.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAlarm();
    }

    @Override
    public void onBackPressed() {
        dismissAlarm();
    }
}