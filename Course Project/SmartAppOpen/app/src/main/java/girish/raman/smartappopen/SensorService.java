package girish.raman.smartappopen;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.widget.Toast;

public class SensorService extends Service implements SensorEventListener {

    String position = "flat";

    public SensorService() {
    }

    @Override
    public void onCreate() {
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float[] g = event.values.clone();
            double norm_Of_g = Math.sqrt(g[0] * g[0] + g[1] * g[1] + g[2] * g[2]);
            g[0] = (float) (g[0] / norm_Of_g);
            g[1] = (float) (g[1] / norm_Of_g);
            g[2] = (float) (g[2] / norm_Of_g);

            int inclination = (int) Math.round(Math.toDegrees(Math.acos(g[2])));

            if ((inclination < 5 || inclination > 175)) {
                position = "flat";
            }
            if (!(inclination < 25 || inclination > 155) && position.equals("flat") && !(8 <= event.values[2] && event.values[2] <= 13)) {
                position = "notflat";
                Toast.makeText(getApplicationContext(), "Device picked up!", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}