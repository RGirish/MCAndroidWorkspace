package girish.raman.locationpredicttry;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class LocationAnalyzeService extends Service {

    public static final String LOCATION_ANALYSIS_RESULT_BROADCAST = "girish.raman.locationpredicttry.LOCATION_ANALYSIS_RESULT_BROADCAST";
    SQLiteDatabase db;

    public LocationAnalyzeService() {
    }

    @Override
    public void onCreate() {
        db = openOrCreateDatabase(Environment.getExternalStorageDirectory() + File.separator + "location.db", SQLiteDatabase.CREATE_IF_NECESSARY, null);
        startAnalysis(Calendar.MONDAY);
    }

    private void startAnalysis(int dayOfWeek) {
        Cursor cursor = db.rawQuery("SELECT hour, minute,address FROM locationLog WHERE dayOfWeek = '" + dayOfWeek + "';", null);
        cursor.moveToFirst();
        //Map<String, Boolean> history = new LinkedHashMap<>();
        List<String> historyOfOut = new ArrayList<>();

        String prevAddress;
        String currAddress = "";
        while (!cursor.isAfterLast()) {
            prevAddress = currAddress;
            currAddress = cursor.getString(2);
            String hour_minute = cursor.getString(0) + ":" + cursor.getString(1);
            if (!currAddress.equals(prevAddress)) {
                int i;
                //prevAddress not updated in this for loop - it will contain addressBeforeGoingOut
                for (i = 0; i < 4 && !currAddress.equals(prevAddress); ++i) {
                    currAddress = cursor.getString(2);
                    cursor.moveToNext();
                }
                if (i == 4) {
                    historyOfOut.add(hour_minute);
                }
            }
            //history.put(hour_minute, currAddress.equals(prevAddress));
            cursor.moveToNext();
        }

        StringBuilder builder = new StringBuilder();
        /*for (Object o : history.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            if (!(Boolean) entry.getValue()) {
                builder.append("Out at ").append(entry.getKey()).append("\n");
            }
        }*/

        for (String timeGoneOut : historyOfOut) {
            Log.e("Out at ", timeGoneOut);
            builder.append("Out at ").append(timeGoneOut).append("\n");
        }

        Intent intent = new Intent(LOCATION_ANALYSIS_RESULT_BROADCAST);
        intent.putExtra("locationAnalysisData", builder.toString());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        cursor.close();
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

}