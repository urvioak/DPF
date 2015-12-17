package com.example.urvi.dpf;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.net.wifi.p2p.nsd.WifiP2pServiceInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import static android.net.wifi.p2p.WifiP2pManager.*;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION;


public class MainActivity extends Activity implements ChannelListener,ConnectionInfoListener
        , Handler.Callback,ChooseNeighborFragment.OnDialogDismissListener/*,ChooseNeighborFragment.OnHopDismissListener*/ {

    final IntentFilter intentFilter = new IntentFilter();
    WifiP2pManager mManager;
    Channel mChannel;
    WifiDirectBroadcastReceiver mReceiver;
    WifiP2pDnsSdServiceRequest serviceRequest;
    public static final int MESSAGE_READ = 0x400 + 1;
    public static final int MY_HANDLE = 0x400 + 2;
    public static final int MY_CREDIT = 0x400 + 3;
    static final int SERVER_PORT = 4242;
    IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
    Intent batteryStatus = null;
    private Handler handlerCurrent = new Handler(this);
    public static MyNeighbors myNeighborObj = null;
    public static ArrayList<WifiP2pDevice> MY_NEIGHBORS = new ArrayList<>();
    public static ArrayList<String> MY_TRANSACTIONS = new ArrayList<>();
    public static int transactionCount = 0;

    public static final String TXTRECORD_PROP_AVAILABLE = "available";
    public static final String SERVICE_REG_TYPE = "_presence._tcp";
    public static MessageExchangeFragment msgFrag;

    final String TAG = "MainActivity";



    ListView listView;
    ArrayList<ServiceObject> AvailableServices = new ArrayList<>();

    HashMap<String,InetAddress> deviceInfoMap = new HashMap<>();

    ArrayAdapter<ServiceObject> availableServicesAdapter;
    Button nextButton;
    CheckBox cb ;

    public static String MY_ADDRESS = "";

    public static Collection<WifiP2pDevice> MYPEERS = new ArrayList<>();

    public static CreditTable MY_CREDIT_TABLE;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        intentFilter.addAction(WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        batteryStatus = this.registerReceiver(null, ifilter);
        msgFrag = new MessageExchangeFragment();
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, this);
        cb = (CheckBox) findViewById(R.id.checkBoxGO);

        //MY_ADDRESS = getCurrentMac();

        availableServicesAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, AvailableServices);
        listView = (ListView) findViewById(R.id.chooseServiceListView);
        listView.setAdapter(availableServicesAdapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                System.out.println("Item clicked at " + i);
                connectToService((ServiceObject) adapterView.getItemAtPosition(i));
            }
        });

        nextButton = (Button) findViewById(R.id.buttonOk);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Starting Registrations");
                String SERVICENAME = "DPF";
                startRegistration(SERVICENAME);
            }

        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, this);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);

    }

    @Override
    protected void onStop() {
        mManager.removeGroup(mChannel, new ActionListener() {

            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "Removed Group successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int arg0) {

            }
        });
        mManager.clearLocalServices(mChannel, new ActionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int reason) {
            }
        });

        super.onStop();

    }

    /**
     * Can be used to vote for group owner,
     * based on battery status and signal strength
     * @return
     */
    public int calculateIntent(){
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = level / (float)scale;
        System.out.println("Batt: "+batteryPct);
        WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        int rssi = wifiManager.getConnectionInfo().getRssi();
       // int signalStrength = wifiManager.calculateSignalLevel(rssi, 10);
        int signalStrength = 0;
        int intentValue = (int) ((batteryPct + signalStrength) *(15));

        return intentValue;
    }

    /*
        Connect to a Service
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void connectToService(ServiceObject serviceObject) {
        //Configure the connection request
        final WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = serviceObject.initiator.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        if(cb.isChecked()){
            config.groupOwnerIntent = 15;
        }else{
            config.groupOwnerIntent = 0;
        }
       //

        //Once service obtained, remove service request from the channel
        if (serviceRequest != null)
            mManager.removeServiceRequest(mChannel, serviceRequest,
                    new ActionListener() {

                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onFailure(int arg0) {

                        }
                    });
        //connect with set configuration
        mManager.connect(mChannel, config, new ActionListener() {

            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "Connecting to " + config.deviceAddress, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int errorCode) {
                Toast.makeText(MainActivity.this, "Failure connecting to" + config.deviceAddress + " Err Code " + errorCode, Toast.LENGTH_SHORT).show();
            }
        });

    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void startRegistration(String serviceName) {
        //add property to be applied to the service
        addService(serviceName);
        Log.d(TAG,"Added Service");
        discoverService();
    }

    /**
     * Add service to the channel
     * @param serviceName
     */
    private void addService(final String serviceName) {
        Map<String, String> record = new HashMap<>();

        record.put(TXTRECORD_PROP_AVAILABLE, "visible");
        //create instance of service with service name, type and properties
        final WifiP2pDnsSdServiceInfo service = WifiP2pDnsSdServiceInfo.newInstance(
                serviceName, SERVICE_REG_TYPE, record);

        //add the instance as a service to current channel
        mManager.addLocalService(mChannel, service, new ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Successfully added service to the channel " + serviceName);
            }

            @Override
            public void onFailure(int reasonCode) {
                Log.d(TAG, "Failed to add a service to the channel. Error Code: " + reasonCode);
            }
        });
    }

    /*
    Start listening for available services
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void discoverService() {
        //create instance of text record listener
        DnsSdTxtRecordListener txtRecList = new DnsSdTxtRecordListener() {
            @Override
            public void onDnsSdTxtRecordAvailable(String fullDomainName
                    , Map<String, String> txtRecordMap
                    , WifiP2pDevice srcDevice) {
                //send different types of requests

            }
        };


        DnsSdServiceResponseListener servRespListener
                = new DnsSdServiceResponseListener() {
            //if service is available, this method is called
            @Override
            public void onDnsSdServiceAvailable(String instanceName, String registrationType,
                                                WifiP2pDevice resourceType) {

                if ("DPF".equals(instanceName)|| "GO".equals(instanceName)) {
                    //add service to list to display to user
                    ServiceObject serviceToAdd = new ServiceObject();
                    serviceToAdd.initiator = resourceType;
                    serviceToAdd.serviceName = instanceName;
                    serviceToAdd.registrationType = registrationType;
                    availableServicesAdapter.add(serviceToAdd);
                    availableServicesAdapter.notifyDataSetChanged();
                }
            }
        };

        //set the listeners to current channel
        mManager.setDnsSdResponseListeners(mChannel, servRespListener, txtRecList);
        //create a new service to request
        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        //add the request to current channel
        mManager.addServiceRequest(mChannel,
                serviceRequest,
                new ActionListener() {
                    @Override
                    public void onSuccess() {
                    }
                    @Override
                    public void onFailure(int reasonCode) {
                    }
                });

        mManager.discoverServices(mChannel, new ActionListener() {

            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "Service discovery initiated",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int arg0) {
                Toast.makeText(MainActivity.this, "Service discovery failed " + arg0,
                        Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    public void onChannelDisconnected() {
        Toast.makeText(MainActivity.this,"Channel disconnected" ,Toast.LENGTH_SHORT).show();

    }

    Thread h = null;
    Thread hServer = null;
    private WifiP2pManager.GroupInfoListener groupInfoListener = new WifiP2pManager.GroupInfoListener() {
        @Override
        public void onGroupInfoAvailable(WifiP2pGroup group) {

            MYPEERS = group.getClientList();

            if(group.getClientList().size()==1)
            {
                hServer.start();
            }

            startChatFragment();
            //clear everything as this device will function only as group owner
            Toast.makeText(MainActivity.this, "Started Server", Toast.LENGTH_LONG).show();
            mManager.clearLocalServices(mChannel, new ActionListener() {
                @Override
                public void onSuccess() {
                    System.out.println("Cleared");
                }

                @Override
                public void onFailure(int reason) {
                }
            });

            startRegistration("GO");


        }
    };


    /**
     * When the connection is complete this method is called
     * @param wifiP2pInfo
     */
    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
        Toast.makeText(MainActivity.this,"Connection available for "+ wifiP2pInfo.toString() ,Toast.LENGTH_LONG).show();


        //If current device is a group owner, start a server else client
        if (wifiP2pInfo.isGroupOwner){
            try {
                hServer = new ServerSocket(handlerCurrent,wifiP2pInfo.groupOwnerAddress);
                mManager.requestGroupInfo(mChannel, groupInfoListener);
            } catch (IOException e) {
                Log.d(TAG,
                        "Failed to create a server thread - " + e.getMessage());
                return;
            }
        } else {
            try {
                    MYPEERS = WifiDirectBroadcastReceiver.peers;
                    myNeighborObj = new MyNeighbors(MY_ADDRESS);
                    ChooseNeighborFragment frag = new ChooseNeighborFragment();
                    Bundle args = new Bundle();
                    args.putString("title", "Choose Neighbors");
                    args.putInt("listType", android.R.layout.simple_list_item_multiple_choice);
                    frag.setArguments(args);
                    frag.show(getFragmentManager(), "dialog");

                h = new ClientSocket(handlerCurrent,
                        wifiP2pInfo.groupOwnerAddress);

                Toast.makeText(this,"Started Client", Toast.LENGTH_LONG).show();

            }catch(Exception e ){
                Log.d(TAG,
                        "Failed to create a client thread - " + e.getMessage());
            }
        }



    }


    /**
     * Messages pushed on the handle
     * @param msg
     * @return
     */
    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MainActivity.MESSAGE_READ:
                DataPacket readBuf = (DataPacket) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = readBuf.data;
                Log.d(TAG, readMessage);
                msgFrag.updateChat("Received: " + readMessage);
                break;

            case MainActivity.MY_HANDLE:
                ChatManager obj = (ChatManager)msg.obj;
                Toast.makeText(this,"MyHandle",Toast.LENGTH_LONG).show();
                msgFrag.chatManager = obj;
                break;
            case MainActivity.MY_CREDIT:
                Toast.makeText(this,"Credit",Toast.LENGTH_LONG).show();
                break;


        }
        return true;
    }



    private void startChatFragment() {
        getFragmentManager().beginTransaction().replace(R.id.mainLayout,msgFrag).commit();

        listView.setVisibility(View.GONE);
        nextButton.setVisibility(View.GONE);
        cb.setVisibility(View.GONE);
    }


    /**
     * set values based on input from users
     * @param selectedDevices
     * @param isHop
     */
    @Override
    public void onDialogDismissListener(ArrayList<WifiP2pDevice> selectedDevices,boolean isHop) {
        try {
            if (isHop) {
                ChooseNeighborFragment.OnHopDismissListener mHopCallback
                        = msgFrag;
                mHopCallback.OnHopDismissListener(selectedDevices.get(0));

            } else {
                MY_NEIGHBORS = selectedDevices;
                for (WifiP2pDevice d : selectedDevices)
                    try {
                        myNeighborObj.addNeighbor(d.deviceAddress);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                h.start();
                startChatFragment();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }


}