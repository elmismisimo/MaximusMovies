package com.sandersoft.maximusmovies.views;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sandersoft.maximusmovies.R;
import com.sandersoft.maximusmovies.controlers.MovieDetailController;
import com.sandersoft.maximusmovies.models.MovieModel;
import com.sandersoft.maximusmovies.utils.Globals;

/**
 * Created by Sander on 12/11/2016.
 */
public class MovieDetailViewFragment extends Fragment {

    //controller that handle action with the movies
    MovieDetailController movieController;

    //vistas de la actividad
    public ImageView img_poster;
    TextView txt_title;
    TextView txt_year;
    TextView txt_overview;
    LinearLayout lay_loading;
    LinearLayout lay_trailer;
    ImageView img_video;
    ImageView img_play;
    RecyclerView lst_images;

    ImagessAdapter imagesAdapter;

    public MovieDetailViewFragment() {
        movieController = new MovieDetailController(this);
    }
    public void setMovieObject(MovieModel movie){
        movieController.setMovieObject(movie);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_movie_detail_view, container, false);
        img_poster = (ImageView) rootview.findViewById(R.id.img_poster);
        txt_title = (TextView) rootview.findViewById(R.id.txt_title);
        txt_year = (TextView) rootview.findViewById(R.id.txt_year);
        txt_overview = (TextView) rootview.findViewById(R.id.txt_overview);
        lay_loading = (LinearLayout) rootview.findViewById(R.id.lay_loading);
        lay_trailer = (LinearLayout) rootview.findViewById(R.id.lay_trailer);
        img_video = (ImageView) rootview.findViewById(R.id.img_video);
        img_play = (ImageView) rootview.findViewById(R.id.img_play);
        lst_images = (RecyclerView) rootview.findViewById(R.id.lst_images);

        if (savedInstanceState != null){
            movieController = savedInstanceState.getParcelable(Globals.MOVIE_CONTROLLER_TAG);
            movieController.setMovieDetailView(this);
            setAsWebListener();
        }

        img_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != movieController.getMovie().getTrailer()){
                    Intent trailerIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(movieController.getMovie().getTrailer()));
                    startActivity(trailerIntent);
                }
            }
        });

        //create the adapter for the list of images
        imagesAdapter = new ImagessAdapter();
        //set the vertical layout so the list is displayed top down
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        lst_images.setLayoutManager(layoutManager);
        //set the adapter
        lst_images.setAdapter(imagesAdapter);

        //draw the element on the activity
        drawElements();

        //if is first load, request the movies
        if (savedInstanceState == null) {
            doInitialMovieRequest();
        }

        //return the view for the activity to be shown
        return rootview;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //get objects backedup so we can reload the activity with the same infromation
        outState.putParcelable(Globals.MOVIE_CONTROLLER_TAG, movieController);
    }

    /**
     * Define this class as the Weblistener for the WebManager
     */
    public void setAsWebListener(){
        movieController.setAsWebListener();
    }

    public void drawElements(){
        txt_title.setText(movieController.getMovie().getTitle());
        txt_year.setText(String.valueOf(movieController.getMovie().getYear()));
        //place the overview
        if (null != movieController.getMovie().getOverview()){
            lay_loading.setVisibility(View.GONE);
            txt_overview.setText(movieController.getMovie().getOverview());
            txt_overview.setVisibility(View.VISIBLE);
        }
        //place the poster
        if (null != movieController.getMoviePoster())
            img_poster.setImageBitmap(movieController.getMoviePoster());
        else if (null != movieController.getMovie().getPoster())
            img_poster.setImageBitmap(movieController.getMovie().getPoster());
        //place the trailer
        if (null != movieController.getMovieTrailerImage())
            img_video.setImageBitmap(movieController.getMovieTrailerImage());
        if (null != movieController.getMovie().getTrailer())
            lay_trailer.setVisibility(View.VISIBLE);
    }
    public void drawImages(){
        imagesAdapter.notifyData();
    }

    public void doInitialMovieRequest(){
        //request the full info of the movie
        movieController.doInitialMovieRequest();
    }

    /**
     * Adapter that handles the display of the movies in the list
     */
    public class ImagessAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_LOADING = 1;

        public class ImagesViewHolder extends RecyclerView.ViewHolder {
            public ImageView img_dropback;

            public ImagesViewHolder(View view) {
                super(view);
                img_dropback = (ImageView) view.findViewById(R.id.img_dropback);
            }
        }
        public class LoadingViewHolder extends RecyclerView.ViewHolder {
            public LinearLayout lay_loading;

            public LoadingViewHolder(View view) {
                super(view);
                lay_loading = (LinearLayout) view.findViewById(R.id.lay_loading);
            }
        }

        @Override
        public int getItemViewType(int position) {
            //use null object as a reference for a loading object in the list (it must be the last on the list)
            return movieController.getBackdrops().size()-1 == position &&
                    movieController.getBackdrops().get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_dropback, parent, false);
                return new ImagesViewHolder(itemView);
            } else if (viewType == VIEW_TYPE_LOADING){
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_loading_vertical, parent, false);
                return new LoadingViewHolder(itemView);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            //verify the type of element
            if (holder instanceof ImagesViewHolder){
                //cast holder to a movieholder
                final ImagesViewHolder ih = (ImagesViewHolder) holder;
                //set the values
                ih.img_dropback.setImageBitmap(movieController.getBackdrops().get(position));
                ih.img_dropback.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getActivity(), "hola", Toast.LENGTH_SHORT).show();
                        //elementClicked(position);
                    }
                });
            } else if (holder instanceof LoadingViewHolder){
                //do nothing, it will just place the loading element
            }

        }

        @Override
        public int getItemCount() {
            return movieController.getBackdrops().size();
        }

        public void notifyData() {
            Log.d("notifyData ", movieController.getBackdrops().size() + "");
            notifyDataSetChanged();
        }
    }
}
