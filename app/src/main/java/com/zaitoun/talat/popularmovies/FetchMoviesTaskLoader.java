package com.zaitoun.talat.popularmovies;


import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.zaitoun.talat.popularmovies.utilities.MovieJsonUtils;
import com.zaitoun.talat.popularmovies.utilities.MovieNetworkUtils;

import java.net.URL;
import java.util.ArrayList;


public class FetchMoviesTaskLoader extends AsyncTaskLoader<ArrayList<Movie>> {

    /* An array used to cache our movie data */
    private ArrayList<Movie> movies;

    private String sortMode;
    private ProgressBar loadingIndicator;

    public FetchMoviesTaskLoader(Context context, String sortMode, ProgressBar loadingIndicator) {
        super(context);
        this.sortMode = sortMode;
        this.loadingIndicator = loadingIndicator;
    }

    /* If the data was cached, deliver result to activity. If not, fetch the data from TMDB */
    @Override
    protected void onStartLoading() {

        if (movies != null) {
            deliverResult(movies);
        }

        else {
            loadingIndicator.setVisibility(View.VISIBLE);
            forceLoad();
        }
    }

    /**
     * This is the method of the AsyncTaskLoader that will load and parse the JSON data
     * from TMDB in the background.
     *
     * @return ArrayList of Movie objects
     *         null if an error occurs
     */
    @Override
    public ArrayList<Movie> loadInBackground() {

        URL url = MovieNetworkUtils.buildUrl(sortMode);

        try {
            String jsonString = MovieNetworkUtils.getResponseFromHttpUrl(url);

            movies = MovieJsonUtils.getMovieInformationFromJson(jsonString);

            return movies;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Sends the result of the load to the registered listener.
     *
     * @param data The result of the load
     */
    @Override
    public void deliverResult(ArrayList<Movie> data) {
        movies = data;
        super.deliverResult(data);
    }
}
