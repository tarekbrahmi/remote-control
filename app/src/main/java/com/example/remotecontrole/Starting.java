package com.example.remotecontrole;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

public class Starting extends AppCompatActivity {
    Button WsServerButton,PairDeviceButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starting);
        WsServerButton=(Button)findViewById(R.id.btn_ws_server);// control via WS server
        PairDeviceButton=(Button)findViewById(R.id.btn_pair_device);// list all paired device
        WsServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Control.class);
                startActivity(intent);
            }
        });

    }
}