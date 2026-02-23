package com.example.note.repository;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.note.dao.TodoDao;
import com.example.note.database.AppDatabase;
import com.example.note.entity.Todo;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TodoRepository {
    
    private TodoDao todoDao;
    private LiveData<List<Todo>> allTodos;//所有待办事项
    private LiveData<List<Todo>> activeTodos;//未完成待办事项
    private LiveData<List<Todo>> completedTodos;//已完成待办事项
    private ExecutorService executorService;//线程池，用于异步执行数据库操作
    
    public TodoRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);//获取数据库实例
        todoDao = database.todoDao();//获取待办事项数据访问对象
        allTodos = todoDao.getAllTodos();//获取所有待办事项
        activeTodos = todoDao.getActiveTodos();//获取未完成待办事项
        completedTodos = todoDao.getCompletedTodos();//获取已完成待办事项
        executorService = Executors.newSingleThreadExecutor();//创建线程池，线程数为1
    }
    
    public TodoRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        todoDao = database.todoDao();
        allTodos = todoDao.getAllTodos();
        activeTodos = todoDao.getActiveTodos();
        completedTodos = todoDao.getCompletedTodos();
        executorService = Executors.newSingleThreadExecutor();
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
        executorService.execute(() -> todoDao.insert(todo));
    }
    
    public void update(Todo todo) {
        executorService.execute(() -> todoDao.update(todo));
    }
    
    public void delete(Todo todo) {
        executorService.execute(() -> todoDao.delete(todo));
    }
    
    public void deleteAll() {
        executorService.execute(todoDao::deleteAll);
    }
    
    public Todo getTodoById(int id) {
        final Todo[] todo = new Todo[1];
        try {
            executorService.submit(() -> {
                todo[0] = todoDao.getTodoById(id);
            }).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return todo[0];
    }
    
    public List<Todo> getAllTodosSync() {
        final List<Todo>[] todos = new List[1];
        try {
            executorService.submit(() -> {
                todos[0] = todoDao.getAllTodosSync();
            }).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return todos[0];
    }
}
