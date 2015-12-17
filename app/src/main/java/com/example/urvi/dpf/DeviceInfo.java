package com.example.urvi.dpf;

import java.net.InetAddress;
import java.util.HashMap;

/**
 * Created by Urvi on 15-Nov-15.
 */
public final class DeviceInfo {

    static HashMap<InetAddress, String> InetToMacMap = new HashMap<>();
    static HashMap<String, InetAddress> MacToInetMap = new HashMap<>();

}
