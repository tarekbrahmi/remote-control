package com.example.remotecontrole.pairdevice;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.remotecontrole.BluetoothControl;
import com.example.remotecontrole.WSControl;
import com.example.remotecontrole.R;

import java.util.List;

public class DeviceListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<Object> deviceList;
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textAddress;
        LinearLayout linearLayout;
        public ViewHolder(View v) {
            super(v);
            textName = v.findViewById(R.id.txt_DeviceName);
            textAddress = v.findViewById(R.id.txt_DeviceAddress);
            linearLayout = v.findViewById(R.id.ll_DeviceInfo);
        }
    }
    public DeviceListAdapter(Context context, List<Object> deviceList) {
        this.context = context;
        this.deviceList = deviceList;
    }
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_info_layout, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        ViewHolder itemHolder = (ViewHolder) holder;
        final DeviceInformation deviceInfo = (DeviceInformation) deviceList.get(position);
        itemHolder.textName.setText(deviceInfo.getDeviceName());
        itemHolder.textAddress.setText(deviceInfo.getDeviceHardwareAddress());
        // When a device is selected
        itemHolder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, BluetoothControl.class);
                // Send device details to the MainActivity
                intent.putExtra("deviceName", deviceInfo.getDeviceName());
                intent.putExtra("deviceAddress",deviceInfo.getDeviceHardwareAddress());
                // Call MainActivity
                context.startActivity(intent);
            }
        });
    }
    @Override
    public int getItemCount() {
        int dataCount = deviceList.size();
        return dataCount;
    }
}
