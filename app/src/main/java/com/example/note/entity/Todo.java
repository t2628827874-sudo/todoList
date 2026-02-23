package com.example.note.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

//RecyclerView的model，也是数据库实体类

@Entity(tableName = "todos")
public class Todo {
    //定义了一个事务由什么构成
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String title;//标题
    private boolean isCompleted;//checkbox是否完成
    private long createdAt;//创建时间
    private Long reminderTime;//提醒时间
    private boolean isReminderTriggered;//是否触发

    //有参构造
    public Todo(String title, boolean isCompleted, long createdAt) {
        this.title = title;
        this.isCompleted = isCompleted;
        this.createdAt = createdAt;
        this.reminderTime = null;
        this.isReminderTriggered = false;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public boolean isCompleted() {
        return isCompleted;
    }
    
    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    
    public Long getReminderTime() {
        return reminderTime;
    }
    
    public void setReminderTime(Long reminderTime) {
        this.reminderTime = reminderTime;
    }
    
    public boolean isReminderTriggered() {
        return isReminderTriggered;
    }
    
    public void setReminderTriggered(boolean reminderTriggered) {
        isReminderTriggered = reminderTriggered;
    }


    //判断待办事项是否设置了提醒时间，如果为null表示没有设置提醒时间
    public boolean hasReminder() {
        return reminderTime != null;
    }
    
    //判断待办事项是否可以被标记为完成，只有在提醒时间触发或者没有设置提醒时间时才可以被标记为完成
    public boolean canBeCompleted() {
        if (!hasReminder()) {
            return true;//没有设置提醒时间，直接返回true
        }
        //提醒时间触发或者当前时间大于等于提醒时间，返回true
        return isReminderTriggered || System.currentTimeMillis() >= reminderTime;
    }
}
