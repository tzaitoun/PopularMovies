package com.zaitoun.talat.popularmovies;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.squareup.picasso.Picasso;
import com.zaitoun.talat.popularmovies.data.PopularMoviesContract;

import java.util.ArrayList;

import static com.zaitoun.talat.popularmovies.data.PopularMoviesContract.*;

public class MoviesCursorLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

    /* We need to have acces to these in order to update the adapter data and UI */
    private Context mContext;
    private MovieAdapter mMovieAdapter;
    private GridView mGridView;

    public MoviesCursorLoaderCallbacks(Context context, MovieAdapter movieAdapter, GridView gridView) {
        mContext = context;
        mMovieAdapter = movieAdapter;
        mGridView = gridView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new AsyncTaskLoader<Cursor>(mContext) {

            /* This is used to cache our data */
            private Cursor mBookmarkedMoviesCursor;

            /* If the data was cached, deliver result to activity. If not, fetch the data from SQLite database */
            @Override
            protected void onStartLoading() {

                if (mBookmarkedMoviesCursor != null) {
                    deliverResult(mBookmarkedMoviesCursor);
                } else {
                    forceLoad();
                }
            }

            /* Queries the database for all the movies */
            @Override
            public Cursor loadInBackground() {

                try {
                    return mContext.getContentResolver()
                            .query(BookmarkedMoviesEntry.CONTENT_URI,
                                    null,
                                    null,
                                    null,
                                    null);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public void deliverResult(Cursor data) {
                mBookmarkedMoviesCursor = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        /* Create a new array to hold the movies */
        ArrayList<Movie> movies = new ArrayList<>();

        /* If the data is valid */
        if (data != null) {

            try {

                /* Get the index of each column */
                final int MOVIE_ID_COLUMN = data.getColumnIndex(BookmarkedMoviesEntry.MOVIE_ID);
                final int ORIGINAL_TITLE_COLUMN = data.getColumnIndex(BookmarkedMoviesEntry.COLUMN_MOVIE_TITLE);
                final int BACKDROP_PATH_COLUMN = data.getColumnIndex(BookmarkedMoviesEntry.COLUMN_MOVIE_BACKDROP_PATH);
                final int OVERVIEW_COLUMN = data.getColumnIndex(BookmarkedMoviesEntry.COLUMN_MOVIE_OVERVIEW);
                final int VOTE_AVERAGE_COLUMN = data.getColumnIndex(BookmarkedMoviesEntry.COLUMN_MOVIE_RATING);
                final int RELEASE_DATE_COLUMN = data.getColumnIndex(BookmarkedMoviesEntry.COLUMN_MOVIE_DATE);
                final int LOCAL_POSTER_PATH_COLUMN = data.getColumnIndex(BookmarkedMoviesEntry.COLUMN_MOVIE_POSTER_PATH);

                /* Loop through the cursor */
                while (data.moveToNext()) {

                    /* Put the data from the cursor into a movie object and add it to the array */
                    String movie_id = Integer.toString(data.getInt(MOVIE_ID_COLUMN));
                    String original_title = data.getString(ORIGINAL_TITLE_COLUMN);
                    String poster_path = null;
                    String backdrop_path = data.getString(BACKDROP_PATH_COLUMN);
                    String overview = data.getString(OVERVIEW_COLUMN);
                    String vote_average = data.getString(VOTE_AVERAGE_COLUMN);
                    String release_date = data.getString(RELEASE_DATE_COLUMN);
                    String local_poster_path = data.getString(LOCAL_POSTER_PATH_COLUMN);

                    Movie movie = new Movie(movie_id, original_title, poster_path, backdrop_path,
                            overview, vote_average, release_date, local_poster_path);

                    movies.add(movie);
                }
            } finally {
                data.close();
            }

            /* If this is the first time launching the application, initialize and set the adapter.
             * The movie adapter is shared between the movie loader and bookmarked movies loader
             * (This loader is the bookmarked movies loader).
             */
            if (mMovieAdapter == null && mContext instanceof Activity) {
                mMovieAdapter = new MovieAdapter((Activity) mContext, movies);
                mGridView.setAdapter(mMovieAdapter);

                /* When an item is clicked, put the data in an intent and launch the DetailActivity */
                mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Movie movie = (Movie) parent.getItemAtPosition(position);
                        Intent intent = new Intent((Activity) mContext, DetailActivity.class);
                        intent.putExtra(Intent.EXTRA_TEXT, movie);
                        mContext.startActivity(intent);
                    }
                });
            }

            /* if this is not the first time, replace the data with the new data and notify the adapter */
            else {
                mMovieAdapter.clear();
                mMovieAdapter.addAll(movies);
                mMovieAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
