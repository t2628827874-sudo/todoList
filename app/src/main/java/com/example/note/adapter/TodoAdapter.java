package com.example.note.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.note.R;
import com.example.note.entity.Todo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


//RecyclerView的适配器adpter
public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder> {
    //列表todom，model
    private List<Todo> todos = new ArrayList<>();
    private OnItemClickListener listener;// 点击事件监听器

    // 加载布局文件 item_todo.xml
    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_todo, parent, false);// 1. 创建 ViewHolder：加载布局文件 item_todo.xml
        return new TodoViewHolder(view);
    }
    //绑定数据：把第 position 个数据填入 ViewHolder
    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
        //先把当前位置的列表取出来
        Todo todo = todos.get(position);
        //用函数把数都填进去,根据情况修改数据
        holder.bind(todo);
    }

    @Override
    public int getItemCount() {
        return todos.size();
    }



    public interface OnItemClickListener {
        void onItemClick(Todo todo);
        void onItemLongClick(Todo todo);
        void onDeleteClick(Todo todo);
    }
    
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    // 更新数据集：使用 DiffUtil 计算差异并更新列表
    public void setTodos(List<Todo> todos) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return TodoAdapter.this.todos.size();
            }
            
            @Override
            public int getNewListSize() {
                return todos.size();
            }
            // 2. 判断是否是同一个 Item：根据 Todo 的 id 来判断
            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return TodoAdapter.this.todos.get(oldItemPosition).getId() == 
                       todos.get(newItemPosition).getId();
            }
            // 3. 判断内容是否相同：比较 Todo 的标题、完成状态和提醒状态    
            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                Todo oldTodo = TodoAdapter.this.todos.get(oldItemPosition);
                Todo newTodo = todos.get(newItemPosition);
                return oldTodo.getTitle().equals(newTodo.getTitle()) &&
                       oldTodo.isCompleted() == newTodo.isCompleted() &&
                       oldTodo.isReminderTriggered() == newTodo.isReminderTriggered();
            }
        });
        // 4. 应用差异：更新列表数据
        this.todos = todos;
        diffResult.dispatchUpdatesTo(this);
    }


    class TodoViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewTitle;// 标题文本视图
        private TextView textViewReminderTime;// 提醒时间文本视图
        private CheckBox checkBoxCompleted;// 完成状态复选框
        private ImageButton buttonDelete;// 删除按钮
        private ImageView imageViewReminder;// 提醒图标
        // 3. 绑定数据：把 Todo 对象的属性填充到 ViewHolder 中的视图组件
        public TodoViewHolder(@NonNull View itemView) {
            super(itemView);
            //初始化
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewReminderTime = itemView.findViewById(R.id.textViewReminderTime);
            checkBoxCompleted = itemView.findViewById(R.id.checkBoxCompleted);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
            imageViewReminder = itemView.findViewById(R.id.imageViewReminder);
            //单击整个项目
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(todos.get(position));
                }
            });
            
            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemLongClick(todos.get(position));
                }
                return true;
            });
            //勾选复选框，划对勾
            checkBoxCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    Todo todo = todos.get(position);
                    if (!todo.canBeCompleted()) {
                        checkBoxCompleted.setChecked(false);
                        return;
                    }
                    todo.setCompleted(isChecked);
                    listener.onItemClick(todo);
                }
            });
            //点击删除按钮
            buttonDelete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDeleteClick(todos.get(position));
                }
            });
        }
        
        public void bind(Todo todo) {
            textViewTitle.setText(todo.getTitle());//设置标题
            checkBoxCompleted.setChecked(todo.isCompleted());//设置复选框
            
            if (todo.hasReminder()) {
                imageViewReminder.setVisibility(View.VISIBLE);//如果设置了时间，那么就让他显示出来
                
                if (todo.isReminderTriggered()) {
                    //时间到了，设置图标，然后把时间隐藏
                    imageViewReminder.setImageResource(R.drawable.ic_alarm_triggered);
                    textViewReminderTime.setVisibility(View.GONE);
                } else {
                    //没有，设置图标，然后显示设置的时间
                    imageViewReminder.setImageResource(R.drawable.ic_alarm_pending);
                    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
                    String reminderTimeStr = sdf.format(new Date(todo.getReminderTime()));
                    textViewReminderTime.setText(reminderTimeStr);
                    textViewReminderTime.setVisibility(View.VISIBLE);
                }
            } else {
                //没有设置时间，那么就不显示图标
                imageViewReminder.setVisibility(View.GONE);
                textViewReminderTime.setVisibility(View.GONE);
            }

            //完成了
            if (todo.isCompleted()) {
                textViewTitle.setAlpha(0.5f);
                checkBoxCompleted.setEnabled(true);
            } else {
                textViewTitle.setAlpha(1.0f);
                checkBoxCompleted.setEnabled(todo.canBeCompleted());
            }
        }
    }
}
