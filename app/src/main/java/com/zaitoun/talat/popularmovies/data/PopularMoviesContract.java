package com.zaitoun.talat.popularmovies.data;


import android.net.Uri;

public class PopularMoviesContract {

    /* The content authority: this is how the code knows which Content Provider to access */
    public static final String AUTHORITY = "com.zaitoun.talat.popularmovies";

    /* The base Uri to access the data in our application */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    /* A path used to access our database */
    public static final String PATH_BOOKMARKED_MOVIES = "bookmarked_movies";

    /* This class represents an entry in our bookmarked_movies table */
    public static final class BookmarkedMoviesEntry {

        /* The Uri used to access our bookmarked_movies table */
        public static final Uri CONTENT_URI =  BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_BOOKMARKED_MOVIES).build();

        public static final String TABLE_NAME = "bookmarked_movies";

        /* Each movie id is unique, and this is how we will find a specific movie in the database */
        public static final String MOVIE_ID = "movieId";

        /* This is all the data that we will store to display the movie information offline */

        /* This path is a local path on the android device */
        public static final String COLUMN_MOVIE_POSTER_PATH = "moviePosterPath";

        public static final String COLUMN_MOVIE_TITLE = "movieTitle";
        public static final String COLUMN_MOVIE_OVERVIEW = "movieOverview";
        public static final String COLUMN_MOVIE_DATE = "movieDate";
        public static final String COLUMN_MOVIE_RATING = "movieRating";
        public static final String COLUMN_MOVIE_BACKDROP_PATH = "movieBackdropPath";
    }
}
