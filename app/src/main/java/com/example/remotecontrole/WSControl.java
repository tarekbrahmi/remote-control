package com.example.remotecontrole;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.slider.RangeSlider;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
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
    String serverIp="192.168.43.181";
    String serverPort="8000";
    String Title="Control With JoyStick";
    TextView txt_vitess_value,txt_decision_value;
    Button btn_start,btn_stop;
    RangeSlider rs_vitess;
    private String deviceName = null;

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
        btn_stop=(Button)findViewById(R.id.btn_stop);
        btn_start=(Button)findViewById(R.id.btn_start);
        rs_vitess=(RangeSlider)findViewById(R.id.rs_vitess);

        txt_vitess_value.setText(String.valueOf(0));
        txt_decision_value.setText("IDLE");

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // landscape
            mainlinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        } else {
            // portrait
            mainlinearLayout.setOrientation(LinearLayout.VERTICAL);
        }
        rs_vitess.addOnChangeListener(new RangeSlider.OnChangeListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onValueChange(@NonNull RangeSlider slider, float value, boolean fromUser) {
                txt_vitess_value.setText(String.valueOf(value));
            }
        });
        Handler handler = new Handler(new Handler.Callback() {

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
        //TODO make server ip address entered from an input
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

        joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                //TODO add vitesse
                List<Float> vitesss=rs_vitess.getValues();

                if (strength>60&&(angle<=100 && angle>=80)){
                    // is up(90)  80<angle<100
                    webSocket.send("{\"decision\":\"FORWARD\",\"vitess\":"+String.valueOf(vitesss.get(0))+"}");
                    txt_decision_value.setText("FORWARD");
                }else if(strength>60&&(angle<=280 && angle>=260)){
                    // is down(270)  260<angle<280
                    webSocket.send("{\"decision\":\"BACKWARD\",\"vitess\":"+String.valueOf(vitesss.get(0))+"}");
                    txt_decision_value.setText("BACKWARD");
                }else if(strength>60&&(angle>0 && angle<=10)){
                    // is right(0)  10<angle<350
                    webSocket.send("{\"decision\":\"RIGHT\",\"vitess\":"+String.valueOf(vitesss.get(0))+"}");
                    txt_decision_value.setText("RIGHT");
                }else if(strength>60&&(angle>=350 && angle<360)){
                    webSocket.send("{\"decision\":\"RIGHT\",\"vitess\":"+String.valueOf(vitesss.get(0))+"}");
                    txt_decision_value.setText("RIGHT");
                }else if(strength>60&&(angle<=190 && angle>=170)){
                    // is left(180)  170<angle<190
                    webSocket.send("{\"decision\":\"LEFT\",\"vitess\":"+String.valueOf(vitesss.get(0))+"}");
                    txt_decision_value.setText("LEFT");
                }else if((angle==0&&strength==0)){
                    webSocket.send("{\"decision\":\"IDLE\"}");
                    txt_decision_value.setText("IDLE");
                }

            }
        });
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

}