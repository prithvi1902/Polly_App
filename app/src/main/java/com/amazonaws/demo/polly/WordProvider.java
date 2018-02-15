package com.amazonaws.demo.polly;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WordProvider extends ContentProvider {
    public WordProvider() {
    }

    public static final String AUTHORITY="com.amazonaws.demo.polly.WordProvider";

    public static final String PATH_TOGET_WORDS ="wordlist";//directory

    public static final Uri CONTENT_URI_1=Uri.parse("content://"+AUTHORITY+"/"+ PATH_TOGET_WORDS);

    public static final int TOGET_WORDS =1;

    SQLiteDatabase sqldb;

    DataBaseAccess dba;

    private static HashMap<String,String> words;

    private static final UriMatcher MATCHER=new UriMatcher(UriMatcher.NO_MATCH);

    static {
        MATCHER.addURI(AUTHORITY, PATH_TOGET_WORDS, TOGET_WORDS);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onCreate() {
        // TODO: Implement this to initialize your content provider on startup.
        dba=new DataBaseAccess(getContext());
        sqldb=dba.open();
        if(sqldb.isOpen() )
            return true;
        else
            return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder qb=new SQLiteQueryBuilder();

        qb.setTables("wordlist");

        switch (MATCHER.match(uri)) {
            case TOGET_WORDS:

                // A projection map maps from passed column names to database column names
                qb.setProjectionMap(words);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        Cursor cursor=qb.query(sqldb,projection,selection,selectionArgs,null,null,sortOrder );

        cursor.setNotificationUri(getContext().getContentResolver(),uri);

        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
