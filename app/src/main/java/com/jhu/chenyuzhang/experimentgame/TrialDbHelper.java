package com.jhu.chenyuzhang.experimentgame;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class TrialDbHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "TrialInformation.db";
    public static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "trialInfo_table";
    public static final String COL_1 = "ID";
    public static final String COL_2 = "Amount1";
    public static final String COL_3 = "Probability1";
    public static final String COL_4 = "Amount2";
    public static final String COL_5 = "Probability2";

    private SQLiteDatabase db;
    Context context;

    public TrialDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        this.db = db;

        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                COL_1 + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_2 + " TEXT, " +
                COL_3 + " TEXT, " +
                COL_4 + " TEXT, " +
                COL_5 + " TEXT)" );

        fillTrialTable();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }

    private void fillTrialTable(){
        String fileName = "trialInfo_list.csv";
        //FileReader file = new FileReader(fileName);
        AssetManager assetManager = context.getAssets();
        InputStream is =  null;
        try {
            is = assetManager.open(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedReader buffer = new BufferedReader(new InputStreamReader(is));
        ArrayList<Trial> arrayList = new ArrayList<>();
        String line;
        try {
            while ((line = buffer.readLine()) != null) {
                String[] columns = line.split(",");
                if (columns.length != 4) {
                    Log.d("CSVParser", "Skipping Bad CSV Row");
                    continue;
                }
                Trial trial = new Trial(columns[0].trim(), columns[1].trim(), columns[2].trim(), columns[3].trim());
                arrayList.add(trial);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.addAllTrials(arrayList);
    }

    private void addAllTrials(ArrayList<Trial> allTrials){
        ContentValues cv = new ContentValues();
        for (Trial trial : allTrials) {
            cv.put(COL_2, trial.getAmount1());
            cv.put(COL_3, trial.getProbability1());
            cv.put(COL_4, trial.getAmount2());
            cv.put(COL_5, trial.getProbability2());
            db.insert(TABLE_NAME, null, cv);
        }
    }

    public ArrayList<Trial> getAllTrials(){
        ArrayList<Trial> trialList = new ArrayList<>();
        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        if (cursor.moveToFirst()) {
            do {
                Trial trial = new Trial();
                trial.setAmount1(cursor.getString(cursor.getColumnIndex(COL_2)));
                trial.setProbability1(cursor.getString(cursor.getColumnIndex(COL_3)));
                trial.setAmount2(cursor.getString(cursor.getColumnIndex(COL_4)));
                trial.setProbability2(cursor.getString(cursor.getColumnIndex(COL_5)));
                trialList.add(trial);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return trialList;
    }
}
