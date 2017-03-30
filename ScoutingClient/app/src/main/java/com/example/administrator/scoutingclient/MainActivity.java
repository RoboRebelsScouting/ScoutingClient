package com.example.administrator.scoutingclient;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    public String messageString = "hi there";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void init() {
        int REQUEST_ENABLE_BT = 1;
        BluetoothAdapter mBluetoothAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // bluetooth enabled
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address

                if (deviceName.equalsIgnoreCase("scoutingserver")) {
                    // do stuff
                    BluetoothClient btClient = new BluetoothClient(mBluetoothAdapter,device);
                    btClient.btSend(messageString);
                }
            }
        }
    }
}
