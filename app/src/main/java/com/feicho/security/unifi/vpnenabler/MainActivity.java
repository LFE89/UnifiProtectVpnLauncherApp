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

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private final String PREF_NAME = "UNIFI_VPN_ENABLER";
    private final String CLOUD_KEY_IPADDRESS_KEY = "CLOUD_KEY_ADDRESS";
    private final String DEVICE_VPN_IPADDRESS_KEY = "DEVICE_VPN_ADDRESS";

    private Intent _serviceIntent;

    private SharedPreferences _preferences;
    private SharedPreferences.Editor _prefEditor;

    private EditText _txtCloudKeyAddress;
    private EditText _txtDeviceVpnAddress;

    private boolean _isServiceStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        _preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        _prefEditor = getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit();

        Button btnSave = (Button)findViewById(R.id.btnSave);
        _txtCloudKeyAddress = (EditText) findViewById(R.id.txtCloudKeyAddress);
        _txtDeviceVpnAddress = (EditText) findViewById(R.id.txtVpnAddress);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!_txtCloudKeyAddress.getText().toString().matches("")
                || !_txtDeviceVpnAddress.getText().toString().matches("")){
                    // Introduce also further input check here....
                    // This is just a PoC to demonstrate technical feasibility

                    setAddress(_txtCloudKeyAddress.getText().toString(), CLOUD_KEY_IPADDRESS_KEY);
                    setAddress(_txtDeviceVpnAddress.getText().toString(), DEVICE_VPN_IPADDRESS_KEY);

                    Toast toast = Toast.makeText(getApplicationContext(), "SAVED", Toast.LENGTH_SHORT);
                    toast.show();

                    startUdpListenerService();
                }
            }
        });

        startUdpListenerService();
    }

    private void startUdpListenerService(){
        if (checkIfAddressesAreSet()){
            if(_isServiceStarted){
                stopService(_serviceIntent);
            }

            _serviceIntent = new Intent(this, UDPListenerService.class);
            startService(_serviceIntent);
            _isServiceStarted = true;


            Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.ubnt.unifi.protect");
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(launchIntent);
            }
        }
    }

    private void setAddress(String address, String key){
        try{
           _prefEditor.putString(key, address);
           _prefEditor.apply();
        }catch (Exception ex){
            // Improve...
        }
    }

    private boolean checkIfAddressesAreSet(){
        String tmpCloudKeyAddress = _preferences.getString(CLOUD_KEY_IPADDRESS_KEY, "none");
        String tmpDeviceVpnAddress = _preferences.getString(DEVICE_VPN_IPADDRESS_KEY, "none");

        if(tmpCloudKeyAddress == null || tmpDeviceVpnAddress == null || tmpCloudKeyAddress == "none" || tmpDeviceVpnAddress == "none"){
            return false;
        }

        _txtCloudKeyAddress.setText(tmpCloudKeyAddress);
        _txtDeviceVpnAddress.setText(tmpDeviceVpnAddress);

        return true;
    }
}