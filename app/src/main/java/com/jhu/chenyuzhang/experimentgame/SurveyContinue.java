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

public class SurveyContinue extends AppCompatActivity {
    TimeDbHelper timeRecordDb;
    int count = 0;
    EditText a1;
    EditText a2;
    EditText a3;
    EditText a4;
    EditText a5;
    TextView q1;
    String Q1;
    TextView q2;
    String Q2;
    TextView q3;
    String Q3;
    TextView q4;
    String Q4;
    TextView q5;
    String Q5;
    TextView instruct;
    Button next;
    private SharedPreferences prefSurvey;
    private long backPressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_continue);
        final String[] questions = getResources().getStringArray(R.array.SurveyQs);
        timeRecordDb = new TimeDbHelper(this);

        prefSurvey = getSharedPreferences("Survey", MODE_PRIVATE);


        instruct = findViewById(R.id.Survey_instruct);
        q1 = findViewById(R.id.Q1);
        q2 = findViewById(R.id.Q2);
        q3 = findViewById(R.id.Q3);
        q4 = findViewById(R.id.Q4);
        q5 = findViewById(R.id.Q5);
        a1 = findViewById(R.id.A1);
        a2 = findViewById(R.id.A2);
        a3 = findViewById(R.id.A3);
        a4 = findViewById(R.id.A4);
        a5 = findViewById(R.id.A5);
        next = findViewById(R.id.Next3);

        if (count < 21) {
            instruct.setText(getResources().getString(R.string.instruct1));
        }
        else if (count < 30) {
            instruct.setText(getResources().getString(R.string.instruct2));
        }
        Q1 = questions[count];
        q1.setText(questions[count++]);
        Q2 = questions[count];
        q2.setText(questions[count++]);
        Q3 = questions[count];
        q3.setText(questions[count++]);
        Q4 = questions[count];
        q4.setText(questions[count++]);
        Q5 = questions[count];
        q5.setText(questions[count++]);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordEvent(Q1);
                recordEvent(a1.getText().toString());
                a1.setText("");
                recordEvent(Q2);
                recordEvent(a2.getText().toString());
                a2.setText("");
                recordEvent(Q3);
                recordEvent(a3.getText().toString());
                a3.setText("");
                recordEvent(Q4);
                recordEvent(a4.getText().toString());
                a4.setText("");
                recordEvent(Q5);
                recordEvent(a5.getText().toString());
                a5.setText("");
                if (count == 30) {
                    prefSurvey.edit().putInt("Status", 2).apply();
                    Intent intent = new Intent(SurveyContinue.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                else {
                    Q1 = questions[count];
                    q1.setText(questions[count++]);
                    Q2 = questions[count];
                    q2.setText(questions[count++]);
                    Q3 = questions[count];
                    q3.setText(questions[count++]);
                    Q4 = questions[count];
                    q4.setText(questions[count++]);
                    Q5 = questions[count];
                    q5.setText(questions[count++]);

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