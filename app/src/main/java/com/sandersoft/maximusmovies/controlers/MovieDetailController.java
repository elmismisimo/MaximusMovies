package com.sandersoft.maximusmovies.controlers;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.ImageView;

import com.sandersoft.maximusmovies.ApplicationMain;
import com.sandersoft.maximusmovies.R;
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
    //the big poster to replace the thumbnail
    Bitmap moviePosterBig;
    //the dropback image that will be used as a trailer image
    Bitmap trailerPreview;

    //list of the backdrop images
    ArrayList<Bitmap> backdrops = new ArrayList<>();

    public MovieDetailController(MovieDetailViewFragment caller){
        setMovieDetailView(caller);
        //add a null object to backdrops so it is interpreted as a loading element of the list
        backdrops.add(null);
    }
    /**
     * Set the view related to this controller
     * @param caller
     */
    public void setMovieDetailView(MovieDetailViewFragment caller){
        movieDetailView = caller;
    }
    /**
     * Set the movie object for the activity
     * @param movie
     */
    public void setMovieObject(MovieModel movie){
        this.movie = movie;
    }

    /**
     * Get the movie object
     * @return
     */
    public MovieModel getMovie(){
        return movie;
    }
    /**
     * get the big poster of the movie (big resolution)
     * @return
     */
    public Bitmap getMoviePoster(){
        return moviePosterBig;
    }
    /**
     * get the image that is being used as trailer preview
     * @return
     */
    public Bitmap getMovieTrailerImage(){
        return trailerPreview;
    }
    /**
     * Get the list of backdrop images
     * @return
     */
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

    /**
     * Do the initial movie request (full movie info, images for the gallery)
     */
    public void doInitialMovieRequest(){
        //request the full info of the movie
        ApplicationMain.webManager.doMovieRequest(this, movie.getIds().getTrakt() + "?extended=full");
        //verify the images of the movie
        if (null == movie.getImages()){//if the images has not been loaded yet (came to early to this activity)
            //request the list of images url
            doImagesListRequest();
        } else {
            //request the bitmap images
            doImagesRequest();
        }
    }
    /**
     * request the list of images url from TMDB
     */
    public void doImagesListRequest(){
        ApplicationMain.webManager.doImagesRequest(this, movie.getIds().getTmdb().toString(), movie);
    }
    /**
     * Request the Poster Bitmap and the backdropd bitmaps
     */
    public void doImagesRequest(){
        //request the poster in better quality
        doImageRequest(movieDetailView.img_poster);
        //request the backdrops for the reciclerview
        for (Image iurl : movie.getImages().getBackdrops())
            //request the image without a imageholder, so we know it is one of the backdrop images
            doImageRequest(null, iurl.getFile_path());
    }
    /**
     * Request an image bitmap from TMDB (the image url will be obtained automatically from the
     * movie object of the controller)
     * @param imageHolder the imageview container of the image
     */
    public void doImageRequest(ImageView imageHolder){
        //executes the fetch of the image
        ApplicationMain.webManager.doImageRequest(this, imageHolder, movie, "w500");
    }
    /**
     * Request an image bitmap from TMDB using a url
     * @param imageHolder the imageview container of the image
     * @param image_url the url of the images
     */
    public void doImageRequest(ImageView imageHolder, String image_url){
        //executes the fetch of the image
        ApplicationMain.webManager.doImageRequest(this, imageHolder, image_url, "w500");
    }

    //receive the movies result
    @Override
    public void onReceiveHttpAnswer(MovieModel[] movies, int cant, int page, String search) {
        //get the needed overview and trailer values (the other are ignored in this version)
        movie.setOverview(movies[0].getOverview());
        movie.setTrailer(movies[0].getTrailer());
        //notify the view that we have new information to be drawn
        movieDetailView.drawElements();
    }
    //receive an error of the movie fetch
    @Override
    public void onReceiveHttpAnswerError(String error) {
        Log.e("MovieController", error);
        //set an empty overview for the loading icon to disapear
        movie.setOverview(movieDetailView.getResources().getString(R.string.no_overview));
        //update the ui with new information
        movieDetailView.drawElements();
    }
    //receive the images from TMDB
    @Override
    public void onReceiveHttpTMDB(Images images, MovieModel movie) {
        //set the images on the movie
        this.movie.setImages(images);
        //go find the poster and backdrops wit this list
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
            //place the image in hte big poster
            imageHolder.setImageBitmap(moviePosterBig);
            //ad the poster to the backdrops
            backdrops.add(0,image);
            //tell the recyclerview that the images are updated
            movieDetailView.drawImages();
        } else { //is a backdrop
            if (trailerPreview == null) { //we place the first backdrop to the trailer section
                trailerPreview = image;
                //we place the trailer image
                movieDetailView.drawElements();
            } else {
                //add the image to the list
                backdrops.add(image);
                //tell the recyclerview that the images are updated
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
