package com.jhu.chenyuzhang.experimentgame;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.widget.TextView;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

public class Bluetooth extends AppCompatActivity {
    private static final String TAG = "Bluetooth";
    private static BluetoothSocket mmSocket;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mmDevice;
    public static OutputStream mmOutputStream;
    public static InputStream mmInputStream;
   // public Queue<String> handShakeMessage = new LinkedList<>();

    private static Thread workerThread;
    public static byte[] readBuffer;
    public static int readBufferPosition;
    public static volatile boolean stopWorker;

    private TimeDbHelper timeRecordDb;
    private Context context;

    public Bluetooth(Context context, TimeDbHelper db) {
        this.context = context;
        this.timeRecordDb = db;
    }

    public void openBT(ParcelUuid[] uuids) throws IOException {
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

    public void timeStamper(String identity, String tstmp) {
        try {
            sendData(identity);
            sendData(tstmp);
            Log.d(TAG, "timestamper sent");
            Log.d(TAG, identity + " " + tstmp);

        } catch (IOException e) {
            Log.d(TAG, "timestamper exceptions");
            Intent intent = new Intent(context, BluetoothFailActivity.class);
            context.startActivity(intent);
        }
    }

    public void timeStamperJustID(String identity) {
        try {
            sendData(identity);
            Log.d(TAG, "ID sent");
            Log.d(TAG, identity);

        } catch (IOException e) {
            Log.d(TAG, "timestamper exceptions");
            Intent intent = new Intent(context, BluetoothFailActivity.class);
            context.startActivity(intent);
        }
    }

    public void sendData(String msg) throws IOException {
        try {
            mmOutputStream = mmSocket.getOutputStream();
        } catch (IOException e) {
            Log.d(TAG,"can't getOutputStream");
        }

        msg += "\n";
        mmOutputStream.write(msg.getBytes());
    }

    public void beginListenForData() {
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
                        if(bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for (int i = 0; i < bytesAvailable; i++) {
                                /*if (i==0){
                                    try {
                                        sendData("s");
                                    }
                                    catch (IOException ex) {
                                        Log.d(TAG, "data not sent");
                                    }
                                }*/
                                byte b = packetBytes[i];
                                if (b == delimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    /*
                                    if (handShakeMessage.peek() != null && !data.contains(handShakeMessage.peek())) {
                                        //if the returned string is not correct
                                        reconnectToBt(1);
                                    } else if (!handShakeMessage.isEmpty()){
                                        handShakeMessage.remove();
                                    }

                                     */
                                    //recordEvent(data);
                                    readBufferPosition = 0;

                                    handler.post(new Runnable() {
                                        public void run() {
                                            // put event in SQL LITE TABLE
                                            // timeRecordDb.insertData(data, "received");
                                            timeRecordDb.insertData(getCurrentTime(), data);
                                            // tvReceived.setText(data);
                                        }
                                    });
                                } else {
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


    /**
     * First pop up window to inform that the bluetooth is not working
     * Then try to reconnect to bluetooth
     */
    /*
    public void reconnectToBt(int n) {
        //int = 1: handshake message incorrect
        //int = 2: bluetooth not responsive for 500 milliseconds
        //int = 3: the thread is interrupted
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.popup, null);
        TextView message = promptsView.findViewById(R.id.popupmessage);
        if (n == 1) {
            message.setText(R.string.handshake_error);
        }
        else if (n == 2) {
            message.setText(R.string.bluetooth_error);
        }
        else {
            message.setText(R.string.thread_error);
        }
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(promptsView);
        alertDialogBuilder
                .setCancelable(true)
                .setNegativeButton("Retry", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();

                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        beginListenForData();
    }

     */
    /**
     * Reset input and output streams and make sure socket is closed.
     * This method will be used when app is quit to ensure that the connection is properly closed during a shutdown.
     */
    public void resetConnection() {
        if (mmInputStream != null) {
            try {mmInputStream.close();} catch(Exception e) {}
            mmInputStream = null;
        }
        if (mmOutputStream != null) {
            try {mmOutputStream.close();} catch (Exception e) {}
            mmOutputStream = null;
        }
        if (mmSocket != null) {
            try {mmSocket.close();} catch (Exception e) {}
            mmSocket = null;
        }
    }

    private void recordEvent(String event) {
        //long timeSpan = System.nanoTime() - startTime;
        //String timeString = String.format("%d", timeSpan / 1000);
        String timeString = getCurrentTime();

        timeRecordDb.insertData(timeString, event);
    }
    //get current time in milliseconds
    private String getCurrentTime() {
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("dd:HH:mm:ss:SSS");
        String formattedDate= dateFormat.format(date);
        return formattedDate;
    }
}
