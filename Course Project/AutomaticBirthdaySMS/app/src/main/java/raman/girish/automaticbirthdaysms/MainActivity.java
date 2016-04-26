package raman.girish.automaticbirthdaysms;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    AlarmManager alarmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupAlarmManagerForBirthdays();
    }

    private void setupAlarmManagerForBirthdays() {
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Cursor cursor = getContactsBirthdays();
        while (cursor.moveToNext()) {
            String bday = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE));
            String phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA));
            Log.e("phone", phone);
            SimpleDateFormat format1 = new SimpleDateFormat("MMM dd,yyyy", Locale.ENGLISH);
            Date date;
            try {
                date = format1.parse(bday);
            } catch (ParseException e) {
                format1 = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                try {
                    date = format1.parse(bday);
                } catch (ParseException e2) {
                    format1 = new SimpleDateFormat("--MM-dd", Locale.ENGLISH);
                    try {
                        date = format1.parse(bday);
                    } catch (ParseException e3) {
                        Log.e("ParseException", "ParseException");
                        continue;
                    }
                }
            }
            Calendar cal = new GregorianCalendar();
            cal.setTime(date);
            String birthday = new DateFormatSymbols().getMonths()[cal.get(Calendar.MONTH)] + " " + String.valueOf(cal.get(Calendar.DAY_OF_MONTH)) + ", " + String.valueOf(cal.get(Calendar.YEAR));
            //you have a birthday. now set the alarm manager

            SimpleDateFormat format = new SimpleDateFormat("MMM dd,yyyy", Locale.ENGLISH);
            Date theDate = null;
            try {
                theDate = format.parse(birthday);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Calendar birthdayCal = new GregorianCalendar();
            birthdayCal.setTime(theDate);

            Calendar alarmCal = Calendar.getInstance();
            int birthdayMonth = birthdayCal.get(Calendar.MONTH);
            int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
            int birthdayDay = birthdayCal.get(Calendar.DAY_OF_MONTH);
            int currentDate = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
            alarmCal.set(Calendar.DAY_OF_MONTH, birthdayDay);
            alarmCal.set(Calendar.MONTH, birthdayMonth);

            if (currentMonth > birthdayMonth) {
                alarmCal.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR) + 1);
            } else {
                if (currentMonth == birthdayMonth) {
                    if (currentDate > birthdayDay)
                        alarmCal.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR) + 1);
                    else
                        alarmCal.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
                } else {
                    alarmCal.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
                }
            }

            alarmCal.set(Calendar.HOUR_OF_DAY, 0);
            alarmCal.set(Calendar.MINUTE, 0);
            alarmCal.set(Calendar.SECOND, 0);
            alarmCal.set(Calendar.MILLISECOND, 0);

            Intent myIntent = new Intent(this, BirthdayAlarmReceiver.class);
            myIntent.putExtra("phone", phone);
            int alarmID = (int) System.currentTimeMillis();
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, alarmID, myIntent, 0);
            alarmManager.set(AlarmManager.RTC, alarmCal.getTimeInMillis(), pendingIntent);
        }
    }

    private Cursor getContactsBirthdays() {
        Uri uri = ContactsContract.Data.CONTENT_URI;
        String[] projection = new String[]{ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.CommonDataKinds.Event.CONTACT_ID, ContactsContract.CommonDataKinds.Event.START_DATE, ContactsContract.CommonDataKinds.Phone.DATA};
        String where = ContactsContract.Data.MIMETYPE + "= ? AND " + ContactsContract.CommonDataKinds.Event.TYPE + "=" + ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY;
        String[] selectionArgs = new String[]{ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE};
        return getContentResolver().query(uri, projection, where, selectionArgs, ContactsContract.Contacts.DISPLAY_NAME);
    }

}
