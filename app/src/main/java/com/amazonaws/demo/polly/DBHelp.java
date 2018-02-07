package com.amazonaws.demo.polly;

import android.content.Context;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class DBHelp extends SQLiteAssetHelper {
    private static final String DATABASE_NAME = "spellit.db";
    private static final int DATABASE_VERSION = 2;

    public DBHelp(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
}



