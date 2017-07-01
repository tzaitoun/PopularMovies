package com.zaitoun.talat.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

public class Movie implements Parcelable {

    final String MOVIE_ID;
    final String ORIGINAL_TITLE;
    final String POSTER_PATH;
    final String BACKDROP_PATH;
    final String OVERVIEW;
    final String VOTE_AVERAGE;
    final String RELEASE_DATE;
    final String LOCAL_POSTER_PATH;


    public Movie(String movie_id, String original_title, String poster_path, String backdrop_path,
                 String overview, String vote_average, String release_date, String local_poster_path) {

        MOVIE_ID = movie_id;
        ORIGINAL_TITLE = original_title;
        POSTER_PATH = poster_path;
        BACKDROP_PATH = backdrop_path;
        OVERVIEW = overview;
        VOTE_AVERAGE = vote_average;
        RELEASE_DATE = release_date;
        LOCAL_POSTER_PATH = local_poster_path;
    }

    private Movie(Parcel in) {
        MOVIE_ID = in.readString();
        ORIGINAL_TITLE = in.readString();
        POSTER_PATH = in.readString();
        BACKDROP_PATH = in.readString();
        OVERVIEW = in.readString();
        VOTE_AVERAGE = in.readString();
        RELEASE_DATE = in.readString();
        LOCAL_POSTER_PATH = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(MOVIE_ID);
        dest.writeString(ORIGINAL_TITLE);
        dest.writeString(POSTER_PATH);
        dest.writeString(BACKDROP_PATH);
        dest.writeString(OVERVIEW);
        dest.writeString(VOTE_AVERAGE);
        dest.writeString(RELEASE_DATE);
        dest.writeString(LOCAL_POSTER_PATH);
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {

        @Override
        public Movie createFromParcel(Parcel source) {
            return new Movie(source);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}
