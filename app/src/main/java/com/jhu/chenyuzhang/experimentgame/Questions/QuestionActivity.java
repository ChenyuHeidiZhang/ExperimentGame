package com.jhu.chenyuzhang.experimentgame.Questions;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
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

// Vertical display of two options; either 2 or 4 attributes in each option
// attributes not present are set to be 'gone'
public class QuestionActivity extends AppCompatActivity {
    private boolean isDemo;
    private CountDownTimer countDownTimer;
    private double amountWon;
    boolean stop;
    private long backPressedTime;
    private long startTime;

    private String eventClick = "Clicked";
    private String eventDisplay = "Displayed";
    private String eventTimeOut = "TimeOut, Covered";
    private String not_covered = "";


    private TrialDbHelper trialInfoDb;
    private Trial currentTrial;
    private static int trialCounter;

    private SharedPreferences demo_prefs;
    private SharedPreferences counter_prefs;

    public static final String KEY_TRIAL_COUNTER = "keyTrialCounter";
    private static final String KEY_DO_DEMO = "keyDoDemo";
    public static final String KEY_USER = "keyUser";

    private double a1;
    private double p1;
    private double a2;
    private double p2;

    private ViewAnimator viewAnimator11;  // 11: 1st of first option (top left here)
    private ViewAnimator viewAnimator12;  // 12: 2nd of first option (bottom left)
    private ViewAnimator viewAnimator21;  // 21: 1st of second option (top right)
    private ViewAnimator viewAnimator22;  // 22: 2nd of second option (bottom right)
    private Button buttonSelect1, buttonSelect2;

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
        setContentView(R.layout.activity_question);

        stop = false;

        demo_prefs = getSharedPreferences("doDemo", MODE_PRIVATE);
        SharedPreferences prefUserName = getSharedPreferences("user", MODE_PRIVATE);
        isDemo = demo_prefs.getBoolean(KEY_DO_DEMO, true);   // get shared preference of whether this is a training session

        buttonSelect1 = findViewById(R.id.button_select1);
        buttonSelect2 = findViewById(R.id.button_select2);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userName = prefUserName.getString(KEY_USER, "");
        userContent = FirebaseDatabase.getInstance().getReference().child("users").child(userName).child("actions");


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
                    userContent.child(getCurrentTime()).setValue("Training ended");
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
                + ", " + "21 " + attributes.get(4) + " " + attributes.get(5)
                + ", " + "22 " + attributes.get(6) + " " + attributes.get(7));

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
                if (checkMinimumTimePassed()) {
                    userContent.child(getCurrentTime()).setValue("Option1 selected successfully");
                    incrementTrialCounter();
                    unmaskAttributes(new ViewAnimator[]{viewAnimator11, viewAnimator12}, "Option1");
                    buttonSelect2.setEnabled(false);
                    showResult(a1, 1);
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
                    unmaskAttributes(new ViewAnimator[]{viewAnimator21, viewAnimator22}, "Option2");
                    buttonSelect1.setEnabled(false);
                    showResult(a2, 2);
                }
                else {
                    userContent.child(getCurrentTime()).setValue("Option2 selected");
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

            //armVSyncHandlerA1();
            if (!not_covered.equals("")) {
                /* if other attributes are uncovered, cover them */
                for (ViewAnimator v: otherViews) {
                    if (v.getDisplayedChild() == 1) {
                        userContent.child(getCurrentTime()).setValue(not_covered + " Early Mask On");
                        not_covered = "";
                        v.showNext();
                    }
                }
            }

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

        Intent intent = new Intent(QuestionActivity.this, EndDemoActivity.class);
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
        // First two are event codes sent via bluetooth (1st 2nd strings are for tap and displayed respectively).
        // 3rd (location) and last (attribute type) strings are the ones inserted into the SQLite database.
        identifiers.put(R.id.view_animator_11, new String[] {"2", "18", "11", attributes.get(0)});
        identifiers.put(R.id.view_animator_12, new String[] {"3", "19", "12", attributes.get(2)});

        identifiers.put(R.id.view_animator_21, new String[] {"4", "20", "21", attributes.get(4)});
        identifiers.put(R.id.view_animator_22, new String[] {"5", "21", "22", attributes.get(6)});

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

    /*
    private String recordEvent(String event) {
        //long timeSpan = System.nanoTime() - startTime;
        //String timeString = String.format("%d", timeSpan / 1000);
        String timeString = getCurrentTime();

        if (!timeRecordDb.insertData(timeString, event)) {
            Toast.makeText(getApplicationContext(), "Something goes wrong with database", Toast.LENGTH_LONG).show();
            timeRecordDb.close();
            Intent intent = new Intent(QuestionActivity.this, Database_fail.class);
            startActivity(intent);
            finish();
        }
        return timeString;
    }

     */

    private boolean checkMinimumTimePassed() {
        if (System.currentTimeMillis() - startTime <
                getResources().getInteger(R.integer.min_time_millis_2Att2Opt)) {
            Toast.makeText(this, getString(R.string.stay_longer), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void unmaskAttributes(ViewAnimator[] viewAnimators, String option) {
        if (!not_covered.equals("")) {
            ViewAnimator[] all = new ViewAnimator[]{viewAnimator11, viewAnimator21,
                    viewAnimator12, viewAnimator22};
            for (ViewAnimator a : all) {
                a.setDisplayedChild(0);
            }
            userContent.child(getCurrentTime()).setValue(not_covered + " Early Mask On");
            not_covered = "";
        }
        for (ViewAnimator v : viewAnimators) {
            v.setDisplayedChild(1);
            // Disable the handler (if one exists for the current view) that sets a 1s cover time.
            Handler handler = viewHandlerMap.get(v.getId());
            if (handler != null) {
                handler.removeCallbacksAndMessages(null);
            }

        }
        userContent.child(getCurrentTime()).setValue(option + " Mask off");
        buttonSelect1.setEnabled(false);
        buttonSelect2.setEnabled(false);
    }

    // If enough time is spent on this trial, show results after one second display of chosen attributes.
    private void showResult(double a, int option) {
        String outcomes[] = currentTrial.getOutcomes();
        String outcome = outcomes[option - 1];
        if ("win".equals(outcome) || "lose".equals(outcome)) {
            amountWon = a;  // This can be either positive or negative.
        } else {  // "no outcome".equals(outcome)
            amountWon = 0;
        }
        final String temp = "Option" + option + " selected, $" + amountWon + " won";

        // Wait for one second during the display of attributes.
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!stop) {
                    Intent intent = new Intent(QuestionActivity.this, ResultActivity.class);
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
