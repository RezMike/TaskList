package com.example.mike.tasklist;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import java.util.LinkedList;

public class NotificationService extends Service {
    public static final String DATE = "date";
    public static final int ADD = 1;
    public static final int DELETE = 2;
    public static final int REMOVE = 3;

    private static LinkedList<Long> mAlarms;
    private static long setTime;

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int flag = intent.getFlags();
        Long date = intent.getLongExtra(DATE, 0);
        switch (flag){
            case ADD:
                if (mAlarms == null){
                    mAlarms = new LinkedList<>();
                }
                addAlarm(date);
                if (!mAlarms.isEmpty()) setAlarm(mAlarms.get(0));
                break;
            case DELETE:
                deleteAlarm(date);
                break;
            case REMOVE:
                deleteAlarm(date);
                if (!mAlarms.isEmpty()) setAlarm(mAlarms.get(0));
                break;
        }
        return Service.START_NOT_STICKY;
    }

    public static boolean checkAlarm(Long date){
        return mAlarms != null && !mAlarms.isEmpty() && mAlarms.contains(date);
    }

    private void addAlarm(Long mainDate){
        if (mainDate <= System.currentTimeMillis()) return;
        if (mAlarms.isEmpty()){
            mAlarms.add(mainDate);
        } else {
            int i = 0;
            while (i < mAlarms.size() && mAlarms.get(i) < mainDate){
                i++;
            }
            if (i == mAlarms.size()) mAlarms.addLast(mainDate);
            else mAlarms.add(i, mainDate);
        }
    }

    private void deleteAlarm(long mainDate){
        if (mAlarms == null) return;
        if (!mAlarms.isEmpty()){
            if (mAlarms.get(0) < System.currentTimeMillis()) mAlarms.remove(0);
            int i = 0;
            while (i < mAlarms.size() && !mAlarms.get(i).equals(mainDate)) {
                i++;
            }
            if (i < mAlarms.size() && mAlarms.get(i).equals(mainDate)) mAlarms.remove(i);
        }
        AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra(DATE, setTime);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        am.cancel(pendingIntent);
        setTime = 0;
        if (!mAlarms.isEmpty()){
            setAlarm(mAlarms.get(0));
        } else {
            this.stopSelf();
        }
    }

    private void setAlarm(long time){
        if (time < System.currentTimeMillis()) return;
        AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra(DATE, time);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        am.cancel(pendingIntent);
        am.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        setTime = time;
    }
}