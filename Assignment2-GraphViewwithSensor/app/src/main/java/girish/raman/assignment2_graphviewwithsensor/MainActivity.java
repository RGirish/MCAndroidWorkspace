package girish.raman.assignment2_graphviewwithsensor;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class MainActivity extends AppCompatActivity {

    static SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = openOrCreateDatabase("assignment2.db", SQLiteDatabase.CREATE_IF_NECESSARY, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS sensorValues(time TEXT, x TEXT, y TEXT, z TEXT);");

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog);
        dialog.setCancelable(false);
        dialog.findViewById(R.id.buttonSubmit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String patientID = ((EditText) dialog.findViewById(R.id.patientID)).getText().toString();
                String patientName = ((EditText) dialog.findViewById(R.id.patientName)).getText().toString();
                String patientAge = ((EditText) dialog.findViewById(R.id.patientAge)).getText().toString();
                String patientSex = ((Spinner) dialog.findViewById(R.id.patientSex)).getSelectedItem().toString();
                if (patientID.equals("") || patientAge.equals("") || patientName.equals("") || patientSex.equals("Choose Patient's Sex")) {
                    Toast.makeText(MainActivity.this, "Do not leave the fields empty!", Toast.LENGTH_LONG).show();
                    return;
                }
                ((TextView) findViewById(R.id.patientIdTV)).setText("Patient's ID: " + patientID);
                ((TextView) findViewById(R.id.patientNameTV)).setText("Patient's Name : " + patientName);
                ((TextView) findViewById(R.id.patientAgeTV)).setText("Patient's Age: " + patientAge);
                ((TextView) findViewById(R.id.patientSexTV)).setText("Patient's Sex: " + patientSex);
                dialog.dismiss();
            }
        });
        dialog.show();

        SensorService sensorService = new SensorService(this);

        Intent intent = new Intent(this, SensorService.class);
        startService(intent);
    }

    public void onClickRun(View view) {
        float[] xValues = new float[10];
        float[] yValues = new float[10];
        float[] zValues = new float[10];
        int i = 0;
        Cursor cursor = db.rawQuery("SELECT * FROM sensorValues ORDER BY time DESC LIMIT 10;", null);
        cursor.moveToFirst();
        while (true) {
            xValues[i] = Float.valueOf(cursor.getString(1));
            yValues[i] = Float.valueOf(cursor.getString(2));
            zValues[i] = Float.valueOf(cursor.getString(3));
            i++;
            cursor.moveToNext();
            if (cursor.isAfterLast()) {
                break;
            }
        }
        cursor.close();

        LinearLayout main = (LinearLayout) findViewById(R.id.main);
        main.removeAllViews();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 320);
        params.setMargins(0, 20, 0, 20);

        GraphView graphViewX = new GraphView(this, xValues, "X Values", new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"}, new String[]{"10", "9", "8", "7", "6", "5", "4", "3", "2", "1"}, true);
        graphViewX.setBackgroundColor(Color.BLACK);
        graphViewX.setLayoutParams(params);
        main.addView(graphViewX);

        GraphView graphViewY = new GraphView(this, yValues, "Y Values", new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"}, new String[]{"10", "9", "8", "7", "6", "5", "4", "3", "2", "1"}, true);
        graphViewY.setBackgroundColor(Color.BLACK);
        graphViewY.setLayoutParams(params);
        main.addView(graphViewY);

        GraphView graphViewZ = new GraphView(this, zValues, "Z Values", new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"}, new String[]{"10", "9", "8", "7", "6", "5", "4", "3", "2", "1"}, true);
        graphViewZ.setBackgroundColor(Color.BLACK);
        graphViewZ.setLayoutParams(params);
        main.addView(graphViewZ);
    }

    public void onClickStop(View view) {
        LinearLayout main = (LinearLayout) findViewById(R.id.main);
        main.removeAllViews();
    }

    public void onClickUpload(View view) {
        UploadTask task = new UploadTask(this);
        task.execute("https://impact.asu.edu/CSE535Spring16Folder/GKS/" + System.currentTimeMillis() + "_database.db");
    }

    public void onClickDownload(View view) {

    }

    private class UploadTask extends AsyncTask<String, Integer, String> {

        ProgressDialog dialog;
        private Context context;

        public UploadTask(Context context) {
            dialog = ProgressDialog.show(context, null, "Uploading. Please wait...", true);
            this.context = context;
        }

        @Override
        protected String doInBackground(String... args) {
            InputStream input = null;
            OutputStream output = null;
            HttpsURLConnection connection = null;
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                    // Not implemented
                }

                @Override
                public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                    // Not implemented
                }
            }};

            try {
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            } catch (KeyManagementException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            try {
                URL url = new URL(args[0]);
                connection = (HttpsURLConnection) url.openConnection();
                connection.connect();
                if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage();
                }

                output = connection.getOutputStream();
                File file = new File(context.getFilesDir().getPath() + "/data/" + getApplicationContext().getPackageName() + "/databases/assignment2.db");
                if (!file.exists()) {
                    Log.e("File doesn't exist", "File doesn't exist");
                }
                input = new FileInputStream(file);
                byte data[] = new byte[512];
                int count;
                while ((count = input.read(data)) != -1) {
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            dialog.dismiss();
            if (result != null) {
                Toast.makeText(context, "Upload error: " + result, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "File uploaded", Toast.LENGTH_LONG).show();
            }
        }
    }
}