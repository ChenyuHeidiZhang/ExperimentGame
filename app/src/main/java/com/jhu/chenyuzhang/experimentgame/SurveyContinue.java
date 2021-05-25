package com.jhu.chenyuzhang.experimentgame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import static com.jhu.chenyuzhang.experimentgame.MainActivity.getCurrentTime;
import java.util.ArrayList;
import java.util.List;

public class SurveyContinue extends AppCompatActivity {
    int count = 0;

    EditText a1;
    EditText a2;
    EditText a3;
    EditText a4;
    EditText a5;

    TextView q1;
    TextView q2;
    TextView q3;
    TextView q4;
    TextView q5;
    TextView instruct;

    String Q1;
    String Q2;
    String Q3;
    String Q4;
    String Q5;

    Button next;

    List<List<String>> list = new ArrayList<>();

    private SharedPreferences prefSurvey;
    public static final String KEY_USER = "keyUser";

    private long backPressedTime;
    private DatabaseReference userContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_continue);

        final String[] questions = getResources().getStringArray(R.array.SurveyQs);

        prefSurvey = getSharedPreferences("Survey", MODE_PRIVATE);
        SharedPreferences prefUserName = getSharedPreferences("user", MODE_PRIVATE);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userName = prefUserName.getString(KEY_USER, "");
        userContent = FirebaseDatabase.getInstance().getReference().child("users").child(userName).child("survey");


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

        instruct.setText(getResources().getString(R.string.instruct1));
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
                if (allFilled()) {
                    if (count >= 21) {
                        instruct.setText(getResources().getString(R.string.instruct2));
                    }
                    storeData(Q1, a1.getText().toString());
                    a1.setText("");

                    storeData(Q2, a2.getText().toString());
                    a2.setText("");

                    storeData(Q3, a3.getText().toString());
                    a3.setText("");

                    storeData(Q4, a4.getText().toString());
                    a4.setText("");

                    storeData(Q5, a5.getText().toString());
                    a5.setText("");

                    userContent.child(getCurrentTime()).setValue(list);

                    if (count == 30) {
                        prefSurvey.edit().putInt("Status", 3).apply();
                        Intent intent = new Intent(SurveyContinue.this, SurveySingle.class);
                        startActivity(intent);
                        finish();
                    } else {
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
                    ScrollView scrollView = findViewById(R.id.Scroll_continue);
                    scrollView.scrollTo(0, 0);
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
        if (a1.getText().toString().equals("")) {
            Toast.makeText(SurveyContinue.this, "Please answer the first question before going to the next page", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (a2.getText().toString().equals("")) {
            Toast.makeText(SurveyContinue.this, "Please answer the second question before going to the next page", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (a3.getText().toString().equals("")) {
            Toast.makeText(SurveyContinue.this, "Please answer the third question before going to the next page", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (a4.getText().toString().equals("")) {
            Toast.makeText(SurveyContinue.this, "Please answer the fourth question before going to the next page", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (a5.getText().toString().equals("")) {
            Toast.makeText(SurveyContinue.this, "Please answer the fifth question before going to the next page", Toast.LENGTH_SHORT).show();
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
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            //timeRecordDb.close();
            finish();
        } else {
            Toast.makeText(this, "Press back again to finish", Toast.LENGTH_SHORT).show();
        }

        backPressedTime = System.currentTimeMillis();
    }
}