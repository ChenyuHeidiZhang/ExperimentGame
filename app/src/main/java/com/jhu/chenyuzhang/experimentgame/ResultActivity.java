package com.jhu.chenyuzhang.experimentgame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jhu.chenyuzhang.experimentgame.Questions.Question2Att4OpActivity;
import com.jhu.chenyuzhang.experimentgame.Questions.Question2Att4OpHorizontal;
import com.jhu.chenyuzhang.experimentgame.Questions.Question4Activity;
import com.jhu.chenyuzhang.experimentgame.Questions.Question4ActivityHorizontal;
import com.jhu.chenyuzhang.experimentgame.Questions.Question4Att2OpActivity;
import com.jhu.chenyuzhang.experimentgame.Questions.Question4Att2OpHorizontal;
import com.jhu.chenyuzhang.experimentgame.Questions.QuestionActivity;
import com.jhu.chenyuzhang.experimentgame.Questions.QuestionActivityHorizontal;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.io.IOException;

public class ResultActivity extends AppCompatActivity {
    private double amountWon;
    private String temp;
    private String bluetooth_indicator;
    private Trial prevTrial;
    private ImageView imageViewCongrats;
    private TextView textViewSorry;
    private TextView textViewAmount;
    private Button buttonNextTrial;
    private String dbTstamp;
    private long backPressedTime;

    private boolean isDemo;
    private static final String KEY_DO_DEMO = "keyDoDemo";

    TrialDbHelper trialInfoDb;

    private SharedPreferences counter_prefs;
    private SharedPreferences prefTraining;
    private int trialCounter;
    public static final String KEY_TRIAL_COUNTER = "keyTrialCounter";
    public static final String KEY_TRAINING_NUM = "keyTrainingNum";
    public static final String KEY_TOTAL_AMOUNT = "keyTotalAmount";

    TimeDbHelper timeRecordDb;
    Bluetooth bluetooth;
    private String resultID = "37";
    private String next_button = "38";
    private String tap_next = "39";
    private String blue_screen = "42";
    boolean stop;
    private int trainingNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // hide the status bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_result);

        stop = false;
        imageViewCongrats = findViewById(R.id.image_view_congrats);
        textViewSorry = findViewById(R.id.text_view_sorry);
        textViewAmount = findViewById(R.id.text_view_result_amount);
        buttonNextTrial = findViewById(R.id.button_next_trial);

        amountWon = getIntent().getDoubleExtra("EXTRA_AMOUNT_WON", 0);   // get amount won passed as extra
        temp = getIntent().getStringExtra("DATABASE_RECORD_STRING");
        bluetooth_indicator = getIntent().getStringExtra("RESULTID");
        SharedPreferences demo_prefs = getSharedPreferences("doDemo", MODE_PRIVATE);
        isDemo = demo_prefs.getBoolean(KEY_DO_DEMO, true);   // get whether to initiate a training trial

        if (isDemo) {
            prefTraining = getSharedPreferences("prefTraining", MODE_PRIVATE);
            trainingNum = prefTraining.getInt(KEY_TRAINING_NUM, 0);
            trainingNum++;
            Log.d("trialCounter", Integer.toString(trainingNum) + " training");
            prefTraining.edit().putInt(KEY_TRAINING_NUM, trainingNum).apply();
        }

        // update total amount won; only add to totalAmountWon with some probability and if is not in training
        //int rewardPercentage = getResources().getInteger(R.integer.reward_percentage);
        //double random = new Random().nextDouble();


        if (!isDemo) {
            SharedPreferences prefs = getSharedPreferences("totalAmountWon", MODE_PRIVATE);
            double totalAmountWon = prefs.getFloat(KEY_TOTAL_AMOUNT, 0);

            totalAmountWon = totalAmountWon + amountWon;
            prefs.edit().putFloat(KEY_TOTAL_AMOUNT, (float) totalAmountWon).apply();

            Log.d("TAG-total_amount", Double.toString(totalAmountWon));
        }

        timeRecordDb = new TimeDbHelper(this);

        trialInfoDb = new TrialDbHelper(this);

        counter_prefs = getSharedPreferences("trialCounter", MODE_PRIVATE);
        trialCounter = counter_prefs.getInt(KEY_TRIAL_COUNTER, 1);

        bluetooth = new Bluetooth(getApplicationContext(), timeRecordDb);

        // get the trial whose result is shown
        prevTrial = trialInfoDb.getTrial(trialCounter-1);

        Handler handler = new Handler();
        dbTstamp = recordEvent("Blank Blue Screen On");
        bluetooth.timeStamper(blue_screen, dbTstamp);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                /*
                try {
                    // send identifier and timestamp
                    bluetooth.timeStamper( "resultID", getCurrentTime());
                    //bluetooth.sendData(String.format ("%.2f",amountWon));
                } catch (IOException e) {}
                 */

                if (!stop) {
                    displayResult();
                    dbTstamp = recordEvent(temp);
                    bluetooth.timeStamper(bluetooth_indicator, dbTstamp);
                }
            }
        }, 1000);
        Handler next_handler = new Handler();
        next_handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!stop) {
                    buttonNextTrial.setVisibility(View.VISIBLE);
                    dbTstamp = recordEvent("Show 'Next' button");
                    bluetooth.timeStamper(next_button, dbTstamp);
                }
            }
        }, 2000);

        final Runnable automaticClick = new Runnable() {
            @Override
            public void run() {
                recordEvent("Auto change page after 5 seconds");
                buttonNextTrial.performClick();
            }
        };

        final Handler automaticNext = new Handler();
        automaticNext.postDelayed(automaticClick,6000);

        buttonNextTrial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // at the end of every 4 blocks (160 trials), display the amount won during these 4 blocks;
                // go to new trial in that activity
                recordEvent("Next button clicked");
                automaticNext.removeCallbacks(automaticClick);
                timeRecordDb.close();
                if (!isDemo && (trialCounter - 1) % 5 == 0) {
                    //incrementTrialCounter();
                    Log.d("160trial", Integer.toString(trialCounter - 1));
                    Intent intent_total = new Intent(ResultActivity.this, TotalAmountActivity.class);
                    intent_total.putExtra("EXTRA_DISPLAY_ID", 1);  // 1 means to display amount over 4 blocks
                    startActivity(intent_total);
                    finish();

                } else {
                    Intent intent = getNextIntent();
                    startActivity(intent);
                    finish();
                }
                /*
                if (!isDemo && trialCounter % 160 == 0) {
                    incrementTrialCounter();
                    Intent intent_total = new Intent(ResultActivity.this, TotalAmountActivity.class);
                    intent_total.putExtra("EXTRA_DISPLAY_ID", 1);// 1 means to display amount over 4 blocks
                    startActivity(intent_total);

                } else {
                    Intent intent = getNextIntent();
                    startActivity(intent);
                }
                finish();

                dbTstamp = recordEvent("Tapped 'Next'");
                bluetooth.timeStamper(tap_next, dbTstamp);
                Intent intent = getNextIntent();
                startActivity(intent);
                finish();

                 */
            }
        });
    }

    private void displayResult() {
        if (prevTrial.getType().equals("1") || prevTrial.getType().equals("3")) {   // 2 att trial
            String firstAttType = prevTrial.getAttributes().get(0);

            // if the first Attribute is A+1 or P+1, then this trial is in gain domain
            if (firstAttType.equals("A+1") || firstAttType.equals("P+1")) {
                if (amountWon != 0) {
                    imageViewCongrats.setVisibility(View.VISIBLE);
                    textViewAmount.setText("You won $" + String.format("%.2f", amountWon) + "!");
                } else {  // if is A-1 or P-1
                    textViewSorry.setVisibility(View.VISIBLE);
                    textViewSorry.setText("Sorry.");
                    textViewAmount.setText("You didn't win any money.");
                }
            } else {    // if this trial is loss domain
                if (amountWon != 0) {
                    textViewSorry.setVisibility(View.VISIBLE);
                    textViewSorry.setText("Oh no!");
                    textViewAmount.setText("You lost $" + String.format("%.2f",-amountWon) + ".");
                } else {
                    textViewSorry.setVisibility(View.VISIBLE);
                    textViewSorry.setText("Phew!");
                    textViewAmount.setText("You didn’t lose any money.");
                }
            }
        } else {    // 4 att trial
            if (amountWon > 0) {
                imageViewCongrats.setVisibility(View.VISIBLE);
                textViewAmount.setText("You won $" + String.format("%.2f", amountWon) + "!");
            } else if (amountWon < 0) {
                textViewSorry.setVisibility(View.VISIBLE);
                textViewSorry.setText("Oh no!");
                textViewAmount.setText("You lost $" + String.format("%.2f",-amountWon) + ".");
            } else {
                textViewSorry.setVisibility(View.VISIBLE);
                textViewSorry.setText("");
                textViewAmount.setText("You didn't win or lose any money.");
            }
        }
    }

    /*
    private void incrementTrialCounter() {  // increment trial counter
        if (trialCounter == trialInfoDb.getNumRows()){
            trialCounter = 1;       // wrap around if reaches the end
        } else {
            trialCounter++;
        }

        counter_prefs.edit().putInt(KEY_TRIAL_COUNTER, trialCounter).apply();
    }

     */

    private Trial getNextTrial() {
        if (isDemo) {   // if is in training, randomly choose a trial; otherwise, pick the next one
            //int trial_num = new Random().nextInt((int)trialInfoDb.getNumRows()); // random integer in [0, table_size)
            int trial_num = new Random().nextInt(160);  // get one of the first 160 trials
            trial_num++;    // need to be in [1, size]
            Log.d("TAG-trial", "TrialNumber" + trial_num);

            counter_prefs.edit().putInt(KEY_TRIAL_COUNTER, trial_num).apply();  // set shared trialCounter to trial_num

            return trialInfoDb.getTrial(trial_num);
        }   // else
        //incrementTrialCounter();
        return trialInfoDb.getTrial(trialCounter);
    }

    private Intent getNextIntent() {
        Intent intent;

        Trial currentTrial = getNextTrial();

        if (currentTrial.getOrient().equals("0")) {  // 0: Horizontal, 1: Vertical
            if (currentTrial.getType().equals("1")) {  // 2Opt2Attr
                intent = new Intent(ResultActivity.this, QuestionActivityHorizontal.class);
            } else if (currentTrial.getType().equals("2")) {  // 2Opt4Attr
                intent = new Intent(ResultActivity.this, Question4Att2OpHorizontal.class);
            } else if (currentTrial.getType().equals("3")) {  // 4Opt2Attr
                intent = new Intent(ResultActivity.this, Question2Att4OpHorizontal.class);
            } else {  // 4Opt4Attr
                intent = new Intent(ResultActivity.this, Question4ActivityHorizontal.class);
            }
        } else {
            if (currentTrial.getType().equals("1")) {  // 2Opt2Attr
                intent = new Intent(ResultActivity.this, QuestionActivity.class);
            } else if (currentTrial.getType().equals("2")) {  // 2Opt4Attr
                intent = new Intent(ResultActivity.this, Question4Att2OpActivity.class);
            } else if (currentTrial.getType().equals("3")) {  // 4Opt2Attr
                intent = new Intent(ResultActivity.this, Question2Att4OpActivity.class);
            } else {  // 4Opt4Attr
                intent = new Intent(ResultActivity.this, Question4Activity.class);
            }
        }

        return intent;
    }

    //get current time in milliseconds
    private String getCurrentTime() {
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("dd:HH:mm:ss:SSS");
        String formattedDate= dateFormat.format(date);
        return formattedDate;
    }

    private String recordEvent(String event) {
        //long timeSpan = System.nanoTime() - startTime;
        //String timeString = String.format("%d", timeSpan / 1000);
        String timeString = getCurrentTime();

        timeRecordDb.insertData(timeString, event);
        return timeString;
    }

    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            recordEvent("Pressed back button, return to main page");
            stop = true;
            finish();
        } else {
            Toast.makeText(this, "Press back again to finish", Toast.LENGTH_SHORT).show();
        }
        backPressedTime = System.currentTimeMillis();
    }
}
