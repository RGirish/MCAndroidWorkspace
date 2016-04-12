package girish.raman.locationpredicttry;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class LocationLogService extends Service implements LocationListener {

    SQLiteDatabase db;

    public LocationLogService() {
    }

    @Override
    public void onCreate() {
        db = openOrCreateDatabase(Environment.getExternalStorageDirectory() + File.separator + "location.db", SQLiteDatabase.CREATE_IF_NECESSARY, null);
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 120000, 0, this);
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
        String speed, latitude, longitude, dayOfWeek, hour, minute, day, month, accuracy, fullAddress = "na";

        latitude = String.valueOf(location.getLatitude());
        longitude = String.valueOf(location.getLongitude());
        accuracy = String.valueOf(location.getAccuracy());
        speed = String.valueOf(location.getSpeed());

        Calendar calendar = Calendar.getInstance();
        dayOfWeek = String.valueOf(calendar.get(Calendar.DAY_OF_WEEK));
        hour = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
        minute = String.valueOf(calendar.get(Calendar.MINUTE));
        day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        month = String.valueOf(calendar.get(Calendar.MONTH));

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (isConnected) {
            fullAddress = "";
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addressList = null;
            try {
                addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                Address addr = addressList.get(0);
                String address = addr.getAddressLine(0);
                String city = addr.getLocality();
                String state = addr.getAdminArea();
                String country = addr.getCountryName();
                fullAddress = address + " " + city + " " + state + " " + country;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        db.execSQL("INSERT INTO locationLog VALUES('" + dayOfWeek + "','" + hour + "','" + minute + "','" + latitude + "','" + longitude + "','" + speed + "','" + fullAddress + "','" + day + "','" + month + "', '" + accuracy + "');");
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