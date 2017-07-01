package com.zaitoun.talat.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.zaitoun.talat.popularmovies.data.PopularMoviesImageInternalStorage;
import com.zaitoun.talat.popularmovies.utilities.MovieNetworkUtils;

import java.util.ArrayList;

public class MovieAdapter extends ArrayAdapter<Movie> {

    public MovieAdapter(Activity context, ArrayList<Movie> movies) {
        super(context, 0, movies);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Movie movie = getItem(position);

        /* If this is the first time creating the view, inflate it from the layout */
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.movie_grid_item, parent, false);
        }

        /* This MovieAdapter initializes the movie posters in the grid view. Two things can happen:
         * 1) The movie data we receive is from the TMDB and we populate the grid view using Picasso
         *    by specifying an image url.
         * 2) The movie data we receive is from the SQLite database adn we populate the grid view
         *    using images that are stored on the internal storage.
         * We use shared preferences to differentiate between the two possibilities
         */

        Context context = getContext();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String value = sharedPreferences.getString(context.getResources().getString(R.string.pref_movie_key),
                context.getResources().getString(R.string.pref_movie_popularity_value));

        ImageView moviePoster = (ImageView) convertView.findViewById(R.id.iv_movie_poster);

        /* Get device width and height in pixels */
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float deviceWidth = displayMetrics.widthPixels;

        /* This is a temporary solution to the white gaps in the grid view */
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            moviePoster.getLayoutParams().width = (int) deviceWidth/2;
            moviePoster.requestLayout();
        }

        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            moviePoster.getLayoutParams().width = (int) deviceWidth/4;
            moviePoster.requestLayout();
        }

        /* When the data is from the SQLite database, get the poster image from the device storage */
        if (value.equals(context.getResources().getString(R.string.pref_movie_bookmarked_value))) {
            if (movie.LOCAL_POSTER_PATH != null) {
                Bitmap bitmap = PopularMoviesImageInternalStorage.loadFromInternalStorage(movie.LOCAL_POSTER_PATH);
                moviePoster.setImageBitmap(bitmap);
            }
        }

        /* When the data is from the TMDB, use Picasso to load the image into the image view */
        else {

            if (movie.POSTER_PATH != null) {
                String imageUrl = MovieNetworkUtils.IMAGES_BASE_URL + MovieNetworkUtils.IMAGE_SIZE +
                        movie.POSTER_PATH;

                Picasso.with(getContext()).load(imageUrl).into(moviePoster);
            }
        }

        return convertView;
    }
}