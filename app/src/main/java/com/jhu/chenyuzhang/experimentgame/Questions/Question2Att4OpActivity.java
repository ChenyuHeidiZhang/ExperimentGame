package com.jhu.chenyuzhang.experimentgame.Questions;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Bundle;
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

public class Question2Att4OpActivity extends AppCompatActivity {
    private static int trialCounter; //A trial counter just for this specific trial
    private double amountWon; //If negative, then it's lose
    private double a1, a2, a3, a4; //Value of choices
    private double p1, p2, p3, p4; //Probability of choices
    private boolean isDemo;
    private boolean stop; //Record the stopping point of the user (whether a trial is finished)
    private long backPressedTime; //The time when the user pressed back button
    private long startTime; //When does the trial start

    private CountDownTimer countDownTimer; //Going back to the main page after countDownTime
    private TrialDbHelper trialInfoDb;
    private Trial currentTrial;

    private SharedPreferences demo_prefs; //The shared preference storing the status for demo
    private SharedPreferences counter_prefs; //Count how many trials have been done

    //Animators for attributes
    private ViewAnimator viewAnimator11, viewAnimator12;
    private ViewAnimator viewAnimator21, viewAnimator22;
    private ViewAnimator viewAnimator31, viewAnimator32;
    private ViewAnimator viewAnimator41, viewAnimator42;
    private Button buttonSelect1, buttonSelect2, buttonSelect3, buttonSelect4; //Four selection button

    //Actions to record
    private String eventClick = "Clicked";
    private String eventDisplay = "Displayed";
    private String eventTimeOut = "TimeOut, Covered";
    private String not_covered = ""; //Store which attribute(s) is not covered

    private static final String KEY_TRIAL_COUNTER = "keyTrialCounter"; //Key for the shared preference storing the number of trials
    private static final String KEY_DO_DEMO = "keyDoDemo"; //Key for the shared preference storing whether the trial is demo
    public static final String KEY_USER = "keyUser";

    private HashMap<Integer, Handler> viewHandlerMap = new HashMap<>(); //A map from viewAnimator ID to their corresponding handlers.
    //Identifiers maps the id of a attribute view to the code sent when it is uncovered
    //For each attribute, contains two codes before and after the uncover; third code is its alias in the database
    private HashMap<Integer, String[]> identifiers = new HashMap<>();

    private DatabaseReference userContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_question_2att_4op);

        demo_prefs = getSharedPreferences("doDemo", MODE_PRIVATE);
        isDemo = demo_prefs.getBoolean(KEY_DO_DEMO, true);
        SharedPreferences prefUserName = getSharedPreferences("user", MODE_PRIVATE);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userName = prefUserName.getString(KEY_USER, "");
        userContent = FirebaseDatabase.getInstance().getReference().child("users").child(userName).child("actions");

        stop = false;
        trialInfoDb = new TrialDbHelper(this);

        // 1st and 2nd items in the string are the event codes sent to the Arduino
        // 3rd item is stored in the database along with the timestamp
        if(a1>0) {
            identifiers.put(R.id.view_animator_11, new String[] {"2", "18", "A+1"});
        }else{
            identifiers.put(R.id.view_animator_11, new String[] {"6", "22", "A-1"});
        }
        if(p1>0) {
            identifiers.put(R.id.view_animator_21, new String[] {"3", "19", "P+1"});
        }else{
            identifiers.put(R.id.view_animator_21, new String[] {"7", "23", "P-1"});
        }
        if(a2>0) {
            identifiers.put(R.id.view_animator_12, new String[] {"4", "20", "A+2"});
        }else{
            identifiers.put(R.id.view_animator_12, new String[] {"8", "24", "A-2"});
        }
        if(p2>0) {
            identifiers.put(R.id.view_animator_22, new String[] {"5", "21", "P+2"});
        }else{
            identifiers.put(R.id.view_animator_22, new String[] {"9", "25", "P-2"});
        }

        if(a3>0) {
            identifiers.put(R.id.view_animator_31, new String[] {"10", "26", "A+3"});
        }else{
            identifiers.put(R.id.view_animator_31, new String[] {"12", "28", "A-3"});
        }
        if(p3>0) {
            identifiers.put(R.id.view_animator_41, new String[] {"11", "27", "P+3"});
        }else{
            identifiers.put(R.id.view_animator_41, new String[] {"13", "29", "P-3"});
        }

        if(a4>0) {
            identifiers.put(R.id.view_animator_32, new String[] {"14", "30", "A+4"});
        }else{
            identifiers.put(R.id.view_animator_32, new String[] {"16", "32", "A-4"});
        }
        if(p4>0) {
            identifiers.put(R.id.view_animator_42, new String[] {"15", "31", "P+4"});
        }else{
            identifiers.put(R.id.view_animator_42, new String[] {"17", "33", "P-4"});
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
        setupTrial();

        //Take down the trial starting time
        startTime = System.currentTimeMillis();

        //If is in training part, display "training" and the "end training" button
        if (isDemo) {
            Button buttonEndDemo = findViewById(R.id.button_end_demo);
            TextView tvDemo = findViewById(R.id.text_view_demo);
            buttonEndDemo.setVisibility(View.VISIBLE);
            tvDemo.setVisibility(View.VISIBLE);

            //If end training button is clicked
            buttonEndDemo.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    userContent.child(getCurrentTime()).setValue("Training ended");
                    endDemo();
                }
            });
        }

        //Start recording data
        if (isDemo) {
            userContent.child(getCurrentTime()).setValue("startTrainingTrial " + trialCounter);
        } else {
            userContent.child(getCurrentTime()).setValue("startTrial " + trialCounter);
        }

        //Go back to the entry page after 1 minute of inactivity
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

        // store trial parameters in database
        ArrayList<String> attributes = currentTrial.getAttributes();
        userContent.child(getCurrentTime()).setValue("V " + "11 " + attributes.get(0) + " " + attributes.get(1)
                + ", " + "12 " + attributes.get(2) + " " + attributes.get(3)
                + ", " + "21 " + attributes.get(4) + " " + attributes.get(5)
                + ", " + "22 " + attributes.get(6) + " " + attributes.get(7)
                + ", " + "31 " + attributes.get(8) + " " + attributes.get(9)
                + ", " + "32 " + attributes.get(10) + " " + attributes.get(11)
                + ", " + "41 " + attributes.get(12) + " " + attributes.get(13)
                + ", " + "42 " + attributes.get(14) + " " + attributes.get(15));

        //Set onclick on all the attributes
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

        //Set onclick on all the selection button
        buttonSelect1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View V) {
                //If minimum time not reached, prevent user from clicking
                if (checkMinimumTimePassed()) {
                    userContent.child(getCurrentTime()).setValue("Option1 selected successfully");
                    incrementTrialCounter();
                    unmaskAttributes(new ViewAnimator[]{viewAnimator11, viewAnimator12}, "Option1");
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
                    showResult(a2, 2);
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
                    unmaskAttributes(new ViewAnimator[]{viewAnimator31, viewAnimator32}, "Option3");
                    showResult(a3, 3);
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
                    unmaskAttributes(new ViewAnimator[]{viewAnimator41, viewAnimator42}, "Option4");
                    showResult(a4, 4);
                }
                else {
                    userContent.child(getCurrentTime()).setValue("Option4 selected");
                }
            }
        });
    }

    /**
     * This method is called when an attribute is clicked
     * @param tappedView The view that is tapped by the user
     * @param otherViews All the other views(attributes) that are on the screen
     */
    private void attributeOnClick(final ViewAnimator tappedView, ViewAnimator[] otherViews) {
        //On tap, if the attribute view is covered, uncover it for 1s and cover other attributes
        if (tappedView.getDisplayedChild() == 0) {
            final String[] codes = identifiers.get(tappedView.getId());
            //Store the action of clicking the attribute
            userContent.child(getCurrentTime()).setValue(codes[2] + ", " + codes[3] + " " + eventClick);
            //If there is another attribute that is not covered, cover them
            if (!not_covered.equals("")) {
                for (ViewAnimator v : otherViews) {
                    if (v.getDisplayedChild() == 1) {
                        userContent.child(getCurrentTime()).setValue(not_covered + " Early Mask On");
                        not_covered = "";
                        v.showNext();
                    }
                }
            }

            //Display the content of the attribute, delayed by 100 milliseconds since tapping
            Handler handler1 = new Handler();
            handler1.postDelayed(new Runnable() {
                @Override
                public void run() {
                    tappedView.showNext();
                }
            }, 100);
            //Store the action of displaying
            userContent.child(getCurrentTime()).setValue(codes[2] + ", " + codes[3] + " " + eventDisplay);
            not_covered = codes[2] + ", " + codes[3];

            //Automatically re-cover after 1000ms
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
        //Reset the inactive time counter
        countDownTimer.cancel();
        countDownTimer.start();
    }

    /**
     * This method is called when the user clicks the endDemo button
     */
    private void endDemo(){
        //Change shared "prefs" for do_demo to false
        demo_prefs.edit().putBoolean(KEY_DO_DEMO, false).apply();

        //Set trialCounter back to 1
        counter_prefs.edit().putInt(KEY_TRIAL_COUNTER, 1).apply();

        //Go to the end demo page
        Intent intent = new Intent(Question2Att4OpActivity.this, EndDemoActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * This method is called at the beginning of the trial
     */
    private void setupTrial() {
        // load trialCounter from shared preference
        counter_prefs = getSharedPreferences("trialCounter", MODE_PRIVATE);
        trialCounter = counter_prefs.getInt(KEY_TRIAL_COUNTER, 1);

        // get current trial
        currentTrial = trialInfoDb.getTrial(trialCounter);
        getAttributes();
    }

    /**
     * This method is to get all the attribute information and set them into the animator and identifier
     */
    private void getAttributes(){
        //This is the array that stores all the attributes information
        ArrayList<String> attributes = currentTrial.getAttributes();

        setAttributesForOneVA(viewAnimator11, attributes.get(0), attributes.get(1));
        setAttributesForOneVA(viewAnimator12, attributes.get(2), attributes.get(3));
        setAttributesForOneVA(viewAnimator21, attributes.get(4), attributes.get(5));
        setAttributesForOneVA(viewAnimator22, attributes.get(6), attributes.get(7));
        setAttributesForOneVA(viewAnimator31, attributes.get(8), attributes.get(9));
        setAttributesForOneVA(viewAnimator32, attributes.get(10), attributes.get(11));
        setAttributesForOneVA(viewAnimator41, attributes.get(12), attributes.get(13));
        setAttributesForOneVA(viewAnimator42, attributes.get(14), attributes.get(15));

        identifiers.put(R.id.view_animator_11, new String[] {"2", "18", "11", attributes.get(0)});
        identifiers.put(R.id.view_animator_12, new String[] {"3", "19", "12", attributes.get(2)});
        identifiers.put(R.id.view_animator_21, new String[] {"4", "20", "21", attributes.get(4)});
        identifiers.put(R.id.view_animator_22, new String[] {"5", "21", "22", attributes.get(6)});
        identifiers.put(R.id.view_animator_31, new String[] {"10", "26", "31", attributes.get(8)});
        identifiers.put(R.id.view_animator_32, new String[] {"11", "27", "32", attributes.get(10)});
        identifiers.put(R.id.view_animator_41, new String[] {"14", "30", "41", attributes.get(12)});
        identifiers.put(R.id.view_animator_42, new String[] {"15", "31", "42", attributes.get(14)});
    }

    /**
     * This method is called by getAttributes() method
     * Helper function to link animator with the attribute information
     * @param va The view animator of the attribute
     * @param attType The type of attribute
     * @param att The amount of the attribute
     */
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

    /**
     * Get current time in dd:HH:mm:ss:SSS format
     * @return The current time in string
     */
    private String getCurrentTime() {
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("dd:HH:mm:ss:SSS");
        String formattedDate= dateFormat.format(date);
        return formattedDate;
    }


    /**
     * A helper function to insert data into database
     * @param event The description of the data
     * @return The current time string
     */
    /*
    private String recordEvent(String event) {
        String timeString = getCurrentTime();
        //If inserting not success, go to the error page
        if (!timeRecordDb.insertData(timeString, event)) {
            Toast.makeText(getApplicationContext(), "Something goes wrong with database", Toast.LENGTH_LONG).show();
            timeRecordDb.close();
            Intent intent = new Intent(Question2Att4OpActivity.this, Database_fail.class);
            startActivity(intent);
            finish();
        }
        return timeString;
    }

     */

    /**
     * This is a helper function checking whether the user stay on the trial page for a minimum amount of time
     * @return True if the minimum time is passed, false otherwise
     */
    private boolean checkMinimumTimePassed() {
        if (System.currentTimeMillis() - startTime <
                getResources().getInteger(R.integer.min_time_millis_2Att4Opt)) {
            Toast.makeText(this, getString(R.string.stay_longer), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * The helper function called when a selection is made and need to unmask all the attributes
     * @param viewAnimators All the animators in the trial
     * @param option The choice that is selected
     */
    private void unmaskAttributes(ViewAnimator[] viewAnimators, String option) {
        //First cover all the attributes
        if (!not_covered.equals("")) {
            ViewAnimator[] all = new ViewAnimator[]{viewAnimator11, viewAnimator21, viewAnimator31, viewAnimator41,
                    viewAnimator12, viewAnimator22, viewAnimator32, viewAnimator42};
            for (ViewAnimator a : all) {
                a.setDisplayedChild(0);
            }
            userContent.child(getCurrentTime()).setValue(not_covered + " Early Mask On");
            not_covered = "";
        }
        //Uncover all the attributes from the specific selection
        for (ViewAnimator v : viewAnimators) {
            v.setDisplayedChild(1);
            // Disable the handler (if one exists for the current view) that sets a 1s cover time.
            Handler handler = viewHandlerMap.get(v.getId());
            if (handler != null) {
                handler.removeCallbacksAndMessages(null);
            }
        }
        userContent.child(getCurrentTime()).setValue(option + " Mask Off");

        //Disable all the button
        buttonSelect1.setEnabled(false);
        buttonSelect2.setEnabled(false);
        buttonSelect3.setEnabled(false);
        buttonSelect4.setEnabled(false);
    }

    /**
     * This method is used to show the result after user select a choice
     * @param a The value of the result
     * @param option The option
     */
    private void showResult(double a, int option){
        String[] outcomes = currentTrial.getOutcomes();
        String outcome = outcomes[option-1];
        if ("win".equals(outcome) || "lose".equals(outcome)) {
            amountWon = a;  // This can be either positive or negative.
        } else {  // "no outcome".equals(outcome)
            amountWon = 0;
        }

        //Store the display of amount into a string
        final String temp = "Option" + option + " selected, $" + amountWon + " won";

        // Wait for one second during the display of attributes.
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!stop) {
                    Intent intent = new Intent(Question2Att4OpActivity.this, ResultActivity.class);
                    intent.putExtra("EXTRA_AMOUNT_WON", amountWon);
                    intent.putExtra("DATABASE_RECORD_STRING", temp);
                    startActivity(intent);
                    finish();
                }
            }
        }, 2000);
    }

    /**
     * If the user press the back button on the bottom
     */
    @Override
    public void onBackPressed() {
        //If pressing twice
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            userContent.child(getCurrentTime()).setValue("Pressed back button, return to main page");
            stop = true;
            finish();
        } else {
            Toast.makeText(this, "Press back again to finish", Toast.LENGTH_SHORT).show();
        }
        backPressedTime = System.currentTimeMillis();
    }

    /**
     * This method increments the number of trials
     */
    private void incrementTrialCounter() {
        if (trialCounter == trialInfoDb.getNumRows()){
            // wrap around if reaches the end
            trialCounter = 1;
        } else {
            trialCounter++;
        }
        //Store the number of trial counters
        counter_prefs.edit().putInt(KEY_TRIAL_COUNTER, trialCounter).apply();
    }
}
