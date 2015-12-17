package com.example.urvi.dpf;

/**
 * Handles all the message exchanges
 */
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;



public class MessageExchangeFragment extends Fragment implements ChooseNeighborFragment.OnHopDismissListener {

    View v;
    public ChatManager chatManager;
    private TextView textToSend;
    private ListView list;
    ArrayAdapter<String> adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_message_exchange, container, false);

        list = (ListView) v.findViewById(R.id.listView);
        ArrayList<String> messageList = new ArrayList<>();
        adapter = new ArrayAdapter<>(getActivity()
                , android.R.layout.simple_list_item_1, messageList);
        list.setAdapter(adapter);
        Button sendButton = (Button) v.findViewById(R.id.buttonSend);
        Button resetButton = (Button) v.findViewById(R.id.resetCredit);

        textToSend = (TextView) v.findViewById(R.id.txtChatLine);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chatManager != null) {
                    ChooseNeighborFragment frag = new ChooseNeighborFragment();
                    Bundle args = new Bundle();
                    args.putString("title", "Whom to send?");
                    args.putInt("listType", android.R.layout.simple_list_item_single_choice);
                    frag.setArguments(args);
                    frag.show(getFragmentManager(), "dialog");
                }
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.MYPEERS = WifiDirectBroadcastReceiver.peers;
                MainActivity.MY_CREDIT_TABLE = new CreditTable();
                chatManager.StartCreditUpdateThread();
            }
        });

        return v;
    }

    public void updateChat(String chatLine){
        adapter.add(chatLine);
        adapter.notifyDataSetChanged();
    }


    public String getTransactionId(){
            return (MainActivity.transactionCount++)+"|"+MainActivity.MY_ADDRESS;
        }

    public void OnHopDismissListener(WifiP2pDevice selectedDevice) {
        String transId = getTransactionId();
        DataPacket dp =new DataPacket(transId,MainActivity.MY_ADDRESS,
                selectedDevice.deviceAddress,textToSend.getText().toString(),true);
        MainActivity.MY_TRANSACTIONS.add(transId);
        dp.hops.push(MainActivity.MY_ADDRESS);
        Log.d("CREDIT STATUS","Sending Message from "+dp.src+" to "+dp.dest);
        int amt = (10*MainActivity.MYPEERS.size());
        MainActivity.MY_CREDIT_TABLE.deductAmount(MainActivity.MY_ADDRESS, amt);
        chatManager.WriteToSocket(dp);
        updateChat("Sent: " + textToSend.getText().toString());
        textToSend.setText("");
        textToSend.clearFocus();
    }

}
