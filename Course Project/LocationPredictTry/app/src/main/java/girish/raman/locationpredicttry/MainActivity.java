package girish.raman.locationpredicttry;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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

        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        ((TextView) findViewById(R.id.logData)).setText(intent.getStringExtra("locationAnalysisData"));
                    }
                }, new IntentFilter(LocationAnalyzeService.LOCATION_ANALYSIS_RESULT_BROADCAST)
        );
        try {
            /*db.execSQL("ALTER TABLE locationLog ADD accuracy TEXT;");
            db.execSQL("ALTER TABLE locationLog ADD day TEXT;");
            db.execSQL("ALTER TABLE locationLog ADD month TEXT;");
            db.execSQL("UPDATE locationLog set month = '1';");
            db.execSQL("UPDATE locationLog set day = '14' WHERE dayOfWeek = '1';");
            db.execSQL("UPDATE locationLog set day = '15' WHERE dayOfWeek = '2';");
            db.execSQL("UPDATE locationLog set day = '16' WHERE dayOfWeek = '3';");
            db.execSQL("UPDATE locationLog set day = '17' WHERE dayOfWeek = '4';");
            db.execSQL("UPDATE locationLog set day = '18' WHERE dayOfWeek = '5';");
            db.execSQL("UPDATE locationLog set day = '19' WHERE dayOfWeek = '6';");
            db.execSQL("UPDATE locationLog set day = '20' WHERE dayOfWeek = '7';");*/
        } catch (Exception e) {
            Log.e("Ex", e.getMessage());
        }
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
                final Map<String, Integer> uniqueLocations = new HashMap<>();

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
                        for (Object o : uniqueLocations.entrySet()) {
                            Map.Entry entry = (Map.Entry) o;
                            textView.append(String.valueOf(entry.getKey()) + " - " + String.valueOf(entry.getValue()) + "\n\n");
                        }
                        dialog.dismiss();
                    }
                });

            }
        }).start();
    }

    public void setAddresses(View view) {

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

                Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                List<Address> addressList = null;

                while (!cursor.isAfterLast()) {
                    try {
                        if (cursor.getString(6).equals("na") || cursor.getString(6).equals("")) {
                            addressList = geocoder.getFromLocation(Double.parseDouble(cursor.getString(3)), Double.parseDouble(cursor.getString(4)), 1);
                            Address addr = addressList.get(0);
                            String address = addr.getAddressLine(0);
                            String city = addr.getLocality();
                            String state = addr.getAdminArea();
                            String country = addr.getCountryName();
                            String fullAddress = address + " " + city + " " + state + " " + country;

                            String dayOfWeek = cursor.getString(0);
                            String hour = cursor.getString(1);
                            String minute = cursor.getString(2);

                            db.execSQL("UPDATE locationLog SET address = '" + fullAddress + "' WHERE dayOfWeek = '" + dayOfWeek + "' AND hour = '" + hour + "' AND minute = '" + minute + "';");
                        }
                    } catch (final IOException e) {
                        e.printStackTrace();
                        Log.e("Exception in setAddresses", e.getMessage());
                    }
                    cursor.moveToNext();
                }
                cursor.close();


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                });


            }
        }).start();
    }

    public void analyze(View view) {
        dialog = ProgressDialog.show(MainActivity.this, null, "Please wait...", true);
        Intent intent = new Intent(this, LocationAnalyzeService.class);
        intent.putExtra("dayOfWeek", Integer.parseInt(((TextView)findViewById(R.id.dayOfWeek)).getText().toString()));
        intent.putExtra("day", Integer.parseInt(((TextView)findViewById(R.id.day)).getText().toString()));
        startService(intent);
    }

    public void changeTo710(View view) {
        db.execSQL("UPDATE locationLog set address = '710 Hardy Dr Tempe Arizona United States' WHERE address = '409 S Westfall Ave Tempe Arizona United States';");
        db.execSQL("UPDATE locationLog set address = '710 Hardy Dr Tempe Arizona United States' WHERE address = 'W Apartment Tempe Arizona United States';");
    }
}