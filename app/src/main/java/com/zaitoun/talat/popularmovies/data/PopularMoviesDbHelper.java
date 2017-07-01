package com.zaitoun.talat.popularmovies.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.zaitoun.talat.popularmovies.data.PopularMoviesContract.*;

public class PopularMoviesDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "popular_movies.db";

    private static final int DATABASE_VERSION = 1;


    public PopularMoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        /* Create a table with the following name and columns */
        final String SQL_CREATE_BOOKMARKED_MOVIES_TABLE = "CREATE TABLE " + BookmarkedMoviesEntry.TABLE_NAME + " (" +
                BookmarkedMoviesEntry.MOVIE_ID + " INTEGER PRIMARY KEY, " +
                BookmarkedMoviesEntry.COLUMN_MOVIE_POSTER_PATH + " TEXT NOT NULL, " +
                BookmarkedMoviesEntry.COLUMN_MOVIE_TITLE + " TEXT NOT NULL, " +
                BookmarkedMoviesEntry.COLUMN_MOVIE_OVERVIEW + " TEXT NOT NULL, " +
                BookmarkedMoviesEntry.COLUMN_MOVIE_DATE + " TEXT NOT NULL, " +
                BookmarkedMoviesEntry.COLUMN_MOVIE_RATING + " TEXT NOT NULL, " +
                BookmarkedMoviesEntry.COLUMN_MOVIE_BACKDROP_PATH + " TEXT NOT NULL" +
                "); ";

        db.execSQL(SQL_CREATE_BOOKMARKED_MOVIES_TABLE);
    }

    /* Drops the table if it exists and creates a new one */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + BookmarkedMoviesEntry.TABLE_NAME);
        onCreate(db);
    }
}
