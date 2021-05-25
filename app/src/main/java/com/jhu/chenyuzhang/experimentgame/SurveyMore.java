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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import static com.jhu.chenyuzhang.experimentgame.MainActivity.getCurrentTime;
import java.util.ArrayList;
import java.util.List;

public class SurveyMore extends AppCompatActivity {
    int count = 0;

    EditText a1;
    EditText a2;
    EditText a3;

    TextView q1;
    TextView q2;
    TextView q3;

    String Q1;
    String Q2;
    String Q3;

    Button next;

    public static final String KEY_USER = "keyUser";
    private DatabaseReference userContent;

    List<List<String>> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_more);
        final String[] questions = getResources().getStringArray(R.array.longquestions);

        SharedPreferences prefUserName = getSharedPreferences("user", MODE_PRIVATE);

        String userName = prefUserName.getString(KEY_USER, "");
        userContent = FirebaseDatabase.getInstance().getReference().child("users").child(userName).child("survey");

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
                    storeData(Q1, a1.getText().toString());
                    a1.setText("");

                    storeData(Q2, a2.getText().toString());
                    a2.setText("");

                    storeData(Q3, a3.getText().toString());
                    userContent.child(getCurrentTime()).setValue(list);

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

    private void storeData(String s1, String s2) {
        List<String> temp = new ArrayList<>();
        temp.add(s1);
        temp.add(s2);
        list.add(temp);
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

    @Override
    public void onBackPressed() {
    }

}