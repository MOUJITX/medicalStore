package com.moujitx.medicalstore;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLites extends SQLiteOpenHelper {
    public SQLites(Context context) {
        super(context, "myDB.db", null, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE user(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username VARCHAR(20), " +
                "password VARCHAR(20), " +
                "city VARCHAR(20)," +
                "cityCode VARCHAR(20))");
        db.execSQL("CREATE TABLE shop(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name VARCHAR(20), " +
                "type VARCHAR(20)," +
                "price FLOAT," +
                "quantity INTEGER)");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}