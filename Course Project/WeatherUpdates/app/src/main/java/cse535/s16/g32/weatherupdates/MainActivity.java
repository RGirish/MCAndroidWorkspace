package cse535.s16.g32.weatherupdates;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        function();
    }

    public void function() {
        String city = "seattle";
        try {
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(this, Locale.getDefault());
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            List<String> providers = lm.getProviders(true);
            for (String provider : providers) {
                Location location = lm.getLastKnownLocation(provider);
                if (location == null) {
                    continue;
                }
                double longitude = location.getLongitude();
                double latitude = location.getLatitude();
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
                Address address = addresses.get(0);
                city = address.getLocality();
                break;
            }
        } catch (IOException e) {
            Log.e("IOException", e.toString());
        }
        new GetWeatherUpdateTask().execute("http://api.openweathermap.org/data/2.5/weather?zip=" + city + ",us&APPID=8edc5829a570111458ae29ecf6701e5a");
    }
}