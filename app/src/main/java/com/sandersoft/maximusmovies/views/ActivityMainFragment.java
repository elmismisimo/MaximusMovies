package com.sandersoft.maximusmovies.views;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sandersoft.maximusmovies.R;
import com.sandersoft.maximusmovies.controlers.MovieController;

/**
 * A placeholder fragment containing a simple view.
 */
public class ActivityMainFragment extends Fragment {

    TextView ret_text;
    ImageView ret_img;
    MovieController movieController;

    public ActivityMainFragment() {
        movieController = new MovieController(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_activity_main, container, false);
        ret_text = (TextView) rootview.findViewById(R.id.ret_text);
        ret_img = (ImageView) rootview.findViewById(R.id.ret_img);
        //TODO buscamos una imagen (boorar esta instruccion)
        doImageRequest("aqhAqttDq7zgsTaBHtCD8wmTk6k.jpg");
        return rootview;
    }

    public void setAsWebListener(){
        movieController.setAsWebListener();
    }

    /**
     * Request the first page of movies
     */
    public void doMoviesRequest(){
        doMoviesRequest("");
    }

    /**
     * Request the first page of movies using a search
     * @param search
     */
    public void doMoviesRequest(String search){
        movieController.doMoviesRequest(search);
    }

    /**
     * request the next page of the last movies search
     */
    public void doMoviesNextPageRequest(){
        movieController.doMoviesNextPageRequest();
    }

    //borrar esta funcion
    public void doImageRequest(String imageUrl){
        movieController.doImageRequest(ret_img, imageUrl);
    }

    /**
     * Receive the response of a movies search
     * @param saludo
     */
    public void receiveMovies(String saludo){
        ret_text.setText(saludo);
    }
}
