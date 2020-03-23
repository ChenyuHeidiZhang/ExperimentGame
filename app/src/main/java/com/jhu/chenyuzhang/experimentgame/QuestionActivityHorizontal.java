package com.jhu.chenyuzhang.experimentgame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

public class QuestionActivityHorizontal extends AppCompatActivity {
    public static double totalAmountWon;
    public static final String KEY_TOTAL_AMOUNT = "keyTotalAmount";

    private CountDownTimer countDownTimer;

    private TrialDbHelper trialInfoDb;
    private ArrayList<Trial> trialList;
    private Trial currentTrial;
    public static int trialCounter=0;
    public static final String KEY_TRIAL_COUNTER = "keyTrialCounter";
    private double amountWon = 0;
    private double p1;
    private double p2;
    private double a1;
    private double a2;

    public static TimeDbHelper timeRecordDb;

    private TextView textViewDollar1;
    private TextView textViewProbability1;
    private TextView textViewDollar2;
    private TextView textViewProbability2;
    private TextView textViewTest;

    private Button buttonSelect1;
    private Button buttonSelect2;

    private ViewAnimator viewAnimatorDollar1;
    private ViewAnimator viewAnimatorDollar2;
    private ViewAnimator viewAnimatorProbability1;
    private ViewAnimator viewAnimatorProbability2;

    private String startTimeWorld;
    private long startTime;

    private String eventClick = "Clicked, Displayed";
    private String eventTimeOut = "TimeOut, Covered";

    private int random_position = new Random().nextInt(2);

    private long backPressedTime;

    Bluetooth bluetooth;

    // identifers maps the id of a attribute view to the code sent when it is uncovered
    // for each attribute, contains two codes before and after the uncover; third code is its alias in the database
    private HashMap<Integer, String[]> identifiers = new HashMap<>();

    // the code sent when an attribute view is covered after 1s
    private String identifier_cover = "16";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_horizontal);

        identifiers.put(R.id.view_animator_dollar1, new String[] {"3", "7", "A1"});
        identifiers.put(R.id.view_animator_dollar2, new String[] {"5", "9", "A2"});
        identifiers.put(R.id.view_animator_probability1, new String[] {"4", "8", "P1"});
        identifiers.put(R.id.view_animator_probability2, new String[] {"6", "10", "P2"});

        textViewDollar1 = findViewById(R.id.text_view_dollar1);
        textViewDollar2 = findViewById(R.id.text_view_dollar2);
        textViewProbability1 = findViewById(R.id.text_view_probability1);
        textViewProbability2 = findViewById(R.id.text_view_probability2);

        textViewTest = findViewById(R.id.text_view_test);

        buttonSelect1 = findViewById(R.id.button_select1);
        buttonSelect2 = findViewById(R.id.button_select2);

        viewAnimatorDollar1 = findViewById(R.id.view_animator_dollar1);
        viewAnimatorDollar2 = findViewById(R.id.view_animator_dollar2);
        viewAnimatorProbability1 = findViewById(R.id.view_animator_probability1);
        viewAnimatorProbability2 = findViewById(R.id.view_animator_probability2);

        startTime = System.nanoTime();
        startTimeWorld = getCurrentTime();

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

        String position = "A1P1,A2P2";
        if (random_position==1){
            position = "P1A1,P2A2";
            exchangeA1P1();
            exchangeA2P2();
        }

        trialInfoDb = new TrialDbHelper(this);
        trialList = trialInfoDb.getAllTrials();
        Collections.shuffle(trialList);

        loadUpdateTrialCounter();
        currentTrial = trialList.get(trialCounter-1);
        getAttributes();

        timeRecordDb = new TimeDbHelper(this);
        timeRecordDb.insertData(startTimeWorld, "startTrial" + trialCounter + "; Option1(Blue): A1=" + a1 + " P1=" + p1 + ", Option2(Green): A2=" + a2 + " P2=" + p2 + "; Orientation: horizontal" + position);

        //bluetooth = new Bluetooth(timeRecordDb);

        /*
        try {
            // send trial number
            bluetooth.timeStamper(Integer.toString(trialCounter + 200),getCurrentTime());
            // send attribute magnitudes
            bluetooth.timeStamper(Integer.toString(16),String.format ("%.0f",a1*100));
            bluetooth.timeStamper(Integer.toString(18),String.format ("%.0f",p1));
            bluetooth.timeStamper(Integer.toString(17),String.format ("%.0f",a2*100));
            bluetooth.timeStamper(Integer.toString(19),String.format ("%.0f",p2));
            bluetooth.timeStamper( "20", Integer.toString(random_position+4));
        } catch (IOException e) {
            e.printStackTrace();
        }
        */

        viewAnimatorDollar1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    attributeOnClick_test(viewAnimatorDollar1.getId(),
                            new int[] {viewAnimatorDollar2.getId(), viewAnimatorProbability1.getId(), viewAnimatorProbability2.getId()});
                } catch (NullPointerException e) {
                    Log.d("onClickMethod", "some error");
                }
            }
        });

        viewAnimatorDollar2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick_test(viewAnimatorDollar2.getId(),
                        new int[] {viewAnimatorDollar1.getId(), viewAnimatorProbability1.getId(), viewAnimatorProbability2.getId()});

            }
        });

        viewAnimatorProbability1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick_test(viewAnimatorProbability1.getId(),
                        new int[] {viewAnimatorDollar1.getId(), viewAnimatorDollar2.getId(), viewAnimatorProbability2.getId()});
            }
        });

        viewAnimatorProbability2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attributeOnClick_test(viewAnimatorProbability2.getId(),
                        new int[] {viewAnimatorDollar1.getId(), viewAnimatorDollar2.getId(), viewAnimatorProbability1.getId()});
            }
        });


        buttonSelect1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View V) {
                /*
                try {
                    // send identifier and timestamp
                    bluetooth.timeStamper( "12", getCurrentTime());
                } catch (IOException e) {e.printStackTrace();}
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
                } catch (IOException e) {e.printStackTrace();}
                */
                showResult(p2, a2,"Option2");
            }
        });
    }

    // called when each attribute is clicked
    private void attributeOnClick_test(int tappedViewID, int[] otherViewsID) {
        //for testing purposes
        if (textViewTest.getVisibility() == View.VISIBLE) {
            textViewTest.setVisibility(View.GONE);
        } else {
            textViewTest.setVisibility(View.VISIBLE);
        }

        final ViewAnimator tappedView = findViewById(tappedViewID);

        /* on tap, if the attribute view is covered, uncover it for 1s and cover other attributes */
        if (tappedView.getDisplayedChild() == 0) {
            final String[] codes = identifiers.get(tappedViewID);

            tappedView.showNext();  /* uncover */

            recordEvent(codes[2] + " " + eventClick);

            /* automatically re-cover after 1000ms */
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (tappedView.getDisplayedChild() == 1) {

                        tappedView.showNext();
                        recordEvent(codes[2] + " " + eventTimeOut);
                    }
                }
            }, 1000);

            ViewAnimator otherView;
            /* if other attributes are uncovered, cover them */
            for (int v_id: otherViewsID) {
                otherView = findViewById(v_id);
                if (otherView.getDisplayedChild() == 1) {

                    otherView.showNext();
                }
            }
        }

        countDownTimer.cancel();
        countDownTimer.start();
    }

    private void loadUpdateTrialCounter(){
        SharedPreferences prefs = getSharedPreferences("trialCounter", MODE_PRIVATE);
        trialCounter = prefs.getInt(KEY_TRIAL_COUNTER, 0);

        if (trialCounter == trialList.size()){
            trialCounter = 1;
        } else {
            trialCounter++;
        }

        prefs.edit().putInt(KEY_TRIAL_COUNTER, trialCounter).apply();
    }

    private void getAttributes(){
        a1 = Double.parseDouble(currentTrial.getAmount1());
        p1 = Double.parseDouble(currentTrial.getProbability1())*100;
        a2 = Double.parseDouble(currentTrial.getAmount2());
        p2 = Double.parseDouble(currentTrial.getProbability2())*100;
        textViewProbability1.setText((int) p1 + "%");
        textViewProbability2.setText((int) p2 + "%");
        textViewDollar1.setText("$" + String.format("%.2f", a1));
        textViewDollar2.setText("$" + String.format("%.2f", a2));
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

        SharedPreferences prefs = getSharedPreferences("totalAmountWon", MODE_PRIVATE);
        totalAmountWon = prefs.getFloat(KEY_TOTAL_AMOUNT, 0);
        totalAmountWon = totalAmountWon + amountWon;
        prefs.edit().putFloat(KEY_TOTAL_AMOUNT, (float)totalAmountWon).apply();

        recordEvent(option+" selected, $"+amountWon+" won; total amount won: $"+totalAmountWon);

        timeRecordDb.close();

        Intent intent = new Intent(QuestionActivityHorizontal.this, ResultActivity.class);
        intent.putExtra("EXTRA_AMOUNT_WON", amountWon);
        startActivity(intent);
        finish();
    }

    private void exchangeA1P1(){
        ViewGroup parent = (ViewGroup) viewAnimatorDollar1.getParent();
        int indexDollar1 = parent.indexOfChild(viewAnimatorDollar1);
        int indexProbability1 = parent.indexOfChild(viewAnimatorProbability1);
        parent.removeView(viewAnimatorDollar1);
        parent.addView(viewAnimatorDollar1, indexProbability1);
        parent.removeView(viewAnimatorProbability1);
        parent.addView(viewAnimatorProbability1, indexDollar1);
    }

    private void exchangeA2P2(){
        ViewGroup parent = (ViewGroup) viewAnimatorDollar2.getParent();
        int indexDollar2 = parent.indexOfChild(viewAnimatorDollar2);
        int indexProbability2 = parent.indexOfChild(viewAnimatorProbability2);
        parent.removeView(viewAnimatorDollar2);
        parent.addView(viewAnimatorDollar2,indexProbability2);
        parent.removeView(viewAnimatorProbability2);
        parent.addView(viewAnimatorProbability2,indexDollar2);
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
