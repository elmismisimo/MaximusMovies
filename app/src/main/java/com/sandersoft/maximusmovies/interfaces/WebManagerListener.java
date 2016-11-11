package com.sandersoft.maximusmovies.interfaces;

import android.graphics.Bitmap;

import com.sandersoft.maximusmovies.models.MovieModel;
import com.sandersoft.maximusmovies.models.QueryModel;
import com.sandersoft.maximusmovies.models.tmdb.Images;

/**
 * Created by Sander on 10/11/2016.
 */
public interface WebManagerListener {

    //receive a successfull answer from the server
    public void onReceiveHttpAnswer(MovieModel jsonResponse);
    //receive a successfull answer from the server
    public void onReceiveHttpAnswer(QueryModel[] jsonResponse, int cant);
    //receive an error answer from the server
    public void onReceiveHttpAnswerError(String error);
    //receive list of images from TMDB
    public void onReceiveHttpTMDB(Images images);

}
