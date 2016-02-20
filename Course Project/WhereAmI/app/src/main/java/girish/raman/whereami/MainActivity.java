package girish.raman.whereami;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tts = new TextToSpeech(this, this);
    }

    public void onClickButton(View view) {
        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            List<String> providers = lm.getProviders(true);
            for (String provider : providers) {
                Location location = lm.getLastKnownLocation(provider);
                if (location == null) {
                    Log.e("null", provider);
                    continue;
                }
                double longitude = location.getLongitude();
                double latitude = location.getLatitude();

                addresses = geocoder.getFromLocation(latitude, longitude, 1);
                Address addr = addresses.get(0);
                String address = addr.getAddressLine(0);
                String city = addr.getLocality();
                String state = addr.getAdminArea();
                String country = addr.getCountryName();
                String postalCode = addr.getPostalCode();
                String knownName = addr.getFeatureName();
                String fullAddress = address + " " + city + " " + state + " " + country;
                tts.speak(fullAddress, TextToSpeech.QUEUE_FLUSH, null);

                Log.e("Address", address);
                Log.e("city", city);
                Log.e("state", state);
                Log.e("country", country);
                Log.e("postalCode", postalCode);
                Log.e("knownName", knownName);
                Log.e("******", "******");
                break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
            tts.setSpeechRate(0.75f);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            }
        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}