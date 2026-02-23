package com.example.note;

import android.app.AlarmManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.note.entity.Todo;
import com.example.note.util.AlarmHelper;
import com.example.note.viewmodel.TodoViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.List;

public class AddTodoActivity extends AppCompatActivity {

    private TodoViewModel todoViewModel; // ViewModel
    private TextInputEditText editTextTitle; //输入框
    private TextInputLayout inputLayoutTitle; //输入框的layout
    private MaterialSwitch checkBoxReminder; //闹铃是否开启的switch
    private MaterialCardView cardReminder; //展开的时间选择的布局，
    private MaterialCardView cardReminderToggle; //整个添加闹铃的布局toggle
    private MaterialButtonToggleGroup toggleGroupDate; //选择今天明天后台的布局，toggle
    private TimePicker timePicker; //时间选择器
    private MaterialButton buttonSave; //保存按钮
    private MaterialButton buttonCancel; //取消按钮


    private Calendar reminderCalendar; // 提醒时间的日历实例
    private ActivityResultLauncher<Intent> alarmPermissionLauncher; // 闹钟权限请求启动器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_todo);

        initViews(); // 初始化所有视图组件
        setupViewModel(); // 设置ViewModel
        setupListeners(); // 设置所有监听器
        setupAlarmPermissionLauncher(); // 设置闹钟权限请求启动器
        setupAnimations(); // 设置动画效果
    }

    private void initViews() {
        editTextTitle = findViewById(R.id.editTextTitle);
        inputLayoutTitle = findViewById(R.id.inputLayoutTitle);
        checkBoxReminder = findViewById(R.id.checkBoxReminder);
        cardReminder = findViewById(R.id.cardReminder);
        cardReminderToggle = findViewById(R.id.cardReminderToggle);
        toggleGroupDate = findViewById(R.id.toggleGroupDate);
        timePicker = findViewById(R.id.timePicker);
        buttonSave = findViewById(R.id.buttonSave);
        buttonCancel = findViewById(R.id.buttonCancel);
        
        timePicker.setIs24HourView(true);
        
        reminderCalendar = Calendar.getInstance();
    }

    private void setupViewModel() {
        todoViewModel = new ViewModelProvider(this).get(TodoViewModel.class);
    }

    private void setupAlarmPermissionLauncher() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmPermissionLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            Toast.makeText(this, "闹钟权限已授予", Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        }
    }

    private void setupAnimations() {
        Animation fadeInSlideUp = AnimationUtils.loadAnimation(this, R.anim.fade_in_slide_up);
        
        inputLayoutTitle.startAnimation(fadeInSlideUp);
        cardReminderToggle.startAnimation(fadeInSlideUp);
        buttonCancel.startAnimation(fadeInSlideUp);
        buttonSave.startAnimation(fadeInSlideUp);
    }

    private void setupListeners() {
        //点击整个添加闹铃的布局，都可以展开闹铃设置的布局
        cardReminderToggle.setOnClickListener(v -> {
            //将开关的按钮设置为反，因为点击了
            boolean newState = !checkBoxReminder.isChecked();
            checkBoxReminder.setChecked(newState);
            //开关打开，动画
            if (newState) {
                Animation fadeInSlideUp = AnimationUtils.loadAnimation(this, R.anim.fade_in_slide_up);
                cardReminder.startAnimation(fadeInSlideUp);
            }
        });
        //开关监听
        checkBoxReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {//开关打开，就设置布局为可见
                cardReminder.setVisibility(View.VISIBLE);
                if (toggleGroupDate.getCheckedButtonId() == View.NO_ID) {
                    toggleGroupDate.check(R.id.buttonToday);//默认今天
                }
                updateReminderTime();//将用户设置的时间保存再reminderCalendar中
            } else {
                cardReminder.setVisibility(View.GONE);
            }
        });
        //日期改变也修改reminderCalendar
        toggleGroupDate.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                updateReminderTime();
            }
        });
        //小时分钟
        timePicker.setOnTimeChangedListener((view, hourOfDay, minute) -> {
            updateReminderTime();
        });
        //保存按钮
        buttonSave.setOnClickListener(v -> {
            Animation scaleAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
            buttonSave.startAnimation(scaleAnimation);
            saveTodo();
        });
        
        buttonCancel.setOnClickListener(v -> {
            Animation scaleAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
            buttonCancel.startAnimation(scaleAnimation);
            finish();
        });
    }
    //将用户设置的时间保存再reminderCalendar中
    private void updateReminderTime() {
        int selectedDateOffset = 0;
        int checkedId = toggleGroupDate.getCheckedButtonId();
        
        if (checkedId == R.id.buttonToday) {
            selectedDateOffset = 0;
        } else if (checkedId == R.id.buttonTomorrow) {
            selectedDateOffset = 1;
        } else if (checkedId == R.id.buttonDayAfterTomorrow) {
            selectedDateOffset = 2;
        }
        
        reminderCalendar = Calendar.getInstance();
        reminderCalendar.add(Calendar.DAY_OF_MONTH, selectedDateOffset);
        reminderCalendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
        reminderCalendar.set(Calendar.MINUTE, timePicker.getMinute());
        reminderCalendar.set(Calendar.SECOND, 0);
        reminderCalendar.set(Calendar.MILLISECOND, 0);
    }
    //检查并请求闹钟权限
    private void checkAndRequestAlarmPermission(Runnable onPermissionGranted) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                showAlarmPermissionDialog(onPermissionGranted);
            } else {
                onPermissionGranted.run();
            }
        } else {
            onPermissionGranted.run();
        }
    }

    private void showAlarmPermissionDialog(Runnable onPermissionGranted) {
        new AlertDialog.Builder(this)
                .setTitle("需要闹钟权限")
                .setMessage("为了确保闹铃准时提醒，请授予精确闹钟权限。")
                .setPositiveButton("去设置", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    alarmPermissionLauncher.launch(intent);
                })
                .setNegativeButton("取消", (dialog, which) -> {
                    Toast.makeText(this, "没有闹钟权限可能影响提醒功能", Toast.LENGTH_LONG).show();
                })
                .setCancelable(false)
                .show();
    }
    //保存后做的事情
    private void saveTodo() {
        String title = editTextTitle.getText().toString().trim();
        //为空，提示
        if (TextUtils.isEmpty(title)) {
            inputLayoutTitle.setError(getString(R.string.empty_input_warning));
            Animation shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake);
            inputLayoutTitle.startAnimation(shakeAnimation);
            return;
        }

        inputLayoutTitle.setError(null);
        //默认未完成
        Todo todo = new Todo(title, false, System.currentTimeMillis());

        if (checkBoxReminder.isChecked()) {//如果开启了提醒
            checkAndRequestAlarmPermission(() -> {
                updateReminderTime();//更新值
                
                long currentTime = System.currentTimeMillis();
                //时间在过去，退出
                if (reminderCalendar.getTimeInMillis() <= currentTime) {
                    Toast.makeText(this, R.string.reminder_time_in_past, Toast.LENGTH_SHORT).show();
                    return;
                }
                //时间合法，保存闹钟
                todo.setReminderTime(reminderCalendar.getTimeInMillis());
                saveTodoWithAlarm(todo, title);
            });
        } else {//没开，只保存todo数据
            saveTodoWithoutAlarm(todo);
        }
    }
    //保存并设置闹钟
    private void saveTodoWithAlarm(Todo todo, String title) {
        todoViewModel.insert(todo);
        
        final long reminderTime = reminderCalendar.getTimeInMillis();
        final String todoTitle = title;
        
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            new Thread(() -> {
                try {
                    List<Todo> todos = todoViewModel.getRepository().getAllTodosSync();
                    if (todos != null && !todos.isEmpty()) {
                        for (Todo t : todos) {
                            if (t.getTitle().equals(todoTitle) && t.getReminderTime() != null && 
                                    t.getReminderTime().equals(reminderTime) &&
                                    !t.isReminderTriggered()) {
                                
                                android.util.Log.d("AddTodoActivity", "Setting alarm for todo: " + t.getTitle() + 
                                        ", id: " + t.getId() + ", time: " + t.getReminderTime());
                                
                                AlarmHelper.setAlarm(this, t.getId(), t.getTitle(), t.getReminderTime());
                                
                                runOnUiThread(() -> {
                                    Toast.makeText(this, "闹钟已设置: " + formatTime(reminderTime), Toast.LENGTH_LONG).show();
                                });
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    android.util.Log.e("AddTodoActivity", "Error setting alarm", e);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "闹钟设置失败", Toast.LENGTH_LONG).show();
                    });
                }
            }).start();
        }, 1000);

        Toast.makeText(this, R.string.todo_added_with_reminder, Toast.LENGTH_SHORT).show();
        finish();
    }
    
    private String formatTime(long timestamp) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date(timestamp));
    }
    //只保存，不设置闹钟
    private void saveTodoWithoutAlarm(Todo todo) {
        todoViewModel.insert(todo);
        Toast.makeText(this, R.string.todo_added, Toast.LENGTH_SHORT).show();
        finish();
    }
}