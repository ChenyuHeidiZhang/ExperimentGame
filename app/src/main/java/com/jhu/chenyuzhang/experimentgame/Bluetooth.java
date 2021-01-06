package com.jhu.chenyuzhang.experimentgame;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Bluetooth extends AppCompatActivity {
    private static final String TAG = "Bluetooth"; //For log.d
    //BLE information
    public static BluetoothAdapter mBluetoothAdapter;
    public static BluetoothDevice mmDevice;
    public static BluetoothGatt mBluetoothGatt;
    public static BluetoothGattService mBluetoothGattService;
    public static Boolean connected = false;
    //initialize bluetooth
    private TimeDbHelper timeRecordDb;
    private Context context;

    /**
     * This is the Bluetooth constructor
     * @param context the main context from where it is called and initialized
     * @param db the database helper
     */
    public Bluetooth(Context context, TimeDbHelper db) {
        this.context = context;
        this.timeRecordDb = db;
    }

    /**
     * This is the function that establish the actual connection
     * @param device the bluetooth device found in the main activity
     * @param context the context of the main activity
     * @throws IOException
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void openBT(BluetoothDevice device, Context context) {
        assert mmDevice != null;
        mmDevice = device;
        mBluetoothGatt = mmDevice.connectGatt(context, false, bluetoothGattCallback);
        //Stuck here until connected
        while (!connected) {
        }
    }

    /**
     * This is the call back of the Bluetooth
     */
    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        /**
         * When connection changes
         */
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            //If the connection is successful
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    mBluetoothGatt = gatt;
                    connected = true;
                }
                mBluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                close();
            }
        }

        /**
         * When services discovered
         */
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //use the first service listed
                mBluetoothGattService = mBluetoothGatt.getServices().get(0);
                /*
                //For debugging purposes, check out existing services and characters of each service
                if (mBluetoothGatt.getServices().isEmpty()) {
                    Log.d("onservices", "nothing");
                }
                else {
                    Log.d("onservices", "yes");
                    List<BluetoothGattService> services = mBluetoothGatt.getServices();
                    for (BluetoothGattService gattService : services) {
                        Log.d("onservices", "Service UUID Found: " + gattService.getUuid().toString());
                        List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                        for (BluetoothGattCharacteristic characteristic: gattCharacteristics) {
                            Log.d("onservice", characteristic.getUuid().toString() + "this uuid has character:" + characteristic.getPermissions());
                        }
                    }
                }
                 */
            }
            else {
                Log.d(TAG, "The BluetoothGatt is not success while calling onServiceDiscovered");
            }

        }

        /**
         * If writing characteristic is called
         */
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }
    };


    /**
     * Write to the BLE
     * @param identity The content string that stores the action
     * @param tstmp The string that stores the time stamp
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void timeStamper(String identity, String tstmp) {
        try {
            sendData(identity);
            sendData(tstmp);
            Log.d(TAG, "timestamper sent");

        } catch (Exception e) {
            Log.d(TAG, "timestamper exceptions");
            Intent intent = new Intent(context, BluetoothFailActivity.class);
            context.startActivity(intent);
        }
    }

    /**
     * A different way of sending data to BLE (used for sending the attributes' information
     * @param identity The string of information
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void timeStamperJustID(String identity) {
        try {
            sendData(identity);
            Log.d(TAG, "ID sent");

        } catch (Exception e) {
            Log.d(TAG, "timestamper exceptions");
            Intent intent = new Intent(context, BluetoothFailActivity.class);
            context.startActivity(intent);
        }
    }

    /**
     * The function that calls the writeCharacteristics() and send the message.
     * @param msg
     * @throws IOException
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void sendData(String msg) {
        //bluetooth record
        BluetoothGattCharacteristic writeCharact = mBluetoothGattService.getCharacteristics().get(0);
        mBluetoothGatt.setCharacteristicNotification(writeCharact, true);
        writeCharact.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        writeCharact.setValue(BytesHexStrTranslate.bytesToHexFun3(msg.getBytes()));
        mBluetoothGatt.writeCharacteristic(writeCharact);
        //db record
        recordEvent(msg);
    }

    /**
     * Reset input and output streams and make sure socket is closed.
     * This method will be used when app is quit to ensure that the connection is properly closed during a shutdown.
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void resetConnection() {
        if (mBluetoothGatt != null) {
            close();
            mBluetoothGatt = null;
        }
    }

    /**
     * Write event into the database
     * @param event the event
     */
    private void recordEvent(String event) {
        String timeString = getCurrentTime();
        timeRecordDb.insertData(timeString, event);
    }

    /** get current time in milliseconds
     * @return the string of time
     */
    private String getCurrentTime() {
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("dd:HH:mm:ss:SSS");
        String formattedDate= dateFormat.format(date);
        return formattedDate;
    }

    /**
     * Close the BLE properly
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.disconnect();
        mBluetoothGatt.close();
        mBluetoothGatt = null;
        mBluetoothGattService = null;
        connected = false;
    }
}

/**
 * This is the class that help format the characteristics which is transfered to BLE
 */
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