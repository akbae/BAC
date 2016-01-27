package com.example.austin.demoapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ContinueService extends Service {

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
}
