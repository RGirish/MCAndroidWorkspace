package girish.raman.locationpredicttry;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

public class BirthdayAlarmReceiver extends BroadcastReceiver {

    public BirthdayAlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent receivedIntent) {
        String phone = receivedIntent.getStringExtra("phone");
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phone, null, "Happy Birthday! :) Have a great day!", null, null);
    }
}