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

    //the view of the controller
    MoviesViewFragment movieView;
    //the terms of the search of elements
    int page = 1;
    String search = "";

    //list of movie models that will store the fetched elements
    ArrayList<MovieModel> movies = new ArrayList<>();

    public MoviesController(MoviesViewFragment caller){
        movieView = caller;
    }

    /**
     * Define thsi controller as the listener of the webmanager (the web manager will report to
     * this controller after every request
     */
    public void setAsWebListener(){
        ApplicationMain.webManager.setWebManagerListener(this);
    }

    /**
     * Return the list of movies
     * @return
     */
    public ArrayList<MovieModel> getMovies(){
        return movies;
    }
    /**
     * Set the list of movies, usually this is called when an activity was destroyed and recreated (example: rotaion)
     * @param newMovies
     */
    public void setMovies(ArrayList<MovieModel> newMovies){
        movies = newMovies;
    }
    /**
     * Empty the movie list
     */
    public void clearMovies(){
        movies.clear();
        addLoadingElement();
    }

    /**
     * Add an null element to the list that will be interpreted as a loading reference
     */
    public void addLoadingElement(){
        //if the list already contain null elements, delete them
        while (movies.contains(null)) movies.remove(null);
        //add the null element to the back of the list
        movies.add(null);
    }

    /**
     * function that checks if it has to do a search for the movie´s images_url or a search of one image
     * @param position
     */
    public void findImage(int position){
        //verify if the element at the position is a valid movie element
        if (position >= movies.size() || movies.get(position).getPoster() != null) return;
        //if no image urls have been fetched, it requests the images, else it request the image bitmap
        if (movies.get(position).getImages() == null)
            doImagesRequest(String.valueOf(movies.get(position).getIds().getTmdb()), movies.get(position));
        else
            doImageRequest(movies.get(position));
    }

    /**
     * Do a movies request for first time (from page one),and return 10 pages
     * @param search The search term (it can be empty)
     */
    public void doMoviesRequest(String search){
        this.page = 1; //set the page as the initial one
        this.search = search; //define the latest search term
        //do the movies request
        ApplicationMain.webManager.doMoviesRequest("popular?limit=10&page=" + page + (search.trim().equals("") ? "" : "&query="), search);
    }
    /**
     * Do a movies request for the next page of the last request
     */
    public void doMoviesNextPageRequest(){
        //increment the page by one to get new page
        page++;
        //do movies request
        ApplicationMain.webManager.doMoviesRequest("popular?limit=10&page=" + page + (search.trim().equals("") ? "" : "&query="), search);
    }
    /**
     * Request the set of url of the images from the movie, it fetches them fomr TMDB
     * @param tmdb_id ID of the movie in TMDB
     * @param movie Movie that will receive the images
     */
    public void doImagesRequest(String tmdb_id, MovieModel movie){
        ApplicationMain.webManager.doImagesRequest(tmdb_id, movie);
    }
    /**
     * Request a bitmap image from TMDB with the lowest setting available (w185)
     * @param movie The movie object that will receive the image
     */
    public void doImageRequest(MovieModel movie) {
        //sets the request to fetch the w185 image from the server (if available)
        doImageRequest(movie, "w185");
    }
    /**
     * Request a bitmap image from TMDB
     * @param movie The movie object that will receive the image
     * @param size The desired size of the image (w185,w500,original are preferred)
     */
    public void doImageRequest(MovieModel movie, String size){
        //executes the fetch (the first param ImageView is null because we dont need it, but the funtion requires it)
        ApplicationMain.webManager.doImageRequest(null, movie, size);
    }

    //TODO delete this funciton, it must be in the MovieDetalController
    public void doMovieRequest(String traktId){
        ApplicationMain.webManager.doMovieRequest(traktId + "?extended=full");
    }

    //receive the movies result
    @Override
    public void onReceiveHttpAnswer(MovieModel[] movies, int cant, int page, String search) {
        //verify if search is the same as latest
        if (!this.search.equals(search)) return;
        page = (page-1) * 10;
        //verify if there is a loading element
        while (this.movies.contains(null))
            this.movies.remove(null);
        //clear the element from the list that will be replaced with this request
        if (this.movies.size() > page)
            this.movies.subList(page, this.movies.size()).clear();
        //add all the elements from the list
        this.movies.addAll(Arrays.asList(movies));
        //notify the view that the movie list changed
        movieView.receiveMovies(cant);
    }
    //receive an error of the movie fetch
    @Override
    public void onReceiveHttpAnswerError(String error) {
        Log.e("MoviewController", error);
    }
    //receive the images from TMDB
    @Override
    public void onReceiveHttpTMDB(Images images, MovieModel movie) {
        int movieIndex = movies.lastIndexOf(movie);
        if (movieIndex != -1){
            //set the images on the movie
            movie.setImages(images);
            //go find the poster
            findImage(movieIndex);
        }
    }
    //receive bitmap image from TMDB
    @Override
    public void onReceiveHttpTMDBImage(Bitmap image, MovieModel movie) {
        int movieIndex = movies.lastIndexOf(movie);
        if (movieIndex != -1) {
            movie.setPoster(image);
            movieView.receiveMovieImages(movieIndex);
        }
    }
}
