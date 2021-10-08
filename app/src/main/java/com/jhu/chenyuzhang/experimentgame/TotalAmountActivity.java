package com.jhu.chenyuzhang.experimentgame;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jhu.chenyuzhang.experimentgame.Questions.QuestionActivity;
import com.jhu.chenyuzhang.experimentgame.Questions.QuestionActivityHorizontal;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class TotalAmountActivity extends AppCompatActivity {

    public static final String KEY_TOTAL_AMOUNT = "keyTotalAmount";
    public static final String KEY_LAST_TOTAL = "keyLastTotal";
    private int display_id; // = 1: display total over 4 blocks; = 0: display grand total
    SharedPreferences signin;
    String signInTime;
    String signInDate;
    TimeDbHelper timeRecordDb;
    Bluetooth bluetooth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // hide the status bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_total_amount);

        timeRecordDb = new TimeDbHelper(this);

        display_id = getIntent().getIntExtra("EXTRA_DISPLAY_ID", 0);   // get total amount passed as extra

        TextView tvTotal = findViewById(R.id.text_view_total);
        final Button btNext = findViewById(R.id.button_next);

        SharedPreferences prefs = getSharedPreferences("totalAmountWon", MODE_PRIVATE);
        float totalAmountWon = prefs.getFloat(KEY_TOTAL_AMOUNT, 0);

        SharedPreferences pref_last = getSharedPreferences("lastTotal", MODE_PRIVATE);
        float lastAmount = pref_last.getFloat(KEY_LAST_TOTAL, 0);

        signin = getSharedPreferences("isSignedIn", MODE_PRIVATE);
        signInDate = signin.getString("startDate", "");
        signInTime = signin.getString("startTime", "");
        bluetooth = new Bluetooth(this.getApplicationContext(), timeRecordDb);


        if (display_id == 1) {  // display total amount over the past 4 blocks
            float thisAmount = totalAmountWon - lastAmount;
            //tvTotal.setText("Total Amount Won Over 4 Blocks: $" + String.format("%.2f", thisAmount));
            //recordEvent("Display 4 block total: $"+thisAmount);
            Log.d("My last amount is", String.valueOf(thisAmount));
            if ((int)thisAmount < getResources().getInteger(R.integer.PAYMAX)) {
                tvTotal.setText("Total Amount Won So Far: $" + String.format("%.2f", thisAmount));
                recordEvent("Display 4 block total: $" + thisAmount);
            }
            else {
                tvTotal.setText("Congratulations!" + "\n" + "You have won the maximum amount possible,\n" + "you will get a payment of $" + getResources().getInteger(R.integer.PAYMAX));
                recordEvent("Got " + totalAmountWon + "Display 4 block total: $" + getResources().getInteger(R.integer.PAYMAX));
            }
            pref_last.edit().putFloat(KEY_LAST_TOTAL, totalAmountWon).apply();  // update last_total with current_total

        } else {    // id == 0, display the grand total
            //tvTotal.setText("Total Amount Won: $" + String.format("%.2f", totalAmountWon));
            //recordEvent("Display grand total: $"+totalAmountWon);
            if ((int)totalAmountWon < getResources().getInteger(R.integer.PAYMAX)) {
                Log.d("not_much", "less");
                tvTotal.setText("Total Amount Won: $" + String.format("%.2f", totalAmountWon));
                recordEvent("Display grand total: $" + totalAmountWon);
            }
            else {
                Log.d("a_lot", "more");
                tvTotal.setText("Congratulations!" + "\n" + "You have won the maximum amount possible,\n" + "you will get a payment of $" + getResources().getInteger(R.integer.PAYMAX));
                recordEvent("Got " + totalAmountWon + "Display grand total: $" + getResources().getInteger(R.integer.PAYMAX));
            }
        }

        final Runnable automaticClick = new Runnable() {
            @Override
            public void run() {
                recordEvent("Auto change page after 5 seconds");
                btNext.performClick();
            }
        };

        final Handler automaticNext = new Handler();

        automaticNext.postDelayed(automaticClick,6000);

        btNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordEvent("Next button clicked");
                automaticNext.removeCallbacks(automaticClick);
                if (display_id == 1) {
                    int random = new Random().nextInt(2);
                    Intent intent;
                    if (random == 0) {
                        intent = new Intent(TotalAmountActivity.this, QuestionActivity.class);
                    } else {
                        intent = new Intent(TotalAmountActivity.this, QuestionActivityHorizontal.class);
                    }
                    startActivity(intent);

                } else {
                    btNext.setEnabled(false);
                    timeRecordDb.insertData(signInTime, "Signed in Time");
                    timeRecordDb.insertData(signInDate, "Signed in Date");
                    bluetooth.timeStamper("43", signInTime);
                    timeRecordDb.close();
                    Toast.makeText(getApplicationContext(), "Exiting...", Toast.LENGTH_LONG).show();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            android.os.Process.killProcess(android.os.Process.myPid());
                        }
                    }, 1000);
                }
                finish();
            }
        });

    }


    private String getCurrentTime() {
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("dd:HH:mm:ss:SSS");
        String formattedDate= dateFormat.format(date);
        return formattedDate;
    }

    private void recordEvent(String event) {    // only record once and close the db
        timeRecordDb.insertData(getCurrentTime(), event);
        timeRecordDb.close();
    }

}
