package com.example.note.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.note.entity.Todo;

import java.util.List;

@Dao
public interface TodoDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Todo todo);
    
    @Update
    void update(Todo todo);
    
    @Delete
    void delete(Todo todo);
    
    @Query("DELETE FROM todos")
    void deleteAll();
    
    @Query("SELECT * FROM todos ORDER BY createdAt DESC")
    LiveData<List<Todo>> getAllTodos();
    
    @Query("SELECT * FROM todos WHERE isCompleted = 0 ORDER BY createdAt DESC")
    LiveData<List<Todo>> getActiveTodos();
    
    @Query("SELECT * FROM todos WHERE isCompleted = 1 ORDER BY createdAt DESC")
    LiveData<List<Todo>> getCompletedTodos();
    
    @Query("SELECT * FROM todos WHERE id = :id")
    Todo getTodoById(int id);
    
    @Query("SELECT * FROM todos ORDER BY createdAt DESC")
    List<Todo> getAllTodosSync();
}
