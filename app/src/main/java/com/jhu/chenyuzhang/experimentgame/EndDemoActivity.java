package com.jhu.chenyuzhang.experimentgame;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.Random;

public class EndDemoActivity extends AppCompatActivity {

    private Button buttonNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_demo);

        buttonNext = findViewById(R.id.button_next);

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int random = new Random().nextInt(2);
                Intent intent;
                if (random==0) {
                    intent = new Intent(EndDemoActivity.this, QuestionActivity.class);
                } else {
                    intent = new Intent(EndDemoActivity.this, QuestionActivityHorizontal.class);
                }
                startActivity(intent);
                finish();
            }
        });
    }
}