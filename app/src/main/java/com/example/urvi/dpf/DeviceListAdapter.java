package com.example.urvi.dpf;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * custom device list adapter to display list of device names
 * Created by Urvi on 04-Dec-15.
 */
public class DeviceListAdapter extends ArrayAdapter<WifiP2pDevice> {

    Context context;

    List<WifiP2pDevice> peers;
    public static class ViewHolder{

        public TextView text;
    }

    public DeviceListAdapter(Context context, List<WifiP2pDevice> objects) {
        super(context, R.layout.device_name_row, objects);
        this.context = context;
        this.peers = objects;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        WifiP2pDevice device = getItem(position);
        ViewHolder holder;

            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.device_name_row, parent, false);
            holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(R.id.textRow);
            holder.text.setText(device.deviceName);




        return convertView;

    }

    public WifiP2pDevice getDevice(int position){
        return getItem(position);
    }

}
