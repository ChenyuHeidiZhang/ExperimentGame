package com.example.chenyuzhang.experimentgame;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.wajahatkarim3.easyflipview.EasyFlipView;

public class QuestionActivity extends AppCompatActivity {
    private ImageView imageViewDollar1;
    private TextView textViewDollar1;
    private ImageView imageViewProbability1;
    private TextView textViewProbability1;
    private ImageView imageViewDollar2;
    private TextView textViewDollar2;
    private ImageView imageViewProbability2;
    private TextView textViewProbability2;
    private TextView textViewTime;

    private EasyFlipView flip_view_dollar1;
    private EasyFlipView flip_view_dollar2;
    private EasyFlipView flip_view_probability1;
    private EasyFlipView flip_view_probability2;

    private long startTime;
    private long timeSpan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        imageViewDollar1 = findViewById(R.id.image_view_dollar1);
        textViewDollar1 = findViewById(R.id.text_view_dollar1);
        imageViewDollar2 = findViewById(R.id.image_view_dollar2);
        textViewDollar2 = findViewById(R.id.text_view_dollar2);
        imageViewProbability1 = findViewById(R.id.image_view_probability1);
        textViewProbability1 = findViewById(R.id.text_view_probability1);
        imageViewProbability2 = findViewById(R.id.image_view_probability2);
        textViewProbability2 = findViewById(R.id.text_view_probability2);

        startTime = System.nanoTime();

        /* textViewDollar1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textViewTime.setText("");
            }
        }); */
        /* flip_view_dollar1.setOnFlipListener(new EasyFlipView.OnFlipAnimationListener() {
            @Override
            public void onViewFlipCompleted(EasyFlipView easyFlipView, EasyFlipView.FlipState newCurrentSide) {
                // recordTime(easyFlipView);
                // if it has been more than 2 seconds, flip back
            }
        }); */
    }

    private void recordTime(View v) {
        long estimatedTime = System.nanoTime() - startTime;
        textViewTime.setText(Long.toString(estimatedTime));
    }
}
