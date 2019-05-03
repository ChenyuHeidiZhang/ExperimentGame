package com.jhu.chenyuzhang.experimentgame;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.ParcelUuid;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private Button playGame;
    private static boolean doDemo;
    private static final String KEY_DO_DEMO = "keyDoDemo";

    private Button signOut;
    private static boolean isSignedIn;
    private static final String KEY_IS_SIGNED_IN = "keyIsSignedIn";
    private SharedPreferences prefSignedIn;

    private Button btOnOff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playGame = findViewById(R.id.button_playGame);

        signOut = findViewById(R.id.button_signOut);
        prefSignedIn = getSharedPreferences("isSignedIn", MODE_PRIVATE);
        isSignedIn = prefSignedIn.getBoolean(KEY_IS_SIGNED_IN, false);

        btOnOff = findViewById(R.id.button_BT);


        btOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BluetoothActivity2.class);
                startActivity(intent);
            }
        });

        playGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = getSharedPreferences("doDemo", MODE_PRIVATE);
                doDemo = prefs.getBoolean(KEY_DO_DEMO, true);

                if (doDemo) {
                    doDemo = false;
                    prefs.edit().putBoolean(KEY_DO_DEMO, doDemo).apply();
                    Intent intent = new Intent(MainActivity.this, DemoActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(MainActivity.this, QuestionActivity.class);
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

}
