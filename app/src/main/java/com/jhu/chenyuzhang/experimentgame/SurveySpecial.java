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
import java.util.ArrayList;
import java.util.List;
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

    public static final String KEY_USER = "keyUser";
    private DatabaseReference userContent;

    List<List<String>> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_special);

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

        SharedPreferences prefUserName = getSharedPreferences("user", MODE_PRIVATE);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userName = prefUserName.getString(KEY_USER, "");
        userContent = FirebaseDatabase.getInstance().getReference().child("users").child(userName).child("survey");

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (allFilled()) {
                    storeData(q11.getText().toString(),a11.getText().toString());
                    storeData(q12.getText().toString(),a12.getText().toString());
                    storeData(q13.getText().toString(),a13.getText().toString());
                    storeData(q14.getText().toString(),a14.getText().toString());

                    storeData(q21.getText().toString(),a21.getText().toString());
                    storeData(q22.getText().toString(),a22.getText().toString());
                    storeData(q23.getText().toString(),a23.getText().toString());
                    storeData(q24.getText().toString(),a24.getText().toString());

                    storeData(q31.getText().toString(),a31.getText().toString());
                    storeData(q32.getText().toString(),a32.getText().toString());

                    userContent.child(getCurrentTime()).setValue(list);

                    Intent intent = new Intent(SurveySpecial.this, SurveyLast.class);
                    startActivity(intent);
                    //timeRecordDb.close();
                    finish();
                }
            }
        });
    }

    private void storeData(String s1, String s2) {
        List<String> temp = new ArrayList<>();
        temp.add(s1);
        temp.add(s2);
        list.add(temp);
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
    }
}