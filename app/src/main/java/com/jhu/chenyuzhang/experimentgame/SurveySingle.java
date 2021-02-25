package com.jhu.chenyuzhang.experimentgame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import static com.jhu.chenyuzhang.experimentgame.MainActivity.getCurrentTime;

public class SurveySingle extends AppCompatActivity {
    TimeDbHelper timeRecordDb;
    private SharedPreferences prefSurvey;
    private long backPressedTime;
    Button next;
    TextView Q1;
    EditText a1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_single);

        timeRecordDb = new TimeDbHelper(this);
        next = findViewById(R.id.Next6);
        Q1 = findViewById(R.id.single_q1);
        a1 = findViewById(R.id.single_a1);
        prefSurvey = getSharedPreferences("Survey", MODE_PRIVATE);


        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!a1.getText().toString().equals("")) {
                    prefSurvey.edit().putInt("Status", 4).apply();
                    recordEvent(Q1.getText().toString());
                    recordEvent(a1.getText().toString());
                    Intent intent = new Intent(SurveySingle.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                else {
                    Toast.makeText(SurveySingle.this, "Please answer this question before you proceed", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private String recordEvent(String event) {
        String timeString = getCurrentTime();

        if (!timeRecordDb.insertData(timeString, event)) {
            Toast.makeText(getApplicationContext(), "Something goes wrong with database", Toast.LENGTH_LONG).show();
            timeRecordDb.close();
            finish();
        }
        return timeString;
    }

    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            timeRecordDb.close();
            finish();
        } else {
            Toast.makeText(this, "Press back again to finish", Toast.LENGTH_SHORT).show();
        }

        backPressedTime = System.currentTimeMillis();
    }
}