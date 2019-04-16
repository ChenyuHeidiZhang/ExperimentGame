package com.jhu.chenyuzhang.experimentgame;

import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.util.UUID;

public class cBaseApplication extends Application {
    private static final String TAG = "BluetoothBaseApp";

    private static cBaseApplication sInstance;

    //public BluetoothConnectionService myBlueComms;

    public static cBaseApplication getApplication() {
        return sInstance;
    }

    BluetoothSocket btSocket = null;

    public void onCreate() {
        super.onCreate();

        sInstance = this;
    }

    public void setupBluetoothConnection(BluetoothSocket socket)
    {
        //myBlueComms = blueComms;
        btSocket = socket;
    }

    public BluetoothSocket getBluetoothConnection() {
        return btSocket;
    }


    //public BluetoothConnectionService myBlueComms;

    /*private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    @Override
    public void onCreate()
    {
        super.onCreate();
        myBlueComms = new BluetoothConnectionService(cBaseApplication.this);

    } */

    /*public void startBTConnection(BluetoothDevice device, UUID uuid){
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection.");

        //mBluetoothConnection.startClient(device,uuid);
        myBlueComms.startClient(device,uuid);
    } */

}
