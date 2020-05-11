package com.jhu.chenyuzhang.experimentgame;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jhu.chenyuzhang.experimentgame.Questions.QuestionActivity;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "BluetoothActivity2";
    private Button playGame;

    private Button signOut;
    private static boolean isSignedIn;
    private static final String KEY_IS_SIGNED_IN = "keyIsSignedIn";
    private SharedPreferences prefSignedIn;

    private TimeDbHelper timeRecordDb;

    private Button btnBT;
    Bluetooth bluetooth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        timeRecordDb = new TimeDbHelper(this);

        setContentView(R.layout.activity_main);
        playGame = findViewById(R.id.button_playGame);

        signOut = findViewById(R.id.button_signOut);
        prefSignedIn = getSharedPreferences("isSignedIn", MODE_PRIVATE);
        isSignedIn = prefSignedIn.getBoolean(KEY_IS_SIGNED_IN, false);

        // bluetooth set up
        bluetooth = new Bluetooth(timeRecordDb);
        btnBT = findViewById(R.id.button_bluetooth);

        btnBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getApplicationContext();
                try {
                    //bluetooth.findBT();
                    findBT();
                    Toast toast = Toast.makeText(context, "bluetooth connected", Toast.LENGTH_SHORT);
                    toast.show();
                } catch (IOException e) {
                    Toast toast = Toast.makeText(context, "bluetooth not connected", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        playGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, QuestionActivity.class);
                startActivity(intent);
            }
        });

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPasswordDialog();
            }
        });
    }

    public void findBT() throws IOException {
        bluetooth.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetooth.mBluetoothAdapter == null)
        {
            Log.d(TAG,"No bluetooth adapter available");
            return;
        } else {
            Log.d(TAG, "Bluetooth adapter is not null");
        }


        if(!bluetooth.mBluetoothAdapter.isEnabled())
        {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }


        Set<BluetoothDevice> pairedDevices = bluetooth.mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            Log.d(TAG, "pairedDevices>0");
            for(BluetoothDevice device : pairedDevices)
            {
                if(device.getName().equals("HC-06"))
                {
                    bluetooth.mmDevice = device;

                    ParcelUuid[] uuids = device.getUuids();
                    bluetooth.openBT(uuids);

                    try {
                        bluetooth.openBT(uuids);
                    } catch (IOException e) {
                        Log.d(TAG, "can't openBT with "+ uuids[0].getUuid());
                    }

                    break;
                }
            }
        }
        Log.d(TAG,"Bluetooth Device Found");
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

                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                startActivity(intent);
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
}
