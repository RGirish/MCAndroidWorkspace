package girish.raman.locationpredicttry;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

public class LocationService extends Service implements LocationListener {

    final static String logFileName = "location_log.txt";
    SQLiteDatabase db;

    public LocationService() {
    }

    @Override
    public void onCreate() {
        db = openOrCreateDatabase("location.db", SQLiteDatabase.CREATE_IF_NECESSARY, null);
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 300000, 20, this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onLocationChanged(Location location) {
        String speed, latitude, longitude, dayOfWeek, hour, minute;

        latitude = String.valueOf(location.getLatitude());
        longitude = String.valueOf(location.getLongitude());
        speed = "none";
        if (location.hasSpeed())
            speed = String.valueOf(location.getSpeed());

        Calendar calendar = Calendar.getInstance();
        dayOfWeek = String.valueOf(calendar.get(Calendar.DAY_OF_WEEK));
        hour = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
        minute = String.valueOf(calendar.get(Calendar.MINUTE));

        db.execSQL("INSERT INTO locationLog VALUES('" + dayOfWeek + "','" + hour + "','" + minute + "','" + latitude + "','" + longitude + "','" + speed + "');");
        Log.e("Updated", "Updated");

        File file = new File(Environment.getExternalStorageDirectory() + File.separator + logFileName);
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            writer.write(dayOfWeek + "_" + hour + "_" + minute + "_" + latitude + "_" + longitude + "_" + speed);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}