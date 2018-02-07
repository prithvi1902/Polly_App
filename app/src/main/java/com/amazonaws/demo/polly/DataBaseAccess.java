package com.amazonaws.demo.polly;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseAccess {
    private SQLiteOpenHelper openHelper;
    private SQLiteDatabase database;
    private static DataBaseAccess instance;


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
        }
        return instance;
    }

    /**
     * Open the database connection.
     */
    public SQLiteDatabase open() {
        this.database = openHelper.getWritableDatabase();
        return this.database;
    }

    /**
     * Close the database connection.
     */
    public void close() {
        if (database != null) {
            this.database.close();
        }
    }



    /*public List<String> getData(String level){

        List<String> word=new ArrayList<>();

        String SELECT_QUERY = "SELECT word FROM wordlist WHERE level = '"+level+"'";

        Cursor cursor=database.rawQuery(SELECT_QUERY,null);

        cursor.moveToFirst();
        while(!cursor.isAfterLast())
        {
                word.add(cursor.getString(0));
                cursor.moveToNext();
        }

        cursor.close();
        // return word list
        return word;
    }*/


    public Cursor getData(String level){

        String SELECT_QUERY = "SELECT * FROM wordlist WHERE level='"+level+"'";

        Cursor cursor=database.rawQuery(SELECT_QUERY,null);

        return cursor;
    }
}
