package com.amazonaws.demo.polly;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class DataBaseAccess extends SQLiteAssetHelper {

    private static final String DATABASE_NAME = "spellit.db";
    private static final int DATABASE_VERSION = 1;

    public DataBaseAccess(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public Cursor getData(String level) {

        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String[] sqlSelect = {"word"};
        String sqlTables = "wordlist";

        qb.setTables(sqlTables);
        Cursor c = qb.query(db, sqlSelect, "level=?", new String[] {level} ,null, null, null);

        return c;

    }
}