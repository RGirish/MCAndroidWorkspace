package com.karthik.profile;

import android.Manifest;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by mkart on 3/15/2016.
 */
public class LocationTrackerService extends Service implements LocationListener {

    private final double homeLocationLatitude = 33.42419586;
    private final double homeLocationLongtitude = -111.94937077;

    public LocationTrackerService() {
    }

    @Override
    public void onCreate() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 0, this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY_COMPATIBILITY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.e("Called for first time", "Test");
        String speed, latitude, longitude, dayOfWeek, hour, minute, day, month, accuracy, fullAddress = "na";
        Location home = new Location("");
        home.setLatitude(homeLocationLatitude);
        home.setLongitude(homeLocationLongtitude);

        Log.e("Location La", String.valueOf(location.getLatitude()));
        Log.e("Location Lo", String.valueOf(location.getLongitude()));

        float distanceInMeters = location.distanceTo(home);

       // Log.e("Distance",String.valueOf(distanceInMeters));
       // Toast.makeText(LocationTrackerService.this, String.valueOf(distanceInMeters), Toast.LENGTH_SHORT).show();
        if(distanceInMeters > 3){
            Toast.makeText(LocationTrackerService.this, "Out of Home", Toast.LENGTH_SHORT).show();
            //Switch off Wifi
            WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            wifi.setWifiEnabled(false); // true or false to activate/deactivate wifi
            //Switch Off bluetooth
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if(mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.disable();
            }

            //Changing phone to loud mode
            AudioManager am= (AudioManager) getBaseContext().getSystemService(Context.AUDIO_SERVICE);
            am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            am.setStreamVolume(
                    AudioManager.STREAM_RING,
                    am.getStreamMaxVolume(AudioManager.STREAM_RING),
                    0);
        }

        Log.e("Updated", "Updated");
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

