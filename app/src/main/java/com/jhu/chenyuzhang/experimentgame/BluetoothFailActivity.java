package com.jhu.chenyuzhang.experimentgame;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class BluetoothFailActivity extends AppCompatActivity {

    private TimeDbHelper timeRecordDb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_fail);
        //final Button next = findViewById(R.id.bluetooth_next);
        timeRecordDb = new TimeDbHelper(this);
        /*
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                next.setEnabled(false);
                //timeRecordDb.insertData(getCurrentTime(), "Bluetooth problem, exit the app");
                //timeRecordDb.close();
                Toast.makeText(getApplicationContext(), "Exiting...", Toast.LENGTH_LONG).show();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                }, 1000);
            }
        });

         */
    }
    private String getCurrentTime() {
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("dd:HH:mm:ss:SSS");
        String formattedDate= dateFormat.format(date);
        return formattedDate;
    }
}