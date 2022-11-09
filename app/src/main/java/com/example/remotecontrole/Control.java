package com.example.remotecontrole;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class Control extends AppCompatActivity {
    LinearLayout mainlinearLayout;//(id) main_ll
    JoystickView joystick;
    TextView txt_angle_value,txt_strength_value;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        mainlinearLayout = (LinearLayout) findViewById(R.id.main_ll);
        joystick = (JoystickView) findViewById(R.id.joystickView);
        txt_angle_value=(TextView)findViewById(R.id.txt_angle_value);
        txt_strength_value=(TextView)findViewById(R.id.txt_strength_value);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // landscape
            mainlinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        } else {
            // portrait
            mainlinearLayout.setOrientation(LinearLayout.VERTICAL);
        }

        joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                txt_angle_value.setText(String.valueOf(angle));
                txt_strength_value.setText(String.valueOf(strength));

            }
        });
    }
}