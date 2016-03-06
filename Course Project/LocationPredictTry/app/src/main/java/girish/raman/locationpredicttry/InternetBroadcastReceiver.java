package girish.raman.locationpredicttry;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class InternetBroadcastReceiver extends BroadcastReceiver {

    static boolean isConnected;

    public InternetBroadcastReceiver() {
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        final SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(Environment.getExternalStorageDirectory() + File.separator + "location.db", null);
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (isConnected) {
            Log.e("Internet is ON", "Internet is ON");
            final Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Cursor cursor = db.rawQuery("SELECT * FROM locationLog WHERE address = 'na';", null);
                    cursor.moveToFirst();

                    Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                    List<Address> addressList = null;

                    while (!cursor.isAfterLast() && isConnected) {
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
                            Log.e("Exception in setAddresses", e.getMessage());
                            cursor.moveToPrevious();
                        }
                        cursor.moveToNext();
                    }
                    Log.e("InternetBroadcastReceiver", "setAddresses complete");
                    cursor.close();
                }
            });
            thread.start();
        }
    }
}