package com.example.urvi.dpf;

import android.net.wifi.p2p.WifiP2pDevice;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * This maintains the connection matrix
 * Created by Urvi on 06-Nov-15.
 */
public final class ConnectionGraph implements Serializable {

    private static HashMap<String,LinkedList<String>> adjacencyMatrix = new HashMap<>();


    public static void addNeighbor(String currentNode,String neighbor) throws UnknownHostException {
        if(!adjacencyMatrix.containsKey(currentNode)){
            adjacencyMatrix.put(currentNode,new LinkedList<String>());
        }
        if(!adjacencyMatrix.get(currentNode).contains(neighbor))
            adjacencyMatrix.get(currentNode).add(neighbor);

    }

    public static void addNeighbor(String currentNode,LinkedList<String> neighbor) throws UnknownHostException {
        if(!adjacencyMatrix.containsKey(currentNode)){
            adjacencyMatrix.put(currentNode,new LinkedList<String >());
        }
        for (String d:neighbor)
        {
            if(!adjacencyMatrix.get(currentNode).contains(d))
                adjacencyMatrix.get(currentNode).add(d);
        }

    }

    public static LinkedList<String> getNeighbors(String currentNode){
        if (!adjacencyMatrix.containsKey(currentNode))
            return new LinkedList();
        return adjacencyMatrix.get(currentNode);
    }

    public static HashMap<String,LinkedList<String>> getAdjacencyMatrix(){
        return adjacencyMatrix;
    }

    public String toString(){
        return adjacencyMatrix.toString();
    }

    public static void setAdjacencyMatrix(HashMap<String,LinkedList<String>> matrix){
        adjacencyMatrix = matrix;
    }



}
