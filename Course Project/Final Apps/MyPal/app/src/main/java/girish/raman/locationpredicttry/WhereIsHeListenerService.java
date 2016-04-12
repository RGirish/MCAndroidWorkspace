package girish.raman.locationpredicttry;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class WhereIsHeListenerService extends Service {

    static SharedPreferences preferences;

    public WhereIsHeListenerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
        final Firebase ref = new Firebase("https://cse535s16g32.firebaseio.com/");

        preferences = getSharedPreferences("WhereIsHe", MODE_PRIVATE);

        ChildEventListener motherLocationListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String[] parts = dataSnapshot.getValue().toString().split("_");
                double latitude = Double.parseDouble(parts[0]);
                double longitude = Double.parseDouble(parts[1]);

                try {
                    Geocoder geocoder = new Geocoder(WhereIsHeListenerService.this, Locale.getDefault());
                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    Address addr = addresses.get(0);
                    String address = addr.getAddressLine(0);
                    String city = addr.getLocality();
                    String state = addr.getAdminArea();
                    String country = addr.getCountryName();
                    String fullAddress = address + " " + city + " " + state + " " + country;
                    preferences.edit().putString("mothersAddress", fullAddress).apply();

                    LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    if (ActivityCompat.checkSelfPermission(WhereIsHeListenerService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(WhereIsHeListenerService.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    List<String> providers = lm.getProviders(true);
                    for (String provider : providers) {
                        Location location = lm.getLastKnownLocation(provider);
                        if (location == null) {
                            continue;
                        }
                        double mylatitude = location.getLatitude();
                        double mylongitude = location.getLongitude();

                        Location myLocation = new Location("");
                        myLocation.setLatitude(mylatitude);
                        myLocation.setLongitude(mylongitude);

                        Location hisLocation = new Location("");
                        hisLocation.setLatitude(latitude);
                        hisLocation.setLongitude(longitude);

                        float distance = myLocation.distanceTo(hisLocation);
                        if (distance < 300) {
                            Log.e("Mother is near", "Mother is near");
                            Intent i = new Intent(WhereIsHeListenerService.this, TTSService.class);
                            i.putExtra("textToSpeak", "Alert - Mother is near");
                            startService(i);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        };
        ref.child("receivedLocations").child("mother").addChildEventListener(motherLocationListener);

        ChildEventListener fatherLocationListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String[] parts = dataSnapshot.getValue().toString().split("_");
                double latitude = Double.parseDouble(parts[0]);
                double longitude = Double.parseDouble(parts[1]);

                try {
                    Geocoder geocoder = new Geocoder(WhereIsHeListenerService.this, Locale.getDefault());
                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    Address addr = addresses.get(0);
                    String address = addr.getAddressLine(0);
                    String city = addr.getLocality();
                    String state = addr.getAdminArea();
                    String country = addr.getCountryName();
                    String fullAddress = address + " " + city + " " + state + " " + country;
                    preferences.edit().putString("fathersAddress", fullAddress).apply();

                    LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    if (ActivityCompat.checkSelfPermission(WhereIsHeListenerService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(WhereIsHeListenerService.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    List<String> providers = lm.getProviders(true);
                    for (String provider : providers) {
                        Location location = lm.getLastKnownLocation(provider);
                        if (location == null) {
                            continue;
                        }
                        double mylatitude = location.getLatitude();
                        double mylongitude = location.getLongitude();

                        Location myLocation = new Location("");
                        myLocation.setLatitude(mylatitude);
                        myLocation.setLongitude(mylongitude);

                        Location hisLocation = new Location("");
                        hisLocation.setLatitude(latitude);
                        hisLocation.setLongitude(longitude);

                        float distance = myLocation.distanceTo(hisLocation);
                        if (distance < 300) {
                            Log.e("Father is near", "Father is near");
                            Intent i = new Intent(WhereIsHeListenerService.this, TTSService.class);
                            i.putExtra("textToSpeak", "Alert - Father is near");
                            startService(i);
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        };
        ref.child("receivedLocations").child("father").addChildEventListener(fatherLocationListener);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
