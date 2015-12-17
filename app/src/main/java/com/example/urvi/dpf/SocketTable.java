package com.example.urvi.dpf;

import java.net.InetAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Table of all the devices and their corresponding i/o streams
 * Created by Urvi on 07-Nov-15.
 */
public class SocketTable {

private static HashMap<InetAddress,SocketEntry> SocketMap = new HashMap<>();


    public static void addSocket(InetAddress ia,SocketEntry se){
        SocketMap.put(ia,se);

    }
     public static SocketEntry getSocket(InetAddress ia){
         return SocketMap.get(ia);
     }

    public static Collection<SocketEntry> getValues(){
        return SocketMap.values();
    }

    public static Collection<InetAddress> getKeys(){
        return SocketMap.keySet();
    }

    public static HashMap<InetAddress,SocketEntry> getSocketMap() {
        return SocketMap;
    }
}
