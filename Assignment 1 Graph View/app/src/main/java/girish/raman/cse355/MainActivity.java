/*

CSE355 Mobile Computing Assignment 1 

Group Members - Girish Raman, Karthikeyan Mohan, Sadhana

*/

package girish.raman.cse355;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.security.SecureRandom;

public class MainActivity extends AppCompatActivity {

    LinearLayout main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        main = (LinearLayout) findViewById(R.id.main);

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
                ((TextView) findViewById(R.id.patientIdTV)).setText("Patient's ID: " + patientID);
                ((TextView) findViewById(R.id.patientNameTV)).setText("Patient's Name : " + patientName);
                ((TextView) findViewById(R.id.patientAgeTV)).setText("Patient's Age: " + patientAge);
                ((TextView) findViewById(R.id.patientSexTV)).setText("Patient's Sex: " + patientSex);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void onClickRun(View view) {
        SecureRandom random = new SecureRandom();
        float[] values = new float[10];
        for (int i = 0; i < 10; i++) {
            values[i] = random.nextInt(50) + random.nextFloat();
        }
        GraphView graphView = new GraphView(this, values, "The Graph", new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"}, new String[]{"50", "40", "30", "20", "10"}, true);
        graphView.setBackgroundColor(Color.BLACK);
        main.removeAllViews();
        main.addView(graphView);
    }

    public void onClickStop(View view) {
        main.removeAllViews();
    }
}