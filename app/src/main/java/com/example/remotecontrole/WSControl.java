package com.example.remotecontrole;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
    public static final int MESSAGE_STATE_CHANGED=-1;
    public static final int NOT_CONNECTED = 0;
    public static final int CONNECTED = 1;
    public static final int CLOSED = 2;
    public static final int FAILED = 3;
    String serverIp="192.168.43.209";
    String serverPort="8000";
    Handler handler;
    String Title="Control With JoyStick";
    TextView txt_vitess_value,txt_decision_value;

    OkHttpClient client = new OkHttpClient.Builder()
            .readTimeout(0,  TimeUnit.MILLISECONDS)
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        getSupportActionBar().setTitle(Title);
        mainlinearLayout = (LinearLayout) findViewById(R.id.main_ll);
        joystick = (JoystickView) findViewById(R.id.joystickView);
        txt_vitess_value=(TextView)findViewById(R.id.txt_vitess_value);
        txt_decision_value=(TextView)findViewById(R.id.txt_decision_value);

        txt_vitess_value.setText(String.valueOf(0));
        txt_decision_value.setText("IDLE");

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // landscape
            mainlinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        } else {
            // portrait
            mainlinearLayout.setOrientation(LinearLayout.VERTICAL);
        }
        handler = new Handler(new Handler.Callback() {

            @Override
            public boolean handleMessage(@NonNull Message message) {
                switch (message.what) {
                    case MESSAGE_STATE_CHANGED:
                        switch (message.arg1) {
                            case NOT_CONNECTED:
                                setState("Not Connected");
                                break;
                            case CONNECTED:
                                setState("Connected to : "+serverIp);
                                break;
                            case CLOSED:
                                setState("Connection Closed");
                                break;
                            case FAILED:
                                setState("Failed to connect");
                                break;
                        }
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                Log.i("DATA",String.valueOf(angle)+" ..... "+String.valueOf(strength));
                if (strength>=100&&(angle<=100 && angle>=80)){
                    // is up(90)  80<angle<100
                    webSocket.send("{\"decision\":\"FORWARD\",\"vitess\":"+String.valueOf(strength)+"}");
                    txt_decision_value.setText("FORWARD");
                }else if(strength>=99&&(angle<=280 && angle>=260)){
                    // is down(270)  260<angle<280
                    webSocket.send("{\"decision\":\"BACKWARD\",\"vitess\":"+String.valueOf(strength)+"}");
                    txt_decision_value.setText("BACKWARD");
                }else if(strength>=99&&(angle>0 && angle<=10)){
                    // is right(0)  10<angle<350
                    webSocket.send("{\"decision\":\"RIGHT\",\"vitess\":"+String.valueOf(strength)+"}");
                    txt_decision_value.setText("RIGHT");
                }else if(strength>=99&&(angle>=350 && angle<360)){
                    webSocket.send("{\"decision\":\"RIGHT\",\"vitess\":"+String.valueOf(strength)+"}");
                    txt_decision_value.setText("RIGHT");
                }else if(strength>=100&&(angle<=190 && angle>=170)){
                    // is left(180)  170<angle<190
                    webSocket.send("{\"decision\":\"LEFT\",\"vitess\":"+String.valueOf(strength)+"}");
                    txt_decision_value.setText("LEFT");
                }else if((angle==0&&strength==0)){
                    webSocket.send("{\"decision\":\"IDLE\"}");
                    txt_decision_value.setText("IDLE");
                }
            }
        },750);
    }
    /* ============================ WS control =================================== */
    class EchoWebSocketListener extends WebSocketListener {
        private static final int NORMAL_CLOSURE_STATUS = 1000;
        Handler handler;
        public EchoWebSocketListener(Handler handler) {
            this.handler=handler;
        }
        public synchronized void setState(int state) {
            handler.obtainMessage(MESSAGE_STATE_CHANGED, state, -1).sendToTarget();
        }
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            Log.d("WS", "onOpen() is called.");
            JSONObject obj = new JSONObject();
            joystick.setEnabled(true);
            output("Connecting to address "+serverIp);
            try {
                setState(1);// connected
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
            setState(2);// closed
            webSocket.close(NORMAL_CLOSURE_STATUS, null);
            joystick.setEnabled(false);
            output("Closing : " + code + " / " + reason);
        }
        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            setState(3);// failed to connect
            Log.d("WS", "onFailure() is called.");
            joystick.setEnabled(false);
            output("Error : " + t.getMessage());
        }
    }
    void output(String txt) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),txt,Toast.LENGTH_LONG).show();
            }
        });
    }
    void setState(CharSequence subTitle) {
        getSupportActionBar().setSubtitle(subTitle);
    }
    void showIpPopUp(Context context,Handler handler){
        LayoutInflater li = LayoutInflater.from(context);
        View ipServerPopup = li.inflate(R.layout.server_ip_popup, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(ipServerPopup);
        EditText ipServerET = (EditText) ipServerPopup
                .findViewById(R.id.etxt_server_ipaddress);
        ipServerET.setText(serverIp);
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                setState("Connecting...");
                                serverIp=ipServerET.getText().toString();
                                Request request = new Request.Builder().url("ws://"+serverIp+":"+serverPort+"/command/").build();
                                EchoWebSocketListener listener = new EchoWebSocketListener(handler);
                                webSocket = client.newWebSocket(request, listener);
                                client.dispatcher().executorService().shutdown();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                                Intent intent = new Intent(getApplicationContext(), Starting.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //TODO make server ip address entered from an input
        Log.i("TAG","onResume");
        if(webSocket==null){
            Log.i("TAG","Called from onResume"+webSocket);
            showIpPopUp(this,handler);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i("TAG","onRestart");
        if(webSocket==null){
            Log.i("TAG","Called from onRestart"+webSocket);
            showIpPopUp(this,handler);
        }
    }
}