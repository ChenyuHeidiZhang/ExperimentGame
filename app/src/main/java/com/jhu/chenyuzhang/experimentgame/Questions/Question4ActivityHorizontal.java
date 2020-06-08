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
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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

public class Question4ActivityHorizontal extends AppCompatActivity {
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

    private double ap1, pp1, am1, pm1;  // option 1: amount plus, prob plus, amount minus, prob minus
    private double ap2, pp2, am2, pm2;
    private double ap3, pp3, am3, pm3;
    private double ap4, pp4, am4, pm4;

    private TimeDbHelper timeRecordDb;

    private ViewAnimator viewAnimator11, viewAnimator12, viewAnimator13, viewAnimator14;
    private ViewAnimator viewAnimator21, viewAnimator22, viewAnimator23, viewAnimator24;
    private ViewAnimator viewAnimator31, viewAnimator32, viewAnimator33, viewAnimator34;
    private ViewAnimator viewAnimator41, viewAnimator42, viewAnimator43, viewAnimator44;

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

        // hide the status bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_question4_horizontal);

        demo_prefs = getSharedPreferences("doDemo", MODE_PRIVATE);
        isDemo = demo_prefs.getBoolean(KEY_DO_DEMO, true);   // get shared preference of whether this is a training session

        // TODO: modify the codes
        identifiers.put(R.id.view_animator_11, new String[]{"3", "7", "A+1"});
        identifiers.put(R.id.view_animator_12, new String[]{"4", "8", "P+1"});
        identifiers.put(R.id.view_animator_13, new String[]{"5", "9", "A-1"});
        identifiers.put(R.id.view_animator_14, new String[]{"6", "10", "P-1"});
        identifiers.put(R.id.view_animator_21, new String[]{"3", "7", "A+2"});
        identifiers.put(R.id.view_animator_22, new String[]{"4", "8", "P+2"});
        identifiers.put(R.id.view_animator_23, new String[]{"5", "9", "A-2"});
        identifiers.put(R.id.view_animator_24, new String[]{"6", "10", "P-2"});
        identifiers.put(R.id.view_animator_31, new String[]{"3", "7", "A+1"});
        identifiers.put(R.id.view_animator_32, new String[]{"4", "8", "P+1"});
        identifiers.put(R.id.view_animator_33, new String[]{"5", "9", "A-1"});
        identifiers.put(R.id.view_animator_34, new String[]{"6", "10", "P-1"});
        identifiers.put(R.id.view_animator_41, new String[]{"3", "7", "A+2"});
        identifiers.put(R.id.view_animator_42, new String[]{"4", "8", "P+2"});
        identifiers.put(R.id.view_animator_43, new String[]{"5", "9", "A-2"});
        identifiers.put(R.id.view_animator_44, new String[]{"6", "10", "P-2"});

        Button buttonSelect1 = findViewById(R.id.button_select1);
        Button buttonSelect2 = findViewById(R.id.button_select2);
        Button buttonSelect3 = findViewById(R.id.button_select3);
        Button buttonSelect4 = findViewById(R.id.button_select4);

        viewAnimator11 = findViewById(R.id.view_animator_11);
        viewAnimator12 = findViewById(R.id.view_animator_12);
        viewAnimator13 = findViewById(R.id.view_animator_13);
        viewAnimator14 = findViewById(R.id.view_animator_14);
        viewAnimator21 = findViewById(R.id.view_animator_21);
        viewAnimator22 = findViewById(R.id.view_animator_22);
        viewAnimator23 = findViewById(R.id.view_animator_23);
        viewAnimator24 = findViewById(R.id.view_animator_24);
        viewAnimator31 = findViewById(R.id.view_animator_31);
        viewAnimator32 = findViewById(R.id.view_animator_32);
        viewAnimator33 = findViewById(R.id.view_animator_33);
        viewAnimator34 = findViewById(R.id.view_animator_34);
        viewAnimator41 = findViewById(R.id.view_animator_41);
        viewAnimator42 = findViewById(R.id.view_animator_42);
        viewAnimator43 = findViewById(R.id.view_animator_43);
        viewAnimator44 = findViewById(R.id.view_animator_44);

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
            timeRecordDb.insertData(getCurrentTime(), "startTrainingTrial " + trialCounter);
        } else {
            timeRecordDb.insertData(getCurrentTime(), "startTrial " + trialCounter);
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

        viewAnimator11.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimator11,
                        new ViewAnimator[]{viewAnimator12, viewAnimator13, viewAnimator14,
                                viewAnimator21, viewAnimator22, viewAnimator23, viewAnimator24,
                                viewAnimator31, viewAnimator32, viewAnimator33, viewAnimator34,
                                viewAnimator41, viewAnimator42, viewAnimator43, viewAnimator44});
            }
        });

        viewAnimator12.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimator12,
                        new ViewAnimator[]{viewAnimator11, viewAnimator13, viewAnimator14,
                                viewAnimator21, viewAnimator22, viewAnimator23, viewAnimator24,
                                viewAnimator31, viewAnimator32, viewAnimator33, viewAnimator34,
                                viewAnimator41, viewAnimator42, viewAnimator43, viewAnimator44});

            }
        });

        viewAnimator13.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimator13,
                        new ViewAnimator[]{viewAnimator11, viewAnimator12, viewAnimator14,
                                viewAnimator21, viewAnimator22, viewAnimator23, viewAnimator24,
                                viewAnimator31, viewAnimator32, viewAnimator33, viewAnimator34,
                                viewAnimator41, viewAnimator42, viewAnimator43, viewAnimator44});
            }
        });

        viewAnimator14.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimator14,
                        new ViewAnimator[]{viewAnimator11, viewAnimator12, viewAnimator13,
                                viewAnimator21, viewAnimator22, viewAnimator23, viewAnimator24,
                                viewAnimator31, viewAnimator32, viewAnimator33, viewAnimator34,
                                viewAnimator41, viewAnimator42, viewAnimator43, viewAnimator44});
            }
        });

        viewAnimator21.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimator21,
                        new ViewAnimator[]{viewAnimator11, viewAnimator12, viewAnimator14, viewAnimator13,
                                viewAnimator22, viewAnimator23, viewAnimator24,
                                viewAnimator31, viewAnimator32, viewAnimator33, viewAnimator34,
                                viewAnimator41, viewAnimator42, viewAnimator43, viewAnimator44});
            }
        });

        viewAnimator22.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimator22,
                        new ViewAnimator[]{viewAnimator11, viewAnimator12, viewAnimator14, viewAnimator13,
                                viewAnimator21, viewAnimator23, viewAnimator24,
                                viewAnimator31, viewAnimator32, viewAnimator33, viewAnimator34,
                                viewAnimator41, viewAnimator42, viewAnimator43, viewAnimator44});

            }
        });

        viewAnimator23.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimator23,
                        new ViewAnimator[]{viewAnimator11, viewAnimator12, viewAnimator14, viewAnimator13,
                                viewAnimator21, viewAnimator22, viewAnimator24,
                                viewAnimator31, viewAnimator32, viewAnimator33, viewAnimator34,
                                viewAnimator41, viewAnimator42, viewAnimator43, viewAnimator44});
            }
        });

        viewAnimator24.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimator24,
                        new ViewAnimator[]{viewAnimator11, viewAnimator12, viewAnimator14, viewAnimator13,
                                viewAnimator21, viewAnimator22, viewAnimator23,
                                viewAnimator31, viewAnimator32, viewAnimator33, viewAnimator34,
                                viewAnimator41, viewAnimator42, viewAnimator43, viewAnimator44});
            }
        });

        viewAnimator31.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimator31,
                        new ViewAnimator[]{viewAnimator11, viewAnimator12, viewAnimator14, viewAnimator13,
                                viewAnimator21, viewAnimator22, viewAnimator23, viewAnimator24,
                                viewAnimator32, viewAnimator33, viewAnimator34,
                                viewAnimator41, viewAnimator42, viewAnimator43, viewAnimator44});
            }
        });

        viewAnimator32.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimator32,
                        new ViewAnimator[]{viewAnimator11, viewAnimator12, viewAnimator14, viewAnimator13,
                                viewAnimator21, viewAnimator22, viewAnimator23, viewAnimator24,
                                viewAnimator31, viewAnimator33, viewAnimator34,
                                viewAnimator41, viewAnimator42, viewAnimator43, viewAnimator44});
            }
        });

        viewAnimator33.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimator33,
                        new ViewAnimator[]{viewAnimator11, viewAnimator12, viewAnimator14, viewAnimator13,
                                viewAnimator21, viewAnimator22, viewAnimator23, viewAnimator24,
                                viewAnimator31, viewAnimator32, viewAnimator34,
                                viewAnimator41, viewAnimator42, viewAnimator43, viewAnimator44});
            }
        });

        viewAnimator34.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimator34,
                        new ViewAnimator[]{viewAnimator11, viewAnimator12, viewAnimator14, viewAnimator13,
                                viewAnimator21, viewAnimator22, viewAnimator23, viewAnimator24,
                                viewAnimator31, viewAnimator32, viewAnimator33,
                                viewAnimator41, viewAnimator42, viewAnimator43, viewAnimator44});
            }
        });

        viewAnimator41.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimator41,
                        new ViewAnimator[]{viewAnimator11, viewAnimator12, viewAnimator14, viewAnimator13,
                                viewAnimator21, viewAnimator22, viewAnimator23, viewAnimator24,
                                viewAnimator31, viewAnimator32, viewAnimator33, viewAnimator34,
                                viewAnimator42, viewAnimator43, viewAnimator44});
            }
        });

        viewAnimator42.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimator42,
                        new ViewAnimator[]{viewAnimator11, viewAnimator12, viewAnimator14, viewAnimator13,
                                viewAnimator21, viewAnimator22, viewAnimator23, viewAnimator24,
                                viewAnimator31, viewAnimator32, viewAnimator33, viewAnimator34,
                                viewAnimator41, viewAnimator43, viewAnimator44});
            }
        });

        viewAnimator43.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimator43,
                        new ViewAnimator[]{viewAnimator11, viewAnimator12, viewAnimator14, viewAnimator13,
                                viewAnimator21, viewAnimator22, viewAnimator23, viewAnimator24,
                                viewAnimator31, viewAnimator32, viewAnimator33, viewAnimator34,
                                viewAnimator41, viewAnimator42, viewAnimator44});
            }
        });

        viewAnimator44.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick(viewAnimator44,
                        new ViewAnimator[]{viewAnimator11, viewAnimator12, viewAnimator14, viewAnimator13,
                                viewAnimator21, viewAnimator22, viewAnimator23, viewAnimator24,
                                viewAnimator31, viewAnimator32, viewAnimator33, viewAnimator34,
                                viewAnimator41, viewAnimator42, viewAnimator43});
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

        Intent intent = new Intent(Question4ActivityHorizontal.this, EndDemoActivity.class);
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
        setAttributesForOneVA(viewAnimator13, attributes.get(4), attributes.get(5));
        setAttributesForOneVA(viewAnimator14, attributes.get(6), attributes.get(7));
        setAttributesForOneVA(viewAnimator21, attributes.get(8), attributes.get(9));
        setAttributesForOneVA(viewAnimator22, attributes.get(10), attributes.get(11));
        setAttributesForOneVA(viewAnimator23, attributes.get(12), attributes.get(13));
        setAttributesForOneVA(viewAnimator24, attributes.get(14), attributes.get(15));
        setAttributesForOneVA(viewAnimator31, attributes.get(16), attributes.get(17));
        setAttributesForOneVA(viewAnimator32, attributes.get(18), attributes.get(19));
        setAttributesForOneVA(viewAnimator33, attributes.get(20), attributes.get(21));
        setAttributesForOneVA(viewAnimator34, attributes.get(22), attributes.get(23));
        setAttributesForOneVA(viewAnimator41, attributes.get(24), attributes.get(25));
        setAttributesForOneVA(viewAnimator42, attributes.get(26), attributes.get(27));
        setAttributesForOneVA(viewAnimator43, attributes.get(28), attributes.get(29));
        setAttributesForOneVA(viewAnimator44, attributes.get(30), attributes.get(31));
    }

    private void setAttributesForOneVA(ViewAnimator va, String attType, String att) {
        ImageView imgView = (ImageView) va.getChildAt(0);
        TextView tv = (TextView) va.getChildAt(1);

        if (attType.equals("A+1")) {
            ap1 = Double.parseDouble(att);
            tv.setText("$" + String.format("%.2f", Math.abs(ap1)));
        } else if (attType.equals("P+1")) {
            pp1 = Double.parseDouble(att);
            tv.setText((int) (pp1 * 100) + "%");
        } else if (attType.equals("A-1")) {
            am1 = Double.parseDouble(att);
            tv.setText("$" + String.format("%.2f", Math.abs(am1)));
        } else if (attType.equals("P-1")) {
            pm1 = Double.parseDouble(att);
            tv.setText((int) (pm1 * 100) + "%");
        } else if (attType.equals("A+2")) {
            ap2 = Double.parseDouble(att);
            tv.setText("$" + String.format("%.2f", Math.abs(ap2)));
        } else if (attType.equals("P+2")) {
            pp2 = Double.parseDouble(att);
            tv.setText((int) (pp2 * 100) + "%");
        } else if (attType.equals("A-2")) {
            am2 = Double.parseDouble(att);
            tv.setText("$" + String.format("%.2f", Math.abs(am2)));
        } else if (attType.equals("P-2")) {
            pm2 = Double.parseDouble(att);
            tv.setText((int) (pm2 * 100) + "%");
        } else if (attType.equals("A+3")) {
            ap3 = Double.parseDouble(att);
            tv.setText("$" + String.format("%.2f", Math.abs(ap3)));
        } else if (attType.equals("P+3")) {
            pp3 = Double.parseDouble(att);
            tv.setText((int) (pp3 * 100) + "%");
        } else if (attType.equals("A-3")) {
            am3 = Double.parseDouble(att);
            tv.setText("$" + String.format("%.2f", Math.abs(am3)));
        } else if (attType.equals("P-3")) {
            pm1 = Double.parseDouble(att);
            tv.setText((int) (pm1 * 100) + "%");
        } else if (attType.equals("A+4")) {
            ap4 = Double.parseDouble(att);
            tv.setText("$" + String.format("%.2f", Math.abs(ap4)));
        } else if (attType.equals("P+4")) {
            pp4 = Double.parseDouble(att);
            tv.setText((int) (pp4 * 100) + "%");
        } else if (attType.equals("A-4")) {
            am4 = Double.parseDouble(att);
            tv.setText("$" + String.format("%.2f", Math.abs(am4)));
        } else if (attType.equals("P-4")) {
            pm4 = Double.parseDouble(att);
            tv.setText((int) (pm4 * 100) + "%");
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

    private void showResult(double pp, double ap, double pm, double am, String option){
        double random = new Random().nextDouble();
        if (random < pp) {  // if random less than probability win, then win
            amountWon = ap;
        } else if (random < pp + pm) {   // if random is between prob win and prob win + prob lose, then lose
            amountWon = am;
        } else { amountWon = 0; }

        recordEvent(option+" selected, $"+amountWon+" won");

        timeRecordDb.close();

        Intent intent = new Intent(Question4ActivityHorizontal.this, ResultActivity.class);
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
