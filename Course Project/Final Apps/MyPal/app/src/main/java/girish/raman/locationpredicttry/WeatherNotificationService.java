package girish.raman.locationpredicttry;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class WeatherNotificationService extends Service implements TaskListener {

    @Override
    public void onCreate() {
        super.onCreate();
        getWeatherUpdate();
    }

    public void getWeatherUpdate() {
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
                Toast.makeText(WeatherNotificationService.this, city, Toast.LENGTH_SHORT).show();
                break;
            }
        } catch (IOException e) {
            Log.e("IOException", e.toString());
        }
        new GetWeatherUpdateTask(this).execute("http://api.openweathermap.org/data/2.5/weather?zip=" + city + ",us&APPID=8edc5829a570111458ae29ecf6701e5a");
    }

    @Override
    public void onComplete(String weather) {
        String textToSpeak = "Unable to obtain weather update.";
        if (weather.toLowerCase().contains("rain")) {
            textToSpeak = "rainy";
        } else if (weather.toLowerCase().contains("clear")) {
            textToSpeak = "clear";
        }
        Intent i = new Intent(this, TTSService.class);
        i.putExtra("textToSpeak", "Weather update. The weather is " + textToSpeak);
        startService(i);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
