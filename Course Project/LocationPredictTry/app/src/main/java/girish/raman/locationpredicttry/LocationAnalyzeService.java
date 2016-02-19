package girish.raman.locationpredicttry;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class LocationAnalyzeService extends Service {

    SQLiteDatabase db;

    public LocationAnalyzeService() {
    }

    @Override
    public void onCreate() {
        db = openOrCreateDatabase(Environment.getExternalStorageDirectory() + File.separator + "location.db", SQLiteDatabase.CREATE_IF_NECESSARY, null);
        startAnalysis(1);
    }

    private void startAnalysis(int dayOfWeek) {
        Cursor cursor = db.rawQuery("SELECT hour, minute,address FROM locationLog WHERE dayOfWeek = '" + dayOfWeek + "';", null);
        cursor.moveToFirst();
        Map<String, Boolean> history = new LinkedHashMap<>();
        String prevAddress = "";
        String currAddress = "";
        while (!cursor.isAfterLast()) {
            prevAddress = currAddress;
            currAddress = cursor.getString(2);
            String key = cursor.getString(0) + ":" + cursor.getString(1);
            history.put(key, currAddress.equals(prevAddress));
            cursor.moveToNext();
        }

        for (Object o : history.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            if (!(Boolean) entry.getValue()) {
                Log.e("Out at ", String.valueOf(entry.getKey()));
            }
        }
        cursor.close();
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

}