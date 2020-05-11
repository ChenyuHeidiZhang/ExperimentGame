package com.jhu.chenyuzhang.experimentgame;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

// Note: when adding a new trial table, put the csv file in the assets folder
// and get rid of the first line (header) of the csv file

public class TrialDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "TrialInformation.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "trialInfo_table";
    private static final String COL_0 = "ID";
    private static final String COL_1 = "Type";
    private static final String COL_2 = "Dominance";
    private static final String COL_3 = "Col3";
    private static final String COL_4 = "Col4";
    private static final String COL_5 = "Col5";
    private static final String COL_6 = "Col6";
    private static final String COL_7 = "Col7";
    private static final String COL_8 = "Col8";
    private static final String COL_9 = "Col9";
    private static final String COL_10 = "Col10";
    private static final String COL_11 = "Col11";
    private static final String COL_12 = "Col12";
    private static final String COL_13 = "Col13";
    private static final String COL_14 = "Col14";
    private static final String COL_15 = "Col15";
    private static final String COL_16 = "Col16";
    private static final String COL_17 = "Col17";
    private static final String COL_18 = "Col18";


    private SQLiteDatabase db;
    Context context;

    public TrialDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        this.db = db;

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                COL_0 + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_1 + " TEXT, " +
                COL_2 + " TEXT, " +
                COL_3 + " TEXT, " + COL_4 + " TEXT, " +
                COL_5 + " TEXT, " + COL_6 + " TEXT, " +
                COL_7 + " TEXT, " + COL_8 + " TEXT, " +
                COL_9 + " TEXT, " + COL_10 + " TEXT, " +
                COL_11 + " TEXT, " + COL_12 + " TEXT, " +
                COL_13 + " TEXT, " + COL_14 + " TEXT, " +
                COL_15 + " TEXT, " + COL_16 + " TEXT, " +
                COL_17 + " TEXT, " + COL_18 + " TEXT)" );

        fillTrialTable();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }

    private void fillTrialTable(){
        String fileName = "allTrialsMADM_Tablet.csv";
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
                if (columns.length > 18) {
                    Log.d("CSVParser", "Skipping Bad CSV Row");
                    continue;
                }
                //Trial trial = new Trial(columns[0].trim(), columns[1].trim(), columns[2].trim(), columns[3].trim());

                Trial trial = new Trial(columns);   // columns.length == 6 / 10 / 18
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
            ArrayList<String> attrs = trial.getAttributes();
            cv.put(COL_1, trial.getType());
            cv.put(COL_2, attrs.get(1));
            cv.put(COL_3, attrs.get(2));
            cv.put(COL_4, attrs.get(3));
            cv.put(COL_5, attrs.get(4));
            cv.put(COL_6, attrs.get(5));
            if (attrs.size() >= 10) {   // if there are 8 or 16 attributes
                cv.put(COL_7, attrs.get(6));
                cv.put(COL_8, attrs.get(7));
                cv.put(COL_9, attrs.get(8));
                cv.put(COL_10, attrs.get(9));
                if (attrs.size() == 18) {    // if there are 16 attributes
                    cv.put(COL_11, attrs.get(10));
                    cv.put(COL_12, attrs.get(11));
                    cv.put(COL_13, attrs.get(12));
                    cv.put(COL_14, attrs.get(13));
                    cv.put(COL_15, attrs.get(14));
                    cv.put(COL_16, attrs.get(15));
                    cv.put(COL_17, attrs.get(16));
                    cv.put(COL_18, attrs.get(17));
                }
            }
            db.insert(TABLE_NAME, null, cv);
        }
    }

    public long getNumRows() {
        db = this.getReadableDatabase();
        long count = DatabaseUtils.queryNumEntries(db, TABLE_NAME);
        db.close();
        return count;
    }

    public ArrayList<Trial> getAllTrials(){
        ArrayList<Trial> trialList = new ArrayList<>();
        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        String[] columnNames = cursor.getColumnNames();

        if (cursor.moveToFirst()) {
            do {
                Trial trial = new Trial();
                trial.setType(cursor.getString(cursor.getColumnIndex(COL_1)));

                ArrayList<String> attributes = new ArrayList<>();
                for (int i = 1; i < columnNames.length; i++) {
                    attributes.add(cursor.getString(cursor.getColumnIndex(columnNames[i])));    // get attributes from COL_1 to the end
                }
                trial.setAttributes(attributes);

                trialList.add(trial);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return trialList;
    }

    public Trial getTrial(int trial_number) {
        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME +
                " WHERE " + COL_0 + " = " + trial_number, null);     // select the trial with ID trial_number

        Trial trial = new Trial();
        String[] columnNames = cursor.getColumnNames();
        if (cursor.moveToFirst()) {
            trial.setType(cursor.getString(cursor.getColumnIndex(COL_1)));

            ArrayList<String> attributes = new ArrayList<>();
            for (int i = 1; i < columnNames.length; i++) {
                attributes.add(cursor.getString(cursor.getColumnIndex(columnNames[i])));    // get attributes from COL_1 to the end
            }
            trial.setAttributes(attributes);
        }
        cursor.close();
        db.close();
        return trial;
    }
}
