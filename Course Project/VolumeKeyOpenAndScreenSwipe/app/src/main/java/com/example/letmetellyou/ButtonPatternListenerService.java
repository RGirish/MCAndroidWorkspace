package com.example.letmetellyou;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

public class ButtonPatternListenerService extends Service {

    Context context;
    SettingsContentObserver mSettingsContentObserver;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        context = this;
        mSettingsContentObserver = new SettingsContentObserver(this, new Handler());
        getApplicationContext().getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, mSettingsContentObserver);
    }

    @Override
    public void onDestroy() {
        getApplicationContext().getContentResolver().unregisterContentObserver(mSettingsContentObserver);
    }
}