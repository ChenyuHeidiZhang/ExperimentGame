package com.jhu.chenyuzhang.experimentgame;

import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.util.UUID;

public class cBaseApplication extends Application {
    private static final String TAG = "BluetoothBaseApp";

    private static cBaseApplication sInstance;

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
        btSocket = socket;
    }

    public BluetoothSocket getBluetoothConnection() {
        return btSocket;
    }


}
