package com.example.mike.tasklist;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class Task {
    private UUID mId;
    private String mTitle;
    private String mDescription;
    private Date mDate;
    private Date mReminder;
    private int mHasReminder;

    public Task(){
        this(UUID.randomUUID());
    }

    public Task(UUID id){
        mId = id;
        mDate = new Date();
        mTitle = "";
        mDescription = "";
    }

    public String toString() {
        return mTitle;
    }

    public UUID getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public Date getDate() {
        return mDate;
    }

    public String getDateToString(){
        SimpleDateFormat format1 = new SimpleDateFormat("d MMMM yyyy, E");
        return format1.format(mDate);
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public Date getTime() {
        return mDate;
    }

    public String getTimeToString(){
        SimpleDateFormat format1 = new SimpleDateFormat("HH:mm");
        return format1.format(mDate);
    }

    public void setTime(Date date) {
        mDate = date;
    }

    public Date getReminder(){
        return mReminder;
    }

    public void setReminder(Date time){
        mReminder = time;
    }

    public void setHasReminder(boolean hasReminder){
        mHasReminder = hasReminder ? 1 : 0;
    }

    public void setHasReminder(int hasReminder){
        mHasReminder = hasReminder;
    }

    public boolean hasReminder(){
        return (mHasReminder == 1);
    }

    public String getTimeReminderToString(){
        SimpleDateFormat format1 = new SimpleDateFormat("HH:mm, d MMMM yyyy, E");
        return format1.format(mReminder);
    }

    public void deletePhoto(Context context){
        File file = TaskLab.get(context).getPhotoFile(this);
        if (file != null && file.exists()) {
            if (!file.delete()) {
                Toast.makeText(context, R.string.error_deleting_jpeg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public String getPhotoFilename(){
        return "IMG_" + getId().toString() + ".jpg";
    }
}
