package com.jhu.chenyuzhang.experimentgame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SurveySingle extends AppCompatActivity {
    private SharedPreferences prefSurvey;
    public static final String KEY_USER = "keyUser";
    private DatabaseReference userContent;

    private long backPressedTime;
    Button next;
    TextView Q1;
    EditText a1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_single);

        next = findViewById(R.id.Next6);
        Q1 = findViewById(R.id.single_q1);
        a1 = findViewById(R.id.single_a1);

        prefSurvey = getSharedPreferences("Survey", MODE_PRIVATE);
        SharedPreferences prefUserName = getSharedPreferences("user", MODE_PRIVATE);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userName = prefUserName.getString(KEY_USER, "");
        userContent = FirebaseDatabase.getInstance().getReference().child("users").child(userName).child("survey");

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!a1.getText().toString().equals("")) {
                    prefSurvey.edit().putInt("Status", 4).apply();
                    userContent.child(Q1.getText().toString()).setValue(a1.getText().toString());
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

    /*
    private String recordEvent(String event) {
        String timeString = getCurrentTime();

        if (!timeRecordDb.insertData(timeString, event)) {
            Toast.makeText(getApplicationContext(), "Something goes wrong with database", Toast.LENGTH_LONG).show();
            timeRecordDb.close();
            finish();
        }
        return timeString;
    }

     */

    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            //timeRecordDb.close();
            finish();
        } else {
            Toast.makeText(this, "Press back again to finish", Toast.LENGTH_SHORT).show();
        }

        backPressedTime = System.currentTimeMillis();
    }
}