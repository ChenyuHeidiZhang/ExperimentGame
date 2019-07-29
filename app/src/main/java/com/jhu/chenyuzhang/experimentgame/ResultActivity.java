package com.jhu.chenyuzhang.experimentgame;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.Random;

public class ResultActivity extends AppCompatActivity {
    private double amountWon;
    private ImageView imageViewCongrats;
    private TextView textViewSorry;
    private TextView textViewAmount;
    private Button buttonNextTrial;

    TimeDbHelper timeRecordDb;
    Bluetooth bluetooth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        amountWon = getIntent().getDoubleExtra("EXTRA_AMOUNT_WON", 0);

        imageViewCongrats = findViewById(R.id.image_view_congrats);
        textViewSorry = findViewById(R.id.text_view_sorry);
        textViewAmount = findViewById(R.id.text_view_result_amount);
        buttonNextTrial = findViewById(R.id.button_next_trial);

        timeRecordDb = new TimeDbHelper(this);
        bluetooth = new Bluetooth(timeRecordDb);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    // send identifier and timestamp
                    bluetooth.timeStamper( "14", MainActivity.getCurrentTime());
                    bluetooth.sendData(String.format ("%.2f",amountWon));
                } catch (IOException e) {}
                buttonNextTrial.setVisibility(View.VISIBLE);
                if (amountWon == 0) {
                    imageViewCongrats.setVisibility(View.GONE);
                    textViewSorry.setVisibility(View.VISIBLE);
                    textViewAmount.setText("You didn't win any money.");
                } else {
                    imageViewCongrats.setVisibility(View.VISIBLE);
                    textViewSorry.setVisibility(View.GONE);
                    textViewAmount.setText("You won $"+String.format("%.2f",amountWon)+"!");
                }
            }
        }, 1000);

        buttonNextTrial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int random = new Random().nextInt(2);
                Intent intent;
                if (random==0) {
                    intent = new Intent(ResultActivity.this, QuestionActivity.class);
                } else {
                    intent = new Intent(ResultActivity.this, QuestionActivityHorizontal.class);
                }
                startActivity(intent);
                finish();
            }
        });
    }
}
