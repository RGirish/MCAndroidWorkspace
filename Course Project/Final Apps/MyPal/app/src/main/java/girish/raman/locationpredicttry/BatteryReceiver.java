package girish.raman.locationpredicttry;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class BatteryReceiver extends BroadcastReceiver {
    public BatteryReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BATTERY_LOW)) {
            takeActionOnBatteryLow(context);
        } else if (intent.getAction().equals(Intent.ACTION_BATTERY_OKAY)) {
            takeActionOnBatteryOkay(context);
        }
    }

    public void takeActionOnBatteryOkay(Context context) {
        Toast.makeText(context, "Battery OKAY. Starting Location Log Service.", Toast.LENGTH_LONG).show();
        Intent intent2 = new Intent(context, LocationLogService.class);
        context.startService(intent2);
    }

    public void takeActionOnBatteryLow(Context context) {
        Intent i = new Intent(context, TTSService.class);
        i.putExtra("textToSpeak", "Battery low. Please plug in the charger. Battery low. Please plug in the charger. ");
        Toast.makeText(context, "Stopping Location Log Service.", Toast.LENGTH_LONG).show();
        context.startService(i);

        Intent intent2 = new Intent(context, LocationLogService.class);
        context.stopService(intent2);
    }
}
