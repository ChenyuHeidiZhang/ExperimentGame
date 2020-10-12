package com.jhu.chenyuzhang.experimentgame;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TimeDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "TimeRecord.db";
    private static final int DATABASE_VERSION = 1;

    public static String TABLE_NAME = "timeRecord_table";

    public static final String COL_1 = "ID";
    public static final String COL_2 = "TIME_Mircroseconds";
    public static final String COL_3 = "EVENT";
    private SharedPreferences user_name;

    public TimeDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        /*db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                COL_1 + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_2 + " TEXT, " +
                COL_3 + " TEXT)" ); */
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(String time, String event) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, time);
        contentValues.put(COL_3, event);
        long result = db.insert(TABLE_NAME, null, contentValues);
        return (result != -1);
    }

    // create table if it doesn't exist; called from log in
    public void createTableIfNotExists(String tableName) {
        this.TABLE_NAME = tableName;    // change the table name to append the user name

        String query = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                COL_1 + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_2 + " TEXT, " +
                COL_3 + " TEXT)" ;
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(query);
    }
}