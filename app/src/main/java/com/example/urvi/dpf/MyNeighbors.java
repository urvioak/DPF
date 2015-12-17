
package com.example.urvi.dpf;


/**
 * This object is sent when the device chooses its neighbors after connecting
 */

import java.io.Serializable;
import java.net.InetAddress;
import java.util.LinkedList;

/**
 * Created by Urvi on 12-Nov-15.
 */

public class MyNeighbors implements Serializable {
     String myAddress;
     InetAddress myInet;
     LinkedList<String> myNeighborList=null;

    MyNeighbors(String myAddress){
        myNeighborList = new LinkedList();
        this.myAddress = myAddress;
    }

    public  void addNeighbor(String  device){
        myNeighborList.add(device);
    }

    public void setInet(InetAddress ia){ myInet = ia; }

}

