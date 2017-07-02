package com.zaitoun.talat.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.zaitoun.talat.popularmovies.data.PopularMoviesContract;
import com.zaitoun.talat.popularmovies.data.PopularMoviesImageInternalStorage;
import com.zaitoun.talat.popularmovies.utilities.MovieNetworkUtils;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.zaitoun.talat.popularmovies.data.PopularMoviesContract.*;

public class DetailActivity extends AppCompatActivity {

    private ImageView mBackdrop;
    private TextView mTitle;
    private TextView mOverview;
    private TextView mRating;
    private TextView mReleaseDate;
    private TextView mUserOne;
    private TextView mReviewOne;
    private TextView mUserTwo;
    private TextView mReviewTwo;
    private ImageView mPlayTrailer;
    private ImageView mBookmark;
    private ImageView mBorderOne;
    private ImageView mBorderTwo;
    private TextView mTrailerTitle;

    /* This is the poster that we store on the device, we store it in this variable when we have
     * access to the internet.
     */
    private Bitmap mPosterBitmap;

    /* This variable stores whether the movie is bookmarked or not, we query the database to figure
     * out if the movie is bookmarked. This variable can change when the user interacts with the
     * bookmark icon.
     */
    private boolean mBookmarkedMovie;

    /* We store local poster path so we can delete it, if the user unbookmarks from outside the
     * bookmarks page.
     */
    private String mLocalPosterPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ActionBar actionBar = this.getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mBackdrop = (ImageView) findViewById(R.id.iv_detail_poster);
        mTitle = (TextView) findViewById(R.id.tv_title);
        mOverview = (TextView) findViewById(R.id.tv_overview);
        mRating = (TextView) findViewById(R.id.tv_rating);
        mReleaseDate = (TextView) findViewById(R.id.tv_date);
        mUserOne = (TextView) findViewById(R.id.tv_user_1);
        mReviewOne = (TextView) findViewById(R.id.tv_review_1);
        mUserTwo = (TextView) findViewById(R.id.tv_user_2);
        mReviewTwo = (TextView) findViewById(R.id.tv_review_2);
        mPlayTrailer = (ImageView) findViewById(R.id.iv_play_trailer);
        mBookmark = (ImageView) findViewById(R.id.iv_bookmark);
        mBorderOne = (ImageView) findViewById(R.id.iv_border_trailer);
        mBorderTwo = (ImageView) findViewById(R.id.iv_border_reviews);
        mTrailerTitle = (TextView) findViewById(R.id.tv_trailer);

        Intent intentThatStartedThisActivity = getIntent();

        /* If the intent is valid and has the movie object as an extra, display the information to the user */
        if (intentThatStartedThisActivity != null) {
            if (intentThatStartedThisActivity.hasExtra(Intent.EXTRA_TEXT)) {

                /* This movie object is obtained either from the SQLite database if viewing the bookmarked
                 * movies or from the TMDB if viewing popular/top rated movies
                 */
                final Movie movie = intentThatStartedThisActivity.getExtras().getParcelable(Intent.EXTRA_TEXT);

                mTitle.setText(movie.ORIGINAL_TITLE);
                mOverview.setText(movie.OVERVIEW);
                mRating.setText(movie.VOTE_AVERAGE + "/10");
                mReleaseDate.setText(formatDate(movie.RELEASE_DATE));

                /* When online, load the movie details (trailer, reviews, backdrop picture) */
                if (isOnline()) {

                    /* Load the backdrop into the image view */
                    String backdropUrl = MovieNetworkUtils.IMAGES_BASE_URL + MovieNetworkUtils.IMAGE_SIZE_BIG +
                            movie.BACKDROP_PATH;

                    Picasso.with(getApplicationContext()).load(backdropUrl).into(mBackdrop);

                    /* Get a reference to the poster bitmap so that we can save it to the device when bookmarked */
                    final String posterUrl = MovieNetworkUtils.IMAGES_BASE_URL + MovieNetworkUtils.IMAGE_SIZE +
                            movie.POSTER_PATH;

                    new AsyncTask<Void, Void, Void>() {

                        @Override
                        protected Void doInBackground(Void... params) {
                            try {
                                mPosterBitmap = Picasso.with(getApplicationContext()).load(posterUrl).get();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                    }.execute();

                    /* Get the reviews and trailer for the movie */
                    FetchMovieDetailAsyncTask fetchMovieDetailAsyncTask = new FetchMovieDetailAsyncTask(
                            mUserOne, mReviewOne, mUserTwo, mReviewTwo, mPlayTrailer);

                    fetchMovieDetailAsyncTask.execute(movie.MOVIE_ID);

                    mPlayTrailer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            playTrailer();
                        }
                    });
                }

                /* If the device is not online, then we will only show the movie information that is
                 * stored in the movie object, and we will hide all other unnecessary views.
                 */
                else {
                    mBackdrop.setVisibility(View.GONE);
                    mPlayTrailer.setVisibility(View.GONE);
                    mBorderOne.setVisibility(View.GONE);
                    mBorderTwo.setVisibility(View.GONE);
                    mTrailerTitle.setVisibility(View.GONE);
                }

                final Context context = getApplicationContext();

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                final String value = sharedPreferences.getString(context.getResources().getString(R.string.pref_movie_key),
                        context.getResources().getString(R.string.pref_movie_popularity_value));

                /* Initialize the bookmark icon */

                /* If the user came from the bookmarked movies, then the movie is bookmarked */
                if (value.equals(context.getResources().getString(R.string.pref_movie_bookmarked_value))) {
                    mBookmark.setImageResource(R.mipmap.ic_bookmark_black_24dp);
                    mBookmark.setColorFilter(ContextCompat.getColor(context, R.color.colorAccent));
                    mBookmarkedMovie = true;
                }

                /* The user came from the popular/top rated movies, so we have to check if it is bookmarked */
                else {
                    /* Check if movie is bookmarked and update UI, if it is retrieve the local poster path */
                    mLocalPosterPath = checkBookmarked(context, movie);
                }

                mBookmark.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        /* If the user came from the bookmarks section */
                        if (value.equals(context.getResources().getString(R.string.pref_movie_bookmarked_value))) {

                            /* If you click on it when its bookmarked, it means the user wants to unbookmark */
                            if (mBookmarkedMovie) {
                                unbookmarkMovie(context, movie, movie.LOCAL_POSTER_PATH);

                                /* Exit activity */
                                finish();

                            } else {
                                Toast.makeText(context, getString(R.string.bookmark_from_bookmarks_not_allowed), Toast.LENGTH_LONG)
                                        .show();
                            }
                        }

                        /* If the user came from the popular movies or top rated section */
                        else {

                            /* If you click on it when its bookmarked, it means the user wants to unbookmark */
                            if (mBookmarkedMovie) {
                                unbookmarkMovie(context, movie, mLocalPosterPath);
                            } else {
                                bookmarkMovie(context, movie);
                            }
                        }
                    }
                });
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // When the home button is pressed, take the user back to the MainActivity
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Plays the trailer of a movie
     */
    private void playTrailer() {

        /* Check if there is a valid tag */
        if (mPlayTrailer.getTag() != null) {
        /* Intent to play the trailer in either youtube app or internet app */
            Intent playTrailerIntent = new Intent(Intent.ACTION_VIEW,
                    MovieNetworkUtils.buildTrailerUri(mPlayTrailer.getTag().toString()));

        /* Check if the device running has an activity/app to perform the action specified */
            if (playTrailerIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(playTrailerIntent);
            }
        }
    }

    /**
     * Formats the date to user-friendly format.
     *
     * @param date The date that will be formatted.
     * @return A String with our reformatted date.
     */
    private String formatDate(String date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date releaseDate = null;

        try {
            releaseDate = simpleDateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");

        return sdf.format(releaseDate).toString();
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

    /**
     * Creates a Content Values and stores movie information in it, to insert it into the database.
     *
     * @param movie The movie we wish to query.
     * @param local_poster_path The path where the poster was saved on the device.
     */
    public void insertMovieIntoDatabase(Movie movie, String local_poster_path) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(BookmarkedMoviesEntry.MOVIE_ID, movie.MOVIE_ID);
        contentValues.put(BookmarkedMoviesEntry.COLUMN_MOVIE_POSTER_PATH, local_poster_path);
        contentValues.put(BookmarkedMoviesEntry.COLUMN_MOVIE_TITLE, movie.ORIGINAL_TITLE);
        contentValues.put(BookmarkedMoviesEntry.COLUMN_MOVIE_OVERVIEW, movie.OVERVIEW);
        contentValues.put(BookmarkedMoviesEntry.COLUMN_MOVIE_DATE, movie.RELEASE_DATE);
        contentValues.put(BookmarkedMoviesEntry.COLUMN_MOVIE_RATING, movie.VOTE_AVERAGE);
        contentValues.put(BookmarkedMoviesEntry.COLUMN_MOVIE_BACKDROP_PATH, movie.BACKDROP_PATH);

        getContentResolver().insert(BookmarkedMoviesEntry.CONTENT_URI, contentValues);
    }

    /**
     * Delete the movie entry from the database
     *
     * @param movie The movie we wish to delete.
     */
    public void deleteMovieFromDatabase(Movie movie) {

        Uri deleteUri = BookmarkedMoviesEntry.CONTENT_URI.buildUpon().appendPath(movie.MOVIE_ID).build();

        getContentResolver().delete(deleteUri, null, null);
    }

    /**
     * Queries for a movie, to check if it exists
     *
     * @param movie The movie we wish to query.
     * @return A cursor with our query data. Null if an error occurs.
     */
    public Cursor queryForMovie(Movie movie) {

        Uri queryUri = BookmarkedMoviesEntry.CONTENT_URI.buildUpon().appendPath(movie.MOVIE_ID).build();

        try {
            return getContentResolver().query(queryUri, null, null, null, null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Checks if the movie is bookmarked (by querying database) and returns local poster path if it is.
     *
     * @param movie The movie we wish to query.
     * @return The local poster path. Null: if the movie is not bookmarked or the cursor is invalid.
     */
    public String checkBookmarked(Context context, Movie movie) {

        Cursor cursor = queryForMovie(movie);

        if (cursor != null) {

            final int LOCAL_POSTER_PATH_COLUMN = cursor.getColumnIndex(BookmarkedMoviesEntry.COLUMN_MOVIE_POSTER_PATH);

            try {
                /* If the movie is in the database, it is bookmarked */
                if (cursor.getCount() == 1 && cursor.moveToFirst()) {
                    mBookmark.setImageResource(R.mipmap.ic_bookmark_black_24dp);
                    mBookmark.setColorFilter(ContextCompat.getColor(context, R.color.colorAccent));
                    mBookmarkedMovie = true;
                    return cursor.getString(LOCAL_POSTER_PATH_COLUMN);
                }

                /* If it's not bookmarked */
                else {
                    /* Bookmark icon is unbookmarked by default */
                    mBookmarkedMovie = false;
                    return null;
                }
            } finally {
                cursor.close();
            }
        }

        return null;
    }

    /**
     * Unbookmarks movie: deletes poster image off device, off database, and updates UI.
     */
    public void unbookmarkMovie(Context context, Movie movie, String localPosterPath) {

        boolean deleted = PopularMoviesImageInternalStorage.deleteFromInternalStorage(localPosterPath);

        /* If the image was successfully deleted, delete it from the database */
        if (deleted) {
            deleteMovieFromDatabase(movie);

            mBookmarkedMovie = false;
            mBookmark.setImageResource(R.mipmap.ic_bookmark_border_black_24dp);
            mBookmark.setColorFilter(ContextCompat.getColor(context, R.color.colorAccent));
            Toast.makeText(context, getString(R.string.unbookmark), Toast.LENGTH_LONG)
                    .show();
        } else {
            Toast.makeText(context, getString(R.string.failed_unbookmark), Toast.LENGTH_LONG)
                    .show();
        }
    }

    /**
     * Bookmarks movie: saves poster image on device, file path on database, and updates UI.
     */
    public void bookmarkMovie(Context context, Movie movie) {

        String local_poster_path = PopularMoviesImageInternalStorage
                .saveToInternalStorage(context, mPosterBitmap, movie.MOVIE_ID);

        /* Copy the path */
        mLocalPosterPath = local_poster_path;

        insertMovieIntoDatabase(movie, local_poster_path);

        mBookmarkedMovie = true;
        mBookmark.setImageResource(R.mipmap.ic_bookmark_black_24dp);
        mBookmark.setColorFilter(ContextCompat.getColor(context, R.color.colorAccent));
        Toast.makeText(context, getString(R.string.bookmark), Toast.LENGTH_LONG)
                .show();
    }
}
