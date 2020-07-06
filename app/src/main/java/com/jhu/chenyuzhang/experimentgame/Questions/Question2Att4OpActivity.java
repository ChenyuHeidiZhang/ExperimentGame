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
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.io.IOException;

public class Question2Att4OpActivity extends AppCompatActivity {
    private boolean isDemo;
    private static final String KEY_DO_DEMO = "keyDoDemo";
    private SharedPreferences demo_prefs;

    private CountDownTimer countDownTimer;

    private TrialDbHelper trialInfoDb;
    private Trial currentTrial;
    private static int trialCounter;
    private SharedPreferences counter_prefs;
    public static final String KEY_TRIAL_COUNTER = "keyTrialCounter";

    private double amountWon;

    private double a1, a2, a3, a4;
    private double p1, p2, p3, p4;

    private TimeDbHelper timeRecordDb;

    private ViewAnimator viewAnimator11, viewAnimator12;
    private ViewAnimator viewAnimator21, viewAnimator22;
    private ViewAnimator viewAnimator31, viewAnimator32;
    private ViewAnimator viewAnimator41, viewAnimator42;
    private Button buttonSelect1, buttonSelect2, buttonSelect3, buttonSelect4;

    private String eventClick = "Clicked, Displayed";
    private String eventTimeOut = "TimeOut, Covered";

    private long backPressedTime;
    private long startTime;

    // A map from viewAnimator ID to their corresponding handlers.
    private HashMap<Integer, Handler> viewHandlerMap = new HashMap<>();

    Bluetooth bluetooth;

    // identifiers maps the id of a attribute view to the code sent when it is uncovered
    // for each attribute, contains two codes before and after the uncover; third code is its alias in the database
    private HashMap<Integer, String[]> identifiers = new HashMap<>();

    // TODO: the code sent when an attribute view is covered after 1s
    private String identifier_cover = "34";
    private String identifier_coverEarly = "35";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // hide the status bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_question_2att_4op);

        demo_prefs = getSharedPreferences("doDemo", MODE_PRIVATE);
        isDemo = demo_prefs.getBoolean(KEY_DO_DEMO, true);   // get shared preference of whether this is a training session

        // TODO: modify the codes
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

        if(a3>0) {
            identifiers.put(R.id.view_animator_11, new String[] {"2", "18", "A+3"});
        }else{
            identifiers.put(R.id.view_animator_11, new String[] {"6", "22", "A-3"});
        }
        if(p3>0) {
            identifiers.put(R.id.view_animator_12, new String[] {"3", "19", "P+3"});
        }else{
            identifiers.put(R.id.view_animator_12, new String[] {"7", "23", "P-3"});
        }

        if(a4>0) {
            identifiers.put(R.id.view_animator_21, new String[] {"4", "20", "A+4"});
        }else{
            identifiers.put(R.id.view_animator_21, new String[] {"8", "24", "A-4"});
        }
        if(p4>0) {
            identifiers.put(R.id.view_animator_22, new String[] {"5", "21", "P+4"});
        }else{
            identifiers.put(R.id.view_animator_22, new String[] {"9", "25", "P-4"});
        }


        buttonSelect1 = findViewById(R.id.button_select1);
        buttonSelect2 = findViewById(R.id.button_select2);
        buttonSelect3 = findViewById(R.id.button_select3);
        buttonSelect4 = findViewById(R.id.button_select4);

        viewAnimator11 = findViewById(R.id.view_animator_11);
        viewAnimator21 = findViewById(R.id.view_animator_21);
        viewAnimator12 = findViewById(R.id.view_animator_12);
        viewAnimator22 = findViewById(R.id.view_animator_22);
        viewAnimator31 = findViewById(R.id.view_animator_31);
        viewAnimator41 = findViewById(R.id.view_animator_41);
        viewAnimator32 = findViewById(R.id.view_animator_32);
        viewAnimator42 = findViewById(R.id.view_animator_42);

        // if is in training part, display "training" and the "end training" button
        if (isDemo) {
            Button buttonEndDemo = findViewById(R.id.button_end_demo);
            TextView tvDemo = findViewById(R.id.text_view_demo);
            buttonEndDemo.setVisibility(View.VISIBLE);
            tvDemo.setVisibility(View.VISIBLE);

            buttonEndDemo.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    recordEvent("Training ended");
                    // end the training; go to EndDemoActivity
                    endDemo();
                }
            });
        }

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

        setupTrial();

        if (isDemo) {
            timeRecordDb.insertData(getCurrentTime(), "startTrainingTrial " + trialCounter);
        } else {
            timeRecordDb.insertData(getCurrentTime(), "startTrial " + trialCounter);
        }

        bluetooth = new Bluetooth(timeRecordDb);
        try {
            // send trial number
            bluetooth.timeStamper(Integer.toString(trialCounter +100),getCurrentTime());
        } catch (IOException e) {
            e.printStackTrace();
        }
        // TODO: setup this for all layouts
        /*
        try {
            // send trial number
            bluetooth.timeStamper(Integer.toString(trialCounter +200),getCurrentTime());
            // send attribute magnitudes
            bluetooth.timeStamper(Integer.toString(16),String.format ("%.0f",a1*100));
            bluetooth.timeStamper(Integer.toString(18),String.format ("%.0f",p1));
            bluetooth.timeStamper(Integer.toString(17),String.format ("%.0f",a2*100));
            bluetooth.timeStamper(Integer.toString(19),String.format ("%.0f",p2));

        } catch (IOException e) {
            e.printStackTrace();
        }
        */

        viewAnimator11.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimator11,
                        new ViewAnimator[] {viewAnimator21, viewAnimator31, viewAnimator41,
                                viewAnimator12, viewAnimator22, viewAnimator32, viewAnimator42});
            }
        });

        viewAnimator21.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimator21,
                        new ViewAnimator[] {viewAnimator11, viewAnimator31, viewAnimator41,
                                viewAnimator12, viewAnimator22, viewAnimator32, viewAnimator42});

            }
        });

        viewAnimator12.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimator12,
                        new ViewAnimator[] {viewAnimator11, viewAnimator21, viewAnimator31, viewAnimator41,
                                viewAnimator22, viewAnimator32, viewAnimator42});
            }
        });

        viewAnimator22.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimator22,
                        new ViewAnimator[] {viewAnimator11, viewAnimator21, viewAnimator31, viewAnimator41,
                                viewAnimator12, viewAnimator32, viewAnimator42});
            }
        });

        viewAnimator31.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimator31,
                        new ViewAnimator[] {viewAnimator11, viewAnimator21, viewAnimator41,
                                viewAnimator12, viewAnimator22, viewAnimator32, viewAnimator42});
            }
        });

        viewAnimator41.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimator41,
                        new ViewAnimator[] {viewAnimator11, viewAnimator21, viewAnimator31,
                                viewAnimator12, viewAnimator22, viewAnimator32, viewAnimator42});

            }
        });

        viewAnimator32.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimator32,
                        new ViewAnimator[] {viewAnimator11, viewAnimator21, viewAnimator31, viewAnimator41,
                                viewAnimator12, viewAnimator22, viewAnimator42});
            }
        });

        viewAnimator42.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimator42,
                        new ViewAnimator[] {viewAnimator11, viewAnimator21, viewAnimator31, viewAnimator41,
                                viewAnimator12, viewAnimator22, viewAnimator32});
            }
        });

        // TODO: modify the codes
        buttonSelect1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View V) {
                try {
                    // send identifier and timestamp
                    bluetooth.timeStamper( "35", getCurrentTime());
                } catch (IOException e) {}

                if (checkMinimumTimePassed()) {
                    unmaskAttributes(new ViewAnimator[]{viewAnimator11, viewAnimator12});
                    showResult(a1, 1);
                }
            }
        });

        buttonSelect2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View V) {
                try {
                    // send identifier and timestamp
                    bluetooth.timeStamper( "35", getCurrentTime());
                } catch (IOException e) {}

                if (checkMinimumTimePassed()) {
                    unmaskAttributes(new ViewAnimator[]{viewAnimator21, viewAnimator22});
                    showResult(a2, 2);
                }
            }
        });

        buttonSelect3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View V) {
                try {
                    // send identifier and timestamp
                    bluetooth.timeStamper( "35", getCurrentTime());
                } catch (IOException e) {}

                if (checkMinimumTimePassed()) {
                    unmaskAttributes(new ViewAnimator[]{viewAnimator31, viewAnimator32});
                    showResult(a3, 3);
                }
            }
        });

        buttonSelect4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View V) {
                try {
                    // send identifier and timestamp
                    bluetooth.timeStamper( "35", getCurrentTime());
                } catch (IOException e) {}

                if (checkMinimumTimePassed()) {
                    unmaskAttributes(new ViewAnimator[]{viewAnimator41, viewAnimator42});
                    showResult(a4, 4);
                }
            }
        });
    }

    // called when each attribute is clicked
    private void attributeOnClick(final ViewAnimator tappedView, ViewAnimator[] otherViews) {
        /* on tap, if the attribute view is covered, uncover it for 1s and cover other attributes */
        if (tappedView.getDisplayedChild() == 0) {
            final String[] codes = identifiers.get(tappedView.getId()); // get the corresponding identifiers for the clicked attribute


            try {
                // send identifier and timestamp
                bluetooth.timeStamperJustID( codes[0]);
            } catch (IOException e) {}

            //armVSyncHandlerA1();

            tappedView.showNext();  /* uncover */

            try {
                bluetooth.timeStamper( codes[1], getCurrentTime());
            } catch (IOException e) {}

            recordEvent(codes[2] + " " + eventClick);

            /* automatically re-cover after 1000ms */
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (tappedView.getDisplayedChild() == 1) {

                        try {
                            bluetooth.timeStamper( identifier_cover, getCurrentTime());
                        } catch (IOException e) {}


                        tappedView.showNext();
                        recordEvent(codes[2] + " " + eventTimeOut);
                    }
                }
            }, 1000);
            viewHandlerMap.put(tappedView.getId(), handler);

            /* if other attributes are uncovered, cover them */
            for (ViewAnimator v: otherViews) {
                if (v.getDisplayedChild() == 1) {

                    try {
                        bluetooth.timeStamper( identifier_coverEarly, getCurrentTime());
                    } catch (IOException e) {}

                    v.showNext();
                }
            }
        }

        countDownTimer.cancel();
        countDownTimer.start();
    }

    private void endDemo(){
        demo_prefs.edit().putBoolean(KEY_DO_DEMO, false).apply();    // change shared "prefs" for do_demo to false

        // set trialCounter back to 1
        counter_prefs.edit().putInt(KEY_TRIAL_COUNTER, 1).apply();

        Intent intent = new Intent(Question2Att4OpActivity.this, EndDemoActivity.class);
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
        ArrayList<String> attributes = currentTrial.getAttributes();

        setAttributesForOneVA(viewAnimator11, attributes.get(0), attributes.get(1));
        setAttributesForOneVA(viewAnimator12, attributes.get(2), attributes.get(3));
        setAttributesForOneVA(viewAnimator21, attributes.get(4), attributes.get(5));
        setAttributesForOneVA(viewAnimator22, attributes.get(6), attributes.get(7));
        setAttributesForOneVA(viewAnimator31, attributes.get(8), attributes.get(9));
        setAttributesForOneVA(viewAnimator32, attributes.get(10), attributes.get(11));
        setAttributesForOneVA(viewAnimator41, attributes.get(12), attributes.get(13));
        setAttributesForOneVA(viewAnimator42, attributes.get(14), attributes.get(15));
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
        } else if (attType.equals("A+3") || attType.equals("A-3")) {
            a3 = Double.parseDouble(att);
            tv.setText("$" + String.format("%.2f", Math.abs(a3)));
        } else if (attType.equals("P+3") || attType.equals("P-3")) {
            p3 = Double.parseDouble(att);
            tv.setText((int) (p3 * 100) + "%");
        } else if (attType.equals("A+4") || attType.equals("A-4")) {
            a4 = Double.parseDouble(att);
            tv.setText("$" + String.format("%.2f", Math.abs(a4)));
        } else if (attType.equals("P+4") || attType.equals("P-4")) {
            p4 = Double.parseDouble(att);
            tv.setText((int) (p4 * 100) + "%");
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

    private void recordEvent(String event) {
        //long timeSpan = System.nanoTime() - startTime;
        //String timeString = String.format("%d", timeSpan / 1000);
        String timeString = getCurrentTime();

        timeRecordDb.insertData(timeString, event);
    }

    private boolean checkMinimumTimePassed() {
        if (System.currentTimeMillis() - startTime <
                getResources().getInteger(R.integer.min_time_millis_2Att4Opt)) {
            Toast.makeText(this, getString(R.string.stay_longer), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void unmaskAttributes(ViewAnimator[] viewAnimators) {
        for (ViewAnimator v : viewAnimators) {
            v.setDisplayedChild(1);
            // Disable the handler (if one exists for the current view) that sets a 1s cover time.
            Handler handler = viewHandlerMap.get(v.getId());
            if (handler != null) {
                handler.removeCallbacksAndMessages(null);
            }
        }

        buttonSelect1.setEnabled(false);
        buttonSelect2.setEnabled(false);
        buttonSelect3.setEnabled(false);
        buttonSelect4.setEnabled(false);
    }

    private void showResult(double a, int option){
        String outcomes[] = currentTrial.getOutcomes();
        String outcome = outcomes[option-1];
        if ("win".equals(outcome) || "lose".equals(outcome)) {
            amountWon = a;  // This can be either positive or negative.
        } else {  // "no outcome".equals(outcome)
            amountWon = 0;
        }

        recordEvent("Option" + option + " selected, $" + amountWon + " won");
        timeRecordDb.close();

        // Wait for one second during the display of attributes.
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Question2Att4OpActivity.this, ResultActivity.class);
                intent.putExtra("EXTRA_AMOUNT_WON", amountWon);
                startActivity(intent);
                finish();
            }
        }, 1000);
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
}
