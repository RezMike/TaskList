package com.example.mike.tasklist;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.example.mike.tasklist.database.TaskBaseHelper;
import com.example.mike.tasklist.database.TaskCursorWrapper;
import com.example.mike.tasklist.database.TaskDbSchema.TaskTable;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

public class TaskLab {
    private static TaskLab sTaskLab;

    private Context mContext;
    private SQLiteDatabase mDatabase;

    public static TaskLab get(Context context){
        if (sTaskLab == null) {
            sTaskLab = new TaskLab(context);
        }
        return sTaskLab;
    }

    private TaskLab(Context context){
        mContext = context.getApplicationContext();
        mDatabase = new TaskBaseHelper(mContext).getWritableDatabase();
    }

    public void addTask(Task task){
        ContentValues values = getContentValues(task);
        mDatabase.insert(TaskTable.NAME, null, values);
    }

    public ArrayList<Task> getTasks(){
        ArrayList<Task> tasks = new ArrayList<>();
        TaskCursorWrapper cursor = queryTasks(null, null);
        try{
            cursor.moveToFirst();
            while (!cursor.isAfterLast()){
                tasks.add(cursor.getTask());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return tasks;
    }

    public Task getTask(UUID id){
        TaskCursorWrapper cursor = queryTasks(
                TaskTable.Cols.UUID + " = ?",
                new String[]{id.toString()}
        );

        try{
            if (cursor.getCount() == 0){
                return null;
            }
            cursor.moveToFirst();
            return cursor.getTask();
        } finally {
            cursor.close();
        }
    }

    public File getPhotoFile(Task task){
        File externalFilesDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (externalFilesDir == null){
            return null;
        }
        return new File(externalFilesDir, task.getPhotoFilename());
    }

    public void updateTask(Task task){
        String uuidString = task.getId().toString();
        ContentValues values = getContentValues(task);

        mDatabase.update(TaskTable.NAME, values,
                TaskTable.Cols.UUID + " = ?",
                new String[]{uuidString});
    }

    public void deleteTask(Task task, Context context){
        task.deletePhoto(context);
        if (task.getReminder() != null) {
            Intent intent = new Intent(context, NotificationService.class);
            intent.putExtra(NotificationService.DATE, task.getReminder().getTime());
            intent.addFlags(NotificationService.DELETE);
            context.startService(intent);
        }
        String uuidString = task.getId().toString();
        mDatabase.delete(TaskTable.NAME,
                TaskTable.Cols.UUID + " = ?",
                new String[]{uuidString});
    }

    private static ContentValues getContentValues(Task task){
        ContentValues values = new ContentValues();
        values.put(TaskTable.Cols.UUID, task.getId().toString());
        values.put(TaskTable.Cols.TITLE, task.getTitle());
        values.put(TaskTable.Cols.DESCRIPTION, task.getDescription());
        values.put(TaskTable.Cols.DATE, task.getDate().getTime());
        if (task.getReminder() != null) {
            values.put(TaskTable.Cols.REMINDER, task.getReminder().getTime());
        } else {
            values.put(TaskTable.Cols.REMINDER, 0);
        }
        values.put(TaskTable.Cols.HAS_REMINDER, task.hasReminder());

        return values;
    }

    private TaskCursorWrapper queryTasks(String whereClause, String[] whereArgs){
        Cursor cursor = mDatabase.query(
                TaskTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );
        return new TaskCursorWrapper(cursor);
    }
}