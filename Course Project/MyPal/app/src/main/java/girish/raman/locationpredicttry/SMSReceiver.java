package girish.raman.locationpredicttry;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSReceiver extends BroadcastReceiver {

    public static String getContactName(Context context, String phoneNumber) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = contentResolver.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (char c : phoneNumber.toCharArray()) builder.append(c).append(" ");
        String name = builder.toString().trim();

        if (cursor.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            cursor.close();
        }
        return name;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final Bundle bundle = intent.getExtras();
        try {
            if (bundle != null) {
                final Object[] pdusObj = (Object[]) bundle.get("pdus");
                if (pdusObj != null) {
                    for (Object aPdusObj : pdusObj) {
                        SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) aPdusObj);
                        String number = smsMessage.getDisplayOriginatingAddress();
                        String text = smsMessage.getDisplayMessageBody();

                        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);

                        Intent i = new Intent(context, TTSService.class);
                        i.putExtra("textToSpeak", "Incoming SMS. Message received from " + getContactName(context, number) + ". The message is, " + text);
                        context.startService(i);

                        Log.e("SMS Received", "Sender - " + getContactName(context, number) + " Message - " + text);
                    }
                }
            }
        } catch (Exception e) {
            Log.e("SmsReceiver", "Exception smsReceiver" + e);
        }
    }
}
