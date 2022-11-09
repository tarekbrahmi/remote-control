package com.example.remotecontrole;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class Control extends AppCompatActivity {
    LinearLayout mainlinearLayout;
    JoystickView joystick;
    TextView txt_angle_value,txt_decision_value;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        mainlinearLayout = (LinearLayout) findViewById(R.id.main_ll);
        joystick = (JoystickView) findViewById(R.id.joystickView);
        txt_angle_value=(TextView)findViewById(R.id.txt_angle_value);
        txt_decision_value=(TextView)findViewById(R.id.txt_decision_value);
        txt_angle_value.setText(String.valueOf(0));
        txt_decision_value.setText(String.valueOf(0));
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
                // define actions
                if (strength>60&&(angle<=100 && angle>=80)){
                    // is up(90)  80<angle<100
                    txt_decision_value.setText("UP");
                }else if(strength>60&&(angle<=280 && angle>=260)){
                    // is down(270)  260<angle<280
                    txt_decision_value.setText("DOWN");
                }else if(strength>60&&(angle>0 && angle<=10)){
                    // is right(0)  10<angle<350
                    txt_decision_value.setText("RIGHT");
                }else if(strength>60&&(angle>=350 && angle<360)){
                    txt_decision_value.setText("RIGHT");
                }else if(strength>60&&(angle<=190 && angle>=170)){
                    // is left(180)  170<angle<190
                    txt_decision_value.setText("LEFT");
                }else if((angle==0&&strength==0)){
                    txt_decision_value.setText("IDLE");
                }

            }
        });
    }
}