package com.example.urvi.dpf;

import java.io.Serializable;

/**
 * Created by Urvi on 11-Dec-15.
 */
public class CreditUpdatePacket implements Packet,Serializable {
    String device;
    CreditEntity ce;
    CreditUpdatePacket(String device,CreditEntity ce){
        this.device = device;
        this.ce = ce;
    }
}
