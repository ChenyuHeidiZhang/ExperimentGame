package com.jhu.chenyuzhang.experimentgame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Random;

public class QuestionActivity extends AppCompatActivity {
    public static double totalAmountWon;
    public static final String KEY_TOTAL_AMOUNT = "keyTotalAmount";

    private CountDownTimer countDownTimer;

    private TrialDbHelper trialInfoDb;
    private ArrayList<Trial> trialList;
    private Trial currentTrial;
    public static int trialCounter;
    public static final String KEY_TRIAL_COUNTER = "keyTrialCounter";
    private double amountWon;
    private double p1;
    private double p2;
    private double a1;
    private double a2;

    private TimeDbHelper timeRecordDb;

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

    //private LocalTime startTimeWorld;
    private String startTimeWorld;
    private long startTime;

    private String eventClick = "Clicked, Displayed";
    private String eventTimeOut = "TimeOut, Covered";

    private int random_position = new Random().nextInt(4);

    private long backPressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

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
        //startTimeWorld = LocalTime.now();  --API 26, nanoseconds
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
            position = "P1A1,A2P2";
            exchangeA1P1();
        } else if(random_position==2){
            position = "A1P1,P2A2";
            exchangeA2P2();
        } else if(random_position==3){
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
        timeRecordDb.insertData(startTimeWorld, "startTrial" + trialCounter + "; Option1(Blue): A1=" + a1 + " P1=" + p1 + ", Option2(Green): A2=" + a2 + " P2=" + p2 + "; Orientation: vertical" + position);

        viewAnimatorDollar1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (viewAnimatorDollar1.getDisplayedChild() == 0) {
                    viewAnimatorDollar1.showNext();
                    recordEvent("A1 "+eventClick);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (viewAnimatorDollar1.getDisplayedChild() == 1) {
                                viewAnimatorDollar1.showNext();
                                recordEvent("A1 "+eventTimeOut);
                            }
                        }
                    }, 1000);

                    if (viewAnimatorDollar2.getDisplayedChild() == 1) {
                        viewAnimatorDollar2.showNext();
                    } else if (viewAnimatorProbability1.getDisplayedChild() == 1) {
                        viewAnimatorProbability1.showNext();
                    } else if (viewAnimatorProbability2.getDisplayedChild() == 1) {
                        viewAnimatorProbability2.showNext();
                    }
                }

                countDownTimer.cancel();
                countDownTimer.start();
            }
        });

        viewAnimatorDollar2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (viewAnimatorDollar2.getDisplayedChild() == 0) {
                    viewAnimatorDollar2.showNext();
                    recordEvent("A2 " + eventClick);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (viewAnimatorDollar2.getDisplayedChild() == 1) {
                                viewAnimatorDollar2.showNext();
                                recordEvent("A2 " + eventTimeOut);
                            }
                        }
                    }, 1000);

                    if (viewAnimatorDollar1.getDisplayedChild() == 1) {
                        viewAnimatorDollar1.showNext();
                    } else if (viewAnimatorProbability1.getDisplayedChild() == 1) {
                        viewAnimatorProbability1.showNext();
                    } else if (viewAnimatorProbability2.getDisplayedChild() == 1) {
                        viewAnimatorProbability2.showNext();
                    }
                }

                countDownTimer.cancel();
                countDownTimer.start();
            }
        });

        viewAnimatorProbability1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (viewAnimatorProbability1.getDisplayedChild() == 0) {
                    viewAnimatorProbability1.showNext();
                    recordEvent("P1 " + eventClick);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (viewAnimatorProbability1.getDisplayedChild() == 1) {
                                viewAnimatorProbability1.showNext();
                                recordEvent("P1 " + eventTimeOut);
                            }
                        }
                    }, 1000);

                    if (viewAnimatorDollar1.getDisplayedChild() == 1) {
                        viewAnimatorDollar1.showNext();
                    } else if (viewAnimatorDollar2.getDisplayedChild() == 1) {
                        viewAnimatorDollar2.showNext();
                    } else if (viewAnimatorProbability2.getDisplayedChild() == 1) {
                        viewAnimatorProbability2.showNext();
                    }
                }

                countDownTimer.cancel();
                countDownTimer.start();
            }
        });

        viewAnimatorProbability2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (viewAnimatorProbability2.getDisplayedChild() == 0) {
                    viewAnimatorProbability2.showNext();
                    recordEvent("P2 " + eventClick);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (viewAnimatorProbability2.getDisplayedChild() == 1) {
                                viewAnimatorProbability2.showNext();
                                recordEvent("P2 " + eventTimeOut);
                            }
                        }
                    }, 1000);

                    if (viewAnimatorDollar1.getDisplayedChild() == 1) {
                        viewAnimatorDollar1.showNext();
                    } else if (viewAnimatorDollar2.getDisplayedChild() == 1) {
                        viewAnimatorDollar2.showNext();
                    } else if (viewAnimatorProbability1.getDisplayedChild() == 1) {
                        viewAnimatorProbability1.showNext();
                    }
                }

                countDownTimer.cancel();
                countDownTimer.start();
            }
        });

        buttonSelect1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View V) {
                showResult(p1, a1,"Option1");
            }
        });

        buttonSelect2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View V) {
                showResult(p2, a2,"Option2");
            }
        });

        //textViewTest.setText(trialCounter+"; "+totalAmountWon);
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
        DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss.SSS");
        String formattedDate= dateFormat.format(date);
        return formattedDate;
    }

    private void recordEvent(String event) {
        long timeSpan = System.nanoTime() - startTime;
        String timeString = String.format("%d", timeSpan / 1000);

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

        Intent intent = new Intent(QuestionActivity.this, ResultActivity.class);
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
