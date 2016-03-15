package com.example.letmetellyou;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import java.util.Calendar;

public class MainActivity extends Activity implements View.OnTouchListener {

    static final int MIN_DISTANCE = 100;
    private float downX;
    private float downY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(this, ButtonPatternListenerService.class));
        speakApplicationHasOpened();
        findViewById(R.id.main).setOnTouchListener(this);
    }

    void startTTSService(String textToSpeak) {
        Intent intent = new Intent(this, TTSService.class);
        intent.putExtra("textToSpeak", textToSpeak);
        startService(intent);
    }

    public void speakApplicationHasOpened() {
        startTTSService("The application has been opened!");
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                downX = event.getX();
                downY = event.getY();
                return true;
            }
            case MotionEvent.ACTION_UP: {
                float upX = event.getX();
                float upY = event.getY();

                float deltaX = downX - upX;
                float deltaY = downY - upY;

                if (Math.abs(deltaX) > MIN_DISTANCE) {
                    if (deltaX < 0) {
                        this.onLeftToRightSwipe();
                        return true;
                    }
                    if (deltaX > 0) {
                        this.onRightToLeftSwipe();
                        return true;
                    }
                } else if (Math.abs(deltaY) > MIN_DISTANCE) {
                    if (deltaY < 0) {
                        this.onTopToBottomSwipe();
                        return true;
                    }
                    if (deltaY > 0) {
                        //this.onBottomToTopSwipe();
                        return true;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public void onLeftToRightSwipe() {
        speakOutTheTime();
    }

    public void onTopToBottomSwipe() {
        speakOutTheDate();
    }

    public void onRightToLeftSwipe() {
        speakOutTheDay();
    }

    public void speakOutTheTime() {
        Calendar calendar = Calendar.getInstance();
        String textToSpeak = calendar.get(Calendar.HOUR_OF_DAY) + " hours " + calendar.get(Calendar.MINUTE) + " minutes " + calendar.get(Calendar.SECOND) + " seconds ";
        startTTSService(textToSpeak);
    }

    public void speakOutTheDate() {
        Calendar calendar = Calendar.getInstance();
        String month = null;
        String textToSpeak;

        switch (calendar.get(Calendar.MONTH)) {
            case 0:
                month = "January";
                break;
            case 1:
                month = "February";
                break;
            case 2:
                month = "March";
                break;
            case 3:
                month = "April";
                break;
            case 4:
                month = "May";
                break;
            case 5:
                month = "June";
                break;
            case 6:
                month = "July";
                break;
            case 7:
                month = "August";
                break;
            case 8:
                month = "September";
                break;
            case 9:
                month = "October";
                break;
            case 10:
                month = "November";
                break;
            case 11:
                month = "December";
                break;

        }
        if (calendar.get(Calendar.DAY_OF_MONTH) == 1 || calendar.get(Calendar.DAY_OF_MONTH) == 21 || calendar.get(Calendar.DAY_OF_MONTH) == 31) {
            textToSpeak = calendar.get(Calendar.DAY_OF_MONTH) + "st of " + month + " " + Integer.toString(calendar.get(Calendar.YEAR)).substring(1);
        } else if (calendar.get(Calendar.DAY_OF_MONTH) == 2 || calendar.get(Calendar.DAY_OF_MONTH) == 22) {
            textToSpeak = calendar.get(Calendar.DAY_OF_MONTH) + "nd of " + month + " " + Integer.toString(calendar.get(Calendar.YEAR)).substring(1);
        } else if (calendar.get(Calendar.DAY_OF_MONTH) == 3 || calendar.get(Calendar.DAY_OF_MONTH) == 23) {
            textToSpeak = calendar.get(Calendar.DAY_OF_MONTH) + "rd of " + month + " " + Integer.toString(calendar.get(Calendar.YEAR)).substring(1);
        } else {
            textToSpeak = calendar.get(Calendar.DAY_OF_MONTH) + "th " + month + " " + Integer.toString(calendar.get(Calendar.YEAR)).substring(1);
        }
        startTTSService(textToSpeak);
    }

    public void speakOutTheDay() {
        Calendar calendar = Calendar.getInstance();
        String textToSpeak = null;
        switch (calendar.get(Calendar.DAY_OF_WEEK)) {
            case 1:
                textToSpeak = "Monday";
                break;
            case 2:
                textToSpeak = "Tuesday";
                break;
            case 3:
                textToSpeak = "Wednesday";
                break;
            case 4:
                textToSpeak = "Thursday";
                break;
            case 5:
                textToSpeak = "Friday";
                break;
            case 6:
                textToSpeak = "Saturday";
                break;
            case 7:
                textToSpeak = "Sunday";
                break;

        }
        startTTSService(textToSpeak);
    }
}