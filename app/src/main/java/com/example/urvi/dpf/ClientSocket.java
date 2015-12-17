package com.example.urvi.dpf;

import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Handler;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Urvi on 19-Oct-15.
 */
public class ClientSocket extends Thread{
    private Handler handler;
    private ChatManager chat;

    private InetAddress mAddress;


    public ClientSocket(Handler handler, InetAddress groupOwnerAddress) {
        this.handler = handler;
        this.mAddress = groupOwnerAddress;

    }

    @Override
    public void run() {
        Socket socket = new Socket();

        try {

            socket.bind(null);
            SocketAddress sa = new InetSocketAddress(mAddress.getHostAddress(),
                    MainActivity.SERVER_PORT);
            socket.connect(sa, 10000);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            SocketEntry se = new SocketEntry(ois,oos);
            SocketTable.addSocket(socket.getInetAddress(), se);


            MainActivity.myNeighborObj.setInet(socket.getLocalAddress());
            MainActivity.myNeighborObj.myAddress = MainActivity.MY_ADDRESS;
            DeviceInfo.MacToInetMap.put(MainActivity.MY_ADDRESS,socket.getLocalAddress());
            DeviceInfo.InetToMacMap.put(socket.getLocalAddress(), MainActivity.MY_ADDRESS);

            //send the neighbor object to server
            oos.writeObject(MainActivity.myNeighborObj);


            chat = new ChatManager(socket, handler,false);

            new Thread(chat).start();


        } catch (IOException e) {
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return;
        }

    }


}
