package com.example.urvi.dpf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Stack;

/**
 * When a payment is to be sent after acknowledgement is received
 * Created by Urvi on 08-Dec-15.
 */
public class Payment implements Packet, Serializable {
    int payment;
    String paymentID;
    ArrayList<String> allHops;
    String src;


    public Payment(int payment,String paymentID,ArrayList<String> allHops,String src){
        this.payment=payment;
        this.paymentID=paymentID;
        this.allHops = allHops;
        this.src = src;
    }
}
