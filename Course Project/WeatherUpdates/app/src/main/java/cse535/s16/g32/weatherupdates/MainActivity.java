package cse535.s16.g32.weatherupdates;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //http://api.openweathermap.org/data/2.5/weather?zip=85281,us&APPID=8edc5829a570111458ae29ecf6701e5a
    }
}
