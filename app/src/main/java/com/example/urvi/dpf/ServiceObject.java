package com.example.urvi.dpf;

import android.net.wifi.p2p.WifiP2pDevice;

/**
 * Created by Urvi on 24-Sep-15.
 */
public class ServiceObject {
    public WifiP2pDevice initiator;
    // WifiP2pDevice backUp;
    public String serviceName;
    public String registrationType;

    @Override
    public String toString() {
        return serviceName + " | "+ initiator.deviceName;
    }
}
