package com.example.remotecontrole;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.github.controlwear.virtual.joystick.android.JoystickView;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class Control extends AppCompatActivity {
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
    OkHttpClient client = new OkHttpClient.Builder()
            .readTimeout(0,  TimeUnit.MILLISECONDS)
            .build();

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
        Request request = new Request.Builder().url("ws://192.168.43.181:8000/command/").build();
        Log.i("WS","request "+request);
        EchoWebSocketListener listener = new EchoWebSocketListener();
        WebSocket ws = this.client.newWebSocket(request, listener);
        this.client.dispatcher().executorService().shutdown();
        joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                txt_angle_value.setText(String.valueOf(angle));
                // define actions
                if (strength>60&&(angle<=100 && angle>=80)){
                    // is up(90)  80<angle<100
                    ws.send("{\"decision_\":\"UP\"}");
                    txt_decision_value.setText("UP");
                }else if(strength>60&&(angle<=280 && angle>=260)){
                    // is down(270)  260<angle<280
                    ws.send("{\"decision_\":\"DOWN\"}");
                    txt_decision_value.setText("DOWN");
                }else if(strength>60&&(angle>0 && angle<=10)){
                    // is right(0)  10<angle<350
                    ws.send("{\"decision_\":\"RIGHT\"}");
                    txt_decision_value.setText("RIGHT");
                }else if(strength>60&&(angle>=350 && angle<360)){
                    ws.send("{\"decision_\":\"RIGHT\"}");
                    txt_decision_value.setText("RIGHT");
                }else if(strength>60&&(angle<=190 && angle>=170)){
                    // is left(180)  170<angle<190
                    ws.send("{\"decision_\":\"LEFT\"}");
                    txt_decision_value.setText("LEFT");
                }else if((angle==0&&strength==0)){
                    ws.send("{\"decision_\":\"IDLE\"}");
                    txt_decision_value.setText("IDLE");
                }

            }
        });
        // If a bluetooth device has been selected from SelectDeviceActivity
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

        handler = new Handler(Looper.getMainLooper()) {
            @SuppressLint("SetTextI18n")
            @Override
            public void handleMessage(Message msg){
                switch (msg.what){
                    case CONNECTING_STATUS:
                        switch(msg.arg1){
                            case 1:
                                Log.i("INFO","Connected to " + deviceName);
                                break;
                            case -1:
                                Log.i("INFO","Device fails to connect");
                                break;
                        }
                        break;

                }
            }
        };
    }
    /* ============================ WS control =================================== */
    class EchoWebSocketListener extends WebSocketListener {
        private static final int NORMAL_CLOSURE_STATUS = 1000;

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            Log.d("WS", "onOpen() is called.");
            JSONObject obj = new JSONObject();
            try {
                obj.put("message" , "Hello");
            } catch (JSONException e) {
                Log.e("WSE",e.toString());
            }
            webSocket.send(obj.toString());
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            Log.d("WS", "onMessage() for String is called.");
            output("Receiving : " + text);
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            Log.d("WS", "onMessage() for ByteString is called.");
            output("Receiving bytes : " + bytes.hex());
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            Log.d("WS", "onClosing() is called.");
            webSocket.close(NORMAL_CLOSURE_STATUS, null);
            output("Closing : " + code + " / " + reason);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            Log.d("WS", "onFailure() is called.");
            output("Error : " + t.getMessage());
        }
    }
    void output(String txt) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i("WSS",txt);
            }
        });
    }

    /* ============================ Thread to Create Bluetooth Connection =================================== */
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
    /* =============================== Thread for Data Transfer =========================================== */
    public static class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes = 0; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    /*
                    Read from the InputStream from Arduino until termination character is reached.
                    Then send the whole String message to GUI Handler.
                     */
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
        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes(); //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e("Send Error","Unable to send message",e);
            }
        }
        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
}