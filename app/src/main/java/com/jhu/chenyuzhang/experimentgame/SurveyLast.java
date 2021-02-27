package com.jhu.chenyuzhang.experimentgame;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import static com.jhu.chenyuzhang.experimentgame.MainActivity.getCurrentTime;

public class SurveyLast extends AppCompatActivity {
    Button next;
    EditText text;
    TimeDbHelper timeRecordDb;
    private long backPressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_last);
        timeRecordDb = new TimeDbHelper(this);

        next = findViewById(R.id.Next6);
        text = findViewById(R.id.Alast);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordEvent(text.getText().toString());
                Intent intent = new Intent(SurveyLast.this, LoginActivity.class);
                startActivity(intent);
                timeRecordDb.close();
                finish();
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