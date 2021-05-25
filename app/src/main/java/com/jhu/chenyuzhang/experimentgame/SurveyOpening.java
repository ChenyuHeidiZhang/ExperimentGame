package com.jhu.chenyuzhang.experimentgame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.List;

import static com.jhu.chenyuzhang.experimentgame.MainActivity.getCurrentTime;

public class SurveyOpening extends AppCompatActivity {
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
    public static final String KEY_USER = "keyUser";

    private long backPressedTime;
    Boolean lan = null;
    Boolean handedness = null;
    Boolean color = null;
    int vision = -1;
    List<String> list = new ArrayList<>();

    private DatabaseReference userContent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_opening);
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
        SharedPreferences prefUserName = getSharedPreferences("user", MODE_PRIVATE);

        String userName = prefUserName.getString(KEY_USER, "");
        userContent = FirebaseDatabase.getInstance().getReference().child("users").child(userName).child("survey");


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
                if (allFilled()) {
                    list.add("Date: " + date.getText().toString());
                    list.add("Age: " + age.getText().toString());
                    list.add("Disease: " + disease.getText().toString());
                    if (lan != null && lan) {
                        list.add("Native English speaker");
                    } else if (lan != null) {
                        list.add("Not native English speaker");
                    }
                    if (handedness != null && handedness) {
                        list.add("Left handed");
                    } else if (handedness != null) {
                        list.add("right handed");
                    }
                    if (color != null && color) {
                        list.add("Is color blind");
                    } else if (color != null) {
                        list.add("Is not color blind");
                    }
                    if (vision != -1) {
                        switch (vision) {
                            case 1:
                                list.add("Normal vision");
                                break;
                            case 2:
                                list.add("Wear contacts");
                                break;
                            case 3:
                                list.add("Wear glasses");
                                break;
                        }
                    }
                    userContent.child(getCurrentTime()).setValue(list);
                    prefSurvey.edit().putInt("Status", 1).apply();
                    Intent intent = new Intent(SurveyOpening.this, Survey_opening2.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

    }

    boolean allFilled() {
        if (date.getText().toString().equals("")) {
            Toast.makeText(SurveyOpening.this, "Please fill in the date", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (age.getText().toString().equals("")) {
            Toast.makeText(SurveyOpening.this, "Please fill in your age", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (disease.getText().toString().equals("")) {
            Toast.makeText(SurveyOpening.this, "Please fill in the last question. Type NONE if no disease", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (lan == null) {
            Toast.makeText(SurveyOpening.this, "Please indicate your language", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (handedness == null) {
            Toast.makeText(SurveyOpening.this, "Please indicate your handedness", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (color == null) {
            Toast.makeText(SurveyOpening.this, "Please indicate whether you have color blindness", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (vision == -1) {
            Toast.makeText(SurveyOpening.this, "Please answer the question regarding your vision", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
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