package com.jhu.chenyuzhang.experimentgame.Questions;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jhu.chenyuzhang.experimentgame.EndDemoActivity;
import com.jhu.chenyuzhang.experimentgame.R;
import com.jhu.chenyuzhang.experimentgame.ResultActivity;
import com.jhu.chenyuzhang.experimentgame.Trial;
import com.jhu.chenyuzhang.experimentgame.TrialDbHelper;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Question4Activity extends AppCompatActivity {
    private boolean isDemo;
    private CountDownTimer countDownTimer;
    boolean stop;
    private long backPressedTime;
    private long startTime;
    private double amountWon;

    private String eventClick = "Clicked";
    private String eventDisplay = "Displayed";
    private String eventTimeOut = "TimeOut, Covered";
    private String not_covered = "";

    private TrialDbHelper trialInfoDb;
    private Trial currentTrial;
    private static int trialCounter;

    private SharedPreferences counter_prefs;
    private SharedPreferences demo_prefs;

    public static final String KEY_TRIAL_COUNTER = "keyTrialCounter";
    private static final String KEY_DO_DEMO = "keyDoDemo";
    public static final String KEY_USER = "keyUser";

    private double ap1, pp1, am1, pm1;  // option 1: amount plus, prob plus, amount minus, prob minus
    private double ap2, pp2, am2, pm2;
    private double ap3, pp3, am3, pm3;
    private double ap4, pp4, am4, pm4;

    private ViewAnimator viewAnimator11, viewAnimator12, viewAnimator13, viewAnimator14;
    private ViewAnimator viewAnimator21, viewAnimator22, viewAnimator23, viewAnimator24;
    private ViewAnimator viewAnimator31, viewAnimator32, viewAnimator33, viewAnimator34;
    private ViewAnimator viewAnimator41, viewAnimator42, viewAnimator43, viewAnimator44;

    private Button buttonSelect1, buttonSelect2, buttonSelect3, buttonSelect4;

    // A map from viewAnimator ID to their corresponding handlers.
    private HashMap<Integer, Handler> viewHandlerMap = new HashMap<>();
    // identifiers maps the id of a attribute view to the code sent when it is uncovered
    // for each attribute, contains two codes before and after the uncover; third code is its alias in the database
    private HashMap<Integer, String[]> identifiers = new HashMap<>();

    private DatabaseReference userContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_question4);

        stop = false;

        demo_prefs = getSharedPreferences("doDemo", MODE_PRIVATE);
        isDemo = demo_prefs.getBoolean(KEY_DO_DEMO, true);   // get shared preference of whether this is a training session
        SharedPreferences prefUserName = getSharedPreferences("user", MODE_PRIVATE);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userName = prefUserName.getString(KEY_USER, "");
        userContent = FirebaseDatabase.getInstance().getReference().child("users").child(userName).child("actions");

        identifiers.put(R.id.view_animator_11, new String[] {"2", "18", "A+1"});
        identifiers.put(R.id.view_animator_12, new String[] {"3", "19", "P+1"});
        identifiers.put(R.id.view_animator_13, new String[] {"6", "22", "A-1"});
        identifiers.put(R.id.view_animator_14, new String[] {"7", "23", "P-1"});

        identifiers.put(R.id.view_animator_21, new String[] {"4", "20", "A+2"});
        identifiers.put(R.id.view_animator_22, new String[] {"5", "21", "P+2"});
        identifiers.put(R.id.view_animator_23, new String[] {"8", "24", "A-2"});
        identifiers.put(R.id.view_animator_24, new String[] {"9", "25", "P-2"});

        identifiers.put(R.id.view_animator_31, new String[]{"10", "26", "A+3"});
        identifiers.put(R.id.view_animator_32, new String[]{"11", "27", "P+3"});
        identifiers.put(R.id.view_animator_33, new String[]{"12", "28", "A-3"});
        identifiers.put(R.id.view_animator_34, new String[]{"13", "29", "P-3"});

        identifiers.put(R.id.view_animator_41, new String[]{"14", "30", "A+4"});
        identifiers.put(R.id.view_animator_42, new String[]{"15", "31", "P+4"});
        identifiers.put(R.id.view_animator_43, new String[]{"16", "32", "A-4"});
        identifiers.put(R.id.view_animator_44, new String[]{"17", "33", "P-4"});

        buttonSelect1 = findViewById(R.id.button_select1);
        buttonSelect2 = findViewById(R.id.button_select2);
        buttonSelect3 = findViewById(R.id.button_select3);
        buttonSelect4 = findViewById(R.id.button_select4);

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
                    userContent.child(getCurrentTime()).setValue("Training ended");
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

        trialInfoDb = new TrialDbHelper(this);
        setupTrial();
        
        if (isDemo) {
            userContent.child(getCurrentTime()).setValue("startTrainingTrial " + trialCounter);
        } else {
            userContent.child(getCurrentTime()).setValue("startTrial " + trialCounter);
        }

        // store trial parameters in database
        ArrayList<String> attributes = currentTrial.getAttributes();
        userContent.child(getCurrentTime()).setValue("V " + "11 " + attributes.get(0) + " " + attributes.get(1)
                + ", " + "12 " + attributes.get(2) + " " + attributes.get(3)
                + ", " + "13 " + attributes.get(4) + " " + attributes.get(5)
                + ", " + "14 " + attributes.get(6) + " " + attributes.get(7)
                + ", " + "21 " + attributes.get(8) + " " + attributes.get(9)
                + ", " + "22 " + attributes.get(10) + " " + attributes.get(11)
                + ", " + "23 " + attributes.get(12) + " " + attributes.get(13)
                + ", " + "24 " + attributes.get(14) + " " + attributes.get(15)
                + ", " + "31 " + attributes.get(16) + " " + attributes.get(17)
                + ", " + "32 " + attributes.get(18) + " " + attributes.get(19)
                + ", " + "33 " + attributes.get(20) + " " + attributes.get(21)
                + ", " + "34 " + attributes.get(22) + " " + attributes.get(23)
                + ", " + "41 " + attributes.get(24) + " " + attributes.get(25)
                + ", " + "42 " + attributes.get(26) + " " + attributes.get(27)
                + ", " + "43 " + attributes.get(28) + " " + attributes.get(29)
                + ", " + "44 " + attributes.get(30) + " " + attributes.get(31));

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


        buttonSelect1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View V) {
                if (checkMinimumTimePassed()) {
                    userContent.child(getCurrentTime()).setValue("Option1 selected successfully");
                    incrementTrialCounter();
                    unmaskAttributes(new ViewAnimator[]{viewAnimator11, viewAnimator12, viewAnimator13, viewAnimator14}, "Option1");
                    showResult(ap1, am1, 1);
                }
                else {
                    userContent.child(getCurrentTime()).setValue("Option1 selected");
                }
            }
        });

        buttonSelect2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View V) {
                if (checkMinimumTimePassed()) {
                    userContent.child(getCurrentTime()).setValue("Option2 selected successfully");
                    incrementTrialCounter();
                    unmaskAttributes(new ViewAnimator[]{viewAnimator21, viewAnimator22, viewAnimator23, viewAnimator24}, "Option2");
                    showResult(ap2, am2, 2);
                }
                else {
                    userContent.child(getCurrentTime()).setValue("Option2 selected");
                }
            }
        });

        buttonSelect3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View V) {
                if (checkMinimumTimePassed()) {
                    userContent.child(getCurrentTime()).setValue("Option3 selected successfully");
                    incrementTrialCounter();
                    unmaskAttributes(new ViewAnimator[]{viewAnimator31, viewAnimator32, viewAnimator33, viewAnimator34}, "Option3");
                    showResult(ap3, am3, 3);
                }
                else {
                    userContent.child(getCurrentTime()).setValue("Option3 selected");
                }
            }
        });

        buttonSelect4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View V) {
                if (checkMinimumTimePassed()) {
                    userContent.child(getCurrentTime()).setValue("Option4 selected successfully");
                    incrementTrialCounter();
                    unmaskAttributes(new ViewAnimator[]{viewAnimator41, viewAnimator42, viewAnimator43, viewAnimator44}, "Option4");
                    showResult(ap4, am4, 4);
                }
                else {
                    userContent.child(getCurrentTime()).setValue("Option4 selected");
                }
            }
        });
    }

    // called when each attribute is clicked
    private void attributeOnClick(final ViewAnimator tappedView, ViewAnimator[] otherViews) {
        /* on tap, if the attribute view is covered, uncover it for 1s and cover other attributes */
        if (tappedView.getDisplayedChild() == 0) {
            final String[] codes = identifiers.get(tappedView.getId()); // get the corresponding identifiers for the clicked attribute

            userContent.child(getCurrentTime()).setValue(codes[2] + ", " + codes[3] + " " + eventClick);
            if (!not_covered.equals("")) {
                /* if other attributes are uncovered, cover them */
                for (ViewAnimator v: otherViews) {
                    if (v.getDisplayedChild() == 1) {
                        userContent.child(getCurrentTime()).setValue(not_covered +  " Early Mask On");
                        not_covered = "";

                        v.showNext();
                    }
                }
            }

            //armVSyncHandlerA1();

            Handler handler1 = new Handler();
            handler1.postDelayed(new Runnable() {
                @Override
                public void run() {
                    tappedView.showNext();  /* uncover */
                }
            }, 100);

            userContent.child(getCurrentTime()).setValue(codes[2] + ", " + codes[3] + " " + eventDisplay);
            not_covered = codes[2] + ", " + codes[3];

            Log.d("Questions", codes[3]);

            /* automatically re-cover after 1000ms */
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (tappedView.getDisplayedChild() == 1 && !not_covered.equals("")) {
                        tappedView.showNext();
                        userContent.child(getCurrentTime()).setValue(codes[2] + ", " + codes[3] + " " + eventTimeOut);
                        not_covered = "";
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

        Intent intent = new Intent(Question4Activity.this, EndDemoActivity.class);
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

        // First two are event codes sent via bluetooth (1st 2nd strings are for tap and displayed respectively).
        // 3rd (location) and last (attribute type) strings are the ones inserted into the SQLite database.
        identifiers.put(R.id.view_animator_11, new String[] {"2", "18", "11", attributes.get(0)});
        identifiers.put(R.id.view_animator_12, new String[] {"3", "19", "12", attributes.get(2)});
        identifiers.put(R.id.view_animator_13, new String[] {"6", "22", "13", attributes.get(4)});
        identifiers.put(R.id.view_animator_14, new String[] {"7", "23", "14", attributes.get(6)});

        identifiers.put(R.id.view_animator_21, new String[] {"4", "20", "21", attributes.get(8)});
        identifiers.put(R.id.view_animator_22, new String[] {"5", "21", "22", attributes.get(10)});
        identifiers.put(R.id.view_animator_23, new String[] {"8", "24", "23", attributes.get(12)});
        identifiers.put(R.id.view_animator_24, new String[] {"9", "25", "24", attributes.get(14)});

        identifiers.put(R.id.view_animator_31, new String[] {"10", "26", "31", attributes.get(16)});
        identifiers.put(R.id.view_animator_32, new String[] {"11", "27", "32", attributes.get(18)});
        identifiers.put(R.id.view_animator_33, new String[] {"12", "28", "33", attributes.get(20)});
        identifiers.put(R.id.view_animator_34, new String[] {"13", "29", "34", attributes.get(22)});

        identifiers.put(R.id.view_animator_41, new String[] {"14", "30", "41", attributes.get(24)});
        identifiers.put(R.id.view_animator_42, new String[] {"15", "31", "42", attributes.get(26)});
        identifiers.put(R.id.view_animator_43, new String[] {"16", "32", "43", attributes.get(28)});
        identifiers.put(R.id.view_animator_44, new String[] {"17", "33", "44", attributes.get(30)});
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
            pm3 = Double.parseDouble(att);
            tv.setText((int) (pm3 * 100) + "%");
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

    /*
    private String recordEvent(String event) {
        //long timeSpan = System.nanoTime() - startTime;
        //String timeString = String.format("%d", timeSpan / 1000);
        String timeString = getCurrentTime();

        if (!timeRecordDb.insertData(timeString, event)) {
            Toast.makeText(getApplicationContext(), "Something goes wrong with database", Toast.LENGTH_LONG).show();
            timeRecordDb.close();
            Intent intent = new Intent(Question4Activity.this, Database_fail.class);
            startActivity(intent);
            finish();
        }
        return timeString;
    }

     */

    private boolean checkMinimumTimePassed() {
        if (System.currentTimeMillis() - startTime <
                getResources().getInteger(R.integer.min_time_millis_4Att4Opt)) {
            Toast.makeText(this, getString(R.string.stay_longer), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void unmaskAttributes(ViewAnimator[] viewAnimators, String option) {
        if (!not_covered.equals("")) {
            ViewAnimator[] all = new ViewAnimator[]{viewAnimator11, viewAnimator12, viewAnimator14, viewAnimator13,
                    viewAnimator21, viewAnimator22, viewAnimator23, viewAnimator24,
                    viewAnimator31, viewAnimator32, viewAnimator33, viewAnimator34,
                    viewAnimator41, viewAnimator42, viewAnimator43, viewAnimator44};
            for (ViewAnimator a : all) {
                a.setDisplayedChild(0);
            }
            userContent.child(getCurrentTime()).setValue(not_covered + " Early Mask On");
            not_covered = "";
        }
        for (ViewAnimator v : viewAnimators) {
            v.setDisplayedChild(1);
            Handler handler = viewHandlerMap.get(v.getId());
            if (handler != null) {
                handler.removeCallbacksAndMessages(null);
            }
        }
        userContent.child(getCurrentTime()).setValue(option + " Mask off");
        buttonSelect1.setEnabled(false);
        buttonSelect2.setEnabled(false);
        buttonSelect3.setEnabled(false);
        buttonSelect4.setEnabled(false);
    }

    private void showResult(double ap, double am, int option) {
        String outcomes[] = currentTrial.getOutcomes();
        String outcome = outcomes[option-1];
        if ("win".equals(outcome)) {
            amountWon = ap;
        } else if ("lose".equals(outcome)) {
            amountWon = am;
        } else {  // "no outcome".equals(outcome)
            amountWon = 0;
        }
        final String temp = "Option" + option + " selected, $" + amountWon + " won";
        //timeRecordDb.close();

        // Wait for one second during the display of attributes.
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!stop) {
                    Intent intent = new Intent(Question4Activity.this, ResultActivity.class);
                    intent.putExtra("EXTRA_AMOUNT_WON", amountWon);
                    intent.putExtra("DATABASE_RECORD_STRING", temp);
                    startActivity(intent);
                    finish();
                }
            }
        }, 2000);
    }

    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            //timeRecordDb.close();
            userContent.child(getCurrentTime()).setValue("Pressed back button, return to main page");
            stop = true;
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
