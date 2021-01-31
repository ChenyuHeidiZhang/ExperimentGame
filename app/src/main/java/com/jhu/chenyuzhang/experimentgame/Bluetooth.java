package com.jhu.chenyuzhang.experimentgame;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

import static java.lang.Thread.sleep;

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
    private Boolean isWriting = false;
    private Queue<String> writeQueue = new LinkedList<String>();
    BluetoothGattCharacteristic writeCharact;
    BluetoothGattCharacteristic notifyCharacteristic;

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
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public Boolean openBT(BluetoothDevice device, Context context) {
        assert mmDevice != null;
        mmDevice = device;
        mBluetoothGatt = mmDevice.connectGatt(context, false, bluetoothGattCallback);
        /*
        Boolean temp = mBluetoothGatt.requestMtu(30);
        try {
            sleep(20);
        } catch(Exception e) {
            Log.d("debug", "failed to request");
        }

         */
        //Stuck here until connected
        /*
        while (!connected) {
        }
        */
        if (mBluetoothGatt != null) {
            return true;
        }
        return false;
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
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.d("debug", "is reading");
            for (int i = 0; i < characteristic.getValue().length; i++) {
                Log.d(TAG, "onCharacteristicRead: "+characteristic.getValue()[i]);
            }
        }



        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            //byte[] value = characteristic.getValue();
            Log.d("debug", "hello");

            //Log.d("debug", "detected notification!!!!!!!!!!!!!!!!!!!!!!!");//Indication or notification was received
            //recordEvent(Arrays.toString(characteristic.getValue()));
        }

        /**
         * If writing characteristic is called
         */
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.d("debug", "写入成功");
            //mBluetoothGatt.setCharacteristicNotification(characteristic, true);
/*
            Log.d("debug", "I am HERE!!!!!!!!");
            Log.d("debug", Arrays.toString(characteristic.getValue()));
            writeNextValueFromQueue();
            if (status==BluetoothGatt.GATT_SUCCESS){
                isWriting = false;
                Log.d("debug", "onCharacteristicWrite: success!!");
                //mBluetoothGatt.setCharacteristicNotification(characteristic, true);
            }else {
                Log.d("debug", "onCharacteristicWrite: fail");
            }

 */
        }
        /*
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

         */

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d("debug", "写入成功");

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (mBluetoothGatt != null) {
                    mBluetoothGattService = mBluetoothGatt.getServices().get(0);
                    writeCharact = mBluetoothGattService.getCharacteristics().get(0);
                    boolean b = mBluetoothGatt.setCharacteristicNotification(writeCharact, true);
                    if (b) {
                        List<BluetoothGattDescriptor> descriptors = writeCharact.getDescriptors();
                        for (BluetoothGattDescriptor descriptor : descriptors) {
                            boolean b1 = descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            if (b1) {
                                mBluetoothGatt.writeDescriptor(descriptor);
                            }
                        }
                    }
                }
            }

                /*
                for (int i = 0; i < supportedGattServices.size(); i++) {
                    Log.i("success", "1:BluetoothGattService UUID=:" + supportedGattServices.get(i).getUuid());
                    List<BluetoothGattCharacteristic> listGattCharacteristic = supportedGattServices.get(i).getCharacteristics();
                    for (int j = 0; j < listGattCharacteristic.size(); j++) {
                        Log.i("success", "2:   BluetoothGattCharacteristic UUID=:" + listGattCharacteristic.get(j).getUuid());
                    }
                }
            } else {
                Log.e("debug", "onservicesdiscovered收到: " + status);
            }


            //设置serviceUUID,原型是：BluetoothGattService bluetoothGattService = bluetoothGatt.getService(UUID.fromString(SERVICESUUID));
            mBluetoothGattService = mBluetoothGatt.getServices().get(0);
            //设置写入特征UUID,原型是：BluetoothGattCharacteristic writeCharacteristic = bluetoothGattService.getCharacteristic(UUID.fromString(WRITEUUID));
            writeCharact = mBluetoothGattService.getCharacteristics().get(0);
            //设置监听特征UUID,原型是：BluetoothGattCharacteristic notifyCharacteristic = bluetoothGattService.getCharacteristic(UUID.fromString(NOTIFYUUID));
            notifyCharacteristic = mBluetoothGattService.getCharacteristics().get(0);

                 */
            //开启监听
            Log.d("debug", "uuid连接成功");

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
            Log.d("debug", "This called stamper: " + identity +";" + tstmp);

            sendData(tstmp);
            //sendData(tstmp);
            Log.d("debug", "timestamper sent");

        } catch (IOException e) {
            Log.d(TAG, "timestamper exceptions");
            Log.d("debug", "This causes the problem:  " + identity + tstmp);
            Intent intent = new Intent(context, BluetoothFailActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.context.startActivity(intent);
        }
    }

    /**
     * A different way of sending data to BLE (used for sending the attributes' information
     * @param identity The string of information
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void timeStamperJustID(String identity) {

        try {
            Log.d("debug", "This called ID: " + identity);
            sendData(identity);
            Log.d(TAG, "ID sent");

        } catch (IOException e) {
            Log.d(TAG, "timestamper exceptions");
            Log.d("debug", "This causes the problem:  " + identity);

            Intent intent = new Intent(context, BluetoothFailActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }

    }

    /**
     * The function that calls the writeCharacteristics() and send the message.
     * @param msg
     * @throws IOException
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void sendData(String msg) throws IOException {
        /*
        BluetoothGattCharacteristic writeCharact = mBluetoothGatt.getServices().get(0).getCharacteristics().get(0);
        if (mBluetoothGatt.setCharacteristicNotification(writeCharact, true)) {
            Log.d("debug", "the setting is true");
        }
        else {
            Log.d("debug", "the setting is false");
        }
        writeCharact.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        if (writeCharact.setValue(msg.getBytes())) {
            Log.d("debug", "the setting value is true");
        }
        else {
            Log.d("debug", "the setting value is false");
        }
        Log.d("debug", "The message is " + msg + " The bytes are " + Arrays.toString(msg.getBytes()));
        boolean temp = mBluetoothGatt.writeCharacteristic(writeCharact);
        if (!temp) {
            Log.d("debug", "it's false");

        }

         */
        //db record
        //recordEvent(msg);

        //bluetooth record
        try {
            sleep(130);
        }catch(InterruptedException e) {
            Log.d("debug", "exception!!!!!!!!!");
        }


        BluetoothGattCharacteristic writeCharact = mBluetoothGatt.getServices().get(0).getCharacteristics().get(0);
        /*
        for(BluetoothGattDescriptor dp:writeCharact.getDescriptors()){
            dp.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            mBluetoothGatt.writeDescriptor(dp);
        }

         */
        mBluetoothGatt.setCharacteristicNotification(writeCharact, true);
        writeCharact.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        writeCharact.setValue(msg.getBytes());
        Log.d("debug", "The message is " + msg + " The bytes are " + Arrays.toString(msg.getBytes()));
        if (!mBluetoothGatt.writeCharacteristic(writeCharact)) {
            Log.d("debug", "actually not success" + msg);
        }
        else {
            Log.d("debug", "written successfully " + msg);
        }

            /*
            if (!mBluetoothGatt.writeCharacteristic(writeCharact)) {
                throw new IOException();

            }

             */
                //db record
        recordEvent(msg);

        if (!mBluetoothGatt.readCharacteristic(writeCharact)) {
            Log.d("debug", "reading not successful");
        }

        /*
            Log.d("debug", "the message is" + Arrays.toString(msg.getBytes()));
            writeQueue.add(msg);
            Log.d("debug", writeQueue.size() + "is the size");

         */

            //writeNextValueFromQueue();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void writeNextValueFromQueue() {
        if (isWriting) {
            return;
        }
        if (writeQueue.size() == 0) {
            return;
        }
        isWriting = true;

        writeCharact = mBluetoothGatt.getServices().get(0).getCharacteristics().get(0);
        mBluetoothGatt.setCharacteristicNotification(writeCharact, true);
        writeCharact.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        writeCharact.setValue(writeQueue.poll().getBytes());
        Log.d("debug", "in the write next value function");
        //mNotifyCharacteristic.setValue(writeQueue.poll().getBytes());
        if (!mBluetoothGatt.writeCharacteristic(writeCharact)) {
            Log.d("debug", "it's false");
        }
        else {
            Log.d("debug", "it's true");
        }
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
        //mBluetoothGatt = null;
        //mBluetoothGattService = null;
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