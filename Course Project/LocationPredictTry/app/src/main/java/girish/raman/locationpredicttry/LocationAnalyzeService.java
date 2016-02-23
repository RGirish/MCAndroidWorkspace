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
import java.util.List;

public class LocationAnalyzeService extends Service {

    public static final String LOCATION_ANALYSIS_RESULT_BROADCAST = "girish.raman.locationpredicttry.LOCATION_ANALYSIS_RESULT_BROADCAST";
    SQLiteDatabase db;

    public LocationAnalyzeService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startAnalysis(intent.getIntExtra("dayOfWeek", 0), intent.getIntExtra("day", 0));
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        db = openOrCreateDatabase(Environment.getExternalStorageDirectory() + File.separator + "location.db", SQLiteDatabase.CREATE_IF_NECESSARY, null);
    }

    private void startAnalysis(int dayOfWeek, int day) {
        Cursor cursor = db.rawQuery("SELECT hour, minute,address FROM locationLog WHERE dayOfWeek = '" + dayOfWeek + "' AND day = '" + day + "';", null);
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
                int i = 0;
                //prevAddress not updated in this for loop - it will contain addressBeforeGoingOut
                for (i = 0; i < 4 && !currAddress.equals(prevAddress); ++i) {
                    currAddress = cursor.getString(2);
                    cursor.moveToNext();
                }
                if (i == 4) {
                    historyOfOut.add(hour_minute);
                    for (int i1 = 0; i1 < i; i1++) {
                        cursor.moveToPrevious();
                    }
                    int continuousCount = 0;
                    currAddress = cursor.getString(2);
                    while (continuousCount < 5) {
                        prevAddress = currAddress;
                        currAddress = cursor.getString(2);
                        if (prevAddress.equals(currAddress)) {
                            continuousCount++;
                        } else {
                            continuousCount = 0;
                        }
                        cursor.moveToNext();
                    }
                    cursor.moveToPrevious();
                } else {
                    for (int i1 = 0; i1 < i; i1++) {
                        cursor.moveToPrevious();
                    }
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