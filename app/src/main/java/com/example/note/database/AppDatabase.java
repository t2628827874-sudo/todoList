package com.example.note.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.note.dao.TodoDao;
import com.example.note.entity.Todo;
//数据库文件
@Database(entities = {Todo.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    //数据库单例实例
    private static AppDatabase instance;
    
    //声明一个方法，返回TodoDao类型的对象
    public abstract TodoDao todoDao();
    
    //创建数据库实例，使用单例模式确保只有一个实例，名字是note_database
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "note_database")
                    .fallbackToDestructiveMigration()
                    .addCallback(roomCallback)
                    .addMigrations(MIGRATION_1_2)
                    .build();
        }
        return instance;
    }
    //数据库生命周期回调
    private static final RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
        }
    };
    //数据库版本迁移策略
    private static final androidx.room.migration.Migration MIGRATION_1_2 = new androidx.room.migration.Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE todos ADD COLUMN reminderTime INTEGER");
            database.execSQL("ALTER TABLE todos ADD COLUMN isReminderTriggered INTEGER NOT NULL DEFAULT 0");
        }
    };
}
