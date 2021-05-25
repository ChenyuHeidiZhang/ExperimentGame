package com.jhu.chenyuzhang.experimentgame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static com.jhu.chenyuzhang.experimentgame.MainActivity.getCurrentTime;

public class SurveyLast extends AppCompatActivity {
    Button next;
    EditText text;
    private long backPressedTime;

    public static final String KEY_USER = "keyUser";
    private DatabaseReference userContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_last);

        next = findViewById(R.id.Next6);
        text = findViewById(R.id.Alast);

        SharedPreferences prefUserName = getSharedPreferences("user", MODE_PRIVATE);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userName = prefUserName.getString(KEY_USER, "");
        userContent = FirebaseDatabase.getInstance().getReference().child("users").child(userName).child("survey");

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userContent.child(getCurrentTime()).setValue(text.getText().toString());
                Intent intent = new Intent(SurveyLast.this, LoginActivity.class);
                startActivity(intent);
                //timeRecordDb.close();
                finish();
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