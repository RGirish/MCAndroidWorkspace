package girish.raman.locationpredicttry;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetWeatherUpdateTask extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(String... params) {
        try {
            URL url = new URL(params[0]);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream inputStream = connection.getInputStream();
            String json = streamToString(inputStream);
            parseJson(json);
            return "success";
        } catch (IOException ignored) {
            return "error";
        }
    }

    private void parseJson(String json) {
        try {
            JSONObject root = new JSONObject(json);
            JSONArray weather = root.getJSONArray("weather");
            String weatherType = ((JSONObject) weather.get(0)).getString("main");
            Log.e("Weather is ", weatherType);
        } catch (JSONException e) {
            Log.e("JSONException", e.toString());
        }
    }

    public String streamToString(InputStream inputStream) {
        final char[] buffer = new char[512];
        final StringBuilder builder = new StringBuilder();
        try {
            Reader reader = new InputStreamReader(inputStream, "UTF-8");
            for (; ; ) {
                int bytesRead = reader.read(buffer, 0, buffer.length);
                if (bytesRead < 0)
                    break;
                builder.append(buffer, 0, bytesRead);
            }
        } catch (IOException ignored) {
        }
        return builder.toString();
    }

}