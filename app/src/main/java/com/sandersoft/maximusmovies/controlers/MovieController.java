package com.sandersoft.maximusmovies.controlers;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import com.sandersoft.maximusmovies.ApplicationMain;
import com.sandersoft.maximusmovies.interfaces.WebManagerListener;
import com.sandersoft.maximusmovies.models.MovieModel;
import com.sandersoft.maximusmovies.models.QueryModel;
import com.sandersoft.maximusmovies.models.tmdb.Images;
import com.sandersoft.maximusmovies.views.ActivityMainFragment;

/**
 * Created by Sander on 09/11/2016.
 */
public class MovieController implements WebManagerListener {

    ActivityMainFragment movieView;
    int page = 1;
    String search = "";

    public MovieController(ActivityMainFragment caller){
        movieView = caller;
    }

    public void setAsWebListener(){
        ApplicationMain.webManager.setWebManagerListener(this);
    }

    /**
     * Do a movies request for first time (from page one),and return 10 pages
     * @param search The search term (it can be empty)
     */
    public void doMoviesRequest(String search){
        this.page = 1; //set the page as the initial one
        this.search = search; //define the search term
        //do the movies request
        ApplicationMain.webManager.doMoviesRequest("limit=10&page=" + page + (search.trim().equals("") ? "" : "&query=" + search));
    }

    /**
     * Do a movies request for the next page of the last request
     */
    public void doMoviesNextPageRequest(){
        //increment the page by one to get new page
        page++;
        //do movies request
        ApplicationMain.webManager.doMoviesRequest("limit=10&page=" + page + (search.trim().equals("") ? "" : "&query=" + search));
    }
    public void doMovieRequest(String traktId){
        ApplicationMain.webManager.doMoviesRequest(traktId + "?extended=full");
    }

    public void doImageRequest(ImageView imageHolder, String imageUrl){
        ApplicationMain.webManager.doImageRequest(imageHolder, imageUrl);
    }

    @Override
    public void onReceiveHttpAnswer(MovieModel movie) {
        movieView.receiveMovies(movie.toString());
    }

    @Override
    public void onReceiveHttpAnswer(QueryModel[] queries, int cant) {
        int i = 0;
        MovieModel[] movies = new MovieModel[queries.length];
        for (QueryModel q : queries)
            movies[i++] = q.getMovie();
        movieView.receiveMovies(cant + " " + movies.toString());
    }

    @Override
    public void onReceiveHttpAnswerError(String error) {
        Log.e("MoviewController", error);
    }

    @Override
    public void onReceiveHttpTMDB(Images images) {

    }
}
