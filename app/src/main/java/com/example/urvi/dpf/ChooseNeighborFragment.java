package com.example.urvi.dpf;

import android.app.Activity;
import android.app.DialogFragment;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Created by Urvi on 08-Nov-15.
 */
public class ChooseNeighborFragment extends DialogFragment {
    OnDialogDismissListener mCallback;
    ListView lv = null;
    Button btnOkay = null;
    String title = "";
    int listType = 0;
    Collection<WifiP2pDevice> peers;
    DeviceListAdapter peerAdapter;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        title = getArguments().getString("title");
        listType = getArguments().getInt("listType");
        View view = inflater.inflate(R.layout.fragment_choose_neighbor, null, false);
        lv = (ListView) view.findViewById(R.id.list);
        btnOkay = (Button) view.findViewById(R.id.buttonChooseNeighbors);
        getDialog().setTitle(title);
//        if(listType == android.R.layout.simple_list_item_single_choice)
//            peers = MainActivity.MY_NEIGHBORS;

//        if(listType == android.R.layout.simple_list_item_multiple_choice)
            peers = WifiDirectBroadcastReceiver.peers;

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
// WifiDirectBroadcastReceiver.peers
        peerAdapter = new DeviceListAdapter(getActivity(), (List<WifiP2pDevice>) peers);
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        lv.setAdapter(peerAdapter);
        btnOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SparseBooleanArray checked = lv.getCheckedItemPositions();
                ArrayList<WifiP2pDevice> selectedDevices = new ArrayList<>();
                for (int i = 0; i < checked.size(); i++) {
                    if (checked.valueAt(i))
                        selectedDevices.add(peerAdapter.getDevice(checked.keyAt(i)));
                }
                System.out.println("Checked");
                dismiss();
                if(listType == android.R.layout.simple_list_item_single_choice)
                    mCallback.onDialogDismissListener(selectedDevices,true);
                if(listType == android.R.layout.simple_list_item_multiple_choice)
                    mCallback.onDialogDismissListener(selectedDevices,false);
            }
        });

    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnDialogDismissListener) activity;

        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnDialogDismissListener");
        }
    }


    public interface OnDialogDismissListener {
         void onDialogDismissListener(ArrayList<WifiP2pDevice> selectedDevices,boolean isHop);
    }

    public interface OnHopDismissListener{
        void OnHopDismissListener(WifiP2pDevice selectedDevice);
    }

}
