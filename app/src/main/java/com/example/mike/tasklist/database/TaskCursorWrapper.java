package com.example.mike.tasklist.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.example.mike.tasklist.Task;

import java.util.Date;
import java.util.UUID;

public class TaskCursorWrapper extends CursorWrapper{
    public TaskCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Task getTask() {
        String uuidString = getString(getColumnIndex(TaskDbSchema.TaskTable.Cols.UUID));
        String title = getString(getColumnIndex(TaskDbSchema.TaskTable.Cols.TITLE));
        String description = getString(getColumnIndex(TaskDbSchema.TaskTable.Cols.DESCRIPTION));
        long date = getLong(getColumnIndex(TaskDbSchema.TaskTable.Cols.DATE));
        long reminder = getLong(getColumnIndex(TaskDbSchema.TaskTable.Cols.REMINDER));
        int hasReminder = getInt(getColumnIndex(TaskDbSchema.TaskTable.Cols.HAS_REMINDER));

        Task task = new Task(UUID.fromString(uuidString));
        task.setTitle(title);
        task.setDescription(description);
        task.setDate(new Date(date));
        if (reminder != 0) {
            task.setReminder(new Date(reminder));
        } else {
            task.setReminder(null);
        }
        task.setHasReminder(hasReminder);

        return task;
    }
}
