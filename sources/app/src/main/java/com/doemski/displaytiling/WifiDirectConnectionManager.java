package com.doemski.displaytiling;

import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

public class WifiDirectConnectionManager {

    WifiP2pManager wifiP2pManager;
    WifiP2pManager.Channel channel;
    DTBroadcastReceiver receiver;
    MainActivity mainActivity;
    WifiP2pManager.PeerListListener peerListListener;
    boolean shouldConnect;
    boolean isMaster;

    public WifiDirectConnectionManager(WifiP2pManager wifiP2pManager, final MainActivity mainActivity){
        this.wifiP2pManager = wifiP2pManager;
        this.mainActivity = mainActivity;
        this.channel = wifiP2pManager.initialize(mainActivity, Looper.getMainLooper(), null);


        this.receiver = new DTBroadcastReceiver(wifiP2pManager, channel, mainActivity, new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(final WifiP2pDeviceList peers) {

                Log.d("isMaster, shouldConnect", isMaster + ", " + shouldConnect);
                //Show connection dialog only on master's screen if not already connected //TODO:Status stuff not workign. needed so windows dont keep popping up
                if(isMaster&&shouldConnect){// && acceptableStatus()){
                    if (peers.getDeviceList().size() >= 1) {
                        Log.d("Peers Available", "Yes");

                        final String[] peerNamesArray = new String[peers.getDeviceList().size()];
                        final String[] peerAdressesArray = new String[peers.getDeviceList().size()];
                        int i = 0;
                        for (WifiP2pDevice peer : peers.getDeviceList()) {
                            peerNamesArray[i] = peer.deviceName;
                            peerAdressesArray[i++] = peer.deviceAddress;
                        }


                        mainActivity.openPeerListWindow(peers, peerNamesArray, peerAdressesArray);
                        shouldConnect=false;

                    } else {
                        Log.d("Peers Available", "No");
                    }
                }
            }
        });
    }

    public DTBroadcastReceiver getReceiver() {
        return receiver;
    }

    public void discoverPeers(final boolean isMaster){
        this.isMaster=isMaster;
        shouldConnect = true;
        wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d("discover p2p peers", "success");


            }

            @Override
            public void onFailure(int reasonCode) {
                Log.d("discover p2p peers", "failure: reasonCode: " + reasonCode);
            }
        });
    }

    public void connectToPeer(WifiP2pDevice device){
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        wifiP2pManager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

                Log.d("Connecting to Peer", "Success");

                /*
                wifiP2pManager.requestConnectionInfo(channel, new WifiP2pManager.ConnectionInfoListener() {
                    @Override
                    public void onConnectionInfoAvailable(WifiP2pInfo info) {
                        Log.d("Connection Info", info.toString());
                    }
                });
                if(isMaster) {
                    new FileServerAsyncTask().execute();
                } else {

                }
                */
            }

            @Override
            public void onFailure(int reason) {
                Log.d("Connecting to Peer", "failure: reasonCode: " + reason);
            }
        });
    }

    private int getDeviceStatus(){
        String deviceTemp = WifiP2pManager.EXTRA_WIFI_P2P_DEVICE.replaceAll("[\\D]", "");
        if(deviceTemp.length()>0) {

            int deviceStatus = Integer.parseInt(deviceTemp.substring(deviceTemp.length() - 1));
            Log.d("device status", "" + deviceStatus);

            return deviceStatus;
        }
        return -1;
    }

    private boolean acceptableStatus(){
        int status = getDeviceStatus();

        return status != 0;
    }


}
