package com.example.remotecontrole;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class BluetoothControl extends AppCompatActivity {
    LinearLayout mainlinearLayout;
    JoystickView joystick;
    TextView txt_angle_value,txt_decision_value;
    private String deviceName = null;
    private String deviceAddress;
    public static Handler handler;
    public static BluetoothSocket mmSocket;
    public static ConnectedThread connectedThread;
    public static CreateConnectThread createConnectThread;
    private final static int CONNECTING_STATUS = 1;
    private final static int MESSAGE_READ = 2;
    String Title="Control With JoyStick";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_control);
        getSupportActionBar().setTitle(Title);
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
        deviceName = getIntent().getStringExtra("deviceName");
        if (deviceName != null){
            Log.i("INFO","Selected device is : " + deviceName);
            deviceAddress = getIntent().getStringExtra("deviceAddress");
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            createConnectThread = new CreateConnectThread(bluetoothAdapter,deviceAddress);
            createConnectThread.start();
        }else{
            Log.i("INFO","Failed to get device name " + deviceName);
        }
    }
    public static class CreateConnectThread extends Thread {
        String TAG ="INFO";
        @SuppressLint("MissingPermission")
        public CreateConnectThread(BluetoothAdapter bluetoothAdapter, String address) {
            BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
            BluetoothSocket tmp = null;
            @SuppressLint("MissingPermission") UUID uuid = bluetoothDevice.getUuids()[0].getUuid();
            try {
                Log.i(TAG, "DEvice UUID "+uuid);
                tmp = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }
        @SuppressLint("MissingPermission")
        public void run() {
            Log.i("INFO", "Connecting thread "+mmSocket);
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            bluetoothAdapter.cancelDiscovery();
            try {
                mmSocket.connect();
                Log.e("INFO", "Device connected");
                handler.obtainMessage(CONNECTING_STATUS, 1, -1).sendToTarget();
            } catch (IOException connectException) {
                try {
                    mmSocket.close();
                    Log.e("INFO", "Cannot connect to device "+connectException);
                    handler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }
            connectedThread = new ConnectedThread(mmSocket);
            connectedThread.run();
        }
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }
    
    public static class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes = 0;
            while (true) {
                try {
                    buffer[bytes] = (byte) mmInStream.read();
                    String readMessage;
                    if (buffer[bytes] == '\n'){
                        readMessage = new String(buffer,0,bytes);
                        Log.e("Message",readMessage);
                        handler.obtainMessage(MESSAGE_READ,readMessage).sendToTarget();
                        bytes = 0;
                    } else {
                        bytes++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
        public void write(String input) {
            byte[] bytes = input.getBytes();
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e("Send Error","Unable to send message",e);
            }
        }
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

}
