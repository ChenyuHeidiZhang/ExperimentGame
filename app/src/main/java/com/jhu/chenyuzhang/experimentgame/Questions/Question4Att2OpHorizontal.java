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

public class Question4Att2OpHorizontal extends AppCompatActivity {
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

    private double ap1;     // amount plus, option 1
    private double pp1;
    private double am1;     // amount minus, option 1
    private double pm1;
    private double ap2;
    private double pp2;
    private double am2;
    private double pm2;

    private TimeDbHelper timeRecordDb;

    private TextView textViewDollarP1;
    private TextView textViewProbP1;
    private TextView textViewDollarM1;
    private TextView textViewProbM1;
    private TextView textViewDollarP2;
    private TextView textViewProbP2;
    private TextView textViewDollarM2;
    private TextView textViewProbM2;

    private ViewAnimator viewAnimatorDollarP1;
    private ViewAnimator viewAnimatorProbP1;
    private ViewAnimator viewAnimatorDollarM1;
    private ViewAnimator viewAnimatorProbM1;
    private ViewAnimator viewAnimatorDollarP2;
    private ViewAnimator viewAnimatorProbP2;
    private ViewAnimator viewAnimatorDollarM2;
    private ViewAnimator viewAnimatorProbM2;

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
        setContentView(R.layout.activity_question_4att_2op_horizontal);

        demo_prefs = getSharedPreferences("doDemo", MODE_PRIVATE);
        isDemo = demo_prefs.getBoolean(KEY_DO_DEMO, true);   // get shared preference of whether this is a training session

        // TODO: modify the codes
        identifiers.put(R.id.view_animator_dollar1_win, new String[] {"3", "7", "A+1"});
        identifiers.put(R.id.view_animator_probability1_win, new String[] {"4", "8", "P+1"});
        identifiers.put(R.id.view_animator_dollar1_lose, new String[] {"5", "9", "A-1"});
        identifiers.put(R.id.view_animator_probability1_lose, new String[] {"6", "10", "P-1"});
        identifiers.put(R.id.view_animator_dollar2_win, new String[] {"3", "7", "A+2"});
        identifiers.put(R.id.view_animator_probability2_win, new String[] {"4", "8", "P+2"});
        identifiers.put(R.id.view_animator_dollar2_lose, new String[] {"5", "9", "A-2"});
        identifiers.put(R.id.view_animator_probability2_lose, new String[] {"6", "10", "P-2"});

        textViewDollarP1 = findViewById(R.id.text_view_dollar1_win);
        textViewProbP1 = findViewById(R.id.text_view_probability1_win);
        textViewDollarM1 = findViewById(R.id.text_view_dollar1_lose);
        textViewProbM1 = findViewById(R.id.text_view_probability1_lose);
        textViewDollarP2 = findViewById(R.id.text_view_dollar2_win);
        textViewProbP2 = findViewById(R.id.text_view_probability2_win);
        textViewDollarM2 = findViewById(R.id.text_view_dollar2_lose);
        textViewProbM2 = findViewById(R.id.text_view_probability2_lose);

        Button buttonSelect1 = findViewById(R.id.button_select1);
        Button buttonSelect2 = findViewById(R.id.button_select2);

        viewAnimatorDollarP1 = findViewById(R.id.view_animator_dollar1_win);
        viewAnimatorProbP1 = findViewById(R.id.view_animator_probability1_win);
        viewAnimatorDollarM1 = findViewById(R.id.view_animator_dollar1_lose);
        viewAnimatorProbM1 = findViewById(R.id.view_animator_probability1_lose);
        viewAnimatorDollarP2 = findViewById(R.id.view_animator_dollar2_win);
        viewAnimatorProbP2 = findViewById(R.id.view_animator_probability2_win);
        viewAnimatorDollarM2 = findViewById(R.id.view_animator_dollar2_lose);
        viewAnimatorProbM2 = findViewById(R.id.view_animator_probability2_lose);

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
        countDownTimer = new CountDownTimer(60000,1000) {
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
                    "; Option2: A+=" + ap2 + " P+=" + pp2 + " A-=" + am2 + " P-=" + pm2 + "; Orientation: horizontal");

        } else {
            incrementTrialCounter();   // increment the counter to indicate the next trial
            timeRecordDb.insertData(getCurrentTime(), "startTrial" + trialCounter + "; Option1: A+=" + ap1 + " P+=" + pp1 + " A-=" + am1 + " P-=" + pm1 +
                    "; Option2: A+=" + ap2 + " P+=" + pp2 + " A-=" + am2 + " P-=" + pm2 + "; Orientation: horizontal");
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
                        new ViewAnimator[] {viewAnimatorDollarP2, viewAnimatorDollarM1, viewAnimatorDollarM2,
                                viewAnimatorProbP1, viewAnimatorProbP2, viewAnimatorProbM1, viewAnimatorProbM2});
            }
        });

        viewAnimatorProbP1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimatorProbP1,
                        new ViewAnimator[] {viewAnimatorDollarP1, viewAnimatorDollarP2, viewAnimatorDollarM1, viewAnimatorDollarM2,
                                viewAnimatorProbP2, viewAnimatorProbM1, viewAnimatorProbM2});

            }
        });

        viewAnimatorDollarM1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimatorDollarM1,
                        new ViewAnimator[] {viewAnimatorDollarP1, viewAnimatorDollarP2, viewAnimatorDollarM2,
                                viewAnimatorProbP1, viewAnimatorProbP2, viewAnimatorProbM1, viewAnimatorProbM2});
            }
        });

        viewAnimatorProbM1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimatorProbM1,
                        new ViewAnimator[] {viewAnimatorDollarP1, viewAnimatorDollarP2, viewAnimatorDollarM1, viewAnimatorDollarM2,
                                viewAnimatorProbP1, viewAnimatorProbP2, viewAnimatorProbM2});
            }
        });

        viewAnimatorDollarP2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimatorDollarP2,
                        new ViewAnimator[] {viewAnimatorDollarP1, viewAnimatorDollarM1, viewAnimatorDollarM2,
                                viewAnimatorProbP1, viewAnimatorProbP2, viewAnimatorProbM1, viewAnimatorProbM2});
            }
        });

        viewAnimatorProbP2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimatorProbP2,
                        new ViewAnimator[] {viewAnimatorDollarP1, viewAnimatorDollarP2, viewAnimatorDollarM1, viewAnimatorDollarM2,
                                viewAnimatorProbP1, viewAnimatorProbM1, viewAnimatorProbM2});

            }
        });

        viewAnimatorDollarM2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimatorDollarM2,
                        new ViewAnimator[] {viewAnimatorDollarP1, viewAnimatorDollarP2, viewAnimatorDollarM1,
                                viewAnimatorProbP1, viewAnimatorProbP2, viewAnimatorProbM1, viewAnimatorProbM2});
            }
        });

        viewAnimatorProbM2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimatorProbM2,
                        new ViewAnimator[] {viewAnimatorDollarP1, viewAnimatorDollarP2, viewAnimatorDollarM1, viewAnimatorDollarM2,
                                viewAnimatorProbP1, viewAnimatorProbP2, viewAnimatorProbM1});
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

        Intent intent = new Intent(Question4Att2OpHorizontal.this, EndDemoActivity.class);
        startActivity(intent);
        finish();
    }

    private void setupTrial() {
        // load trialCounter from shared preference
        counter_prefs = getSharedPreferences("trialCounter", MODE_PRIVATE);
        trialCounter = counter_prefs.getInt(KEY_TRIAL_COUNTER, 1);

        Log.d("QH-2 Test", Integer.toString(trialCounter));

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

        textViewDollarP1.setText("$" + String.format("%.2f", ap1));
        textViewProbP1.setText((int) pp1 + "%");
        textViewDollarM1.setText("$" + String.format("%.2f", am1));
        textViewProbM1.setText((int) pm1 + "%");
        textViewDollarP2.setText("$" + String.format("%.2f", ap2));
        textViewProbP2.setText((int) pp2 + "%");
        textViewDollarM2.setText("$" + String.format("%.2f", am2));
        textViewProbM2.setText((int) pm2 + "%");
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

        Intent intent = new Intent(Question4Att2OpHorizontal.this, ResultActivity.class);
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
