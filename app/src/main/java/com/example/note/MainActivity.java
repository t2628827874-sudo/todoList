package com.example.note;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.note.adapter.TodoAdapter;
import com.example.note.entity.Todo;
import com.example.note.util.AlarmHelper;
import com.example.note.util.NotificationHelper;
import com.example.note.viewmodel.TodoViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TodoViewModel todoViewModel;
    private TodoAdapter adapter;//Recyclerview的adapter
    private TextView textViewEmpty;
    private FloatingActionButton fabAddTodo;//1.定义主界面右下角➕的控件
    private ActivityResultLauncher<String> notificationPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        //设置视图的内容
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //4.初始化视图组件
        initViews();
        setupRecyclerView();
        setupViewModel();
        setupFabButton();//设置点击事件
        setupNotificationPermission();
    }

    private void initViews() {
        //找到空列表提示文本控件
        textViewEmpty = findViewById(R.id.textViewEmpty);
        //2.找到右下角➕的控件
        fabAddTodo = findViewById(R.id.fabAddTodo);
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerViewTodos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TodoAdapter();
        recyclerView.setAdapter(adapter);
        //点击某一个列表项的时候调用，重写接口
        adapter.setOnItemClickListener(new TodoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Todo todo) {
                if (todo.isCompleted()) {
                    deleteTodo(todo);//任务完成删除
                } else {
                    if (todo.canBeCompleted()) {
                        todoViewModel.update(todo);
                    } else {
                        Toast.makeText(MainActivity.this, R.string.cannot_complete_before_reminder, Toast.LENGTH_SHORT).show();
                    }
                }
            }
            //长按删除
            @Override
            public void onItemLongClick(Todo todo) {
                showDeleteDialog(todo);
            }
            //删除
            @Override
            public void onDeleteClick(Todo todo) {
                showDeleteDialog(todo);
            }
        });
    }

    private void setupViewModel() {
        todoViewModel = new ViewModelProvider(this).get(TodoViewModel.class);
        todoViewModel.getAllTodos().observe(this, todos -> {
            adapter.setTodos(todos);
            updateEmptyState(todos);
        });
    }

    private void setupFabButton() {
        //3.监听事件：当用户点击时跳转到添加界面,添加页面是AddTodoActivity
        fabAddTodo.setOnClickListener(v -> {
            if (checkNotificationPermission()) {
                Intent intent = new Intent(MainActivity.this, AddTodoActivity.class);
                //响应
                startActivity(intent);
            } else {
                //如果没有权限，请求权限
                requestNotificationPermission();
            }
        });
    }
    //设置通知权限
    private void setupNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher = registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (!isGranted) {
                            showNotificationPermissionDialog();
                        }
                    }
            );
        }
    }
    //检查通知权限是否已授予
    private boolean checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }
    //请求通知权限
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }
    //显示通知权限对话框
    private void showNotificationPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("需要通知权限")
                .setMessage("为了确保闹铃提醒功能正常工作，请授予通知权限。")
                .setPositiveButton("去设置", (dialog, which) -> {
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                })
                .setNegativeButton("取消", null)
                .show();
    }
    //删除待办事项
    private void deleteTodo(Todo todo) {
        if (todo.hasReminder()) {
            AlarmHelper.cancelAlarm(this, todo.getId());
            NotificationHelper.cancelNotification(this, todo.getId());
        }
        todoViewModel.delete(todo);
        Toast.makeText(this, R.string.todo_deleted, Toast.LENGTH_SHORT).show();
    }
    //显示删除确认对话框
    private void showDeleteDialog(Todo todo) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_todo)
                .setMessage(R.string.delete_confirm)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    deleteTodo(todo);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
    //更新空状态
    private void updateEmptyState(List<Todo> todos) {
        if (todos == null || todos.isEmpty()) {
            textViewEmpty.setVisibility(View.VISIBLE);
        } else {
            textViewEmpty.setVisibility(View.GONE);
        }
    }
}
