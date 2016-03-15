package com.example.letmetellyou;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;
import android.util.Log;

public class SettingsContentObserver extends ContentObserver {

    int previousVolume;
    Context context;
    String pattern = "UDDU", now = "****";

    public SettingsContentObserver(Context context, Handler handler) {
        super(handler);
        this.context = context;
        AudioManager audio = (AudioManager) this.context.getSystemService(Context.AUDIO_SERVICE);
        previousVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);

        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = audio.getStreamVolume(AudioManager.STREAM_RING);

        int delta = previousVolume - currentVolume;

        if (delta > 0) {
            Log.e("Decreased", "Decreased");
            now = now.substring(1) + "D";
            if (now.equals(pattern)) {
                Intent intent = new Intent();
                intent.setClass(context, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
            previousVolume = currentVolume;
        } else if (delta < 0) {
            Log.e("Increased", "Increased");
            now = now.substring(1) + "U";
            if (now.equals(pattern)) {
                Intent intent = new Intent();
                intent.setClass(context, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
            previousVolume = currentVolume;
        }
    }
}