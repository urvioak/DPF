package com.example.urvi.dpf;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Urvi on 24-Sep-15.
 */
public class WifiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private MainActivity mActivity;
    public static ArrayList<WifiP2pDevice> peers = new ArrayList();
    StringBuilder sb = new StringBuilder();

    private WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {

            if(peerList.getDeviceList().size() > peers.size())
            {
                peers.clear();
                peers.addAll(peerList.getDeviceList());
                sb = new StringBuilder();

                for(WifiP2pDevice device: peers){
                    sb.append(device.deviceName+" | "+device.isGroupOwner()+"\n");
                }
            }
        }
    };


    public WifiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                       MainActivity activity) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;
    }


    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        Log.d("RECEIVED_INFO", action);

        //If there any changes to Wifi P2p connection
        if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            if (mManager == null) {
                return;
            }

            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {
                //grab the connection Info for the new device in current channel
                mManager.requestConnectionInfo(mChannel, mActivity);
            } else {
                Toast.makeText(context, "Disconnected from network", Toast.LENGTH_SHORT).show();
            }
        }
        else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){

            if (mManager != null) {
                mManager.requestPeers(mChannel, peerListListener);
            }
            Toast.makeText(context,"P2p peers changed" + sb.toString(),Toast.LENGTH_LONG).show();
        }else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){
            WifiP2pDevice device = (WifiP2pDevice) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);

            MainActivity.MY_ADDRESS = device.deviceAddress;


        }
    }


}
