package com.sandersoft.maximusmovies.controlers;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.sandersoft.maximusmovies.views.GalleryViewFragment;
import com.sandersoft.maximusmovies.views.MovieDetailViewFragment;

import java.util.ArrayList;

/**
 * Created by Sander on 13/11/2016.
 */
public class GalleryController implements Parcelable {

    //the view of the controller
    GalleryViewFragment galleryView;

    //the list of bitmap images for the gallery
    ArrayList<Bitmap> backdrops = new ArrayList<>();

    public GalleryController(GalleryViewFragment caller){
        setGalleryView(caller);
    }
    public void setGalleryView(GalleryViewFragment caller){
        galleryView = caller;
    }
    public void setImages(ArrayList<Bitmap> backdrops){
        this.backdrops = backdrops;
    }
    /**
     * Get the list of bitmap images
     * @return
     */
    public ArrayList<Bitmap> getImages(){
        return backdrops;
    }

    /**
     * Obtain the correct 3 images for the slider to show. With this method we do not overload the
     * UI with a lot of images displayed at the same time, just have in cache the current, the next
     * and the previous
     * @param dir the direction of the movement (1: right, -1: left)
     */
    public void get3Photos(int dir)
    {
        //place the global images position correctly
        galleryView.pos += dir;
        if ( galleryView.pos >= backdrops.size())
            galleryView.pos = 0;
        if ( galleryView.pos < 0)
            galleryView.pos = backdrops.size() - 1;
        //place the slider elements position correctly
        galleryView.posSl += dir;
        if ( galleryView.posSl >=  galleryView.relaImas.size())
            galleryView.posSl = 0;
        if ( galleryView.posSl < 0)
            galleryView.posSl =  galleryView.relaImas.size() - 1;
        int posR =  galleryView.posSl + dir;
        if (posR >=  galleryView.relaImas.size())
            posR = 0;
        if (posR < 0)
            posR =  galleryView.relaImas.size() - 1;
        //get the allowed positions
        ArrayList<Integer> ps = new ArrayList<Integer>();
        ps.add( galleryView.pos);
        if ( galleryView.pos + 1 >= backdrops.size())
            ps.add(0);
        else
            ps.add( galleryView.pos + 1);
        if ( galleryView.pos - 1 < 0)
            ps.add(backdrops.size() - 1);
        else
            ps.add( galleryView.pos - 1);
        //we go through the images list and delete the ones that are not part of the ps, and place the ones that are
        for (int i = 1; i < ps.size(); i++){
            //if the image is either the next one or the previous
            if ((dir == 1 && i == 1) || (dir == -1 && i == 2)){
                //place the image
                galleryView.relaImas.get(posR).setImageBitmap(backdrops.get(ps.get(i)));
            }
        }
        //execute a garbage collector, just in case...
        System.gc();
    }

    // Parcelling part
    public GalleryController(Parcel in){
        backdrops = in.readArrayList(Bitmap.class.getClassLoader());
    }
    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
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
