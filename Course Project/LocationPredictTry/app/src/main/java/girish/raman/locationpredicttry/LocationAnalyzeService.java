package girish.raman.locationpredicttry;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LocationAnalyzeService extends Service {

    static HashMap<String, Integer> mondayOutTimes = new HashMap<>();
    static HashMap<String, Integer> tuesdayOutTimes = new HashMap<>();
    static HashMap<String, Integer> wednesdayOutTimes = new HashMap<>();
    static HashMap<String, Integer> thursdayOutTimes = new HashMap<>();
    static HashMap<String, Integer> fridayOutTimes = new HashMap<>();
    static HashMap<String, Integer> saturdayOutTimes = new HashMap<>();
    static HashMap<String, Integer> sundayOutTimes = new HashMap<>();
    SQLiteDatabase db;

    public LocationAnalyzeService() {
    }

    @Override
    public void onCreate() {
        db = openOrCreateDatabase(Environment.getExternalStorageDirectory() + File.separator + "location.db", SQLiteDatabase.CREATE_IF_NECESSARY, null);
        startAnalysis();
    }

    private void startAnalysis() {
        Cursor cursor = db.rawQuery("SELECT day, month FROM locationLog LIMIT 1", null);
        cursor.moveToFirst();
        int day = Integer.parseInt(cursor.getString(0));
        int month = Integer.parseInt(cursor.getString(1));
        for (int i = 0; i < 42; ++i) {
            findAndSetOutTimes(day, month);
            String nextDate = getNextDay(day, month, 2016);
            String[] parts = nextDate.split("/");
            day = Integer.parseInt(parts[0]);
            month = Integer.parseInt(parts[1]);
        }
        cursor.close();
    }

    private String getNextDay(int day, int month, int year) {
        String modifiedFromDate = String.valueOf(day) + '/' + String.valueOf(month) + '/' + String.valueOf(year);
        int MILLIS_IN_DAY = 1000 * 60 * 60 * 24;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
        Date dateSelectedFrom = null;
        try {
            dateSelectedFrom = dateFormat.parse(modifiedFromDate);
        } catch (ParseException e) {
            Log.e("getNextDay()", e.toString());
        }
        return dateFormat.format(dateSelectedFrom.getTime() + MILLIS_IN_DAY);
    }

    private void findAndSetOutTimes(int day, int month) {
        Cursor cursor = db.rawQuery("SELECT hour, minute, address, dayOfWeek FROM locationLog WHERE day = '" + day + "' AND month ='" + month + "';", null);
        cursor.moveToFirst();
        int dayOfWeek;
        try {
            dayOfWeek = Integer.parseInt(cursor.getString(3));
        } catch (Exception e) {
            return;
        }
        List<String> historyOfOut = new ArrayList<>();

        String prevAddress;
        String currAddress = "";
        while (!cursor.isAfterLast()) {
            prevAddress = currAddress;
            currAddress = cursor.getString(2);
            String hour_minute = cursor.getString(0) + ":" + cursor.getString(1);
            if (!currAddress.equals(prevAddress)) {
                int i;
                //prevAddress not updated in this for loop - it will contain addressBeforeGoingOut
                try {
                    for (i = 0; i < 4 && !currAddress.equals(prevAddress); ++i) {
                        currAddress = cursor.getString(2);
                        cursor.moveToNext();
                    }
                    if (i == 4) {
                        historyOfOut.add(hour_minute);
                        for (int i1 = 0; i1 < i; i1++) {
                            cursor.moveToPrevious();
                        }
                        int continuousCount = 0;
                        currAddress = cursor.getString(2);
                        while (continuousCount < 5) {
                            prevAddress = currAddress;
                            currAddress = cursor.getString(2);
                            if (prevAddress.equals(currAddress)) {
                                continuousCount++;
                            } else {
                                continuousCount = 0;
                            }
                            cursor.moveToNext();
                        }
                        cursor.moveToPrevious();
                    } else {
                        for (int i1 = 0; i1 < i; i1++) {
                            cursor.moveToPrevious();
                        }
                    }
                } catch (CursorIndexOutOfBoundsException ignored) {
                }
            }
            cursor.moveToNext();
        }

        for (String timeGoneOut : historyOfOut) {
            String[] parts = timeGoneOut.split(":");
            if (!parts[0].equals("0")) {
                addOutTimeToCorrespondingHashMap(dayOfWeek, parts[0]);
            }
        }

        setAlarmsForWeatherUpdates();
        cursor.close();
        stopSelf();
    }

    private void setAlarmsForWeatherUpdates() {
        mondayOutTimes = sortHashMapByValues(mondayOutTimes);
        tuesdayOutTimes = sortHashMapByValues(tuesdayOutTimes);
        wednesdayOutTimes = sortHashMapByValues(wednesdayOutTimes);
        thursdayOutTimes = sortHashMapByValues(thursdayOutTimes);
        fridayOutTimes = sortHashMapByValues(fridayOutTimes);
        saturdayOutTimes = sortHashMapByValues(saturdayOutTimes);
        sundayOutTimes = sortHashMapByValues(sundayOutTimes);

        Iterator iterator = mondayOutTimes.entrySet().iterator();
        Map.Entry pair = (Map.Entry) iterator.next();
        int hour = Integer.parseInt(pair.getKey().toString()) - 1;
        int minute = 30;
        Calendar now = Calendar.getInstance();
        if (now.get(Calendar.DAY_OF_WEEK) == 1) {
            if (now.get(Calendar.HOUR_OF_DAY) < hour) {
                //diff bet now and today's 'hour'
            } else {
                //diff bet now and the next sunday's 'hour'
            }
        } else {
            //diff bet now and the next sunday's 'hour'
        }
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, WeatherNotificationService.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), AlarmManager.INTERVAL_DAY * 7, pendingIntent);
    }

    public LinkedHashMap sortHashMapByValues(HashMap passedMap) {
        List mapKeys = new ArrayList(passedMap.keySet());
        List mapValues = new ArrayList(passedMap.values());
        Collections.sort(mapValues);
        Collections.sort(mapKeys);

        LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<>();
        for (Object val : mapValues) {
            for (Object key : mapKeys) {
                String comp1 = passedMap.get(key).toString();
                String comp2 = val.toString();
                if (comp1.equals(comp2)) {
                    passedMap.remove(key);
                    mapKeys.remove(key);
                    sortedMap.put((String) key, (Integer) val);
                    break;
                }
            }
        }
        return sortedMap;
    }

    private void addOutTimeToCorrespondingHashMap(int dayOfWeek, String timeGoneOut) {
        switch (dayOfWeek) {
            case Calendar.MONDAY:
                if (mondayOutTimes.containsKey(timeGoneOut)) {
                    mondayOutTimes.put(timeGoneOut, mondayOutTimes.get(timeGoneOut) + 1);
                } else {
                    mondayOutTimes.put(timeGoneOut, 1);
                }
                break;
            case Calendar.TUESDAY:
                if (tuesdayOutTimes.containsKey(timeGoneOut)) {
                    tuesdayOutTimes.put(timeGoneOut, tuesdayOutTimes.get(timeGoneOut) + 1);
                } else {
                    tuesdayOutTimes.put(timeGoneOut, 1);
                }
                break;
            case Calendar.WEDNESDAY:
                if (wednesdayOutTimes.containsKey(timeGoneOut)) {
                    wednesdayOutTimes.put(timeGoneOut, wednesdayOutTimes.get(timeGoneOut) + 1);
                } else {
                    wednesdayOutTimes.put(timeGoneOut, 1);
                }
                break;
            case Calendar.THURSDAY:
                if (thursdayOutTimes.containsKey(timeGoneOut)) {
                    thursdayOutTimes.put(timeGoneOut, thursdayOutTimes.get(timeGoneOut) + 1);
                } else {
                    thursdayOutTimes.put(timeGoneOut, 1);
                }
                break;
            case Calendar.FRIDAY:
                if (fridayOutTimes.containsKey(timeGoneOut)) {
                    fridayOutTimes.put(timeGoneOut, fridayOutTimes.get(timeGoneOut) + 1);
                } else {
                    fridayOutTimes.put(timeGoneOut, 1);
                }
                break;
            case Calendar.SATURDAY:
                if (saturdayOutTimes.containsKey(timeGoneOut)) {
                    saturdayOutTimes.put(timeGoneOut, saturdayOutTimes.get(timeGoneOut) + 1);
                } else {
                    saturdayOutTimes.put(timeGoneOut, 1);
                }
                break;
            case Calendar.SUNDAY:
                if (sundayOutTimes.containsKey(timeGoneOut)) {
                    sundayOutTimes.put(timeGoneOut, sundayOutTimes.get(timeGoneOut) + 1);
                } else {
                    sundayOutTimes.put(timeGoneOut, 1);
                }
                break;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

}