package girish.raman.assignment2_graphviewwithsensor;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final int WRITE_EXTERNAL_STORAGE = 101;
    static SQLiteDatabase db;
    static String FILENAME = "assignment2_gks.db";
    static String TABLENAME = "sensorValues";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            showDialogBox();
        } else {
            askForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE);
        }
    }

    private void startSensorService() {
        SensorService sensorService = new SensorService(this);
        Intent intent = new Intent(this, SensorService.class);
        startService(intent);
    }

    private void showDialogBox() {
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
                TABLENAME = patientName + "_" + patientID + "_" + patientAge + "_" + patientSex;
                setupDatabase();
                startSensorService();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void askForPermission(String permission, int permissionID) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            Toast.makeText(MainActivity.this, "External Storage Access Permission Needed!", Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{permission}, permissionID);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showDialogBox();
                } else {
                    hideButtons();
                    Toast.makeText(MainActivity.this, "External Storage Access Permission Needed!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void hideButtons() {
        findViewById(R.id.hsv).setVisibility(View.GONE);
    }

    private void setupDatabase() {
        db = openOrCreateDatabase(Environment.getExternalStorageDirectory() + "/" + FILENAME, SQLiteDatabase.CREATE_IF_NECESSARY, null);
        db.setVersion(1);
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLENAME + "(time TEXT, x TEXT, y TEXT, z TEXT);");
    }

    public void onClickRun(View view) {
        float[] xValues = new float[10];
        float[] yValues = new float[10];
        float[] zValues = new float[10];
        int i = 0;
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLENAME + " ORDER BY time DESC LIMIT 10;", null);
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

    public void plotGraphWithDownloadedData() {
        float[] xValues = new float[10];
        float[] yValues = new float[10];
        float[] zValues = new float[10];
        int i = 0;

        SQLiteDatabase db = openOrCreateDatabase(Environment.getExternalStorageDirectory().getPath() + File.separator + FILENAME + "_downloaded.db", SQLiteDatabase.CREATE_IF_NECESSARY, null);
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLENAME + " ORDER BY time DESC LIMIT 10;", null);
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

    private boolean connectedToInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void onClickUpload(View view) {
        LinearLayout main = (LinearLayout) findViewById(R.id.main);
        main.removeAllViews();
        if (connectedToInternet()) {
            new UploadTask().execute();
        } else {
            Toast.makeText(MainActivity.this, "Check your Internet Connection!", Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickDownload(View view) {
        LinearLayout main = (LinearLayout) findViewById(R.id.main);
        main.removeAllViews();
        if (connectedToInternet()) {
            DownloadTask task = new DownloadTask();
            task.execute("http://cse535gks.netai.net/uploads/" + FILENAME);
        } else {
            Toast.makeText(MainActivity.this, "Check your Internet Connection!", Toast.LENGTH_SHORT).show();
        }
    }

    class UploadTask extends AsyncTask<String, Void, String> {

        ProgressDialog dialog;

        @Override
        protected String doInBackground(String... args) {

            HttpURLConnection httpURLConnection;
            DataOutputStream dataOutputStream;
            int bytesRead, remaining, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1024 * 1024;
            File file = new File(Environment.getExternalStorageDirectory().getPath() + "/" + FILENAME);

            try {

                FileInputStream fileInputStream = new FileInputStream(file);
                URL url = new URL("http://cse535gks.netai.net/upload_file_to_server.php");

                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setUseCaches(false);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
                httpURLConnection.setRequestProperty("ENCTYPE", "multipart/form-data");
                httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + "*****");
                httpURLConnection.setRequestProperty("theFile", Environment.getExternalStorageDirectory().getPath() + "/" + "" + FILENAME);

                dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());

                dataOutputStream.writeBytes("--" + "*****" + "\r\n");
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"theFile\";filename=\""
                        + Environment.getExternalStorageDirectory().getPath() + "/" + FILENAME + "\"" + "\r\n");

                dataOutputStream.writeBytes("\r\n");

                remaining = fileInputStream.available();

                bufferSize = Math.min(remaining, maxBufferSize);
                buffer = new byte[bufferSize];

                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dataOutputStream.write(buffer, 0, bufferSize);
                    remaining = fileInputStream.available();
                    bufferSize = Math.min(remaining, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                dataOutputStream.writeBytes("\r\n");
                dataOutputStream.writeBytes("--" + "*****" + "--" + "\r\n");
                Log.e("", String.valueOf(httpURLConnection.getResponseCode()));
                fileInputStream.close();
                dataOutputStream.flush();
                dataOutputStream.close();
                return "success";
            } catch (MalformedURLException ex) {
                return "MalformedURLException";
            } catch (Exception e) {
                return "Exception occurred! " + e.toString();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(MainActivity.this, null, "Uploading. Please wait...", true);
        }

        @Override
        protected void onPostExecute(String result) {
            dialog.dismiss();
            if (result.equals("success")) {
                Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(null)
                        .setMessage("Database uploaded to http://cse535gks.netai.net/uploads/")
                        .setPositiveButton("See in Server", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://cse535gks.netai.net/uploads/"));
                                startActivity(intent);
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setCancelable(false)
                        .show();
            } else {
                Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
            }
        }
    }

    class DownloadTask extends AsyncTask<String, Void, String> {

        ProgressDialog dialog;

        public DownloadTask() {
        }

        @Override
        protected String doInBackground(String... args) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(args[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Some error occurred. Please try again later!";
                }

                input = connection.getInputStream();
                output = new FileOutputStream(Environment.getExternalStorageDirectory().getPath() + "/" + FILENAME + "_downloaded.db");

                byte data[] = new byte[4096];
                int count;
                while ((count = input.read(data)) != -1) {
                    output.write(data, 0, count);
                }
                return "success";
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
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(MainActivity.this, null, "Downloading. Please wait...", true);
        }

        @Override
        protected void onPostExecute(String result) {
            dialog.dismiss();
            if (result == null) {
                Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
            } else if (result.equals("success")) {
                Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(null)
                        .setMessage("Database downloaded to Phone Storage!")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setNegativeButton("Plot the Graph with new data", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                plotGraphWithDownloadedData();
                            }
                        })
                        .setCancelable(false)
                        .show();
            } else {
                Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
            }
        }
    }
}