package com.example.urvi.dpf;

import android.net.wifi.p2p.WifiP2pDevice;

import java.util.HashMap;

/**
 * Created by Urvi on 07-Dec-15.
 */
public class CreditTable {
    HashMap<String,CreditEntity> CreditMap = new HashMap<>();

    public CreditTable(){
        for(WifiP2pDevice d:MainActivity.MYPEERS){
            CreditMap.put(d.deviceAddress,new CreditEntity());
        }
        CreditMap.put(MainActivity.MY_ADDRESS,new CreditEntity());
    }

    public int getCreditValue(String device){
        return CreditMap.get(device).getTotalScore();
    }

    public void deductAmount(String device,int amt){
        CreditMap.get(device).makePayment(amt);
    }

    public void addAmount(String device,int amt){
        CreditMap.get(device).addRewards(amt);
    }

    public CreditEntity getCreditRecord(String device){
             return CreditMap.get(device);
    }

    public void setCreditRecord(String device,CreditEntity ce){
             CreditMap.put(device, ce);
    }

    public String toString(){
        String s= "";
        for(String v:CreditMap.keySet()){
            s+=v+" | ";
            s+=CreditMap.get(v).toString()+"\n";
        }
        return s;
    }

}
