package com.example.mylats;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothHealth;


public class ConnectActivity extends Activity
{
    private BluetoothHealth bluetoothHealth;
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private BluetoothProfile.ServiceListener profileListener = new BluetoothProfile.ServiceListener() {
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if(profile == BluetoothProfile.HEALTH)
                bluetoothHealth = (BluetoothHealth) proxy;
        }

        public void onServiceDisconnected(int profile) {
            if(profile == BluetoothProfile.HEALTH)
                bluetoothHealth = null;
        }
    };

  //  bluetoothAdapter.getProfileProxy(context, profileListener, BluetoothProfile.HEALTH);

    // call functions on bluetoothHealth

    //bluetoothAdapter.closeProfileProxy(bluetoothHealth);
}