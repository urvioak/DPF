package com.example.urvi.dpf;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by Urvi on 08-Dec-15.
 */
public class ConnectionInfo implements Packet,Serializable {

    public HashMap<String,LinkedList<String>> adjacencyMatrix;

    ConnectionInfo(HashMap<String,LinkedList<String>> adjacencyMatrix){
        this.adjacencyMatrix = adjacencyMatrix;
        System.out.println("SIZE: "+adjacencyMatrix.size());
    }



}
