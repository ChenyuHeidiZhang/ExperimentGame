package com.jhu.chenyuzhang.experimentgame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.List;
import static com.jhu.chenyuzhang.experimentgame.MainActivity.getCurrentTime;

public class Survey_opening2 extends AppCompatActivity {
    TextView sex;
    TextView ethnic;
    TextView racial;

    RadioButton sex_female;
    RadioButton sex_male;
    RadioButton sex_none;
    RadioButton ethnic_hispanic;
    RadioButton ethnic_no_hispanic;
    RadioButton ethnic_none;
    RadioButton racial_american;
    RadioButton racial_asian;
    RadioButton racial_hawaiian;
    RadioButton racial_black;
    RadioButton racial_white;
    RadioButton racial_none;

    SharedPreferences prefSurvey;

    Button next;

    private long backPressedTime;
    public static final String KEY_USER = "keyUser";
    private DatabaseReference userContent;
    List<String> list = new ArrayList<>();

    int gender = -1;
    int ethnicity = -1;
    int race = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_opening2);

        sex = findViewById(R.id.Survey_sex);
        ethnic = findViewById(R.id.Survey_Ethnic);
        racial = findViewById(R.id.Survey_racial);

        sex_female = findViewById(R.id.Sex_female);
        sex_male = findViewById(R.id.Sex_male);
        sex_none = findViewById(R.id.Sex_no_response);
        ethnic_hispanic = findViewById(R.id.Ethnic_Hispanic);
        ethnic_no_hispanic = findViewById(R.id.Ethnic_no_hispanic);
        ethnic_none = findViewById(R.id.Ethnic_no_response);
        racial_american = findViewById(R.id.Racial_American);
        racial_asian = findViewById(R.id.Racial_Asia);
        racial_hawaiian = findViewById(R.id.Racial_Hawaiian);
        racial_black = findViewById(R.id.Racial_black);
        racial_white = findViewById(R.id.Racial_white);
        racial_none = findViewById(R.id.Racial_no_response);
        next = findViewById(R.id.Next5);

        prefSurvey = getSharedPreferences("Survey", MODE_PRIVATE);
        SharedPreferences prefUserName = getSharedPreferences("user", MODE_PRIVATE);

        String userName = prefUserName.getString(KEY_USER, "");
        userContent = FirebaseDatabase.getInstance().getReference().child("users").child(userName).child("survey");

        sex_female.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (gender != -1) {
                    sex_male.setChecked(false);
                    sex_none.setChecked(false);
                }
                gender = 1;
                sex_female.setChecked(true);
            }
        });

        sex_male.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (gender != -1) {
                    sex_female.setChecked(false);
                    sex_none.setChecked(false);
                }
                gender = 2;
                sex_male.setChecked(true);
            }
        });

        sex_none.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (gender != -1) {
                    sex_male.setChecked(false);
                    sex_female.setChecked(false);
                }
                gender = 0;
                sex_none.setChecked(true);
            }
        });

        ethnic_hispanic.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (ethnicity != -1) {
                    ethnic_no_hispanic.setChecked(false);
                    ethnic_none.setChecked(false);
                }
                ethnicity = 1;
                ethnic_hispanic.setChecked(true);
            }
        });

        ethnic_no_hispanic.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (ethnicity != -1) {
                    ethnic_hispanic.setChecked(false);
                    ethnic_none.setChecked(false);
                }
                ethnicity = 2;
                ethnic_no_hispanic.setChecked(true);
            }
        });

        ethnic_none.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (ethnicity != -1) {
                    ethnic_no_hispanic.setChecked(false);
                    ethnic_hispanic.setChecked(false);
                }
                ethnicity = 0;
                ethnic_none.setChecked(true);
            }
        });

        racial_american.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (race != -1) {
                    racial_asian.setChecked(false);
                    racial_hawaiian.setChecked(false);
                    racial_black.setChecked(false);
                    racial_white.setChecked(false);
                    racial_none.setChecked(false);
                }
                race = 1;
                racial_american.setChecked(true);
            }
        });

        racial_asian.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (race != -1) {
                    racial_american.setChecked(false);
                    racial_hawaiian.setChecked(false);
                    racial_black.setChecked(false);
                    racial_white.setChecked(false);
                    racial_none.setChecked(false);
                }
                race = 2;
                racial_asian.setChecked(true);
            }
        });

        racial_hawaiian.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (race != -1) {
                    racial_american.setChecked(false);
                    racial_asian.setChecked(false);
                    racial_black.setChecked(false);
                    racial_white.setChecked(false);
                    racial_none.setChecked(false);
                }
                race = 3;
                racial_hawaiian.setChecked(true);
            }
        });

        racial_black.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (race != -1) {
                    racial_american.setChecked(false);
                    racial_hawaiian.setChecked(false);
                    racial_asian.setChecked(false);
                    racial_white.setChecked(false);
                    racial_none.setChecked(false);
                }
                race = 4;
                racial_black.setChecked(true);
            }
        });

        racial_white.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (race != -1) {
                    racial_american.setChecked(false);
                    racial_hawaiian.setChecked(false);
                    racial_black.setChecked(false);
                    racial_asian.setChecked(false);
                    racial_none.setChecked(false);
                }
                race = 5;
                racial_white.setChecked(true);
            }
        });

        racial_none.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (race != -1) {
                    racial_american.setChecked(false);
                    racial_hawaiian.setChecked(false);
                    racial_black.setChecked(false);
                    racial_white.setChecked(false);
                    racial_asian.setChecked(false);
                }
                race = 6;
                racial_none.setChecked(true);
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (allFilled()) {
                    if (gender == 1) {
                        list.add("Female");
                    } else if (gender == 2) {
                        list.add("Male");
                    } else {
                        list.add("Sex no response");
                    }
                    if (ethnicity == 1) {
                        list.add("Hispanic or Latino");
                    } else if (ethnicity == 2) {
                        list.add("Not Hispanic nor Latino");
                    } else {
                        list.add("Ethnicity no response");
                    }
                    if (race == 1) {
                        list.add("American");
                    } else if (race == 2) {
                        list.add("Asian");
                    } else if (race == 3) {
                        list.add("Hawaiian");
                    } else if (race == 4) {
                        list.add("Black");
                    } else if (race == 5) {
                        list.add("White");
                    } else {
                        list.add("Race no response");
                    }
                    userContent.child(getCurrentTime()).setValue(list);
                    prefSurvey.edit().putInt("Status", 2).apply();
                    Intent intent = new Intent(Survey_opening2.this, SurveyContinue.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    private boolean allFilled() {
        if (gender == -1) {
            Toast.makeText(Survey_opening2.this, "Please indicate your gender", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (ethnicity == -1) {
            Toast.makeText(Survey_opening2.this, "Please indicate your ethnicity", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (race == -1) {
            Toast.makeText(Survey_opening2.this, "Please indicate your race", Toast.LENGTH_SHORT).show();
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