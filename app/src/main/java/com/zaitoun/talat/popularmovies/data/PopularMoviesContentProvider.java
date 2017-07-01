package com.zaitoun.talat.popularmovies.data;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static com.zaitoun.talat.popularmovies.data.PopularMoviesContract.*;

public class PopularMoviesContentProvider extends ContentProvider {

    private PopularMoviesDbHelper mPopularMoviesDbHelper;

    private static UriMatcher sUriMatcher = buildUriMatcher();

    /* Arbitrary codes used to differentiate between data accesses */
    private static final int BOOKMARKED_MOVIES = 100;
    private static final int BOOKMARKED_MOVIE_WITH_ID = 101;

    /* This builds a UriMatcher that returns the type of data that was accessed */
    public static UriMatcher buildUriMatcher() {

        /* Initialize a UriMatcher with no matches by passing in NO_MATCH to the constructor */
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        /* The Uri used to access the bookmarked_movies table */
        uriMatcher.addURI(AUTHORITY,
                PATH_BOOKMARKED_MOVIES, BOOKMARKED_MOVIES);

        /* The Uri used to access a specific movie in the bookmarked_movies table */
        uriMatcher.addURI(AUTHORITY, PATH_BOOKMARKED_MOVIES + "/#",
                BOOKMARKED_MOVIE_WITH_ID);

        return uriMatcher;
    }

    /* Initialize our Db helper to get access to our database */
    @Override
    public boolean onCreate() {

        Context context = getContext();
        mPopularMoviesDbHelper = new PopularMoviesDbHelper(context);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        /* Get a reference to our database, we only need read-only since we are querying */
        final SQLiteDatabase database = mPopularMoviesDbHelper.getReadableDatabase();

        /* Find out which Uri they are trying to access */
        int match = sUriMatcher.match(uri);

        Cursor cursor;

        switch (match) {
            case BOOKMARKED_MOVIES:

                cursor = database.query(BookmarkedMoviesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case BOOKMARKED_MOVIE_WITH_ID:

                String id = uri.getPathSegments().get(1);

                cursor = database.query(BookmarkedMoviesEntry.TABLE_NAME,
                        projection,
                        BookmarkedMoviesEntry.MOVIE_ID + "=?",
                        new String[] {id},
                        null,
                        null,
                        sortOrder);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        /* Set a notification URI on the Cursor and return that Cursor */
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        /* Get a reference to our database */
        final SQLiteDatabase database = mPopularMoviesDbHelper.getWritableDatabase();

        /* Find out which Uri they are trying to access */
        int match = sUriMatcher.match(uri);

        Uri returnUri;

        switch (match) {
            case BOOKMARKED_MOVIES:

                long id = database.insert(BookmarkedMoviesEntry.TABLE_NAME,  null, values);

                /* if the id is valid */
                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(BookmarkedMoviesEntry.CONTENT_URI, id);
                }

                else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }

                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        /* Notify the resolver if the uri has been changed, and return the newly inserted URI */
        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        /* Get a reference to our database */
        final SQLiteDatabase database = mPopularMoviesDbHelper.getWritableDatabase();

        /* Find out which Uri they are trying to access */
        int match = sUriMatcher.match(uri);

        int numTasksDeleted;

        switch (match) {
            case BOOKMARKED_MOVIE_WITH_ID:

                String id = uri.getPathSegments().get(1);

                numTasksDeleted = database.delete(BookmarkedMoviesEntry.TABLE_NAME,
                        BookmarkedMoviesEntry.MOVIE_ID + "=?", new String[] {id});
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        /* Notify the resolver of a change and return the number of items deleted */
        if (numTasksDeleted != 0) {
            // A task was deleted, set notification
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numTasksDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
