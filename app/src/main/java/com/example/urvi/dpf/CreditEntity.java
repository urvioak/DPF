package com.example.urvi.dpf;

import java.io.Serializable;

/**
 * Created by Urvi on 01-Dec-15.
 */
public class CreditEntity implements Serializable {
    String transId;
    int initial;
    int payments;
    int rewards;
    CreditEntity(){
        initial = 500;
        payments = 0;
        rewards = 0;
    }

    public int getTotalScore(){
        return initial - payments + rewards;
    }

    public void makePayment(int pay){ payments += pay;}

    public void addRewards(int reward){ rewards += reward;}

    public void setTransId(String transId){
        this.transId = transId;
    }

    public String toString(){
        return "I:"+initial+"|P:"+payments+"|R:"+rewards;
    }

}
