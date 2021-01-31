package com.jhu.chenyuzhang.experimentgame;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.Toast;

import com.jhu.chenyuzhang.experimentgame.Questions.Question2Att4OpActivity;
import com.jhu.chenyuzhang.experimentgame.Questions.Question2Att4OpHorizontal;
import com.jhu.chenyuzhang.experimentgame.Questions.Question4Activity;
import com.jhu.chenyuzhang.experimentgame.Questions.Question4ActivityHorizontal;
import com.jhu.chenyuzhang.experimentgame.Questions.Question4Att2OpActivity;
import com.jhu.chenyuzhang.experimentgame.Questions.Question4Att2OpHorizontal;
import com.jhu.chenyuzhang.experimentgame.Questions.QuestionActivity;
import com.jhu.chenyuzhang.experimentgame.Questions.QuestionActivityHorizontal;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Bluetooth_Main";
    //private final String BluetoothName = "J205";    //HC-06

    private static boolean isSignedIn;
    private static final String KEY_IS_SIGNED_IN = "keyIsSignedIn";
    private SharedPreferences prefSignedIn;
    private static final String KEY_CONNECTED_BLUETOOTH = "keyConnectedBluetooth";
    private SharedPreferences prefBluetooth;
    private Boolean has_picked = false;

    private TimeDbHelper timeRecordDb;

    TrialDbHelper trialInfoDb;

    public static int trialCounter;
    public static final String KEY_TRIAL_COUNTER = "keyTrialCounter";

    private static final String SPINNER_DEFAULT = "- Bluetooth -";
    private Spinner spnBT;
    private Bluetooth bluetooth;

    private long backPressedTime = 0;
    private BluetoothAdapter bluetoothAdapter;


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // hide the status bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        timeRecordDb = new TimeDbHelper(this);

        Button playGame = findViewById(R.id.button_playGame);

        Button signOut = findViewById(R.id.button_signOut);
        prefSignedIn = getSharedPreferences("isSignedIn", MODE_PRIVATE);
        isSignedIn = prefSignedIn.getBoolean(KEY_IS_SIGNED_IN, false);

        trialInfoDb = new TrialDbHelper(this);

        prefBluetooth = getSharedPreferences("connectedBluetooth", MODE_PRIVATE);
        String connectedBluetooth = prefBluetooth.getString(KEY_CONNECTED_BLUETOOTH, "");

        // bluetooth set up
        bluetooth = new Bluetooth(getApplicationContext(), timeRecordDb);
        spnBT = findViewById(R.id.spinner_bluetooth);  // The dropdown selector for bluetooth devices.

        if (!initiateBT()) {
            Toast.makeText(this, "No bluetooth adapter available. Cannot connect to Bluetooth.", Toast.LENGTH_SHORT).show();
        } else {
            List<String> spnBluetoothItems = getBluetoothItems();

            ArrayAdapter<String> btItemsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spnBluetoothItems);
            btItemsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spnBT.setAdapter(btItemsAdapter);

            // Set the currently connected device on the spinner so that we don't need to connect again.
            if (!"".equals(connectedBluetooth)) {
                int spnPosition = btItemsAdapter.getPosition(connectedBluetooth);
                if (spnPosition != -1) {
                    spnBT.setSelection(spnPosition);
                }
            }
        }

        final Context context = getApplicationContext();
        spnBT.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                has_picked = true;
                String itemSelected = spnBT.getSelectedItem().toString();
                // If a Bluetooth module is selected, connect to it.
                if (!SPINNER_DEFAULT.equals(itemSelected)) {
                    try {
                        Toast.makeText(context, "Trying to connect to Bluetooth...", Toast.LENGTH_SHORT).show();
                        findBT(itemSelected);
                        Toast.makeText(context, "Bluetooth connected", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Toast.makeText(context, "bluetooth not connected", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                has_picked = false;
            }
        });

        playGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!has_picked) {
                    Toast.makeText(context, "You should connect to bluetooth first", Toast.LENGTH_SHORT).show();
                }
                else {
                    Intent intent = getNextIntent();
                    startActivity(intent);
                }
            }
        });

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPasswordDialog();
            }
        });
    }

    private Trial getNextTrial() {
        SharedPreferences counter_prefs = getSharedPreferences("trialCounter", MODE_PRIVATE);
        trialCounter = counter_prefs.getInt(KEY_TRIAL_COUNTER, 1);
        // always start with the shared trialCounter, which is initiated to 1 in Login and updated in ResultActivity

        return trialInfoDb.getTrial(trialCounter);
    }

    private Intent getNextIntent() {
        Intent intent;

        Trial currentTrial = getNextTrial();
        if (currentTrial.getOrient().equals("0")) {  // 0: Horizontal, 1: Vertical
            if (currentTrial.getType().equals("1")) {  // 2Opt2Attr
                intent = new Intent(MainActivity.this, QuestionActivityHorizontal.class);
            } else if (currentTrial.getType().equals("2")) {  // 2Opt4Attr
                intent = new Intent(MainActivity.this, Question4Att2OpHorizontal.class);
            } else if (currentTrial.getType().equals("3")) {  // 4Opt2Attr
                intent = new Intent(MainActivity.this, Question2Att4OpHorizontal.class);
            } else {  // 4Opt4Attr
                intent = new Intent(MainActivity.this, Question4ActivityHorizontal.class);
            }
        } else {
            if (currentTrial.getType().equals("1")) {  // 2Opt2Attr
                intent = new Intent(MainActivity.this, QuestionActivity.class);
            } else if (currentTrial.getType().equals("2")) {  // 2Opt4Attr
                intent = new Intent(MainActivity.this, Question4Att2OpActivity.class);
            } else if (currentTrial.getType().equals("3")) {  // 4Opt2Attr
                intent = new Intent(MainActivity.this, Question2Att4OpActivity.class);
            } else {  // 4Opt4Attr
                intent = new Intent(MainActivity.this, Question4Activity.class);
            }
        }

        return intent;
    }

    /* Returns true if bluetooth adapter is successfully initiated or false otherwise. */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public boolean initiateBT() {
        /*
        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetooth.mBluetoothAdapter = bluetoothManager.getAdapter();

        // 确认当前设备的蓝牙是否可用,
        // 如果不可用, 弹出一个对话框, 请求打开设备的蓝牙模块
        if (bluetooth.mBluetoothAdapter == null || !bluetooth.mBluetoothAdapter.isEnabled()) {
            Log.d(TAG,"No bluetooth adapter available");
            return false;
        }
        else {
            Log.d(TAG, "Bluetooth adapter is not null");
        }

        if(!bluetooth.mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }
        return true;
        */
        //如果不支持蓝牙\
        /*
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            //提示不支持蓝牙
            Toast.makeText(this, "Doesn't support bluetooth", Toast.LENGTH_SHORT).show();
            //退出程序
            return false;
        }

         */
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        //创建蓝牙适配器原型是BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetooth.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //如果蓝牙适配器为空
        if (bluetooth.mBluetoothAdapter == null) {
            //显示设备无蓝牙
            Toast.makeText(this, "No bluetooth on this device", Toast.LENGTH_SHORT).show();
            //退出
            return false;
        }
        //如果蓝牙未开启
        if (!bluetooth.mBluetoothAdapter.isEnabled()) {
            //不提示,直接开启蓝牙
            bluetooth.mBluetoothAdapter.enable();
            //提示开启蓝牙中
            Toast.makeText(this, "Bluetooth is on", Toast.LENGTH_SHORT).show();

        }
        return true;
    }

    /*
     * Returns a list of Bluetooth items to be shown in the dropdown.
     * Throws NullPointerException if the mBluetoothAdapter is null.
     */
    public List<String> getBluetoothItems() throws NullPointerException {
        Set<BluetoothDevice> pairedDevices = bluetooth.mBluetoothAdapter.getBondedDevices();
        List<String> bluetoothItems = new ArrayList<>();
        bluetoothItems.add(SPINNER_DEFAULT);
        if(pairedDevices.size() > 0) {
            Log.d(TAG, "pairedDevices>0");
            for(BluetoothDevice device : pairedDevices) {
                bluetoothItems.add(device.getName());
            }
        }
        return bluetoothItems;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void findBT(String bluetoothName) throws IOException {
        Set<BluetoothDevice> pairedDevices = bluetooth.mBluetoothAdapter.getBondedDevices();
        for(BluetoothDevice device : pairedDevices) {
            if(device.getName().equals(bluetoothName)) {
                Bluetooth.mmDevice = device;

                ParcelUuid[] uuids = device.getUuids();
                if (!bluetooth.openBT(device, getApplicationContext())) {
                    throw new IOException();
                }
                break;
            }
        }
        Log.d(TAG,"Bluetooth Device Found");

        prefBluetooth.edit().putString(KEY_CONNECTED_BLUETOOTH, bluetoothName).apply();
    }

    public void checkPasswordDialog() {
            LayoutInflater li = LayoutInflater.from(MainActivity.this);
            View promptsView = li.inflate(R.layout.signout_prompt, null);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
            alertDialogBuilder.setView(promptsView);

            final EditText passwordInput = promptsView.findViewById(R.id.edit_text_password_input);

            alertDialogBuilder
                    .setCancelable(true)
                    .setPositiveButton("Sign Out", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (passwordInput.getText().toString().equals(getString(R.string.password))) {
                                isSignedIn = false;
                                prefSignedIn.edit().putBoolean(KEY_IS_SIGNED_IN, isSignedIn).apply();

                                // display final total when logging out
                                Intent intent_total = new Intent(MainActivity.this, TotalAmountActivity.class);
                                intent_total.putExtra("EXTRA_DISPLAY_ID", 0);  // 0 means to display overall total
                                startActivity(intent_total);
                                finish();
                            } else {
                                Toast.makeText(MainActivity.this, "Wrong password", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public static String getCurrentTime() {
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("dd:HH:mm:ss:SSS");
        String formattedDate= dateFormat.format(date);
        return formattedDate;
    }

    public static String getBinaryTime() {
        String time = getCurrentTime();
        String binaryTime = "";
        // sending numbers instead of binary string
        // 14 characters instead of 32
        binaryTime = time;
        return binaryTime;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onBackPressed() {
        // Finish the app if the user back presses twice within 2 seconds.
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            // Shutdown bluetooth connection before exiting the app.
            bluetooth.resetConnection();
            prefBluetooth.edit().putString(KEY_CONNECTED_BLUETOOTH, "").apply();
            finish();
        } else {
            Toast.makeText(MainActivity.this, "Press back again to exit the app", Toast.LENGTH_SHORT).show();
        }

        backPressedTime = System.currentTimeMillis();
    }
}
