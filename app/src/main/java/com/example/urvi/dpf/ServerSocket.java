package com.example.urvi.dpf;

import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class ServerSocket extends Thread {
    java.net.ServerSocket socket = null;
    private final int THREAD_COUNT =10;
    private Handler handler;
    private static final String TAG = "GroupOwnerSocketHandler";
    private InetAddress serverIp = null;

    public ServerSocket(Handler handler, InetAddress serverIp) throws IOException {
        try {
            socket = new java.net.ServerSocket(MainActivity.SERVER_PORT);
            this.handler = handler;
            this.serverIp = serverIp;
        } catch (IOException e) {
            e.printStackTrace();
            pool.shutdownNow();
            throw e;
        }

    }

    /**
     * A ThreadPool for client sockets.
     */
    private final ThreadPoolExecutor pool = new ThreadPoolExecutor(
            THREAD_COUNT, THREAD_COUNT, 10, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>());

    @Override
    public void run() {
        DeviceInfo.InetToMacMap.put(serverIp,MainActivity.MY_ADDRESS);
        DeviceInfo.MacToInetMap.put(MainActivity.MY_ADDRESS, serverIp);

        while (true) {
            try {

                Socket newClient = socket.accept();
                ObjectOutputStream oos = new ObjectOutputStream(newClient.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(newClient.getInputStream());
                SocketEntry se = new SocketEntry(ois,oos);
                //socket table is used to identify which i/o streams to push data on
                SocketTable.addSocket(newClient.getInetAddress(), se);
                HashMap<InetAddress,SocketEntry> SOCK_TABLE = SocketTable.getSocketMap();
                //My Neighbor information for current client
                MyNeighbors myNeighborObj = (MyNeighbors) ois.readObject();
                //Mac address to IP address mapping
                DeviceInfo.MacToInetMap.put(myNeighborObj.myAddress, myNeighborObj.myInet);
                DeviceInfo.InetToMacMap.put(myNeighborObj.myInet, myNeighborObj.myAddress);
                //send connection information
                ConnectionGraph.addNeighbor(myNeighborObj.myAddress, myNeighborObj.myNeighborList);
                for(String d: myNeighborObj.myNeighborList){
                    ConnectionGraph.addNeighbor(d,myNeighborObj.myAddress);
                }
                for (SocketEntry a : SocketTable.getValues()) {
                    a.outputStream.writeObject(new ConnectionInfo(ConnectionGraph.getAdjacencyMatrix()));
                }
                List<String> myCurrentNeighbors = ConnectionGraph.getNeighbors(MainActivity.MY_ADDRESS);
                MainActivity.MY_NEIGHBORS = new ArrayList<>();
                for(WifiP2pDevice d:MainActivity.MYPEERS){
                    if(myCurrentNeighbors.contains(d.deviceAddress)){
                        MainActivity.MY_NEIGHBORS.add(d);
                    }
                }
                HashMap<String,LinkedList<String>> CHECK_THIS = ConnectionGraph.getAdjacencyMatrix();
                pool.execute(new ChatManager(newClient, handler,true));

                Log.d(TAG, "Launching the I/O handler");

            } catch (IOException e) {
                try {
                    if (socket != null && !socket.isClosed())
                        socket.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                e.printStackTrace();
                pool.shutdownNow();
                break;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
