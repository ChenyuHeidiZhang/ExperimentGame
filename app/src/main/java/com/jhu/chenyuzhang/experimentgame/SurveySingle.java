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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import static com.jhu.chenyuzhang.experimentgame.MainActivity.getCurrentTime;
import java.util.ArrayList;
import java.util.List;

public class SurveySingle extends AppCompatActivity {
    private SharedPreferences prefSurvey;
    public static final String KEY_USER = "keyUser";
    private DatabaseReference userContent;

    private long backPressedTime;
    Button next;
    TextView Q1;
    EditText a1;

    List<String> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_single);

        next = findViewById(R.id.Next6);
        Q1 = findViewById(R.id.single_q1);
        a1 = findViewById(R.id.single_a1);

        prefSurvey = getSharedPreferences("Survey", MODE_PRIVATE);
        SharedPreferences prefUserName = getSharedPreferences("user", MODE_PRIVATE);

        String userName = prefUserName.getString(KEY_USER, "");
        userContent = FirebaseDatabase.getInstance().getReference().child("users").child(userName).child("survey");

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!a1.getText().toString().equals("")) {
                    prefSurvey.edit().putInt("Status", 4).apply();
                    list.add(0, Q1.getText().toString());
                    list.add(1, a1.getText().toString());
                    userContent.child(getCurrentTime()).setValue(list);

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