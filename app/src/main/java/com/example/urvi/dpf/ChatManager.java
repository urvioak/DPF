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
import java.util.List;
import java.util.Stack;
import java.util.Vector;

/**
 * Handles reading and writing of messages with socket buffers. Uses a Handler
 * to post messages to UI thread for UI updates.
 */
public class ChatManager implements Runnable {

    private Socket socket;
    private Handler handler;
    private boolean isServer ;
     private Thread sendCredit;

    public ChatManager(Socket socket, Handler handler, boolean isServer) {
        this.socket = socket;
        this.handler = handler;
        this.isServer = isServer;

    }

    private ObjectInputStream iStream;

    private static final String TAG = "ChatHandler";

    @Override
    public void run() {
        try {
            SocketEntry sockEntry =SocketTable.getSocket(socket.getInetAddress());

            iStream = sockEntry.inputStream;

            handler.obtainMessage(MainActivity.MY_HANDLE, this)
                    .sendToTarget();


            while (true) {

                    // Read from the InputStream
                    Packet packet = (Packet) iStream.readObject();


                     if(packet instanceof DataPacket){
                            DataPacket dp = (DataPacket)packet;

                            if(dp == null)
                                break   ;

                         //If packet is to be delivered to current device
                            if(dp.dest.equals(MainActivity.MY_ADDRESS)){
                                dp.hops.pop(); //pop address of current device
                                //if it is real data
                                if(dp.isData)
                                {
                                    handler.obtainMessage(MainActivity.MESSAGE_READ, dp).sendToTarget(); //print
                                    DataPacket dpAck = new DataPacket(dp.id,dp.dest,dp.src,"",false); //create ACK

                                    Log.d("CREDIT STATUS", "Received data from" + dp.src + " at " + dp.dest);
                                    dpAck.hops = dp.hops;
                                    for(String s:dp.hops){
                                        dpAck.hopsBackup.add(s);
                                    }
                                    dp.hopsBackup.add(MainActivity.MY_ADDRESS);
                                    dp.hopsBackup.remove(dp.src);
                                    WriteToSocket(dpAck);
                                }
                                else
                                {
                                    if(dp.hops.isEmpty()){

                                        int amt = (10*MainActivity.MYPEERS.size())/dp.hopsBackup.size();
                                        Payment pay = new Payment(amt,dp.id,dp.hopsBackup,dp.src);
                                        Log.d("CREDIT STATUS","Received ack from "+dp.src+" to "+dp.dest);
                                        MakePayment(pay);


                                    }
                                }
                                continue;
                            }
                            else
                            {
                                //forward to next hop
                                forwardToSocket(dp);
                            }

                            // Send the obtained bytes to the UI Activity
                            Log.d(TAG, "Rec:" + dp.id);
                    }
                    else if(packet instanceof Payment){
                         //when the current device gets paid
                        Payment pay = (Payment)packet;
                         if(isServer){
                             MakePayment(pay);
                         }else {
                             for (String h : pay.allHops) {
                                 if(MainActivity.MY_CREDIT_TABLE.CreditMap.keySet().contains(h))
                                 MainActivity.MY_CREDIT_TABLE.addAmount(h, pay.payment);
                                 Log.d("CREDIT STATUS","Received payment of "+pay.payment+" for "+h);
                             }
                         }


                    }
                    else if(packet instanceof ConnectionInfo){
                        //update the neighbor list
                         //this packet is received when a new device is added to the group
                        ConnectionInfo ci =(ConnectionInfo) packet;
                        ConnectionGraph.addNeighbor(MainActivity.MY_ADDRESS,ci.adjacencyMatrix.get(MainActivity.MY_ADDRESS));
                        List<String> myCurrentNeighbors = ConnectionGraph.getNeighbors(MainActivity.MY_ADDRESS);
                        MainActivity.MY_NEIGHBORS = new ArrayList<>();
                        for(WifiP2pDevice d:MainActivity.MYPEERS){
                            if(myCurrentNeighbors.contains(d.deviceAddress)){
                                MainActivity.MY_NEIGHBORS.add(d);
                            }
                        }
                    }else if (packet instanceof  CreditUpdatePacket){
                         // update the credit table
                         CreditUpdatePacket cPack = (CreditUpdatePacket)packet;
                         if(cPack.device.equals(MainActivity.MY_ADDRESS))
                             continue;
                         MainActivity.MY_CREDIT_TABLE.setCreditRecord(cPack.device,cPack.ce);
                         for (SocketEntry a : SocketTable.getValues()) {
                             a.outputStream.writeObject(cPack);
                         }
                     }

            }
        } catch (Exception e) {
             e.printStackTrace();
        } finally {
            try {
               socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Make payments to all the hops
     * @param pay
     * @throws IOException
     */
    private void MakePayment( Payment pay) throws IOException {

        for(String s:pay.allHops){
            MainActivity.MY_CREDIT_TABLE.addAmount(s, pay.payment);
            Log.d("CREDIT STATUS", "Received payment of " + pay.payment + " for " + s);
            if(isServer){
                if(s.equals(MainActivity.MY_ADDRESS)){
                    continue;
                }
                InetAddress nextHop = DeviceInfo.MacToInetMap.get(s);
                if(nextHop == null)
                    continue;
                HashMap<InetAddress,SocketEntry> SOCK_TABLE = SocketTable.getSocketMap();
                SocketEntry se = SocketTable.getSocket(nextHop);
                ObjectOutputStream os = se.outputStream;
                try {
                    os.writeObject(pay);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                for (SocketEntry a : SocketTable.getValues()) {
                    a.outputStream.writeObject(pay);
                }
            }
        }


    }


    /**
     * WriteToSocket to desired socket
     * top of stack for server,
     * only socket for client
     *
     * @param dp
     */
    public void WriteToSocket(DataPacket dp) {
        try {
            //if it is real data, check to top of stack. If current device is a hop, deduct credit
        if(dp.isData){
            if(dp.hops.peek()== MainActivity.MY_ADDRESS){

                dp= DeductCreditGetNextHop(dp);
                Log.d("CREDIT STATUS","A hop in Message from "+dp.src+" to "+dp.dest);
            }
        }

                if(isServer){
                    SendToNextHop(dp);
                }else {
                    //there will be only one socket entry for client
                    for (SocketEntry a : SocketTable.getValues()) {
                        a.outputStream.writeObject(dp);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

    }

    /**
     * reduce credit by 10 when a packet passes through a node
     * @param dp
     * @return
     */
    private DataPacket DeductCreditGetNextHop(DataPacket dp) {
        for(String a:dp.hops){
            MainActivity.MY_CREDIT_TABLE.deductAmount(a,10);
        }

        String nextHop = getNextHop(dp.hops);
        dp.hops.push(nextHop);
        MainActivity.MY_CREDIT_TABLE.deductAmount(nextHop,10);
        return dp;
    }


    /**
     * Forward to next hop in the hop stack
     * if it is a data packet, reduce credit and push next hop and forward
     * else pop next hop and forward
     * @param dp
     */
    public void forwardToSocket(DataPacket dp){

        if(isServer ){
            if(dp.isData){

                if(dp.hops.peek().equals(MainActivity.MY_ADDRESS)){
                    if(!MainActivity.MY_TRANSACTIONS.contains(dp.id)){
                        MainActivity.MY_TRANSACTIONS.add(dp.id);
                    }
                    dp=DeductCreditGetNextHop(dp);
                }

                    SendToNextHop(dp);

            }else{
                //acknowledgement
                //unwind the stack
                if(dp.hops.peek().equals(MainActivity.MY_ADDRESS))
                    dp.hops.pop();
                SendToNextHop(dp);
            }
        }else {
            if(dp.isData){
                if(dp.hops.peek().equals(MainActivity.MY_ADDRESS)){
                    if(!MainActivity.MY_TRANSACTIONS.contains(dp.id)){
                        MainActivity.MY_TRANSACTIONS.add(dp.id);
                    }
                    dp= DeductCreditGetNextHop(dp);
                    Log.d("CREDIT STATUS","A hop in Message from "+dp.src+" to "+dp.dest);
                }
            }else{
                dp.hops.pop();
            }
                for (SocketEntry a : SocketTable.getValues()) {
                    try {
                        a.outputStream.writeObject(dp);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

        }




    }

    /**
     * Get next hop based on credit score
     * @param hops
     * @return
     */
    public String getNextHop(Stack<String>hops){
        int maxCredit =0;
        String nextHop = "";
        for(WifiP2pDevice d:MainActivity.MY_NEIGHBORS){
            if(hops.contains(d.deviceAddress))
                continue;
            int currentCredit = MainActivity.MY_CREDIT_TABLE.getCreditValue(d.deviceAddress);
            if (maxCredit<currentCredit){
                maxCredit = currentCredit;
                nextHop = d.deviceAddress;
            }
        }

        return nextHop;
    }

    /*
    WriteToSocket to top of stack no matter what
     */
    private void SendToNextHop(DataPacket dp) {
        InetAddress nextHop = null;
        String top = dp.hops.peek();

         nextHop = DeviceInfo.MacToInetMap.get(top);

        HashMap<InetAddress,SocketEntry> SOCK_TABLE = SocketTable.getSocketMap();
        SocketEntry se = SocketTable.getSocket(nextHop);
        ObjectOutputStream os = se.outputStream;
        try {
            os.writeObject(dp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Print the credit table on console after every 30 sec
     */
    public void StartCreditUpdateThread(){

        sendCredit = new Thread(){
            public void run(){


                try {
                    while (true) {
                        Thread.sleep(30000);


//                   CreditUpdatePacket creditUpdatePacket = new CreditUpdatePacket(MainActivity.MY_ADDRESS
//                           ,MainActivity.MY_CREDIT_TABLE.getCreditRecord(MainActivity.MY_ADDRESS));
//                    for (SocketEntry a : SocketTable.getValues()) {
//                        a.outputStream.writeObject(creditUpdatePacket);
//                    }
                        Log.d("CREDIT STATUS", "\n" + MainActivity.MY_CREDIT_TABLE.toString());
                    }
                } catch (InterruptedException e) {
                    // We've been interrupted: no more messages.
                    System.out.println("Thread interrupted due to acknowledgment");
                    return;
                }
                // Print a message


            }
        };

       sendCredit.start();
    }



}
