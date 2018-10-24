package com.example.chenyuzhang.experimentgame;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private Button playGame;
    private Button quit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playGame = findViewById(R.id.button_playGame);
        quit = findViewById(R.id.button_quit);
        
        playGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startQuiz();
            }
        });
    }

    private void startQuiz() {
        Intent intent = new Intent(MainActivity.this, QuestionActivity.class);
        startActivity(intent);
    }


}
