package com.example.dogbreedclassifier;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;



public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(Context context){
        super(context, "DOGINFO", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE dogTBL (Did INTEGER primary key autoincrement not null," +
                "Dname CHAR(20)," +
                "Dage INTEGER," +
                "Dweight INTEGER," +
                "Dsize char(20)," +
                "Dfur char(20)," +
                "Dimage char(150)," +
                "Dresult char(300)" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS dogTBL");
        onCreate(db);
    }

}
