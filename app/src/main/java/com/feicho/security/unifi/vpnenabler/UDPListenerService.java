// Copyright (C) 2022 Lars D. Feicho
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
// GNU General Public License for more details.
// You should have received a copy of the GNU General Public License
// along with this program.If not, see<http://www.gnu.org/licenses/>.

package com.feicho.security.unifi.vpnenabler;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class UDPListenerService extends Service {
    private final String PREF_NAME = "UNIFI_VPN_ENABLER";
    private final String CLOUD_KEY_IPADDRESS_KEY = "CLOUD_KEY_ADDRESS";
    private final String DEVICE_VPN_IPADDRESS_KEY = "DEVICE_VPN_ADDRESS";

    private MulticastSocket _socket;
    private WifiManager.MulticastLock _multicastLock;
    private Boolean _shouldRestartListener = true;
    private Thread _udpListenerThread;

    private String _deviceVpnAddress;
    private String _cloudKeyAddress;

    @Override
    public void onCreate() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        _cloudKeyAddress = prefs.getString(CLOUD_KEY_IPADDRESS_KEY, "none");
        _deviceVpnAddress = prefs.getString(DEVICE_VPN_IPADDRESS_KEY, "none");
    };

    @Override
    public void onDestroy() {
        stopListen();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        _shouldRestartListener = true;
        startListenForUDPBroadcast();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private byte[] intToBytes(int value)  {
        byte[] result = new byte[2];
        if ((value > Math.pow(2,31)) || (value < 0)) {
            return null;
        }
        result[0] = (byte)((value >>> 8) & 0xFF);
        result[1] = (byte)(value & 0xFF);
        return result;
    }

    private void listenAndWaitAndThrowIntent() throws Exception {
        InetAddress group = InetAddress.getByName("233.89.188.1");

        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        _multicastLock = wifi.createMulticastLock("multicastLock");
        _multicastLock.setReferenceCounted(true);
        _multicastLock.acquire();

        _socket = new MulticastSocket(10001);
        _socket.joinGroup(group);

        boolean cancellation = false;
        byte[] buffer = new byte[256];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        while(cancellation == false)
        {
            try {
                _socket.receive(packet);

                InetAddress host = InetAddress.getByName(_cloudKeyAddress);
                InetAddress hostTo = InetAddress.getByName(_deviceVpnAddress);

                byte[] ipAddress = hostTo.getAddress();
                byte[] messagePayload = new byte[6];
                messagePayload[0] = (ipAddress[0]);
                messagePayload[1] = (ipAddress[1]);
                messagePayload[2] = (ipAddress[2]);
                messagePayload[3] = (ipAddress[3]);

                int portNumber = packet.getPort();
                byte[] portNumberBytes = intToBytes(portNumber);

                messagePayload[4] = (portNumberBytes[0]);
                messagePayload[5] = (portNumberBytes[1]);

                DatagramPacket outgoingPacket = new DatagramPacket(messagePayload, messagePayload.length, host, 1338);
                DatagramSocket outgoingSocket = new DatagramSocket();

                outgoingSocket.send(outgoingPacket);
            } catch (Exception ex) {
                cancellation = true;
                // improve..
            }
        }

        try {
            _socket.leaveGroup(group);

            if(_socket != null && !_socket.isClosed()){
                _socket.close();
            }

            if (_multicastLock != null) {
                _multicastLock.release();
            }
        } catch (Exception ex){
            // improve..
        }
    }

    private void startListenForUDPBroadcast() {
        _udpListenerThread = new Thread(new Runnable() {
            public void run() {
                try {
                    while (_shouldRestartListener) {
                        listenAndWaitAndThrowIntent();
                    }
                } catch (Exception e) {
                    Log.i("UDP Listener", e.getMessage());
                }
            }
        });
        _udpListenerThread.start();
    }

    private void stopListen() {
        _shouldRestartListener = false;
        if(_socket != null && !_socket.isClosed())
        {
            _socket.close();
        }

        if (_multicastLock != null)
        {
            _multicastLock.release();
        }
    }
}