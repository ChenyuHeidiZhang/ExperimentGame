package com.jhu.chenyuzhang.experimentgame;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import static com.jhu.chenyuzhang.experimentgame.MainActivity.getCurrentTime;

public class SurveyMore extends AppCompatActivity {
    TimeDbHelper timeRecordDb;
    int count = 0;
    EditText a1;
    EditText a2;
    EditText a3;
    TextView q1;
    String Q1;
    TextView q2;
    String Q2;
    TextView q3;
    String Q3;
    Button next;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_more);
        final String[] questions = getResources().getStringArray(R.array.longquestions);
        timeRecordDb = new TimeDbHelper(this);

        q1 = findViewById(R.id.Ql1);
        q2 = findViewById(R.id.Ql2);
        q3 = findViewById(R.id.Ql3);
        a1 = findViewById(R.id.Al1);
        a2 = findViewById(R.id.Al2);
        a3 = findViewById(R.id.Al3);
        next = findViewById(R.id.Next4);

        Q1 = questions[count];
        q1.setText(questions[count++]);
        Q2 = questions[count];
        q2.setText(questions[count++]);
        Q3 = questions[count];
        q3.setText(questions[count++]);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (allFilled()) {
                    recordEvent(Q1);
                    recordEvent(a1.getText().toString());
                    a1.setText("");
                    recordEvent(Q2);
                    recordEvent(a2.getText().toString());
                    a2.setText("");
                    recordEvent(Q3);
                    recordEvent(a3.getText().toString());
                    //a3.setText("");
                    if (count == 6) {
                        Intent intent = new Intent(SurveyMore.this, SurveySpecial.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Q1 = questions[count];
                        q1.setText(questions[count++]);
                        Q2 = questions[count];
                        q2.setText(questions[count++]);
                        Q3 = questions[count];
                        q3.setText(questions[count++]);
                        a3.setVisibility(View.INVISIBLE);
                    }
                    ScrollView scrollView = findViewById(R.id.more);
                    scrollView.scrollTo(0, 0);
                }
            }
        });

    }
    private boolean allFilled() {
        if (a1.getText().toString().equals("")) {
            Toast.makeText(SurveyMore.this, "Please answer the first question before going to the next page", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (a2.getText().toString().equals("")) {
            Toast.makeText(SurveyMore.this, "Please answer the second question before going to the next page", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (a3.getText().toString().equals("")) {
            Toast.makeText(SurveyMore.this, "Please answer the third question before going to the next page", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
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
    }

}