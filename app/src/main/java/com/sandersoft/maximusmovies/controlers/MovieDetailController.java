package com.sandersoft.maximusmovies.controlers;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.ImageView;

import com.sandersoft.maximusmovies.ApplicationMain;
import com.sandersoft.maximusmovies.interfaces.WebManagerListener;
import com.sandersoft.maximusmovies.models.MovieModel;
import com.sandersoft.maximusmovies.models.tmdb.Image;
import com.sandersoft.maximusmovies.models.tmdb.Images;
import com.sandersoft.maximusmovies.views.MovieDetailViewFragment;

import java.util.ArrayList;

/**
 * Created by Sander on 12/11/2016.
 */
public class MovieDetailController implements WebManagerListener, Parcelable {

    //the view of the controller
    MovieDetailViewFragment movieDetailView;

    //The movie being detailed
    MovieModel movie;
    Bitmap moviePosterBig;
    Bitmap trailerPreview;

    ArrayList<Bitmap> backdrops = new ArrayList<>();

    public MovieDetailController(MovieDetailViewFragment caller){
        setMovieDetailView(caller);
        //add a null object to backdrops so it is interpreted as a loading element of the list
        backdrops.add(null);
    }
    public void setMovieDetailView(MovieDetailViewFragment caller){
        movieDetailView = caller;
    }
    public void setMovieObject(MovieModel movie){
        this.movie = movie;
    }

    public MovieModel getMovie(){
        return movie;
    }
    public Bitmap getMoviePoster(){
        return moviePosterBig;
    }
    public Bitmap getMovieTrailerImage(){
        return trailerPreview;
    }
    public ArrayList<Bitmap> getBackdrops(){
        return backdrops;
    }

    /**
     * Define thsi controller as the listener of the webmanager (the web manager will report to
     * this controller after every request
     */
    public void setAsWebListener(){
        ApplicationMain.webManager.setWebManagerListener(this);
    }

    public void doInitialMovieRequest(){
        //request the full info of the movie
        ApplicationMain.webManager.doMovieRequest(this, movie.getIds().getTrakt() + "?extended=full");
        //verify the images of the movie
        if (null == movie.getImages()){//if the images has not been loaded yet (came to early to this activity)
            //request the images
            doImagesListRequest();
        } else {
            doImagesRequest();
        }
    }
    public void doImagesListRequest(){
        ApplicationMain.webManager.doImagesRequest(this, movie.getIds().getTmdb().toString(), movie);
    }
    public void doImagesRequest(){
        //request the poster in better quality
        doImageRequest(movieDetailView.img_poster);
        //request the backdrops for the reciclerview
        for (Image iurl : movie.getImages().getBackdrops())
            doImageRequest(null, iurl.getFile_path());
    }
    public void doImageRequest(ImageView imageHolder){
        //executes the fetch (the first param ImageView is null because we dont need it, but the funtion requires it)
        ApplicationMain.webManager.doImageRequest(this, imageHolder, movie, "w500");
    }
    public void doImageRequest(ImageView imageHolder, String image_url){
        //executes the fetch (the first param ImageView is null because we dont need it, but the funtion requires it)
        ApplicationMain.webManager.doImageRequest(this, imageHolder, image_url, "w500");
    }

    //receive the movies result
    @Override
    public void onReceiveHttpAnswer(MovieModel[] movies, int cant, int page, String search) {
        movie.setOverview(movies[0].getOverview());
        movie.setTrailer(movies[0].getTrailer());
        movieDetailView.drawElements();
    }
    //receive an error of the movie fetch
    @Override
    public void onReceiveHttpAnswerError(String error) {
        Log.e("MovieController", error);
        //verify if there is a loading element
        /*while (this.movies.contains(null))
            this.movies.remove(null);*/
        //notify the view that the movie list changed
        //movieView.receiveMovies();
    }
    //receive the images from TMDB
    @Override
    public void onReceiveHttpTMDB(Images images, MovieModel movie) {
        //set the images on the movie
        this.movie.setImages(images);
        //go find the poster and backdrops
        doImagesRequest();
    }
    //receive bitmap image from TMDB
    @Override
    public void onReceiveHttpTMDBImage(Bitmap image, MovieModel movie, ImageView imageHolder) {
        //clear all the null elements of the list
        while (backdrops.contains(null))
            backdrops.remove(null);
        //verify if an imageholder was provided
        if (null != imageHolder) {//its the poster
            moviePosterBig = image;
            imageHolder.setImageBitmap(moviePosterBig);
            backdrops.add(0,image);
        } else { //is a backdrop
            if (trailerPreview == null) { //we place the first backdrop to the trailer section
                trailerPreview = image;
                //we place the trailer image
                movieDetailView.drawElements();
            } else {
                //add the image to the list
                backdrops.add(image);
                //tell the view that the images are updated
                movieDetailView.drawImages();
            }
        }
    }


    // Parcelling part
    public MovieDetailController(Parcel in){
        movie = in.readParcelable(MovieModel.class.getClassLoader());
        moviePosterBig = in.readParcelable(Bitmap.class.getClassLoader());
        trailerPreview = in.readParcelable(Bitmap.class.getClassLoader());
        backdrops = in.readArrayList(Bitmap.class.getClassLoader());
    }
    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(movie, flags);
        dest.writeParcelable(moviePosterBig, flags);
        dest.writeParcelable(trailerPreview, flags);
        dest.writeList(backdrops);
    }
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public MovieDetailController createFromParcel(Parcel in) {
            return new MovieDetailController(in);
        }

        public MovieDetailController[] newArray(int size) {
            return new MovieDetailController[size];
        }
    };
}
