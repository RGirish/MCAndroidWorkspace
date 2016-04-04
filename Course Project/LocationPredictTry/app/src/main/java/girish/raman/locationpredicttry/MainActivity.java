package girish.raman.locationpredicttry;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    static ProgressDialog dialog;
    final int REQUEST_FINE_LOCATION = 100;
    final int REQUEST_EXTERNAL_STORAGE = 101;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
        } else {
            createDatabase();
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
        } else {
            Intent intent = new Intent(this, LocationLogService.class);
            startService(intent);
        }

        Intent intent = new Intent(this, LocationAnalyzeService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis(), 4838400000L, pendingIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {

            case REQUEST_EXTERNAL_STORAGE: {
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(MainActivity.this, "External Storage Access needed!", Toast.LENGTH_SHORT).show();
                } else {
                    createDatabase();
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
                    }
                }
            }

            case REQUEST_FINE_LOCATION: {
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(MainActivity.this, "GPS Access Permission needed!", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(this, LocationLogService.class);
                    startService(intent);
                }
            }
        }
    }

    private void createDatabase() {
        db = openOrCreateDatabase(Environment.getExternalStorageDirectory() + File.separator + "location.db", SQLiteDatabase.CREATE_IF_NECESSARY, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS locationLog(dayOfWeek TEXT, hour TEXT, minute TEXT, latitude TEXT, longitude TEXT, speed TEXT, address TEXT, day TEXT, month TEXT, accuracy TEXT);");
    }

    public void onClickSeeLoggedData(View view) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog = ProgressDialog.show(MainActivity.this, null, "Please wait...", true);
                    }
                });

                Cursor cursor = db.rawQuery("SELECT * FROM locationLog;", null);
                cursor.moveToFirst();
                final HashMap<String, Integer> uniqueLocations = new HashMap<>();

                while (!cursor.isAfterLast()) {
                    String fullAddress = cursor.getString(6);
                    if (!uniqueLocations.containsKey(fullAddress)) {
                        uniqueLocations.put(fullAddress, 1);
                    } else {
                        uniqueLocations.put(fullAddress, uniqueLocations.get(fullAddress) + 1);
                    }
                    cursor.moveToNext();
                }
                cursor.close();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView textView = (TextView) MainActivity.this.findViewById(R.id.logData);
                        textView.setText("");
                        for (Object o : sortHashMapByValues(uniqueLocations).entrySet()) {
                            Map.Entry entry = (Map.Entry) o;
                            textView.append(String.valueOf(entry.getKey()) + " - " + String.valueOf(entry.getValue()) + "\n\n");
                        }
                        dialog.dismiss();
                    }
                });

            }
        }).start();
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

    public void analyze(View view) {
        dialog = ProgressDialog.show(MainActivity.this, null, "Please wait...", true);
        Intent intent = new Intent(this, LocationAnalyzeService.class);
        startService(intent);
    }
}