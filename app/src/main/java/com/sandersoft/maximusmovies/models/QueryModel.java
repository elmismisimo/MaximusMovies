package com.sandersoft.maximusmovies.models;

/**
 * Created by Sander on 09/11/2016.
 */
public class QueryModel {
    private int watchers;
    private MovieModel movie;

    public MovieModel getMovie() {
        return movie;
    }

    public void setMovie(MovieModel movie) {
        this.movie = movie;
    }

    public int getWatchers() {
        return watchers;
    }

    public void setWatchers(int watchers) {
        this.watchers = watchers;
    }
}
