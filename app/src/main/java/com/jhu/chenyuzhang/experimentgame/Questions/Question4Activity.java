package com.jhu.chenyuzhang.experimentgame.Questions;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

public class Question4Activity extends AppCompatActivity {
    private static final String TAG = "bluetooth";

    public static double totalAmountWon;
    public static final String KEY_TOTAL_AMOUNT = "keyTotalAmount";

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

    private double ap1, pp1, am1, pm1;  // option 1: amount plus, prob plus, amount minus, prob minus
    private double ap2, pp2, am2, pm2;
    private double ap3, pp3, am3, pm3;
    private double ap4, pp4, am4, pm4;

    private TimeDbHelper timeRecordDb;

    private TextView textViewDollarP1, textViewProbP1, textViewDollarM1, textViewProbM1;
    private TextView textViewDollarP2, textViewProbP2, textViewDollarM2, textViewProbM2;
    private TextView textViewDollarP3, textViewProbP3, textViewDollarM3, textViewProbM3;
    private TextView textViewDollarP4, textViewProbP4, textViewDollarM4, textViewProbM4;

    private ViewAnimator viewAnimatorDollarP1, viewAnimatorProbP1, viewAnimatorDollarM1, viewAnimatorProbM1;
    private ViewAnimator viewAnimatorDollarP2, viewAnimatorProbP2, viewAnimatorDollarM2, viewAnimatorProbM2;
    private ViewAnimator viewAnimatorDollarP3, viewAnimatorProbP3, viewAnimatorDollarM3, viewAnimatorProbM3;
    private ViewAnimator viewAnimatorDollarP4, viewAnimatorProbP4, viewAnimatorDollarM4, viewAnimatorProbM4;

    private String eventClick = "Clicked, Displayed";
    private String eventTimeOut = "TimeOut, Covered";

    private long backPressedTime;

    Bluetooth bluetooth;

    // identifers maps the id of a attribute view to the code sent when it is uncovered
    // for each attribute, contains two codes before and after the uncover; third code is its alias in the database
    private HashMap<Integer, String[]> identifiers = new HashMap<>();

    // TODO: the code sent when an attribute view is covered after 1s
    private String identifier_cover = "16";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question4);

        demo_prefs = getSharedPreferences("doDemo", MODE_PRIVATE);
        isDemo = demo_prefs.getBoolean(KEY_DO_DEMO, true);   // get shared preference of whether this is a training session

        // TODO: modify the codes
        identifiers.put(R.id.view_animator_dollar1_win, new String[]{"3", "7", "A+1"});
        identifiers.put(R.id.view_animator_probability1_win, new String[]{"4", "8", "P+1"});
        identifiers.put(R.id.view_animator_dollar1_lose, new String[]{"5", "9", "A-1"});
        identifiers.put(R.id.view_animator_probability1_lose, new String[]{"6", "10", "P-1"});
        identifiers.put(R.id.view_animator_dollar2_win, new String[]{"3", "7", "A+2"});
        identifiers.put(R.id.view_animator_probability2_win, new String[]{"4", "8", "P+2"});
        identifiers.put(R.id.view_animator_dollar2_lose, new String[]{"5", "9", "A-2"});
        identifiers.put(R.id.view_animator_probability2_lose, new String[]{"6", "10", "P-2"});
        identifiers.put(R.id.view_animator_dollar3_win, new String[]{"3", "7", "A+1"});
        identifiers.put(R.id.view_animator_probability3_win, new String[]{"4", "8", "P+1"});
        identifiers.put(R.id.view_animator_dollar3_lose, new String[]{"5", "9", "A-1"});
        identifiers.put(R.id.view_animator_probability3_lose, new String[]{"6", "10", "P-1"});
        identifiers.put(R.id.view_animator_dollar4_win, new String[]{"3", "7", "A+2"});
        identifiers.put(R.id.view_animator_probability4_win, new String[]{"4", "8", "P+2"});
        identifiers.put(R.id.view_animator_dollar4_lose, new String[]{"5", "9", "A-2"});
        identifiers.put(R.id.view_animator_probability4_lose, new String[]{"6", "10", "P-2"});

        textViewDollarP1 = findViewById(R.id.text_view_dollar1_win);
        textViewProbP1 = findViewById(R.id.text_view_probability1_win);
        textViewDollarM1 = findViewById(R.id.text_view_dollar1_lose);
        textViewProbM1 = findViewById(R.id.text_view_probability1_lose);
        textViewDollarP2 = findViewById(R.id.text_view_dollar2_win);
        textViewProbP2 = findViewById(R.id.text_view_probability2_win);
        textViewDollarM2 = findViewById(R.id.text_view_dollar2_lose);
        textViewProbM2 = findViewById(R.id.text_view_probability2_lose);
        textViewDollarP3 = findViewById(R.id.text_view_dollar3_win);
        textViewProbP3 = findViewById(R.id.text_view_probability3_win);
        textViewDollarM3 = findViewById(R.id.text_view_dollar3_lose);
        textViewProbM3 = findViewById(R.id.text_view_probability3_lose);
        textViewDollarP4 = findViewById(R.id.text_view_dollar4_win);
        textViewProbP4 = findViewById(R.id.text_view_probability4_win);
        textViewDollarM4 = findViewById(R.id.text_view_dollar4_lose);
        textViewProbM4 = findViewById(R.id.text_view_probability4_lose);

        Button buttonSelect1 = findViewById(R.id.button_select1);
        Button buttonSelect2 = findViewById(R.id.button_select2);
        Button buttonSelect3 = findViewById(R.id.button_select3);
        Button buttonSelect4 = findViewById(R.id.button_select4);

        viewAnimatorDollarP1 = findViewById(R.id.view_animator_dollar1_win);
        viewAnimatorProbP1 = findViewById(R.id.view_animator_probability1_win);
        viewAnimatorDollarM1 = findViewById(R.id.view_animator_dollar1_lose);
        viewAnimatorProbM1 = findViewById(R.id.view_animator_probability1_lose);
        viewAnimatorDollarP2 = findViewById(R.id.view_animator_dollar2_win);
        viewAnimatorProbP2 = findViewById(R.id.view_animator_probability2_win);
        viewAnimatorDollarM2 = findViewById(R.id.view_animator_dollar2_lose);
        viewAnimatorProbM2 = findViewById(R.id.view_animator_probability2_lose);
        viewAnimatorDollarP3 = findViewById(R.id.view_animator_dollar3_win);
        viewAnimatorProbP3 = findViewById(R.id.view_animator_probability3_win);
        viewAnimatorDollarM3 = findViewById(R.id.view_animator_dollar3_lose);
        viewAnimatorProbM3 = findViewById(R.id.view_animator_probability3_lose);
        viewAnimatorDollarP4 = findViewById(R.id.view_animator_dollar4_win);
        viewAnimatorProbP4 = findViewById(R.id.view_animator_probability4_win);
        viewAnimatorDollarM4 = findViewById(R.id.view_animator_dollar4_lose);
        viewAnimatorProbM4 = findViewById(R.id.view_animator_probability4_lose);

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

        //finish activity after 1 minute of inactivity
        countDownTimer = new CountDownTimer(60000, 1000) {
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
            timeRecordDb.insertData(getCurrentTime(), "startTrainingTrial" + trialCounter + "; Option1: A+=" + ap1 + " P+=" + pp1 + " A-=" + am1 + " P-=" + pm1 +
                    "; Option2: A+=" + ap2 + " P+=" + pp2 + " A-=" + am2 + " P-=" + pm2 +
                    "; Option3: A+=" + ap3 + " P+=" + pp3 + " A-=" + am3 + " P-=" + pm3 +
                    "; Option4: A+=" + ap4 + " P+=" + pp4 + " A-=" + am4 + " P-=" + pm4 + "; Orientation: vertical");

        } else {
            incrementTrialCounter();   // increment the counter to indicate the next trial
            timeRecordDb.insertData(getCurrentTime(), "startTrial" + trialCounter + "; Option1: A+=" + ap1 + " P+=" + pp1 + " A-=" + am1 + " P-=" + pm1 +
                    "; Option2: A+=" + ap2 + " P+=" + pp2 + " A-=" + am2 + " P-=" + pm2 +
                    "; Option3: A+=" + ap3 + " P+=" + pp3 + " A-=" + am3 + " P-=" + pm3 +
                    "; Option4: A+=" + ap4 + " P+=" + pp4 + " A-=" + am4 + " P-=" + pm4 + "; Orientation: vertical");
        }

        //bluetooth = new Bluetooth(timeRecordDb);

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

        viewAnimatorDollarP1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimatorDollarP1,
                        new ViewAnimator[]{viewAnimatorProbP1, viewAnimatorDollarM1, viewAnimatorProbM1,
                                viewAnimatorDollarP2, viewAnimatorProbP2, viewAnimatorDollarM2, viewAnimatorProbM2,
                                viewAnimatorDollarP3, viewAnimatorProbP3, viewAnimatorDollarM3, viewAnimatorProbM3,
                                viewAnimatorDollarP4, viewAnimatorProbP4, viewAnimatorDollarM4, viewAnimatorProbM4});
            }
        });

        viewAnimatorProbP1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimatorProbP1,
                        new ViewAnimator[]{viewAnimatorDollarP1, viewAnimatorDollarM1, viewAnimatorProbM1,
                                viewAnimatorDollarP2, viewAnimatorProbP2, viewAnimatorDollarM2, viewAnimatorProbM2,
                                viewAnimatorDollarP3, viewAnimatorProbP3, viewAnimatorDollarM3, viewAnimatorProbM3,
                                viewAnimatorDollarP4, viewAnimatorProbP4, viewAnimatorDollarM4, viewAnimatorProbM4});

            }
        });

        viewAnimatorDollarM1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimatorDollarM1,
                        new ViewAnimator[]{viewAnimatorDollarP1, viewAnimatorProbP1, viewAnimatorProbM1,
                                viewAnimatorDollarP2, viewAnimatorProbP2, viewAnimatorDollarM2, viewAnimatorProbM2,
                                viewAnimatorDollarP3, viewAnimatorProbP3, viewAnimatorDollarM3, viewAnimatorProbM3,
                                viewAnimatorDollarP4, viewAnimatorProbP4, viewAnimatorDollarM4, viewAnimatorProbM4});
            }
        });

        viewAnimatorProbM1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimatorProbM1,
                        new ViewAnimator[]{viewAnimatorDollarP1, viewAnimatorProbP1, viewAnimatorDollarM1,
                                viewAnimatorDollarP2, viewAnimatorProbP2, viewAnimatorDollarM2, viewAnimatorProbM2,
                                viewAnimatorDollarP3, viewAnimatorProbP3, viewAnimatorDollarM3, viewAnimatorProbM3,
                                viewAnimatorDollarP4, viewAnimatorProbP4, viewAnimatorDollarM4, viewAnimatorProbM4});
            }
        });

        viewAnimatorDollarP2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimatorDollarP2,
                        new ViewAnimator[]{viewAnimatorDollarP1, viewAnimatorProbP1, viewAnimatorProbM1, viewAnimatorDollarM1,
                                viewAnimatorProbP2, viewAnimatorDollarM2, viewAnimatorProbM2,
                                viewAnimatorDollarP3, viewAnimatorProbP3, viewAnimatorDollarM3, viewAnimatorProbM3,
                                viewAnimatorDollarP4, viewAnimatorProbP4, viewAnimatorDollarM4, viewAnimatorProbM4});
            }
        });

        viewAnimatorProbP2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimatorProbP2,
                        new ViewAnimator[]{viewAnimatorDollarP1, viewAnimatorProbP1, viewAnimatorProbM1, viewAnimatorDollarM1,
                                viewAnimatorDollarP2, viewAnimatorDollarM2, viewAnimatorProbM2,
                                viewAnimatorDollarP3, viewAnimatorProbP3, viewAnimatorDollarM3, viewAnimatorProbM3,
                                viewAnimatorDollarP4, viewAnimatorProbP4, viewAnimatorDollarM4, viewAnimatorProbM4});

            }
        });

        viewAnimatorDollarM2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimatorDollarM2,
                        new ViewAnimator[]{viewAnimatorDollarP1, viewAnimatorProbP1, viewAnimatorProbM1, viewAnimatorDollarM1,
                                viewAnimatorDollarP2, viewAnimatorProbP2, viewAnimatorProbM2,
                                viewAnimatorDollarP3, viewAnimatorProbP3, viewAnimatorDollarM3, viewAnimatorProbM3,
                                viewAnimatorDollarP4, viewAnimatorProbP4, viewAnimatorDollarM4, viewAnimatorProbM4});
            }
        });

        viewAnimatorProbM2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimatorProbM2,
                        new ViewAnimator[]{viewAnimatorDollarP1, viewAnimatorProbP1, viewAnimatorProbM1, viewAnimatorDollarM1,
                                viewAnimatorDollarP2, viewAnimatorProbP2, viewAnimatorDollarM2,
                                viewAnimatorDollarP3, viewAnimatorProbP3, viewAnimatorDollarM3, viewAnimatorProbM3,
                                viewAnimatorDollarP4, viewAnimatorProbP4, viewAnimatorDollarM4, viewAnimatorProbM4});
            }
        });

        viewAnimatorDollarP3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimatorDollarP3,
                        new ViewAnimator[]{viewAnimatorDollarP1, viewAnimatorProbP1, viewAnimatorProbM1, viewAnimatorDollarM1,
                                viewAnimatorDollarP2, viewAnimatorProbP2, viewAnimatorDollarM2, viewAnimatorProbM2,
                                viewAnimatorProbP3, viewAnimatorDollarM3, viewAnimatorProbM3,
                                viewAnimatorDollarP4, viewAnimatorProbP4, viewAnimatorDollarM4, viewAnimatorProbM4});
            }
        });

        viewAnimatorProbP3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimatorProbP3,
                        new ViewAnimator[]{viewAnimatorDollarP1, viewAnimatorProbP1, viewAnimatorProbM1, viewAnimatorDollarM1,
                                viewAnimatorDollarP2, viewAnimatorProbP2, viewAnimatorDollarM2, viewAnimatorProbM2,
                                viewAnimatorDollarP3, viewAnimatorDollarM3, viewAnimatorProbM3,
                                viewAnimatorDollarP4, viewAnimatorProbP4, viewAnimatorDollarM4, viewAnimatorProbM4});
            }
        });

        viewAnimatorDollarM3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimatorDollarM3,
                        new ViewAnimator[]{viewAnimatorDollarP1, viewAnimatorProbP1, viewAnimatorProbM1, viewAnimatorDollarM1,
                                viewAnimatorDollarP2, viewAnimatorProbP2, viewAnimatorDollarM2, viewAnimatorProbM2,
                                viewAnimatorDollarP3, viewAnimatorProbP3, viewAnimatorProbM3,
                                viewAnimatorDollarP4, viewAnimatorProbP4, viewAnimatorDollarM4, viewAnimatorProbM4});
            }
        });

        viewAnimatorProbM3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimatorProbM3,
                        new ViewAnimator[]{viewAnimatorDollarP1, viewAnimatorProbP1, viewAnimatorProbM1, viewAnimatorDollarM1,
                                viewAnimatorDollarP2, viewAnimatorProbP2, viewAnimatorDollarM2, viewAnimatorProbM2,
                                viewAnimatorDollarP3, viewAnimatorProbP3, viewAnimatorDollarM3,
                                viewAnimatorDollarP4, viewAnimatorProbP4, viewAnimatorDollarM4, viewAnimatorProbM4});
            }
        });

        viewAnimatorDollarP4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimatorDollarP4,
                        new ViewAnimator[]{viewAnimatorDollarP1, viewAnimatorProbP1, viewAnimatorProbM1, viewAnimatorDollarM1,
                                viewAnimatorDollarP2, viewAnimatorProbP2, viewAnimatorDollarM2, viewAnimatorProbM2,
                                viewAnimatorDollarP3, viewAnimatorProbP3, viewAnimatorDollarM3, viewAnimatorProbM3,
                                viewAnimatorProbP4, viewAnimatorDollarM4, viewAnimatorProbM4});
            }
        });

        viewAnimatorProbP4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimatorProbP4,
                        new ViewAnimator[]{viewAnimatorDollarP1, viewAnimatorProbP1, viewAnimatorProbM1, viewAnimatorDollarM1,
                                viewAnimatorDollarP2, viewAnimatorProbP2, viewAnimatorDollarM2, viewAnimatorProbM2,
                                viewAnimatorDollarP3, viewAnimatorProbP3, viewAnimatorDollarM3, viewAnimatorProbM3,
                                viewAnimatorDollarP4, viewAnimatorDollarM4, viewAnimatorProbM4});
            }
        });

        viewAnimatorDollarM4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimatorDollarM4,
                        new ViewAnimator[]{viewAnimatorDollarP1, viewAnimatorProbP1, viewAnimatorProbM1, viewAnimatorDollarM1,
                                viewAnimatorDollarP2, viewAnimatorProbP2, viewAnimatorDollarM2, viewAnimatorProbM2,
                                viewAnimatorDollarP3, viewAnimatorProbP3, viewAnimatorDollarM3, viewAnimatorProbM3,
                                viewAnimatorDollarP4, viewAnimatorProbP4, viewAnimatorProbM4});
            }
        });

        viewAnimatorProbM4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimatorProbM4,
                        new ViewAnimator[]{viewAnimatorDollarP1, viewAnimatorProbP1, viewAnimatorProbM1, viewAnimatorDollarM1,
                                viewAnimatorDollarP2, viewAnimatorProbP2, viewAnimatorDollarM2, viewAnimatorProbM2,
                                viewAnimatorDollarP3, viewAnimatorProbP3, viewAnimatorDollarM3, viewAnimatorProbM3,
                                viewAnimatorDollarP4, viewAnimatorProbP4, viewAnimatorDollarM4});
            }
        });

        // TODO: modify the codes
        buttonSelect1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View V) {
                /*
                try {
                    // send identifier and timestamp
                    bluetooth.timeStamper( "12", getCurrentTime());
                } catch (IOException e) {}
                */
                showResult(pp1, ap1, pm1, am1, "Option1");
            }
        });

        buttonSelect2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View V) {
                /*
                try {
                    // send identifier and timestamp
                    bluetooth.timeStamper( "13", getCurrentTime());
                } catch (IOException e) {}
                */
                showResult(pp2, ap2, pm2, am2, "Option2");
            }
        });

        buttonSelect3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View V) {
                /*
                try {
                    // send identifier and timestamp
                    bluetooth.timeStamper( "12", getCurrentTime());
                } catch (IOException e) {}
                */
                showResult(pp3, ap3, pm3, am3, "Option3");
            }
        });

        buttonSelect4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View V) {
                /*
                try {
                    // send identifier and timestamp
                    bluetooth.timeStamper( "13", getCurrentTime());
                } catch (IOException e) {}
                */
                showResult(pp4, ap4, pm4, am4, "Option4");
            }
        });
    }

    // called when each attribute is clicked
    private void attributeOnClick(final ViewAnimator tappedView, ViewAnimator[] otherViews) {
        /* on tap, if the attribute view is covered, uncover it for 1s and cover other attributes */
        if (tappedView.getDisplayedChild() == 0) {
            final String[] codes = identifiers.get(tappedView.getId()); // get the corresponding identifiers for the clicked attribute

            /*
            try {
                // send identifier and timestamp
                bluetooth.timeStamper( codes[0], getCurrentTime());
            } catch (IOException e) {}
            */
            //armVSyncHandlerA1();

            tappedView.showNext();  /* uncover */
            /*
            try {
                bluetooth.timeStamper( codes[1], getCurrentTime());
            } catch (IOException e) {}
            */
            recordEvent(codes[2] + " " + eventClick);

            /* automatically re-cover after 1000ms */
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (tappedView.getDisplayedChild() == 1) {
                        /*
                        try {
                            bluetooth.timeStamper( identifier_cover, getCurrentTime());
                        } catch (IOException e) {}
                        */

                        tappedView.showNext();
                        recordEvent(codes[2] + " " + eventTimeOut);
                    }
                }
            }, 1000);

            /* if other attributes are uncovered, cover them */
            for (ViewAnimator v: otherViews) {
                if (v.getDisplayedChild() == 1) {
                    /*
                    try {
                        bluetooth.timeStamper( identifier_cover, getCurrentTime());
                    } catch (IOException e) {}
                    */
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

        Intent intent = new Intent(Question4Activity.this, EndDemoActivity.class);
        startActivity(intent);
        finish();
    }

    private void setupTrial() {
        // load trialCounter from shared preference
        counter_prefs = getSharedPreferences("trialCounter", MODE_PRIVATE);
        trialCounter = counter_prefs.getInt(KEY_TRIAL_COUNTER, 1);

        Log.d("Q-4 Test", Integer.toString(trialCounter));

        // get current trial
        currentTrial = trialInfoDb.getTrial(trialCounter);
        getAttributes();
    }

    private void getAttributes(){
        ArrayList<String> attributes = currentTrial.getAttributes();
        ap1 = Double.parseDouble(attributes.get(2));
        pp1 = Double.parseDouble(attributes.get(3))*100;
        am1 = Double.parseDouble(attributes.get(4));
        pm1 = Double.parseDouble(attributes.get(5))*100;
        ap2 = Double.parseDouble(attributes.get(6));
        pp2 = Double.parseDouble(attributes.get(7))*100;
        am2 = Double.parseDouble(attributes.get(8));
        pm2 = Double.parseDouble(attributes.get(9))*100;
        ap3 = Double.parseDouble(attributes.get(10));
        pp3 = Double.parseDouble(attributes.get(11))*100;
        am3 = Double.parseDouble(attributes.get(12));
        pm3 = Double.parseDouble(attributes.get(13))*100;
        ap4 = Double.parseDouble(attributes.get(14));
        pp4 = Double.parseDouble(attributes.get(15))*100;
        am4 = Double.parseDouble(attributes.get(16));
        pm4 = Double.parseDouble(attributes.get(17))*100;

        textViewDollarP1.setText("$" + String.format("%.2f", ap1));
        textViewProbP1.setText((int) pp1 + "%");
        textViewDollarM1.setText("$" + String.format("%.2f", am1));
        textViewProbM1.setText((int) pm1 + "%");
        textViewDollarP2.setText("$" + String.format("%.2f", ap2));
        textViewProbP2.setText((int) pp2 + "%");
        textViewDollarM2.setText("$" + String.format("%.2f", am2));
        textViewProbM2.setText((int) pm2 + "%");
        textViewDollarP3.setText("$" + String.format("%.2f", ap3));
        textViewProbP3.setText((int) pp3 + "%");
        textViewDollarM3.setText("$" + String.format("%.2f", am3));
        textViewProbM3.setText((int) pm3 + "%");
        textViewDollarP4.setText("$" + String.format("%.2f", ap4));
        textViewProbP4.setText((int) pp4 + "%");
        textViewDollarM4.setText("$" + String.format("%.2f", am4));
        textViewProbM4.setText((int) pm4 + "%");
    }

    private void incrementTrialCounter() {
        if (trialCounter == trialInfoDb.getNumRows()){  // increment trial counter
            trialCounter = 1;       // wrap around if reaches the end
        } else {
            trialCounter++;
        }

        counter_prefs.edit().putInt(KEY_TRIAL_COUNTER, trialCounter).apply();
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

    private void showResult(double pp, double ap, double pm, double am, String option){
        double random = new Random().nextDouble()*100;
        if (random < pp) {  // if random less than probability win, then win
            amountWon = ap;
        } else if (random < pp + pm) {   // if random is between prob win and prob win + prob lose, then lose
            amountWon = am;
        } else { amountWon = 0; }

        SharedPreferences prefs = getSharedPreferences("totalAmountWon", MODE_PRIVATE);
        totalAmountWon = prefs.getFloat(KEY_TOTAL_AMOUNT, 0);
        totalAmountWon = totalAmountWon + amountWon;
        prefs.edit().putFloat(KEY_TOTAL_AMOUNT, (float)totalAmountWon).apply();

        recordEvent(option+" selected, $"+amountWon+" won; total amount won: $"+totalAmountWon);

        timeRecordDb.close();

        Intent intent = new Intent(Question4Activity.this, ResultActivity.class);
        intent.putExtra("EXTRA_AMOUNT_WON", amountWon);
        startActivity(intent);
        finish();
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
