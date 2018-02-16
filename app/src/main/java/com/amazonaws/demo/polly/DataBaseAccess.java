package com.amazonaws.demo.polly;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.IOException;

public class DataBaseAccess {
    private SQLiteOpenHelper openHelper;
    private SQLiteDatabase database;
    private static DataBaseAccess instance;
    static Context ct;

    /**
     * Private constructor to aboid object creation from outside classes.
     *
     * @param context
     */
    protected DataBaseAccess(Context context) {
        this.openHelper = new DBHelp(context);
    }

    /**
     * Return a singleton instance of DatabaseAccess.
     *
     * @param context the Context
     * @return the instance of DabaseAccess
     */
    public static DataBaseAccess getInstance(Context context) {
        if (instance == null) {
            instance = new DataBaseAccess(context);
            ct=context;
        }
        return instance;
    }

    /**
     * Open the database connection.
     */
    public SQLiteDatabase open() throws IOException {
        //return ct.getAssets().open("spellit.db");

        return openHelper.getWritableDatabase();
         //return this.database;
    }

    /**
     * Close the database connection.
     */
    public void close() {
        if (database != null) {
            this.database.close();
        }
    }
}
