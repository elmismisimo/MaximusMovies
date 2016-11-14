package com.sandersoft.maximusmovies.views;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ViewFlipper;

import com.sandersoft.maximusmovies.R;
import com.sandersoft.maximusmovies.controlers.GalleryController;
import com.sandersoft.maximusmovies.utils.Globals;

import java.util.ArrayList;

/**
 * Created by Sander on 13/11/2016.
 */
public class GalleryViewFragment extends Fragment {

    //controller that handle action with the movies
    GalleryController galleryController;

    //the slideshow engine (a viewflipper)
    ViewFlipper slideShow;

    //list of the elements on screen (3 imageviews top)
    public ArrayList<ImageView> relaImas = new ArrayList<ImageView>();
    //position of the image on the list of bitmaps
    public int pos = 0;
    //position of the imageview displaying the current image
    public int posSl = 0;

    //gesture detector and touch listener for the interaction with the gallery
    private GestureDetector gestureDetector;
    View.OnTouchListener gestureListener;

    /**
     * Gesture detector that manages the slides to move images
     * @author Sander
     *
     */
    class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                //if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                //    return false;
                // right to left swipe
                if(e1.getX() - e2.getX() > Globals.SWIPE_MIN_DISTANCE && Math.abs(velocityX) > Globals.SWIPE_THRESHOLD_VELOCITY) {
                    nextElm();
                }  else if (e2.getX() - e1.getX() > Globals.SWIPE_MIN_DISTANCE && Math.abs(velocityX) > Globals.SWIPE_THRESHOLD_VELOCITY) {
                    prevElm();
                }
            } catch (Exception e) {
                // nothing
            }
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    }

    /**
     * Move the next element to the right, moving everything to the left
     */
    public void nextElm()
    {
        if (galleryController.getImages().size() > 3) //get the next three images
            galleryController.get3Photos(1);
        //apply animation
        slideShow.setInAnimation(getActivity(), R.anim.animation_in_left);
        slideShow.setOutAnimation(getActivity(), R.anim.animation_out_left);
        //show next element in the slider
        slideShow.showNext();
    }
    /**
     * Move the previous element to the left, moving everything to the right
     */
    public void prevElm()
    {
        if (galleryController.getImages().size() > 3) //get the next three images
            galleryController.get3Photos(-1);
        //apply animation
        slideShow.setInAnimation(getActivity(), R.anim.animation_in_right);
        slideShow.setOutAnimation(getActivity(), R.anim.animation_out_right);
        //show previous element in the slider
        slideShow.showPrevious();
    }

    public GalleryViewFragment() {
        //create a gallery controller
        galleryController = new GalleryController(this);
    }

    /**
     * Define the initial parameter of the gallery
     * @param images the list of bitmap images
     * @param first the first element
     */
    public void setInitialValues(ArrayList<Bitmap> images, int first){
        galleryController.setImages(images);
        this.pos = first;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_gallery_view, container, false);

        //instantiate the image container
        slideShow = (ViewFlipper) rootview.findViewById(R.id.slideShow);

        // Gesture detection
        gestureDetector = new GestureDetector(getActivity(), new MyGestureDetector());
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        };
        //add the gesture listener to the image container
        slideShow.setOnTouchListener(gestureListener);

        //if its a recreation of the activity (rotation perhaps)
        if (savedInstanceState != null){
            //get the controller
            galleryController = savedInstanceState.getParcelable(Globals.MOVIE_CONTROLLER_TAG);
            //get the last position of the gallery
            pos = savedInstanceState.getInt(Globals.GALLERY_POS);
            //define this view as the view related to the gallery controller
            galleryController.setGalleryView(this);
        }

        //define the first images that the image container will show
        defineImages();

        return rootview;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //get objects backedup so we can reload the activity with the same infromation
        outState.putParcelable(Globals.MOVIE_CONTROLLER_TAG, galleryController);
        outState.putInt(Globals.GALLERY_POS, pos);
    }

    /**
     * Define the images for the image container (view flipper) usign the backdroplist of images
     * from the controller
     */
    public void defineImages(){
        //add only 3 elements, and each time the position changes, an element content is placed with another
        //clean the container
        if (slideShow.getChildCount() > 0)
            slideShow.removeAllViews();
        //get the images (remember we only get 3 images from the list to keep the cache memory low)
        for (int i = 0; i < 3 && i < galleryController.getImages().size(); i++){
            //define the image index (pos: current, pos+1: next, pos-1: prev)
            int j = i == 0 ? pos : i == 1 ? pos + 1 : pos - 1;
            //restart the next image position if its overflowing
            if (j >= galleryController.getImages().size()) j = 0;
            if (j < 0) j = galleryController.getImages().size() - 1;

            //inflate a view specificale for the image
            View itemView = getActivity().getLayoutInflater().inflate(R.layout.item_image, null);
            //instantiate the imageview and put the bitmap image in it
            ImageView foto = (ImageView) itemView.findViewById(R.id.img_image);
            foto.setImageBitmap(galleryController.getImages().get(j));

            //add the view to the list of image elements (so we can have a reference when we need to change it)
            relaImas.add(foto);

            //add the view to the slide show (displayed and animated elements)
            slideShow.addView(itemView);
        }

    }
}
