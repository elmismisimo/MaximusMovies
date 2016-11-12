package com.sandersoft.maximusmovies.controlers;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import com.sandersoft.maximusmovies.ApplicationMain;
import com.sandersoft.maximusmovies.interfaces.WebManagerListener;
import com.sandersoft.maximusmovies.models.MovieModel;
import com.sandersoft.maximusmovies.models.tmdb.Image;
import com.sandersoft.maximusmovies.models.tmdb.Images;
import com.sandersoft.maximusmovies.views.MoviesViewFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Sander on 09/11/2016.
 */
public class MoviesController implements WebManagerListener {

    MoviesViewFragment movieView;
    int page = 1;
    String search = "";
    String list = "";

    ArrayList<MovieModel> movies = new ArrayList<>();

    public MoviesController(MoviesViewFragment caller){
        movieView = caller;
    }

    public void setAsWebListener(){
        ApplicationMain.webManager.setWebManagerListener(this);
    }

    public ArrayList<MovieModel> getMovies(){
        return movies;
    }
    public void clearMovies(){
        movies.clear();
    }
    public void findImage(ImageView imageHolder, int position){
        if (position >= movies.size() || movies.get(position).getPoster() != null) return;
        if (movies.get(position).getImages() == null)
            doImagesRequest(String.valueOf(movies.get(position).getIds().getTmdb()), movies.get(position));
        else
            doImageRequest(imageHolder, movies.get(position));
    }

    /**
     * Do a movies request for first time (from page one),and return 10 pages
     * @param search The search term (it can be empty)
     */
    public void doMoviesRequest(String search, String list){
        this.page = 1; //set the page as the initial one
        this.search = search; //define the latest search term
        this.list = list; //define the list
        //do the movies request
        ApplicationMain.webManager.doMoviesRequest(list + "?limit=10&page=" + page + (search.trim().equals("") ? "" : "&query="), search);
    }
    /**
     * Do a movies request for the next page of the last request
     */
    public void doMoviesNextPageRequest(){
        //increment the page by one to get new page
        page++;
        //do movies request
        ApplicationMain.webManager.doMoviesRequest(list + "?limit=10&page=" + page + (search.trim().equals("") ? "" : "&query="), search);
    }
    public void doMovieRequest(String traktId){
        ApplicationMain.webManager.doMovieRequest(traktId + "?extended=full");
    }

    public void doImagesRequest(String tmdb_id, MovieModel movie){
        ApplicationMain.webManager.doImagesRequest(tmdb_id, movie);
    }

    public void doImageRequest(ImageView imageHolder, MovieModel movie) {
        doImageRequest(imageHolder, movie, "w185");
    }
    public void doImageRequest(ImageView imageHolder, MovieModel movie, String size){
        ApplicationMain.webManager.doImageRequest(imageHolder, movie, size);
    }

    @Override
    public void onReceiveHttpAnswer(MovieModel[] movies, int cant, int page, String search) {
        //verify if search is the same as latest
        if (!this.search.equals(search)) return;
        page = (page-1) * 10;
        if (this.movies.size() > page)
            this.movies.subList(page, this.movies.size()).clear();
        this.movies.addAll(Arrays.asList(movies));
        movieView.receiveMovies(cant);
    }

    @Override
    public void onReceiveHttpAnswerError(String error) {
        Log.e("MoviewController", error);
    }

    @Override
    public void onReceiveHttpTMDB(Images images, MovieModel movie) {
        int movieIndex = movies.lastIndexOf(movie);
        if (movieIndex != -1){
            movie.setImages(images);
            movieView.receiveMovieImages(movieIndex);
        }
    }

    @Override
    public void onReceiveHttpTMDBImage(Bitmap image, MovieModel movie) {
        int movieIndex = movies.lastIndexOf(movie);
        if (movieIndex != -1) {
            movie.setPoster(image);
            movieView.receiveMovieImages(movieIndex);
        }
    }
}
