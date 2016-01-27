package com.example.austin.demoapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

public class ContinueService extends Service {

    public IBinder onBind(Intent arg0) {
        return null;
    }//onBind

    @Override
    // Keeps app running in background
    public int onStartCommand(Intent intent, int flags, int startId) {
        display();
        return START_STICKY;
    }//onStartCommand

    @Override
    public void onDestroy() {
        stopForeground(true);
    }

    private void display() {
        int notifyID = 1;

        NotificationCompat.Builder note = new NotificationCompat.Builder(this)
                                        .setSmallIcon(R.mipmap.icon)
                                        .setContentTitle("BAC-track")
                                        .setContentText("You are drinking.");
        Intent BAC = new Intent(this, DemoActivity.class);
        PendingIntent resultPendingIntent =
                            PendingIntent.getActivity(this,0,BAC,PendingIntent.FLAG_UPDATE_CURRENT);

        note.setContentIntent(resultPendingIntent);

        startForeground(notifyID, note.build());
    }


}//ContinueService
