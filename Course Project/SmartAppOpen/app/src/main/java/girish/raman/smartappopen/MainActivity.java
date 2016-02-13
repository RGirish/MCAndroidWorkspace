package girish.raman.smartappopen;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startService(new Intent(this, SensorService.class));
    }

    public void print(View view) {
        TextView textView = (TextView) findViewById(R.id.text);
        textView.setText("");
        textView.append("minX " + SensorService.minX + "\n");
        textView.append("maxX " + SensorService.maxX + "\n\n");

        textView.append("minY " + SensorService.minY + "\n");
        textView.append("maxY " + SensorService.maxY + "\n\n");

        textView.append("minZ " + SensorService.minZ + "\n");
        textView.append("maxZ " + SensorService.maxZ + "\n\n");
    }
}