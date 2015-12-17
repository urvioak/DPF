package com.example.urvi.dpf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Stack;

/**
 * when a data packet is created and is to be sent to destination
 * Created by Urvi on 23-Nov-15.
 */
public class DataPacket implements Packet,Serializable {

    String id;
    String src;
    String dest;
    String data;
    Stack<String> hops = new Stack<String>();
    ArrayList<String> hopsBackup = new ArrayList<>();
    boolean isData;


    DataPacket(String id,String src,String dest,String data ,boolean isData){
        this.id = id;
        this.src = src;
        this.dest = dest;
        this.data = data;
        this.isData = isData;
    }


}
