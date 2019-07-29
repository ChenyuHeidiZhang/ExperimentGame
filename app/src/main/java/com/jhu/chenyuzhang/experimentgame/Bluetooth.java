package com.jhu.chenyuzhang.experimentgame;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

public class Bluetooth {
    private static final String TAG = "Bluetooth";
    private static BluetoothSocket mmSocket;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mmDevice;
    public static OutputStream mmOutputStream;
    public static InputStream mmInputStream;

    private static Thread workerThread;
    public static byte[] readBuffer;
    public static int readBufferPosition;
    public static volatile boolean stopWorker;

    private TimeDbHelper timeRecordDb;

    public Bluetooth(TimeDbHelper db) {
        this.timeRecordDb = db;
    }


    public void openBT(ParcelUuid[] uuids) throws IOException
    {
        //UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuids[0].getUuid());
        Log.d(TAG, "createRfcommSocketToServiceRecord" + uuids[0].getUuid());
        mmSocket.connect();
        Log.d(TAG, "connect");
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();

        beginListenForData();

        Log.d(TAG,"Bluetooth Opened");
    }

    public void timeStamper(String identity, String tstmp) throws IOException {
        try {
            sendData(identity);
            sendData(tstmp);
            Log.d(TAG, "timestamper sent");

        } catch (IOException e) {
            Log.d(TAG, "timestamper exceptions");
        }
    }

    public void sendData(String msg) throws IOException
    {
        try {
            mmOutputStream = mmSocket.getOutputStream();
        } catch (IOException e) {
            Log.d(TAG,"can't getOutputStream");
        }

        msg += "\n";
        mmOutputStream.write(msg.getBytes());
    }


    public void beginListenForData()
    {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        int bytesAvailable = mmInputStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                /*if (i==0){
                                    try {
                                        sendData("s");
                                    }
                                    catch (IOException ex) {
                                        Log.d(TAG, "data not sent");
                                    }
                                }*/
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            // put event in SQL LITE TABLE
                                            timeRecordDb.insertData(data, "received");
                                            // tvReceived.setText(data);
                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }
                }
            }
        });
        workerThread.start();
    }

}