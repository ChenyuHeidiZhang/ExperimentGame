package com.jhu.chenyuzhang.experimentgame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import static com.jhu.chenyuzhang.experimentgame.MainActivity.getCurrentTime;

public class SurveyOpening extends AppCompatActivity {
    TimeDbHelper timeRecordDb;
    EditText date;
    EditText age;
    EditText disease;
    RadioButton languageYes;
    RadioButton languageNo;
    RadioButton handnessLeft;
    RadioButton handnessRight;
    RadioButton colorBlindYes;
    RadioButton colorBlindNo;
    RadioButton visionNormal;
    RadioButton visionContacts;
    RadioButton visionGlasses;
    Button next;
    private SharedPreferences prefSurvey;
    private long backPressedTime;


    Boolean lan = null;
    Boolean handedness = null;
    Boolean color = null;
    int vision = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_opening);
        timeRecordDb = new TimeDbHelper(this);
        date = findViewById(R.id.editTextDate);
        age = findViewById(R.id.editTextNumber);
        disease = findViewById(R.id.disease);
        languageYes = findViewById(R.id.Lan_yes);
        languageNo = findViewById(R.id.Lan_no);
        handnessLeft = findViewById(R.id.Handedness_left);
        handnessRight = findViewById(R.id.Handedness_right);
        colorBlindYes = findViewById(R.id.Color_yes);
        colorBlindNo = findViewById(R.id.Color_no);
        visionNormal = findViewById(R.id.Vision_normal);
        visionContacts = findViewById(R.id.Vision_contacts);
        visionGlasses = findViewById(R.id.Vision_glasses);
        next = findViewById(R.id.Next);
        prefSurvey = getSharedPreferences("Survey", MODE_PRIVATE);

        languageYes.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (lan != null && !lan) {
                    languageNo.setChecked(false);
                }
                lan = true;
                languageYes.setChecked(true);
            }
        });

        languageNo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (lan != null && lan) {
                    languageYes.setChecked(false);
                }
                lan = false;
                languageNo.setChecked(true);
            }
        });

        handnessLeft.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (handedness != null && !handedness) {
                    handnessRight.setChecked(false);
                }
                handedness = true;
                handnessLeft.setChecked(true);
            }
        });

        handnessRight.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(handedness != null && handedness) {
                    handnessLeft.setChecked(false);
                }
                handedness = false;
                handnessRight.setChecked(true);
            }
        });

        colorBlindYes.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(color != null && !color)
                    colorBlindNo.setChecked(false);
                color = true;
                colorBlindYes.setChecked(true);
            }
        });

        colorBlindNo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(color != null && color) {
                    colorBlindYes.setChecked(false);
                }
                color = false;
                colorBlindNo.setChecked(true);
            }
        });

        visionNormal.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (vision != -1 && vision != 1) {
                    visionContacts.setChecked(false);
                    visionGlasses.setChecked(false);
                }
                vision = 1;
                visionNormal.setChecked(true);
            }
        });

        visionContacts.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (vision != -1 && vision != 2) {
                    visionNormal.setChecked(false);
                    visionGlasses.setChecked(false);
                }
                vision = 2;
                visionContacts.setChecked(true);
            }
        });

        visionGlasses.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (vision != -1 && vision != 3) {
                    visionContacts.setChecked(false);
                    visionNormal.setChecked(false);
                }
                vision = 3;
                visionGlasses.setChecked(true);
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordEvent("Date: " + date.getText().toString());
                recordEvent("Age: " + age.getText().toString());
                recordEvent("Disease" + disease.getText().toString());
                if (lan != null && lan) {
                    recordEvent("Native English speaker");
                }
                else if(lan != null) {
                    recordEvent("Not native English speaker");
                }
                if (handedness != null && handedness) {
                    recordEvent("Left handed");
                }
                else if(handedness != null) {
                    recordEvent("right handed");
                }
                if (color != null && color) {
                    recordEvent("Is color blind");
                }
                else if(color != null) {
                    recordEvent("Is not color blind");
                }
                if (vision != -1) {
                    switch (vision) {
                        case 1:
                            recordEvent("Normal vision");
                            break;
                        case 2:
                            recordEvent("Wear contacts");
                            break;
                        case 3:
                            recordEvent("Wear glasses");
                            break;
                    }
                }
                prefSurvey.edit().putInt("Status", 1).apply();
                Intent intent = new Intent(SurveyOpening.this, SurveyContinue.class);
                startActivity(intent);
                finish();
            }
        });

    }

    private String recordEvent(String event) {
        String timeString = getCurrentTime();

        if (!timeRecordDb.insertData(timeString, event)) {
            Toast.makeText(getApplicationContext(), "Something goes wrong with database", Toast.LENGTH_LONG).show();
            timeRecordDb.close();
            finish();
        }
        return timeString;
    }

    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            timeRecordDb.close();
            finish();
        } else {
            Toast.makeText(this, "Press back again to finish", Toast.LENGTH_SHORT).show();
        }

        backPressedTime = System.currentTimeMillis();
    }
}