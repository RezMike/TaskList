package com.example.mike.tasklist;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;

public class NotificationReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Resources resources = context.getResources();
        Notification notification = new Notification.Builder(context)
                .setTicker(resources.getString(R.string.notification_title))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(resources.getString(R.string.notification_title))
                .setContentText(resources.getString(R.string.notification_text))
                .setContentIntent(PendingIntent.getActivity(context, 0,
                        new Intent(context, TaskListActivity.class), PendingIntent.FLAG_CANCEL_CURRENT))
                .setAutoCancel(true)
                .setSound(Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.notification_sound))
                .build();
        nm.notify(1, notification);
        Intent i = new Intent(context, NotificationService.class);
        i.putExtra(NotificationService.DATE, intent.getLongExtra(NotificationService.DATE, 0));
        i.setFlags(NotificationService.REMOVE);
        context.startService(i);
    }
}
