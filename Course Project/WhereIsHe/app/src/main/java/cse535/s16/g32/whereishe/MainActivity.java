package cse535.s16.g32.whereishe;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
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

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    static SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = getSharedPreferences("WhereIsHe", MODE_PRIVATE);

        Firebase.setAndroidContext(this);
        final Firebase ref = new Firebase("https://cse535s16g32.firebaseio.com/");

        ChildEventListener karthikLocationListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String[] parts = dataSnapshot.getValue().toString().split("_");
                double latitude = Double.parseDouble(parts[0]);
                double longitude = Double.parseDouble(parts[1]);

                try {
                    Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    Address addr = addresses.get(0);
                    String address = addr.getAddressLine(0);
                    String city = addr.getLocality();
                    String state = addr.getAdminArea();
                    String country = addr.getCountryName();
                    String fullAddress = address + " " + city + " " + state + " " + country;
                    preferences.edit().putString("karthiksAddress", fullAddress).apply();

                    LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                            Log.e("Karthik is near", "Karthik is near");
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
        ref.child("receivedLocations").child("karthik").addChildEventListener(karthikLocationListener);

        ChildEventListener girishLocationListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String[] parts = dataSnapshot.getValue().toString().split("_");
                double latitude = Double.parseDouble(parts[0]);
                double longitude = Double.parseDouble(parts[1]);

                try {
                    Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    Address addr = addresses.get(0);
                    String address = addr.getAddressLine(0);
                    String city = addr.getLocality();
                    String state = addr.getAdminArea();
                    String country = addr.getCountryName();
                    String fullAddress = address + " " + city + " " + state + " " + country;
                    preferences.edit().putString("girishsAddress", fullAddress).apply();

                    LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                            Log.e("Girish is near", "Girish is near");
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
        ref.child("receivedLocations").child("girish").addChildEventListener(girishLocationListener);
    }

    public void onClickWhereIskarthik(View view) {
        Log.e("karthik's location: ", getSharedPreferences("WhereIsHe", MODE_PRIVATE).getString("karthiksAddress", "Location Unknown"));
    }

    public void onClickWhereIsgirish(View view) {
        Log.e("girish's location: ", getSharedPreferences("WhereIsHe", MODE_PRIVATE).getString("girishsAddress", "Location Unknown"));
    }
}