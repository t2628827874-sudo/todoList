package com.example.note.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.note.entity.Todo;
import com.example.note.repository.TodoRepository;

import java.util.List;

public class TodoViewModel extends AndroidViewModel {
    
    private TodoRepository repository;
    private LiveData<List<Todo>> allTodos;//所有待办事项
    private LiveData<List<Todo>> activeTodos;//未完成待办事项
    private LiveData<List<Todo>> completedTodos;//已完成待办事项
    
    public TodoViewModel(@NonNull Application application) {
        super(application);
        repository = new TodoRepository(application);
        allTodos = repository.getAllTodos();
        activeTodos = repository.getActiveTodos();
        completedTodos = repository.getCompletedTodos();
    }
    
    public LiveData<List<Todo>> getAllTodos() {
        return allTodos;
    }
    
    public LiveData<List<Todo>> getActiveTodos() {
        return activeTodos;
    }
    
    public LiveData<List<Todo>> getCompletedTodos() {
        return completedTodos;
    }
    
    public void insert(Todo todo) {
        repository.insert(todo);
    }
    
    public void update(Todo todo) {
        repository.update(todo);
    }
    
    public void delete(Todo todo) {
        repository.delete(todo);
    }
    
    public void deleteAll() {
        repository.deleteAll();
    }
    
    public TodoRepository getRepository() {
        return repository;
    }
}
