package com.zaitoun.talat.popularmovies.utilities;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class MovieNetworkUtils {

    private static final String API_KEY = "";

    private static final String MOVIES_BASE_URL = "https://api.themoviedb.org/3/movie";

    private static final String QUERY_PARAM = "api_key";

    public static final String POPULAR_MOVIES = "/popular";

    public static final String TOP_RATED_MOVIES = "/top_rated";

    public static final String REVIEWS = "/reviews";

    public static final String TRAILERS = "/videos";

    public static final String IMAGES_BASE_URL = "https://image.tmdb.org/t/p/";

    public static final String IMAGE_SIZE = "w185";

    public static final String IMAGE_SIZE_BIG = "w780";

    /**
     * Builds the URL used to talk to the movie database API
     *
     * @param movieSortMode The sort mode: by popularity or rating
     * @return The URL to use to query the movie database API
     */
    public static URL buildUrl(String movieSortMode) {
        String baseUrl = MOVIES_BASE_URL + movieSortMode;

        Uri builtUri = Uri.parse(baseUrl).buildUpon()
                .appendQueryParameter(QUERY_PARAM, API_KEY)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    /**
     * Builds the URL used to get specific movie details like trailers and reviews
     *
     * @param movieId The id of the movie that we want to fetch its details
     * @param movieDetail The movie detail: Reviews or Trailers
     * @return The URL to use to query the movie database API
     */
    public static URL buildMovieDetailUrl(String movieId, String movieDetail) {
        String baseUrl = MOVIES_BASE_URL + "/" + movieId + movieDetail;

        Uri builtUri = Uri.parse(baseUrl).buildUpon()
                .appendQueryParameter(QUERY_PARAM, API_KEY)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    /**
     * Builds the Uri used to play a trailer in youtube app or internet
     *
     * @param trailerKey The key to query in order to get the trailer
     * @return The Uri used to play the trailer
     */
    public static Uri buildTrailerUri(String trailerKey) {

        final String BASE_URL = "https://www.youtube.com/watch";
        final String QUERY_PARAM = "v";

        Uri trailer = Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter(QUERY_PARAM, trailerKey)
                .build();

        return trailer;
    }

    /**
     * This method returns the entire result from the HTTP response.
     * Author: Udacity Android Development Nanodegree Program
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}
