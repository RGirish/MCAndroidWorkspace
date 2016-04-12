package cse535.s16.g32.whereishe;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startService(new Intent(this, WhereIsHeListenerService.class));
    }

    public void onClickWhereIsMother(View view) {
        Log.e("Mother's location: ", getSharedPreferences("WhereIsHe", MODE_PRIVATE).getString("mothersAddress", "Location Unknown"));
    }

    public void onClickWhereIsFather(View view) {
        Log.e("Father's location: ", getSharedPreferences("WhereIsHe", MODE_PRIVATE).getString("fathersAddress", "Location Unknown"));
    }
}