package com.zaitoun.talat.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zaitoun.talat.popularmovies.utilities.MovieJsonUtils;
import com.zaitoun.talat.popularmovies.utilities.MovieNetworkUtils;

import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList<Movie>>,
        SharedPreferences.OnSharedPreferenceChangeListener {

    /* We have 2 loaders that use different loader callbacks: one for popular and top rated movies
     * (reads from a TMDB). And another one for bookmarked movies (reads from SQLite database).
     */
    private static final int MOVIE_LOADER_ID = 0;
    private static final int BOOKMARKED_MOVIE_LOADER_ID = 1;

    /* Used to store the sort mode (popular/top rated) in the bundle when initializing/restarting the loader */
    private static final String SORTMODE_BUNDLE_KEY = "sortmode_bundle_key";

    private GridView mGridView;
    private MovieAdapter mMovieAdapter;
    private ProgressBar mProgressBar;
    private TextView mNoConnectionTextView;
    private TextView mErrorTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGridView = (GridView) findViewById(R.id.gv_movies);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        mNoConnectionTextView = (TextView) findViewById(R.id.tv_no_connection);
        mErrorTextView = (TextView) findViewById(R.id.tv_error);

        /* Display the movies based on the shared preferences */
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String value = sharedPreferences.getString(getResources().getString(R.string.pref_movie_key),
                getResources().getString(R.string.pref_movie_popularity_value));

        /* Change in data occurs when the shared preferences change */
        moviesToDisplayNoChangeInData(value);

        /* Register the listener so that when the user changes the shared preferences, we can update the grid */
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String value = sharedPreferences.getString(getResources().getString(R.string.pref_movie_key),
                getResources().getString(R.string.pref_movie_popularity_value));

        /* When coming back from the bookmarks section, always reload the data */
        if (value.equals(this.getResources().getString(R.string.pref_movie_bookmarked_value))) {
            moviesToDisplayChangeInData(value);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        /* When the settings are clicked, launch our shared preferences activity */
        if (id == R.id.settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        /* Refresh the screen, but making sure to display the movie specified by the shared preferences
         * Refresh restarts the loader to get new data.
         */
        if (id == R.id.refresh) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            String value = sharedPreferences.getString(getResources().getString(R.string.pref_movie_key),
                    getResources().getString(R.string.pref_movie_popularity_value));

            moviesToDisplayChangeInData(value);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     *
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<ArrayList<Movie>> onCreateLoader(int id, Bundle args) {

        if (args != null && args.containsKey(SORTMODE_BUNDLE_KEY)) {
            return new FetchMoviesTaskLoader(this, args.getString(SORTMODE_BUNDLE_KEY), mProgressBar);
        }

        return null;
    }


    /**
     * Called when a previously created loader has finished its load.
     *
     * @param loader The Loader that has finished.
     * @param movies The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader<ArrayList<Movie>> loader, ArrayList<Movie> movies) {

        mProgressBar.setVisibility(View.INVISIBLE);

        /* When the data is error-free */
        if (movies != null) {

            mErrorTextView.setVisibility(View.INVISIBLE);
            mGridView.setVisibility(View.VISIBLE);

            /* If this is the first time launching the application, initialize and set the adapter */
            if (mMovieAdapter == null) {
                mMovieAdapter = new MovieAdapter(MainActivity.this, movies);
                mGridView.setAdapter(mMovieAdapter);

                /* When an item is clicked, put the data in an intent and launch the DetailActivity */
                mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Movie movie = (Movie) parent.getItemAtPosition(position);
                        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                        intent.putExtra(Intent.EXTRA_TEXT, movie);
                        startActivity(intent);
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

        /* When an error occurs when fetching the data */
        else {
            mGridView.setVisibility(View.INVISIBLE);
            mErrorTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Movie>> loader) {
    }

    /**
     * Checks if the device has a network connection.
     * Author: @stackoverflow by gar
     */
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /* When the shared preferences change, display the new and correct data */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals(getResources().getString(R.string.pref_movie_key))) {
            String value = sharedPreferences.getString(getResources().getString(R.string.pref_movie_key),
                    getResources().getString(R.string.pref_movie_popularity_value));

            moviesToDisplayChangeInData(value);
        }
    }

    /* This is used for when the data changes and the loader needs to be restarted */
    private void moviesToDisplayChangeInData(String value) {

        if (value.equals(getResources().getString(R.string.pref_movie_popularity_value))) {
            loadMoviesChangeInData(MovieNetworkUtils.POPULAR_MOVIES);
        }

        else if (value.equals(getResources().getString(R.string.pref_movie_top_rated_value))) {
            loadMoviesChangeInData(MovieNetworkUtils.TOP_RATED_MOVIES);
        }

        else {
            loadBookmarkedMovies(this, mMovieAdapter, mGridView);
        }
    }

    /* This is used for when the Activity is created/recreated */
    private void moviesToDisplayNoChangeInData(String value) {

        if (value.equals(getResources().getString(R.string.pref_movie_popularity_value))) {
            loadMoviesNoChangeInData(MovieNetworkUtils.POPULAR_MOVIES);
        }

        else if (value.equals(getResources().getString(R.string.pref_movie_top_rated_value))) {
            loadMoviesNoChangeInData(MovieNetworkUtils.TOP_RATED_MOVIES);
        }

        else {
            loadBookmarkedMovies(this, mMovieAdapter, mGridView);
        }
    }

    /* Loads bookmarked movies from the database */
    private void loadBookmarkedMovies(Context context, MovieAdapter movieAdapter, GridView gridView) {

        mGridView.setVisibility(View.VISIBLE);
        mNoConnectionTextView.setVisibility(View.INVISIBLE);

        MoviesCursorLoaderCallbacks callbacks = new MoviesCursorLoaderCallbacks(context, movieAdapter, gridView);

        /* If the loader was already created, restart the loader because we close the cursor in onLoadFinished */
        if (getSupportLoaderManager().getLoader(BOOKMARKED_MOVIE_LOADER_ID) != null) {
            getSupportLoaderManager().restartLoader(BOOKMARKED_MOVIE_LOADER_ID, null, callbacks);
        }

        /* Initialize the loader */
        getSupportLoaderManager().initLoader(BOOKMARKED_MOVIE_LOADER_ID, null, callbacks);
    }

    /**
     * Loads the movies from the API if a network connection is available, displays an error otherwise.
     * This is called only when there is a change in shared preferences and thus a change in sort mode.
     * @param sortMode The sort mode: by popularity or rating
     */
    public void loadMoviesChangeInData(String sortMode) {
        if (isOnline()) {
            mGridView.setVisibility(View.VISIBLE);
            mNoConnectionTextView.setVisibility(View.INVISIBLE);

            LoaderManager.LoaderCallbacks<ArrayList<Movie>> callbacks = MainActivity.this;

            Bundle bundleForLoader = new Bundle();
            bundleForLoader.putString(SORTMODE_BUNDLE_KEY, sortMode);

            getSupportLoaderManager().restartLoader(MOVIE_LOADER_ID, bundleForLoader, callbacks);
        }

        else {
            mGridView.setVisibility(View.INVISIBLE);
            mNoConnectionTextView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Loads the movies from the API if a network connection is available, displays an error otherwise.
     * This is called in onCreate in order to initialize the loader or to reload information that is
     * cached on the loader.
     * @param sortMode The sort mode: by popularity or rating
     */
    public void loadMoviesNoChangeInData(String sortMode) {
        if (isOnline()) {
            mGridView.setVisibility(View.VISIBLE);
            mNoConnectionTextView.setVisibility(View.INVISIBLE);

            LoaderManager.LoaderCallbacks<ArrayList<Movie>> callbacks = MainActivity.this;

            Bundle bundleForLoader = new Bundle();
            bundleForLoader.putString(SORTMODE_BUNDLE_KEY, sortMode);

            getSupportLoaderManager().initLoader(MOVIE_LOADER_ID, bundleForLoader, callbacks);
        }

        else {
            mGridView.setVisibility(View.INVISIBLE);
            mNoConnectionTextView.setVisibility(View.VISIBLE);
        }
    }
}