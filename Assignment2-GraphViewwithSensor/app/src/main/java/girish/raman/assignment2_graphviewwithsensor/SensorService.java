package girish.raman.assignment2_graphviewwithsensor;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.math.BigDecimal;

public class SensorService extends Service implements SensorEventListener {

    SQLiteDatabase db;
    SensorManager sensorManager;
    Context context;
    double x, y, z;

    public SensorService(Context context) {
        this.context = context;
        sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        db = context.openOrCreateDatabase(Environment.getExternalStorageDirectory() + "/assignment2_gks.db", SQLiteDatabase.CREATE_IF_NECESSARY, null);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String s = String.valueOf(System.currentTimeMillis());
                db.execSQL("INSERT INTO sensorValues VALUES('" + s + "','" + x + "','" + y + "','" + z + "');");
                new Handler().postDelayed(this, 1000);
            }
        }, 1000);
    }

    public SensorService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            x = new BigDecimal(event.values[0]).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
            y = new BigDecimal(event.values[1]).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
            z = new BigDecimal(event.values[2]).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}