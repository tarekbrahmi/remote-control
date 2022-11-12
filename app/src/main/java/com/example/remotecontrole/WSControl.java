package com.example.remotecontrole;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.concurrent.TimeUnit;

import io.github.controlwear.virtual.joystick.android.JoystickView;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WSControl extends AppCompatActivity {
    LinearLayout mainlinearLayout;
    JoystickView joystick;
    WebSocket webSocket;
    String serverIp="192.168.43.181";
    TextView txt_angle_value,txt_decision_value;
    private String deviceName = null;

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
        WifiManager wifiMgr = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        String ipAddress = Formatter.formatIpAddress(ip);

        //TODO make server ip address entred from an input
        LayoutInflater li = LayoutInflater.from(this);
        View ipServerPopup = li.inflate(R.layout.server_ip_popup, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(ipServerPopup);
        EditText ipServerET = (EditText) ipServerPopup
                .findViewById(R.id.etxt_server_ipaddress);
        ipServerET.setText(serverIp);
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                serverIp=ipServerET.getText().toString();
                                Request request = new Request.Builder().url("ws://"+serverIp+":8000/command/").build();
                                Log.i("WS","request from address "+ipAddress);
                                EchoWebSocketListener listener = new EchoWebSocketListener();
                                webSocket = client.newWebSocket(request, listener);
                                client.dispatcher().executorService().shutdown();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                                Intent intent = new Intent(getApplicationContext(), Starting.class);
                                startActivity(intent);
                            }
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                txt_angle_value.setText(String.valueOf(angle));
                // define actions
                if (strength>60&&(angle<=100 && angle>=80)){
                    // is up(90)  80<angle<100
                    webSocket.send("{\"decision_\":\"FORWARD\"}");
                    txt_decision_value.setText("FORWARD");
                }else if(strength>60&&(angle<=280 && angle>=260)){
                    // is down(270)  260<angle<280
                    webSocket.send("{\"decision_\":\"BACKWARD\"}");
                    txt_decision_value.setText("BACKWARD");
                }else if(strength>60&&(angle>0 && angle<=10)){
                    // is right(0)  10<angle<350
                    webSocket.send("{\"decision_\":\"RIGHT\"}");
                    txt_decision_value.setText("RIGHT");
                }else if(strength>60&&(angle>=350 && angle<360)){
                    webSocket.send("{\"decision_\":\"RIGHT\"}");
                    txt_decision_value.setText("RIGHT");
                }else if(strength>60&&(angle<=190 && angle>=170)){
                    // is left(180)  170<angle<190
                    webSocket.send("{\"decision_\":\"LEFT\"}");
                    txt_decision_value.setText("LEFT");
                }else if((angle==0&&strength==0)){
                    webSocket.send("{\"decision_\":\"IDLE\"}");
                    txt_decision_value.setText("IDLE");
                }

            }
        });
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
}