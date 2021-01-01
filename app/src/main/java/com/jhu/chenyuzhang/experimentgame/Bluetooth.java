package com.jhu.chenyuzhang.experimentgame;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Build;
import android.support.annotation.RequiresApi;
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
import java.util.UUID;

public class Bluetooth extends AppCompatActivity {
    private static final String TAG = "Bluetooth";
    private static BluetoothSocket mmSocket;
    BluetoothAdapter mBluetoothAdapter;
    private String WRITEUUID = "0000ff02-0000-1000-8000-00805f9b34fb";
    private String SERVICESUUID = "0000ff00-0000-1000-8000-00805f9b34fb";
    BluetoothDevice mmDevice;
    BluetoothGattCallback mGatCallback;
    BluetoothGatt mBluetoothGatt;
    BluetoothGattService bluetoothGattService;
    BluetoothGattCharacteristic writeCharacteristic;
    public static OutputStream mmOutputStream;
    public static InputStream mmInputStream;
    private BytesHexStrTranslate bytesHexStrTranslate;

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

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void openBT(BluetoothDevice device, Context context, ParcelUuid[] uuids) throws IOException {
        //UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        mBluetoothGatt = mmDevice.connectGatt(context, false, bluetoothGattCallback);
        bluetoothGattService = mBluetoothGatt.getService(UUID.fromString(SERVICESUUID));
        writeCharacteristic = bluetoothGattService.getCharacteristic(UUID.fromString(WRITEUUID));
        Log.d(TAG, "connect");
    }

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        //连接状态改变时回调
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            mBluetoothGatt.discoverServices();
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("Bluetooth","Bluetooth Connected!");
            }
            else {
                Log.d("Bluetooth","Bluetooth Not Connected!");
            }

        }


        //写入成功回调函数
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        //接受数据回调
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            byte[] value = characteristic.getValue();
        }


    };



    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void timeStamper(String identity, String tstmp) {
        try {
            sendData(identity);
            sendData(tstmp);
            Log.d(TAG, "timestamper sent");

        } catch (IOException e) {
            Log.d(TAG, "timestamper exceptions");
            Intent intent = new Intent(context, BluetoothFailActivity.class);
            context.startActivity(intent);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void timeStamperJustID(String identity) {
        try {
            sendData(identity);
            Log.d(TAG, "ID sent");

        } catch (IOException e) {
            Log.d(TAG, "timestamper exceptions");
            Intent intent = new Intent(context, BluetoothFailActivity.class);
            context.startActivity(intent);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void sendData(String msg) throws IOException {
        msg += "\n";
        byte[] mybyte = BytesHexStrTranslate.StringtoBytes(msg);
        writeCharacteristic.setValue(mybyte);
        writeCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        mBluetoothGatt.writeCharacteristic(writeCharacteristic);
    }
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
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

}

class BytesHexStrTranslate {
    private static final char[] HEX_CHAR = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String bytesToHexFun1(byte[] bytes) {
        // 一个byte为8位，可用两个十六进制位标识
        char[] buf = new char[bytes.length * 2];
        int a = 0;
        int index = 0;
        for (byte b : bytes) { // 使用除与取余进行转换
            if (b < 0) {
                a = 256 + b;
            } else {
                a = b;
            }

            buf[index++] = HEX_CHAR[a / 16];
            buf[index++] = HEX_CHAR[a % 16];
        }

        return new String(buf);
    }

    public static String bytesToHexFun2(byte[] bytes) {
        char[] buf = new char[bytes.length * 2];
        int index = 0;
        for (byte b : bytes) { // 利用位运算进行转换，可以看作方法一的变种
            buf[index++] = HEX_CHAR[b >>> 4 & 0xf];
            buf[index++] = HEX_CHAR[b & 0xf];
        }

        return new String(buf);
    }

    public static String bytesToHexFun3(byte[] bytes) {
        StringBuilder buf = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) { // 使用String的format方法进行转换
            buf.append(String.format("%02x", new Integer(b & 0xff)));
        }

        return buf.toString();
    }

    public static byte[] StringtoBytes(String str) {
        if (str == null || str.trim().equals("")) {
            return new byte[0];
        }

        byte[] bytes = new byte[str.length() / 2];
        for (int i = 0; i < str.length() / 2; i++) {
            String subStr = str.substring(i * 2, i * 2 + 2);
            bytes[i] = (byte) Integer.parseInt(subStr, 16);
        }

        return bytes;
    }
}