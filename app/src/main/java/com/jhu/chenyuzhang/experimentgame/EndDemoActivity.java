package com.jhu.chenyuzhang.experimentgame;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.jhu.chenyuzhang.experimentgame.Questions.QuestionActivity;
import com.jhu.chenyuzhang.experimentgame.Questions.QuestionActivityHorizontal;

import java.util.Random;

public class EndDemoActivity extends AppCompatActivity {

    private Button buttonNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // hide the status bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_end_demo);

        buttonNext = findViewById(R.id.button_next);

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int random = new Random().nextInt(2);   // generate random integer between 0 (inclusive) and 2 (exclusive)
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
