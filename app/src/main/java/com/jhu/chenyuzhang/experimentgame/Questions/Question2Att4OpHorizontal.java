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

public class Question2Att4OpHorizontal extends AppCompatActivity {
    private static final String TAG = "bluetooth";

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

    private double a1;
    private double p1;
    private double a2;
    private double p2;
    private double a3;
    private double p3;
    private double a4;
    private double p4;

    private TimeDbHelper timeRecordDb;

    private TextView textViewDollar1;
    private TextView textViewProb1;
    private TextView textViewDollar2;
    private TextView textViewProb2;
    private TextView textViewDollar3;
    private TextView textViewProb3;
    private TextView textViewDollar4;
    private TextView textViewProb4;

    private ViewAnimator viewAnimatorDollar1;
    private ViewAnimator viewAnimatorDollar2;
    private ViewAnimator viewAnimatorProb1;
    private ViewAnimator viewAnimatorProb2;
    private ViewAnimator viewAnimatorDollar3;
    private ViewAnimator viewAnimatorDollar4;
    private ViewAnimator viewAnimatorProb3;
    private ViewAnimator viewAnimatorProb4;

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
        setContentView(R.layout.activity_question_2att_4op_horizontal);

        demo_prefs = getSharedPreferences("doDemo", MODE_PRIVATE);
        isDemo = demo_prefs.getBoolean(KEY_DO_DEMO, true);   // get shared preference of whether this is a training session

        // TODO: modify the codes
        identifiers.put(R.id.view_animator_dollar1, new String[] {"3", "7", "A1"});
        identifiers.put(R.id.view_animator_dollar2, new String[] {"5", "9", "A2"});
        identifiers.put(R.id.view_animator_probability1, new String[] {"4", "8", "P1"});
        identifiers.put(R.id.view_animator_probability2, new String[] {"6", "10", "P2"});
        identifiers.put(R.id.view_animator_dollar3, new String[] {"3", "7", "A3"});
        identifiers.put(R.id.view_animator_dollar4, new String[] {"5", "9", "A4"});
        identifiers.put(R.id.view_animator_probability3, new String[] {"4", "8", "P3"});
        identifiers.put(R.id.view_animator_probability4, new String[] {"6", "10", "P4"});

        textViewDollar1 = findViewById(R.id.text_view_dollar1);
        textViewDollar2 = findViewById(R.id.text_view_dollar2);
        textViewProb1 = findViewById(R.id.text_view_probability1);
        textViewProb2 = findViewById(R.id.text_view_probability2);
        textViewDollar3 = findViewById(R.id.text_view_dollar3);
        textViewDollar4 = findViewById(R.id.text_view_dollar4);
        textViewProb3 = findViewById(R.id.text_view_probability3);
        textViewProb4 = findViewById(R.id.text_view_probability4);

        Button buttonSelect1 = findViewById(R.id.button_select1);
        Button buttonSelect2 = findViewById(R.id.button_select2);
        Button buttonSelect3 = findViewById(R.id.button_select3);
        Button buttonSelect4 = findViewById(R.id.button_select4);

        viewAnimatorDollar1 = findViewById(R.id.view_animator_dollar1);
        viewAnimatorDollar2 = findViewById(R.id.view_animator_dollar2);
        viewAnimatorProb1 = findViewById(R.id.view_animator_probability1);
        viewAnimatorProb2 = findViewById(R.id.view_animator_probability2);
        viewAnimatorDollar3 = findViewById(R.id.view_animator_dollar3);
        viewAnimatorDollar4 = findViewById(R.id.view_animator_dollar4);
        viewAnimatorProb3 = findViewById(R.id.view_animator_probability3);
        viewAnimatorProb4 = findViewById(R.id.view_animator_probability4);

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
            timeRecordDb.insertData(getCurrentTime(), "startTrainingTrial" + trialCounter + "; Option1: A1=" + a1 + " P1=" + p1 + ", Option2: A2=" + a2 + " P2=" + p2 +
                    "; Option3: A3=" + a3 + " P3=" + p3 + ", Option4: A4=" + a4 + " P4=" + p4 + "; Orientation: horizontal");

        } else {
            timeRecordDb.insertData(getCurrentTime(), "startTrial" + trialCounter + "; Option1: A1=" + a1 + " P1=" + p1 + ", Option2: A2=" + a2 + " P2=" + p2 +
                    "; Option3: A3=" + a3 + " P3=" + p3 + ", Option4: A4=" + a4 + " P4=" + p4 + "; Orientation: horizontal");
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

        viewAnimatorDollar1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimatorDollar1,
                        new ViewAnimator[] {viewAnimatorDollar2, viewAnimatorDollar3, viewAnimatorDollar4,
                                viewAnimatorProb1, viewAnimatorProb2, viewAnimatorProb3, viewAnimatorProb4});
            }
        });

        viewAnimatorDollar2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimatorDollar2,
                        new ViewAnimator[] {viewAnimatorDollar1, viewAnimatorDollar3, viewAnimatorDollar4,
                                viewAnimatorProb1, viewAnimatorProb2, viewAnimatorProb3, viewAnimatorProb4});

            }
        });

        viewAnimatorProb1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimatorProb1,
                        new ViewAnimator[] {viewAnimatorDollar1, viewAnimatorDollar2, viewAnimatorDollar3, viewAnimatorDollar4,
                                viewAnimatorProb2, viewAnimatorProb3, viewAnimatorProb4});
            }
        });

        viewAnimatorProb2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimatorProb2,
                        new ViewAnimator[] {viewAnimatorDollar1, viewAnimatorDollar2, viewAnimatorDollar3, viewAnimatorDollar4,
                                viewAnimatorProb1, viewAnimatorProb3, viewAnimatorProb4});
            }
        });

        viewAnimatorDollar3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimatorDollar3,
                        new ViewAnimator[] {viewAnimatorDollar1, viewAnimatorDollar2, viewAnimatorDollar4,
                                viewAnimatorProb1, viewAnimatorProb2, viewAnimatorProb3, viewAnimatorProb4});
            }
        });

        viewAnimatorDollar4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimatorDollar4,
                        new ViewAnimator[] {viewAnimatorDollar1, viewAnimatorDollar2, viewAnimatorDollar3,
                                viewAnimatorProb1, viewAnimatorProb2, viewAnimatorProb3, viewAnimatorProb4});

            }
        });

        viewAnimatorProb3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimatorProb3,
                        new ViewAnimator[] {viewAnimatorDollar1, viewAnimatorDollar2, viewAnimatorDollar3, viewAnimatorDollar4,
                                viewAnimatorProb1, viewAnimatorProb2, viewAnimatorProb4});
            }
        });

        viewAnimatorProb4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimatorProb4,
                        new ViewAnimator[] {viewAnimatorDollar1, viewAnimatorDollar2, viewAnimatorDollar3, viewAnimatorDollar4,
                                viewAnimatorProb1, viewAnimatorProb2, viewAnimatorProb3});
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
                showResult(p1, a1,"Option1");
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
                showResult(p2, a2,"Option2");
            }
        });

        buttonSelect3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View V) {
                /*
                try {
                    // send identifier and timestamp
                    bluetooth.timeStamper( "??", getCurrentTime());
                } catch (IOException e) {}
                */
                showResult(p3, a3,"Option3");
            }
        });

        buttonSelect4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View V) {
                /*
                try {
                    // send identifier and timestamp
                    bluetooth.timeStamper( "??", getCurrentTime());
                } catch (IOException e) {}
                */
                showResult(p4, a4,"Option4");
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

        Intent intent = new Intent(Question2Att4OpHorizontal.this, EndDemoActivity.class);
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
        a1 = Double.parseDouble(attributes.get(2));
        p1 = Double.parseDouble(attributes.get(3))*100;
        a2 = Double.parseDouble(attributes.get(4));
        p2 = Double.parseDouble(attributes.get(5))*100;
        a3 = Double.parseDouble(attributes.get(6));
        p3 = Double.parseDouble(attributes.get(7))*100;
        a4 = Double.parseDouble(attributes.get(8));
        p4 = Double.parseDouble(attributes.get(9))*100;

        textViewProb1.setText((int) p1 + "%");
        textViewProb2.setText((int) p2 + "%");
        textViewDollar1.setText("$" + String.format("%.2f", a1));
        textViewDollar2.setText("$" + String.format("%.2f", a2));
        textViewProb3.setText((int) p3 + "%");
        textViewProb4.setText((int) p4 + "%");
        textViewDollar3.setText("$" + String.format("%.2f", a3));
        textViewDollar4.setText("$" + String.format("%.2f", a4));

        if (a1 < 0) {   // if the two dollar amounts are negative, set icons to losing
            ImageView img_dollar1 = findViewById(R.id.image_view_dollar1);
            ImageView img_prob1 = findViewById(R.id.image_view_probability1);
            ImageView img_dollar2 = findViewById(R.id.image_view_dollar2);
            ImageView img_prob2 = findViewById(R.id.image_view_probability2);
            img_dollar1.setImageResource(R.drawable.dollar_lose);
            img_dollar2.setImageResource(R.drawable.dollar_lose);
            img_prob1.setImageResource(R.drawable.probability_lose);
            img_prob2.setImageResource(R.drawable.probability_lose);
            ImageView img_dollar3 = findViewById(R.id.image_view_dollar3);
            ImageView img_prob3 = findViewById(R.id.image_view_probability3);
            ImageView img_dollar4 = findViewById(R.id.image_view_dollar4);
            ImageView img_prob4 = findViewById(R.id.image_view_probability4);
            img_dollar3.setImageResource(R.drawable.dollar_lose);
            img_dollar4.setImageResource(R.drawable.dollar_lose);
            img_prob3.setImageResource(R.drawable.probability_lose);
            img_prob4.setImageResource(R.drawable.probability_lose);
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

    private void showResult(double p, double a, String option){
        double random = new Random().nextDouble()*100;
        if (random < p) {
            amountWon = a;
        } else {
            amountWon = 0;
        }

        recordEvent(option+" selected, $"+amountWon+" won");

        timeRecordDb.close();

        Intent intent = new Intent(Question2Att4OpHorizontal.this, ResultActivity.class);
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
