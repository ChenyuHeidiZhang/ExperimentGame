package com.jhu.chenyuzhang.experimentgame.Questions;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.jhu.chenyuzhang.experimentgame.Bluetooth;
import com.jhu.chenyuzhang.experimentgame.EndDemoActivity;
import com.jhu.chenyuzhang.experimentgame.R;
import com.jhu.chenyuzhang.experimentgame.ResultActivity;
import com.jhu.chenyuzhang.experimentgame.TimeDbHelper;
import com.jhu.chenyuzhang.experimentgame.Trial;
import com.jhu.chenyuzhang.experimentgame.TrialDbHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.io.IOException;

public class QuestionActivityHorizontal extends AppCompatActivity {
    private boolean isDemo;
    private static final String KEY_DO_DEMO = "keyDoDemo";
    private SharedPreferences demo_prefs;

    private CountDownTimer countDownTimer;

    private TrialDbHelper trialInfoDb;
    private Trial currentTrial;
    public static int trialCounter;
    private SharedPreferences counter_prefs;
    private SharedPreferences prefTrialStatus;
    public static final String KEY_TRIAL_COUNTER = "keyTrialCounter";
    private double amountWon;
    private double a1;
    private double p1;
    private double a2;
    private double p2;

    private TimeDbHelper timeRecordDb;

    private ViewAnimator viewAnimator11;  // 11: 1st of first option (top left here)
    private ViewAnimator viewAnimator12;  // 12: 2nd of first option (bottom left)
    private ViewAnimator viewAnimator21;  // 21: 1st of second option (top right)
    private ViewAnimator viewAnimator22;  // 22: 2nd of second option (bottom right)
    private Button buttonSelect1, buttonSelect2;

    private String eventClick = "Tap";
    private String eventDisplay = "Show";
    private String eventTimeOut = "TimeOut, Mask On";
    private String dbTstamp;
    private String temp_click_holder = "";

    private long backPressedTime;
    private long startTime;

    // A map from viewAnimator ID to their corresponding handlers.
    private HashMap<Integer, Handler> viewHandlerMap = new HashMap<>();

    Bluetooth bluetooth;

    // identifiers maps the id of a attribute view to the code sent when it is uncovered
    // for each attribute, contains two codes before and after the uncover; third code is its alias in the database
    private HashMap<Integer, String[]> identifiers = new HashMap<>();

    // the code sent when an attribute view is covered after 1s
    private String identifier_cover = "34";
    private String identifier_coverEarly = "35";
    private String choice = "36";
    private String resultID = "37";
    private String select_uncover = "41";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefTrialStatus = getSharedPreferences("theTrialStatus", MODE_PRIVATE);
        //prefTrialStatus.edit().putBoolean("trialDone", false).apply();

        // hide the status bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_question_horizontal);

        demo_prefs = getSharedPreferences("doDemo", MODE_PRIVATE);
        isDemo = demo_prefs.getBoolean(KEY_DO_DEMO, true);   // get shared preference of whether this is a training session

        if(a1>0) {
            identifiers.put(R.id.view_animator_11, new String[] {"2", "18", "A+1"});
        }else{
            identifiers.put(R.id.view_animator_11, new String[] {"6", "22", "A-1"});
        }
        if(p1>0) {
            identifiers.put(R.id.view_animator_12, new String[] {"3", "19", "P+1"});
        }else{
            identifiers.put(R.id.view_animator_12, new String[] {"7", "23", "P-1"});
        }

        if(a2>0) {
            identifiers.put(R.id.view_animator_21, new String[] {"4", "20", "A+2"});
        }else{
            identifiers.put(R.id.view_animator_21, new String[] {"8", "24", "A-2"});
        }
        if(p2>0) {
            identifiers.put(R.id.view_animator_22, new String[] {"5", "21", "P+2"});
        }else{
            identifiers.put(R.id.view_animator_22, new String[] {"9", "25", "P-2"});
        }

        buttonSelect1 = findViewById(R.id.button_select1);
        buttonSelect2 = findViewById(R.id.button_select2);

        viewAnimator11 = findViewById(R.id.view_animator_11);
        viewAnimator12 = findViewById(R.id.view_animator_12);
        viewAnimator21 = findViewById(R.id.view_animator_21);
        viewAnimator22 = findViewById(R.id.view_animator_22);

        // if is in training part, display "training" and the "end training" button
        if (isDemo) {
            Button buttonEndDemo = findViewById(R.id.button_end_demo);
            TextView tvDemo = findViewById(R.id.text_view_demo);
            buttonEndDemo.setVisibility(View.VISIBLE);
            tvDemo.setVisibility(View.VISIBLE);

            buttonEndDemo.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    dbTstamp = recordEvent("Training ended");
                    // send identifier and timestamp
                    bluetooth.timeStamper(dbTstamp, "Training ended");

                    // end the training; go to EndDemoActivity
                    endDemo();
                }
            });
        }

        //startTime = System.nanoTime();    // get relative start time in nanoseconds; not used for now
        startTime = System.currentTimeMillis();

        //finish activity after 1 minute of inactivity
        int timeoutLength = getResources().getInteger(R.integer.trial_timeout_millis);
        countDownTimer = new CountDownTimer(timeoutLength,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                finish();
            }
        }.start();

        timeRecordDb = new TimeDbHelper(this);
        trialInfoDb = new TrialDbHelper(this);
        bluetooth = new Bluetooth(this.getApplicationContext(), timeRecordDb);

        setupTrial();

        if (isDemo) {
            dbTstamp = recordEvent("startTrainingTrial " + trialCounter);
        } else {
            dbTstamp = recordEvent("startTrial " + trialCounter);
        }
        // Trial start event
        bluetooth.timeStamper("1",dbTstamp);
        // send trial number + 100 followed by trial parameters followed by 0
        bluetooth.timeStamper(Integer.toString(trialCounter +100),dbTstamp);

        // store trial parameters in database
        ArrayList<String> attributes = currentTrial.getAttributes();
        dbTstamp = recordEvent("H " + "11 " + attributes.get(0) + " " + attributes.get(1)
                + ", " + "12 " + attributes.get(2) + " " + attributes.get(3)
                + ", " + "21 " + attributes.get(4) + " " + attributes.get(5)
                + ", " + "22 " + attributes.get(6) + " " + attributes.get(7));

        // send attribute magnitudes
        bluetooth.timeStamperJustID("40"); // event code for horizontal display
        bluetooth.timeStamperJustID(Double.toString(Math.round(Double.parseDouble(attributes.get(1))*10.0+60.0)));
        bluetooth.timeStamperJustID(Double.toString(Math.round(Double.parseDouble(attributes.get(3))*10.0+60.0)));
        bluetooth.timeStamperJustID(Double.toString(Math.round(Double.parseDouble(attributes.get(5))*10.0+60.0)));
        bluetooth.timeStamperJustID(Double.toString(Math.round(Double.parseDouble(attributes.get(7))*10.0+60.0)));

        // end the stream with the identifier 0
        bluetooth.timeStamperJustID(Integer.toString(0));

        viewAnimator11.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimator11,
                        new ViewAnimator[] {viewAnimator21, viewAnimator12, viewAnimator22});
            }
        });

        viewAnimator21.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimator21,
                        new ViewAnimator[] {viewAnimator11, viewAnimator12, viewAnimator22});

            }
        });

        viewAnimator12.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimator12,
                        new ViewAnimator[] {viewAnimator11, viewAnimator21, viewAnimator22});
            }
        });

        viewAnimator22.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimator22,
                        new ViewAnimator[] {viewAnimator11, viewAnimator21, viewAnimator12});
            }
        });

        buttonSelect1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View V) {
                //prefTrialStatus.edit().putBoolean("trialDone", true).apply();
                dbTstamp = recordEvent("Option1 selected");
                // send identifier and timestamp
                bluetooth.timeStamper( choice, dbTstamp);

                if (checkMinimumTimePassed()) {
                    incrementTrialCounter();
                    unmaskAttributes(new ViewAnimator[]{viewAnimator11, viewAnimator12}, "Option1");
                    showResult(a1, 1);
                }
            }
        });

        buttonSelect2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View V) {
                //prefTrialStatus.edit().putBoolean("trialDone", true).apply();
                dbTstamp = recordEvent("Option2 selected");
                // send identifier and timestamp
                bluetooth.timeStamper( choice, dbTstamp);

                if (checkMinimumTimePassed()) {
                    incrementTrialCounter();
                    unmaskAttributes(new ViewAnimator[]{viewAnimator21, viewAnimator22}, "Option2");
                    showResult(a2, 2);
                }
            }
        });
    }

    // called when each attribute is clicked
    private void attributeOnClick(final ViewAnimator tappedView, ViewAnimator[] otherViews) {
        /* on tap, if the attribute view is covered, uncover it for 1s and cover other attributes */
        if (tappedView.getDisplayedChild() == 0) {
            final String[] codes = identifiers.get(tappedView.getId());
            dbTstamp = recordEvent(codes[2] + ", " + codes[3] + " " + eventClick);
            // send identifier and timestamp
            bluetooth.timeStamper( codes[0], dbTstamp);

            if(!temp_click_holder.equals("")) {
                /* if other attributes are uncovered, cover them */
                for (ViewAnimator v: otherViews) {
                    if (v.getDisplayedChild() == 1) {
                        dbTstamp = recordEvent(temp_click_holder + " Early Mask On");
                        bluetooth.timeStamper( identifier_coverEarly, dbTstamp);
                        temp_click_holder = "";
                        v.showNext();
                    }
                }
            }
            //armVSyncHandlerA1();

            tappedView.showNext();  /* uncover */
            dbTstamp = recordEvent(codes[2] + ", " + codes[3] + " " + eventDisplay);
            bluetooth.timeStamper( codes[1], dbTstamp);
            temp_click_holder = codes[2] + ", " + codes[3];


            Log.d("Questions", codes[3]);

            /* automatically re-cover after 1000ms */
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (tappedView.getDisplayedChild() == 1 && !temp_click_holder.equals("")) {
                        tappedView.showNext();
                        dbTstamp = recordEvent(codes[2] + ", " + codes[3] + " " + eventTimeOut);
                        bluetooth.timeStamper( identifier_cover, dbTstamp);
                        temp_click_holder = "";
                    }
                }
            }, 1000);
            viewHandlerMap.put(tappedView.getId(), handler);
        }

        countDownTimer.cancel();
        countDownTimer.start();
    }

    private void endDemo(){
        demo_prefs.edit().putBoolean(KEY_DO_DEMO, false).apply();    // change shared "prefs" for do_demo to false

        // set trialCounter back to 1
        counter_prefs.edit().putInt(KEY_TRIAL_COUNTER, 1).apply();

        Intent intent = new Intent(QuestionActivityHorizontal.this, EndDemoActivity.class);
        startActivity(intent);
        finish();
    }

    private void setupTrial() {
        // load trialCounter from shared preference
        counter_prefs = getSharedPreferences("trialCounter", MODE_PRIVATE);
        trialCounter = counter_prefs.getInt(KEY_TRIAL_COUNTER, 1);

        // get current trial
        currentTrial = trialInfoDb.getTrial(trialCounter);
        getAttributes();
    }

    private void getAttributes(){
        String[][] send_code = new String[4][2];
        ArrayList<String> attributes = currentTrial.getAttributes();

        setAttributesForOneVA(viewAnimator11, attributes.get(0), attributes.get(1));
        setAttributesForOneVA(viewAnimator12, attributes.get(2), attributes.get(3));
        setAttributesForOneVA(viewAnimator21, attributes.get(4), attributes.get(5));
        setAttributesForOneVA(viewAnimator22, attributes.get(6), attributes.get(7));

        for (int i = 0; i < 4; ++i) {
            switch (attributes.get(i * 2)) {
                case "A+1":
                    send_code[i][0] = "2";
                    send_code[i][1] = "18";
                    break;
                case "A-1":
                    send_code[i][0] = "6";
                    send_code[i][1] = "22";
                    break;
                case "P+1":
                    send_code[i][0] = "3";
                    send_code[i][1] = "19";
                    break;
                case "P-1":
                    send_code[i][0] = "7";
                    send_code[i][1] = "23";
                    break;
                case "A+2":
                    send_code[i][0] = "4";
                    send_code[i][1] = "20";
                    break;
                case "A-2":
                    send_code[i][0] = "8";
                    send_code[i][1] = "24";
                    break;
                case "P+2":
                    send_code[i][0] = "5";
                    send_code[i][1] = "21";
                    break;
                case "P-2":
                    send_code[i][0] = "9";
                    send_code[i][1] = "25";
                    break;
                case "A+3":
                    send_code[i][0] = "10";
                    send_code[i][1] = "26";
                    break;
                case "A-3":
                    send_code[i][0] = "12";
                    send_code[i][1] = "28";
                    break;
                case "P+3":
                    send_code[i][0] = "11";
                    send_code[i][1] = "27";
                    break;
                case "P-3":
                    send_code[i][0] = "13";
                    send_code[i][1] = "29";
                    break;
                case "A+4":
                    send_code[i][0] = "14";
                    send_code[i][1] = "30";
                    break;
                case "A-4":
                    send_code[i][0] = "16";
                    send_code[i][1] = "32";
                    break;
                case "P+4":
                    send_code[i][0] = "15";
                    send_code[i][1] = "31";
                    break;
                case "P-4":
                    send_code[i][0] = "17";
                    send_code[i][1] = "33";
                    break;

            }
        }

        // First two are event codes sent via bluetooth (1st 2nd strings are for tap and displayed respectively).
        // 3rd (location) and last (attribute type) strings are the ones inserted into the SQLite database.
        identifiers.put(R.id.view_animator_11, new String[] {send_code[0][0], send_code[0][1], "11", attributes.get(0)});
        identifiers.put(R.id.view_animator_12, new String[] {send_code[1][0], send_code[1][1], "12", attributes.get(2)});

        identifiers.put(R.id.view_animator_21, new String[] {send_code[2][0], send_code[2][1], "21", attributes.get(4)});
        identifiers.put(R.id.view_animator_22, new String[] {send_code[3][0], send_code[3][1], "22", attributes.get(6)});

        // 1st 2 items in the string are the event codes sent to the arduino
        // 3rd item is stored in the database along with the timestamp
        /*
        if(a1>0) {
            identifiers.put(R.id.view_animator_11, new String[] {"2", "18", "A+1"});
        }else{
            identifiers.put(R.id.view_animator_11, new String[] {"6", "22", "A-1"});
        }
        if(p1>0) {
            identifiers.put(R.id.view_animator_12, new String[] {"3", "19", "P+1"});
        }else{
            identifiers.put(R.id.view_animator_12, new String[] {"7", "23", "P-1"});
        }

        if(a2>0) {
            identifiers.put(R.id.view_animator_21, new String[] {"4", "20", "A+2"});
        }else{
            identifiers.put(R.id.view_animator_21, new String[] {"8", "24", "A-2"});
        }
        if(p2>0) {
            identifiers.put(R.id.view_animator_22, new String[] {"5", "21", "P+2"});
        }else{
            identifiers.put(R.id.view_animator_22, new String[] {"9", "25", "P-2"});
        }

         */
    }

    private void setAttributesForOneVA(ViewAnimator va, String attType, String att) {
        ImageView imgView = (ImageView) va.getChildAt(0);
        TextView tv = (TextView) va.getChildAt(1);

        if (attType.equals("A+1") || attType.equals("A-1")) {
            a1 = Double.parseDouble(att);
            tv.setText("$" + String.format("%.2f", Math.abs(a1)));
        } else if (attType.equals("P+1") || attType.equals("P-1")) {
            p1 = Double.parseDouble(att);
            tv.setText((int) (p1 * 100) + "%");
        } else if (attType.equals("A+2") || attType.equals("A-2")) {
            a2 = Double.parseDouble(att);
            tv.setText("$" + String.format("%.2f", Math.abs(a2)));
        } else if (attType.equals("P+2") || attType.equals("P-2")) {
            p2 = Double.parseDouble(att);
            tv.setText((int) (p2 * 100) + "%");
        }

        if (attType.substring(0, 2).equals("A+")) {
            imgView.setImageResource(R.drawable.dollar_win);
            tv.setTextColor(Color.GREEN);
        } else if (attType.substring(0, 2).equals("A-")){
            imgView.setImageResource(R.drawable.dollar_lose);
            tv.setTextColor(Color.RED);
        } else if (attType.substring(0, 2).equals("P+")) {
            imgView.setImageResource(R.drawable.probability_win);
            tv.setTextColor(Color.GREEN);
        } else if (attType.substring(0, 2).equals("P-")) {
            imgView.setImageResource(R.drawable.probability_lose);
            tv.setTextColor(Color.RED);
        }
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

        if (!timeRecordDb.insertData(timeString, event))
            Toast.makeText(getApplicationContext(), "Something goes wrong with database", Toast.LENGTH_LONG).show();
        return timeString;
    }

    private boolean checkMinimumTimePassed() {
        if (System.currentTimeMillis() - startTime <
                getResources().getInteger(R.integer.min_time_millis_2Att2Opt)) {
            Toast.makeText(this, getString(R.string.stay_longer), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void unmaskAttributes(ViewAnimator[] viewAnimators, String option) {
        if (!temp_click_holder.equals("")) {
            ViewAnimator[] all = new ViewAnimator[]{viewAnimator11, viewAnimator21,
                    viewAnimator12, viewAnimator22};
            for (ViewAnimator a : all) {
                a.setDisplayedChild(0);
            }
            dbTstamp = recordEvent(temp_click_holder + " Early Mask On");
            bluetooth.timeStamper(identifier_coverEarly, dbTstamp);
            temp_click_holder = "";
        }
        for (ViewAnimator v : viewAnimators) {
            v.setDisplayedChild(1);
            Handler handler = viewHandlerMap.get(v.getId());
            if (handler != null) {
                handler.removeCallbacksAndMessages(null);
            }
        }
        dbTstamp = recordEvent(option + " Mask Off");
        bluetooth.timeStamper(select_uncover, dbTstamp);
        buttonSelect1.setEnabled(false);
        buttonSelect2.setEnabled(false);
    }

    private void showResult(double a, int option) {
        String outcomes[] = currentTrial.getOutcomes();
        String outcome = outcomes[option - 1];
        if ("win".equals(outcome) || "lose".equals(outcome)) {
            amountWon = a;  // This can be either positive or negative.
        } else {  // "no outcome".equals(outcome)
            amountWon = 0;
        }

       final String temp = "Option" + option + " selected, $" + amountWon + " won";
        // send identifier and timestamp
        //bluetooth.timeStamper( resultID, dbTstamp);
        //bluetooth.sendData(String.format ("%.2f",amountWon));

        timeRecordDb.close();

        // Wait for one second during the display of attributes.
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(QuestionActivityHorizontal.this, ResultActivity.class);
                intent.putExtra("EXTRA_AMOUNT_WON", amountWon);
                intent.putExtra("DATABASE_RECORD_STRING", temp);
                intent.putExtra("RESULTID", resultID);
                startActivity(intent);
                finish();
            }
        }, 2000);
    }

    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            finish();
        } else {
            Toast.makeText(this, "Press back again to finish", Toast.LENGTH_SHORT).show();
        }
        backPressedTime = System.currentTimeMillis();
    }

    private void incrementTrialCounter() {  // increment trial counter
        if (trialCounter == trialInfoDb.getNumRows()){
            trialCounter = 1;       // wrap around if reaches the end
        } else {
            trialCounter++;
        }

        counter_prefs.edit().putInt(KEY_TRIAL_COUNTER, trialCounter).apply();
    }
}
