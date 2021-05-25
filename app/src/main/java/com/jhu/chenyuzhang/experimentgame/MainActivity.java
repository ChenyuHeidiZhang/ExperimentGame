package com.jhu.chenyuzhang.experimentgame;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.jhu.chenyuzhang.experimentgame.Questions.Question2Att4OpActivity;
import com.jhu.chenyuzhang.experimentgame.Questions.Question2Att4OpHorizontal;
import com.jhu.chenyuzhang.experimentgame.Questions.Question4Activity;
import com.jhu.chenyuzhang.experimentgame.Questions.Question4ActivityHorizontal;
import com.jhu.chenyuzhang.experimentgame.Questions.Question4Att2OpActivity;
import com.jhu.chenyuzhang.experimentgame.Questions.Question4Att2OpHorizontal;
import com.jhu.chenyuzhang.experimentgame.Questions.QuestionActivity;
import com.jhu.chenyuzhang.experimentgame.Questions.QuestionActivityHorizontal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static boolean isSignedIn;
    private long backPressedTime = 0;
    int survey_stats;
    public static int trialCounter;

    private SharedPreferences prefSignedIn;
    private SharedPreferences counter_prefs;
    private SharedPreferences prefSurvey;

    private static final String KEY_IS_SIGNED_IN = "keyIsSignedIn";
    public static final String KEY_TRIAL_COUNTER = "keyTrialCounter";

    TrialDbHelper trialInfoDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        Button playGame = findViewById(R.id.button_playGame);
        Button signOut = findViewById(R.id.button_signOut);

        prefSurvey = getSharedPreferences("Survey", MODE_PRIVATE);
        prefSignedIn = getSharedPreferences("isSignedIn", MODE_PRIVATE);
        counter_prefs = getSharedPreferences("trialCounter", MODE_PRIVATE);

        isSignedIn = prefSignedIn.getBoolean(KEY_IS_SIGNED_IN, false);

        trialInfoDb = new TrialDbHelper(this);

        playGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                survey_stats = prefSurvey.getInt("Status", 0);
                Intent intent;
                if (survey_stats == 0) {
                    intent = new Intent(MainActivity.this, SurveyOpening.class);
                }
                else if (survey_stats == 2) {
                    intent = new Intent(MainActivity.this, SurveyContinue.class);
                }
                else if (survey_stats == 1){
                    intent = new Intent(MainActivity.this, Survey_opening2.class);

                }
                else if (survey_stats == 3) {
                    intent = new Intent(MainActivity.this, SurveySingle.class);
                }
                else {
                    intent = getNextIntent();
                }
                startActivity(intent);
            }
        });

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPasswordDialog();
            }
        });
    }

    private void incrementTrialCounter() {  // increment trial counter
        if (trialCounter == trialInfoDb.getNumRows()){
            trialCounter = 1;       // wrap around if reaches the end
        } else {
            trialCounter++;
        }

        counter_prefs.edit().putInt(KEY_TRIAL_COUNTER, trialCounter).apply();
    }

    private Trial getNextTrial() {
        SharedPreferences counter_prefs = getSharedPreferences("trialCounter", MODE_PRIVATE);
        trialCounter = counter_prefs.getInt(KEY_TRIAL_COUNTER, 1);
        return trialInfoDb.getTrial(trialCounter);
    }

    private Intent getNextIntent() {
        Intent intent;

        Trial currentTrial = getNextTrial();
        if (currentTrial.getOrient().equals("0")) {  // 0: Horizontal, 1: Vertical
            if (currentTrial.getType().equals("1")) {  // 2Opt2Attr
                intent = new Intent(MainActivity.this, QuestionActivityHorizontal.class);
            } else if (currentTrial.getType().equals("2")) {  // 2Opt4Attr
                intent = new Intent(MainActivity.this, Question4Att2OpHorizontal.class);
            } else if (currentTrial.getType().equals("3")) {  // 4Opt2Attr
                intent = new Intent(MainActivity.this, Question2Att4OpHorizontal.class);
            } else {  // 4Opt4Attr
                intent = new Intent(MainActivity.this, Question4ActivityHorizontal.class);
            }
        } else {
            if (currentTrial.getType().equals("1")) {  // 2Opt2Attr
                intent = new Intent(MainActivity.this, QuestionActivity.class);
            } else if (currentTrial.getType().equals("2")) {  // 2Opt4Attr
                intent = new Intent(MainActivity.this, Question4Att2OpActivity.class);
            } else if (currentTrial.getType().equals("3")) {  // 4Opt2Attr
                intent = new Intent(MainActivity.this, Question2Att4OpActivity.class);
            } else {  // 4Opt4Attr
                intent = new Intent(MainActivity.this, Question4Activity.class);
            }
        }

        return intent;
    }

    public void checkPasswordDialog() {
            LayoutInflater li = LayoutInflater.from(MainActivity.this);
            View promptsView = li.inflate(R.layout.signout_prompt, null);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
            alertDialogBuilder.setView(promptsView);

            final EditText passwordInput = promptsView.findViewById(R.id.edit_text_password_input);

            alertDialogBuilder
                    .setCancelable(true)
                    .setPositiveButton("Sign Out", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (passwordInput.getText().toString().equals(getString(R.string.password))) {
                                isSignedIn = false;
                                prefSignedIn.edit().putBoolean(KEY_IS_SIGNED_IN, isSignedIn).apply();

                                // display final total when logging out
                                Intent intent_total = new Intent(MainActivity.this, TotalAmountActivity.class);
                                intent_total.putExtra("EXTRA_DISPLAY_ID", 0);  // 0 means to display overall total
                                startActivity(intent_total);
                                finish();
                            } else {
                                Toast.makeText(MainActivity.this, "Wrong password", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public static String getCurrentTime() {
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("dd:HH:mm:ss:SSS");
        String formattedDate= dateFormat.format(date);
        return formattedDate;
    }

    @Override
    public void onBackPressed() {
        // Finish the app if the user back presses twice within 2 seconds.
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            finish();
        } else {
            Toast.makeText(MainActivity.this, "Press back again to exit the app", Toast.LENGTH_SHORT).show();
        }

        backPressedTime = System.currentTimeMillis();
    }
}
