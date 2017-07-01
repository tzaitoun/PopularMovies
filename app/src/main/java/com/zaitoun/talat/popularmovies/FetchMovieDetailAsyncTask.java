package com.zaitoun.talat.popularmovies;


import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.zaitoun.talat.popularmovies.utilities.MovieJsonUtils;
import com.zaitoun.talat.popularmovies.utilities.MovieNetworkUtils;

import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;

/* Fetches the reviews and trailers of a movie, you have to pass to it the movie id
 * In the constructor, pass the views that need to be updated with the new information */

public class FetchMovieDetailAsyncTask extends AsyncTask<String, Void, String[]> {

    private TextView userOne;
    private TextView reviewOne;
    private TextView userTwo;
    private TextView reviewTwo;
    private ImageView playTrailer;

    public FetchMovieDetailAsyncTask(TextView user_one, TextView review_one, TextView user_two, TextView review_two,
                                     ImageView play_trailer) {
        userOne = user_one;
        reviewOne = review_one;
        userTwo = user_two;
        reviewTwo = review_two;
        playTrailer = play_trailer;
    }

    @Override
    protected String[] doInBackground(String... params) {

        /* if no movie id is passed */
        if (params.length == 0) {
            return null;
        }

        String movieId = params[0];

        /* Urls to get a movie's reviews and trailers */
        URL movieReviewsUrl = MovieNetworkUtils.buildMovieDetailUrl(movieId, MovieNetworkUtils.REVIEWS);
        URL movieTrailersUrl = MovieNetworkUtils.buildMovieDetailUrl(movieId, MovieNetworkUtils.TRAILERS);

        try {

            String jsonMovieReviews = MovieNetworkUtils.getResponseFromHttpUrl(movieReviewsUrl);
            String jsonMovieTrailers = MovieNetworkUtils.getResponseFromHttpUrl(movieTrailersUrl);

            String[] jsonMovieDetails = new String[] {jsonMovieReviews, jsonMovieTrailers};

            return jsonMovieDetails;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(String[] jsonData) {

        /* There should be 2 json Strings in the array */
        if (jsonData != null && jsonData.length == 2) {

            ArrayList<MovieReview> movieReviews = null;
            String movieTrailer = null;

            try {
                movieReviews = MovieJsonUtils.getMovieReviews(jsonData[0]);
                movieTrailer = MovieJsonUtils.getMovieTrailer(jsonData[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }

            /* if no error occurred fetching the movie reviews */
            if (movieReviews != null) {

                /* If there are at least 2 movie reviews, display them both */
                if (movieReviews.size() >= 2) {
                    userOne.setText(movieReviews.get(0).AUTHOR);
                    reviewOne.setText(movieReviews.get(0).REVIEW);
                    userTwo.setText(movieReviews.get(1).AUTHOR);
                    reviewTwo.setText(movieReviews.get(1).REVIEW);
                }

                /* If there is one movie review, display it and hide the other unused views */
                else if (movieReviews.size() == 1) {
                    userOne.setText(movieReviews.get(0).AUTHOR);
                    reviewOne.setText(movieReviews.get(0).REVIEW);
                    userTwo.setVisibility(View.GONE);
                    reviewTwo.setVisibility(View.GONE);
                }

                /* If there isn't any reviews, hide both views */
                else {
                    userOne.setVisibility(View.GONE);
                    reviewOne.setVisibility(View.GONE);
                    userTwo.setVisibility(View.GONE);
                    reviewTwo.setVisibility(View.GONE);
                }
            }

            /* If no error occurred fetching the trailer and the string is not empty */
            if (movieTrailer != null && !movieTrailer.isEmpty()) {
                /* Put the trailer link into the ImageView, so we can access it from DetailActivity */
                playTrailer.setTag(movieTrailer);
            }
        }
    }
}
