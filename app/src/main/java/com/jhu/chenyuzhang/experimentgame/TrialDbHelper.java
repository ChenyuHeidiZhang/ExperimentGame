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
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_NAME = "trialnfo_table";
    private static final String COL_0 = "ID";
    private static final String COL_5 = "Orientation";
    private static final String COL_6 = "Type";
    private static final String COL_7 = "Dominance";

    private SQLiteDatabase db;
    Context context;

    public TrialDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        this.db = db;

        StringBuilder commandBuilder = new StringBuilder(800);

        commandBuilder.append("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                COL_0 + " INTEGER PRIMARY KEY AUTOINCREMENT, ");

        for (int i = 1; i <= 4; i++) {
            commandBuilder.append("Option" + i + "Outcome").append(" TEXT, ");
        }

        commandBuilder.append(COL_5 + " TEXT, " +
                COL_6 + " TEXT, " + COL_7 + " TEXT, ");

        for (int i = 8; i <= 38; i++) {
            commandBuilder.append("Col" + i).append(" TEXT, ");
        }
        commandBuilder.append("Col39 TEXT)");

        db.execSQL(commandBuilder.toString());

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
                String[] columns = line.split(",");  // Default removes trailing empty strings from the result array.
                if (columns.length > 39) {
                    Log.d("CSVParser", "Skipping Bad CSV Row");
                    continue;
                }

                Trial trial = new Trial(columns);  // columns.length == 15 or 23 or 39
                arrayList.add(trial);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.addAllTrials(arrayList);
    }

    // Adds all the trials to the db.
    private void addAllTrials(ArrayList<Trial> allTrials){
        ContentValues cv = new ContentValues();
        for (Trial trial : allTrials) {
            String[] outcomes = trial.getOutcomes();
            for (int i = 1; i <= 4; i++) {
                cv.put("Option" + i + "Outcome", outcomes[i-1]);
            }
            cv.put(COL_5, trial.getOrient());
            cv.put(COL_6, trial.getType());
            cv.put(COL_7, trial.getDominance());

            ArrayList<String> attrs = trial.getAttributes();
            for (int i = 8; i <= 15; i++) {
                cv.put("Col" + i, attrs.get(i - 8));
            }
            if (attrs.size() >= 16) {   // if there are 8 or 16 attributes
                for (int i = 16; i <= 23; i++) {
                    cv.put("Col" + i, attrs.get(i - 8));
                }
                if (attrs.size() == 32) {    // if there are 16 attributes
                    for (int i = 24; i<= 39; i++) {
                        cv.put("Col" + i, attrs.get(i - 8));
                    }
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

    public Trial getTrial(int trial_number) {
        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME +
                " WHERE " + COL_0 + " = " + trial_number, null);  // select the trial with ID trial_number

        Trial trial = new Trial();
        String[] columnNames = cursor.getColumnNames();
        if (cursor.moveToFirst()) {
            String[] outcomes = new String[4];
            for (int i = 1; i <= 4; i++) {
                outcomes[i-1] = cursor.getString(cursor.getColumnIndex(columnNames[i]));
            }
            trial.setOutcomes(outcomes);
            trial.setOrient(cursor.getString(cursor.getColumnIndex(COL_5)));  // Set the trial orientation
            trial.setType(cursor.getString(cursor.getColumnIndex(COL_6)));
            trial.setDominance(cursor.getString(cursor.getColumnIndex(COL_7)));

            ArrayList<String> attributes = new ArrayList<>();
            for (int i = 8; i < columnNames.length; i++) {
                attributes.add(cursor.getString(cursor.getColumnIndex(columnNames[i])));  // get attributes from Col4 to the end
            }
            trial.setAttributes(attributes);
        }
        cursor.close();
        db.close();
        return trial;
    }
}
