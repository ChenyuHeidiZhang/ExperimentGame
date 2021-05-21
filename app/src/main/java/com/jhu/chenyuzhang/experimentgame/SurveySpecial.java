package com.jhu.chenyuzhang.experimentgame;

import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Text;

import static com.jhu.chenyuzhang.experimentgame.MainActivity.getCurrentTime;

public class SurveySpecial extends AppCompatActivity {
    EditText a11;
    EditText a12;
    EditText a13;
    EditText a14;
    EditText a21;
    EditText a22;
    EditText a23;
    EditText a24;
    EditText a31;
    EditText a32;
    Button next;
    TextView q1;
    TextView q11;
    TextView q12;
    TextView q13;
    TextView q14;
    TextView q2;
    TextView q21;
    TextView q22;
    TextView q23;
    TextView q24;
    TextView q3;
    TextView q31;
    TextView q32;
    TimeDbHelper timeRecordDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_special);
        timeRecordDb = new TimeDbHelper(this);

        a11 = findViewById(R.id.As11);
        a12 = findViewById(R.id.As12);
        a13 = findViewById(R.id.As13);
        a14 = findViewById(R.id.As14);
        q11 = findViewById(R.id.Qs11);
        q12 = findViewById(R.id.Qs12);
        q13 = findViewById(R.id.Qs13);
        q14 = findViewById(R.id.Qs14);

        a21 = findViewById(R.id.As21);
        a22 = findViewById(R.id.As22);
        a23 = findViewById(R.id.As23);
        a24 = findViewById(R.id.As24);
        q21 = findViewById(R.id.Qs21);
        q22 = findViewById(R.id.Qs22);
        q23 = findViewById(R.id.Qs23);
        q24 = findViewById(R.id.Qs24);

        a31 = findViewById(R.id.As31);
        a32 = findViewById(R.id.As32);
        q31 = findViewById(R.id.Qs31);
        q32 = findViewById(R.id.Qs32);

        q1 = findViewById(R.id.Qs1);
        q2 = findViewById(R.id.Qs2);
        q3 = findViewById(R.id.Qs3);


        next = findViewById(R.id.Next4);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (allFilled()) {
                    recordEvent(q1.getText().toString());
                    recordEvent(q11.getText().toString());
                    recordEvent(a11.getText().toString());
                    recordEvent(q12.getText().toString());
                    recordEvent(a12.getText().toString());
                    recordEvent(q13.getText().toString());
                    recordEvent(a13.getText().toString());
                    recordEvent(q14.getText().toString());
                    recordEvent(a14.getText().toString());

                    recordEvent(q2.getText().toString());
                    recordEvent(q21.getText().toString());
                    recordEvent(a21.getText().toString());
                    recordEvent(q22.getText().toString());
                    recordEvent(a22.getText().toString());
                    recordEvent(q23.getText().toString());
                    recordEvent(a23.getText().toString());
                    recordEvent(q24.getText().toString());
                    recordEvent(a24.getText().toString());

                    recordEvent(q3.getText().toString());
                    recordEvent(q31.getText().toString());
                    recordEvent(a31.getText().toString());
                    recordEvent(q32.getText().toString());
                    recordEvent(a32.getText().toString());
                    Intent intent = new Intent(SurveySpecial.this, SurveyLast.class);
                    startActivity(intent);
                    timeRecordDb.close();
                    finish();
                }
            }
        });
    }

    private boolean allFilled() {
        if (a11.getText().toString().equals("") || a12.getText().toString().equals("") ||
                a13.getText().toString().equals("") || a14.getText().toString().equals("")) {
            Toast.makeText(SurveySpecial.this, "Please answer all the sub-questions from the first question before going to the next page", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (a21.getText().toString().equals("") || a22.getText().toString().equals("") ||
        a23.getText().toString().equals("") || a24.getText().toString().equals("")) {
            Toast.makeText(SurveySpecial.this, "Please answer all the sub-questions from the second question before going to the next page", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (a31.getText().toString().equals("") || a32.getText().toString().equals("")) {
            Toast.makeText(SurveySpecial.this, "Please answer all the sub-questions from the third question before going to the next page", Toast.LENGTH_SHORT).show();
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