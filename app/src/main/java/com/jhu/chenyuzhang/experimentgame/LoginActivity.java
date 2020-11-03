package com.jhu.chenyuzhang.experimentgame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.Normalizer2;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextName;
    private EditText editTextKey;
    private EditText editTextNotes;
    private Button buttonSignIn;
    private String name;
    private String key;

    private boolean isSignedIn;
    private SharedPreferences prefSignedIn;
    private SharedPreferences user_name;
    private SharedPreferences hasStarted;
    private static final String KEY_IS_SIGNED_IN = "keyIsSignedIn";

    private static final String KEY_DO_DEMO = "keyDoDemo";

    private static final String KEY_TRIAL_COUNTER = "keyTrialCounter";

    private static final String KEY_TOTAL_AMOUNT = "keyTotalAmount";
    public static final String KEY_LAST_TOTAL = "keyLastTotal";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // hide the status bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_login);

        user_name = getSharedPreferences("userName", MODE_PRIVATE);
        prefSignedIn = getSharedPreferences("isSignedIn", MODE_PRIVATE);
        hasStarted = getSharedPreferences("started", MODE_PRIVATE);
        isSignedIn = prefSignedIn.getBoolean(KEY_IS_SIGNED_IN, false);

        if (isSignedIn) {
            goToMainActivity();
        } else {
            hasStarted.edit().putBoolean("record_signin", false).apply();
            editTextName = findViewById(R.id.edit_text_name);
            editTextKey = findViewById(R.id.edit_text_key);
            editTextNotes = findViewById(R.id.edit_text_notes);
            buttonSignIn = findViewById(R.id.button_signIn);

            buttonSignIn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    name = editTextName.getText().toString();
                    user_name.edit().putString("name", name).apply();
                    // Check that if the patient id contains special characters, then it is invalid.
                    Pattern pattern = Pattern.compile("[^A-Za-z0-9_]");
                    Matcher matcher = pattern.matcher(name);
                    boolean nameInvalid = matcher.find();

                    key = editTextKey.getText().toString();
                    if (name.equals("") || key.equals("")) {
                        Toast.makeText(LoginActivity.this, "Please enter patient ID and password", Toast.LENGTH_SHORT).show();
                    } else if (nameInvalid) {
                        Toast.makeText(LoginActivity.this, "Invalid patient ID (use only letters, numbers, and underscore(_))", Toast.LENGTH_LONG).show();
                    } else if (!key.equals(getString(R.string.password))) {
                        Toast.makeText(LoginActivity.this, "Wrong password", Toast.LENGTH_SHORT).show();
                    } else {
                        signIn();
                    }
                }
            });
        }
    }

    private void signIn() {
        isSignedIn = true;
        prefSignedIn.edit().putBoolean(KEY_IS_SIGNED_IN, isSignedIn).apply();

        SharedPreferences prefDoDemo = getSharedPreferences("doDemo", MODE_PRIVATE);
        prefDoDemo.edit().putBoolean(KEY_DO_DEMO, true).apply();

        SharedPreferences prefTrialCounter = getSharedPreferences("trialCounter", MODE_PRIVATE);
        prefTrialCounter.edit().putInt(KEY_TRIAL_COUNTER, 1).apply();

        SharedPreferences prefTotalAmount = getSharedPreferences("totalAmountWon", MODE_PRIVATE);
        prefTotalAmount.edit().putFloat(KEY_TOTAL_AMOUNT, 0).apply();

        SharedPreferences prefLastAmount = getSharedPreferences("lastTotal", MODE_PRIVATE);
        prefLastAmount.edit().putFloat(KEY_LAST_TOTAL, 0).apply();  // total amount 4 blocks ago

        String tableName = "timeRecord_table_" + name;

        TimeDbHelper timeRecordDb = new TimeDbHelper(LoginActivity.this);
        timeRecordDb.createTableIfNotExists(tableName);

        String startTimeWorld = getCurrentTime();
        //store the signin time
        prefSignedIn.edit().putString("startTime", startTimeWorld).apply();
        //store the signin date
        prefSignedIn.edit().putString("startDate", getCurrentDate()).apply();

        String notes = editTextNotes.getText().toString();
        timeRecordDb.insertData(startTimeWorld, "Sign In: Patient ID: " + name + ", Password: " + key + ", Notes: " + notes);

        timeRecordDb.close();

        goToMainActivity();
    }

    private void goToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    //get current time in milliseconds
    private String getCurrentTime() {
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("dd:HH:mm:ss:SSS");
        String formattedDate= dateFormat.format(date);
        return formattedDate;
    }

    private String getCurrentDate() {
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd:G:HH:mm:ss");
        String formattedDate = dateFormat.format(date);
        return formattedDate;
    }
}
