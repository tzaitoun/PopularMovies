package com.zaitoun.talat.popularmovies.utilities;

import com.zaitoun.talat.popularmovies.Movie;
import com.zaitoun.talat.popularmovies.MovieReview;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MovieJsonUtils {

    private static final String STATUS_CODE = "status_code";

    /**
     * Parses a JSON string into an ArrayList of Movie objects.
     *
     * @param jsonString The JSON string that will be parsed
     * @return The ArrayList of Movie objects, null if an error occurs.
     */
    public static ArrayList<Movie> getMovieInformationFromJson(String jsonString)
            throws JSONException {

        JSONObject root = new JSONObject(jsonString);

        /* The API returns a status code when an error occurs */
        if (root.has(STATUS_CODE)) {
            return null;
        }

        /* Each movie result is an element of the results array */
        final String RESULTS = "results";

        /* The JSON array of movies */
        JSONArray resultsArray = root.getJSONArray(RESULTS);

        /* The array that will hold the movies */
        ArrayList<Movie> movies = new ArrayList<>(resultsArray.length());

        final String MOVIE_ID = "id";
        final String ORIGINAL_TITLE = "original_title";
        final String POSTER_PATH = "poster_path";
        final String BACKDROP_PATH = "backdrop_path";
        final String OVERVIEW = "overview";
        final String VOTE_AVERAGE = "vote_average";
        final String RELEASE_DATE = "release_date";

        /* Create each Movie object and add it to the ArrayList */
        for (int i = 0; i < resultsArray.length(); i++) {

            JSONObject jsonMovie = resultsArray.getJSONObject(i);

            String movie_id = jsonMovie.getString(MOVIE_ID);
            String original_title = jsonMovie.getString(ORIGINAL_TITLE);
            String poster_path = jsonMovie.getString(POSTER_PATH);
            String backdrop_path = jsonMovie.getString(BACKDROP_PATH);
            String overview = jsonMovie.getString(OVERVIEW);
            String vote_average = jsonMovie.getString(VOTE_AVERAGE);
            String release_date = jsonMovie.getString(RELEASE_DATE);

            Movie movie = new Movie(movie_id, original_title, poster_path, backdrop_path, overview,
                    vote_average, release_date, null);

            movies.add(i, movie);
        }

        return movies;
    }

    /**
     * Parses a JSON string into an ArrayList of MovieReview objects.
     * This only gets the first two movie reviews.
     * @param jsonString The JSON string that will be parsed
     * @return The ArrayList of MovieReview objects, null if an error occurs.
     */
    public static ArrayList<MovieReview> getMovieReviews(String jsonString) throws JSONException {

        JSONObject root = new JSONObject(jsonString);

        /* The API returns a status code when an error occurs */
        if (root.has(STATUS_CODE)) {
            return null;
        }

        /* Each review result is an element of the results array */
        final String RESULTS = "results";

        /* The JSON array of reviews */
        JSONArray resultsArray = root.getJSONArray(RESULTS);

        ArrayList<MovieReview> movieReviews = new ArrayList<>();

        final String AUTHOR = "author";
        final String CONTENT = "content";

        /* We will only get the first 2 reviews */
        for (int i = 0; i < resultsArray.length(); i++) {

            if (i == 2) {
                break;
            }

            JSONObject jsonReview = resultsArray.getJSONObject(i);

            String author = jsonReview.getString(AUTHOR);
            String content = jsonReview.getString(CONTENT);

            MovieReview movieReview = new MovieReview(author, content);

            movieReviews.add(i, movieReview);
        }

        return movieReviews;
    }

    /**
     * Parses a JSON string into a movie trailer string.
     * This only gets the first trailer.
     * @param jsonString The JSON string that will be parsed
     * @return A string of the first trailer, null if an error occurs
     */
    public static String getMovieTrailer(String jsonString) throws JSONException {
        JSONObject root = new JSONObject(jsonString);

        /* The API returns a status code when an error occurs */
        if (root.has(STATUS_CODE)) {
            return null;
        }

        /* Each trailer result is an element of the results array */
        final String RESULTS = "results";

        /* The JSON array of trailers */
        JSONArray resultsArray = root.getJSONArray(RESULTS);

        final String TRAILER_KEY = "key";

        String trailerKey = null;

        /* if the resultArray contains at least one trailer result, we take the first result and return it */
        if (resultsArray.length() >= 1) {

            JSONObject jsonTrailer = resultsArray.getJSONObject(0);

            trailerKey = jsonTrailer.getString(TRAILER_KEY);
        }

        return trailerKey;
    }
}
