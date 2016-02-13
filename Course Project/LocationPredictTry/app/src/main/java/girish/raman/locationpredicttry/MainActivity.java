package girish.raman.locationpredicttry;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {

    final static String logFileName = "location_log.txt";
    final int REQUEST_FINE_LOCATION = 100;
    final int REQUEST_EXTERNAL_STORAGE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
        } else {
            createDatabaseAndFile();
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
        } else {
            Intent intent = new Intent(this, LocationService.class);
            startService(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {

            case REQUEST_EXTERNAL_STORAGE: {
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(MainActivity.this, "External Storage Access needed!", Toast.LENGTH_SHORT).show();
                } else {
                    createDatabaseAndFile();
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
                    }
                }
            }

            case REQUEST_FINE_LOCATION: {
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(MainActivity.this, "GPS Access Permission needed!", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(this, LocationService.class);
                    startService(intent);
                }
            }
        }
    }

    private void createDatabaseAndFile() {
        SQLiteDatabase db = openOrCreateDatabase(Environment.getExternalStorageDirectory() + File.separator + "location.db", SQLiteDatabase.CREATE_IF_NECESSARY, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS locationLog(dayOfWeek TEXT, hour TEXT, minute TEXT, latitude TEXT, longitude TEXT, speed TEXT);");

        File file = new File(Environment.getExternalStorageDirectory() + File.separator + logFileName);
        boolean fileCreated = false;
        if (!file.exists()) {
            try {
                fileCreated = file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (!fileCreated)
                    Toast.makeText(MainActivity.this, "Log File Not Created", Toast.LENGTH_LONG).show();
                else {
                    File logFile = new File(Environment.getExternalStorageDirectory() + File.separator + logFileName);
                    FileWriter writer = null;
                    try {
                        writer = new FileWriter(logFile, true);
                        writer.write("Log File Created\n");
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}