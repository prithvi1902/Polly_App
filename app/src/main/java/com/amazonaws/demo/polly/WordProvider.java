package com.amazonaws.demo.polly;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WordProvider extends ContentProvider {

    //public static final String AUTHORITY="com.amazonaws.demo.polly.WordProvider";

//    public static final String PATH_TOGET_WORDS ="src/main/assets/databases/";//directory

  //  public static final Uri CONTENT_URL=Uri.parse("content://"+AUTHORITY+"/"+ PATH_TOGET_WORDS);

    //public static final int uricode =1;

    private SQLiteDatabase sqldb;

    static final String DB_Name="spellit.db";

    private static HashMap<String,String> words;

    //private static final UriMatcher MATCHER=new UriMatcher(UriMatcher.NO_MATCH);

    //static {
    //    MATCHER.addURI(AUTHORITY, PATH_TOGET_WORDS, uricode);
    //}


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteOpenHelper oHelper=new DataBaseHelper(getContext()); //Inner Class declared at the bottom
        sqldb=oHelper.getWritableDatabase();

        SQLiteQueryBuilder qb=new SQLiteQueryBuilder();

        qb.setTables("wordlist");

        //   switch (MATCHER.match(uri)) {
        //     case uricode:

        // A projection map maps from passed column names to database column names
        qb.setProjectionMap(words);
        //       break;
        // default:
        //   throw new IllegalArgumentException("Unknown URI " + uri);
        //}

        Cursor cursor=qb.query(sqldb,projection,selection,selectionArgs,null,null,sortOrder );

       // cursor.setNotificationUri(getContext().getContentResolver(),uri);

        return cursor;
    }



    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
//        throw new UnsupportedOperationException("Not yet implemented");
        return 1;
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        //throw new UnsupportedOperationException("Not yet implemented");
        return String.valueOf(true);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        //throw new UnsupportedOperationException("Not yet implemented");
        return uri;
    }

    @Override
    public boolean onCreate() {
        // TODO: Implement this to initialize your content provider on startup.
        return true;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        //throw new UnsupportedOperationException("Not yet implemented");
        return 1;
    }

    private static class DataBaseHelper extends SQLiteAssetHelper{

        DataBaseHelper(Context context){
            super(context,DB_Name,null,1);
        }
    }
}
