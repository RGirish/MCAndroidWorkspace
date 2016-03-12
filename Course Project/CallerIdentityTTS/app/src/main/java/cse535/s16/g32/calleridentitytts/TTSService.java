package cse535.s16.g32.calleridentitytts;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.util.Locale;

public class TTSService extends Service implements TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener {

    private TextToSpeech tts;
    private String textToSpeak;

    @Override
    public void onCreate() {
        textToSpeak = "Incoming Call";
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        textToSpeak = intent.getStringExtra("textToSpeak");
        tts = new TextToSpeech(this, this);
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
            }

            @Override
            public void onDone(String utteranceId) {
                stopSelf();
            }

            @Override
            public void onError(String utteranceId) {
            }
        });
        return START_NOT_STICKY;
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }

    @Override
    public void onUtteranceCompleted(String uttId) {
        stopSelf();
    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}