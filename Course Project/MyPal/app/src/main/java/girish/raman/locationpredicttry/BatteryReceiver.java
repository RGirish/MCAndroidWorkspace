package girish.raman.locationpredicttry;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BatteryReceiver extends BroadcastReceiver {
    public BatteryReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BATTERY_LOW)) {
            //TTS
            Log.e("BATTERY_LOW", "BATTERY_LOW");
        } else if (intent.getAction().equals(Intent.ACTION_BATTERY_OKAY)) {

        }
    }
}
