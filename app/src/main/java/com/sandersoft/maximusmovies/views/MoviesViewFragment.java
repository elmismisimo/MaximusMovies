package com.sandersoft.maximusmovies.views;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.sandersoft.maximusmovies.ActivityDetail;
import com.sandersoft.maximusmovies.ActivityMain;
import com.sandersoft.maximusmovies.ApplicationMain;
import com.sandersoft.maximusmovies.R;
import com.sandersoft.maximusmovies.controlers.MoviesController;
import com.sandersoft.maximusmovies.models.MovieModel;
import com.sandersoft.maximusmovies.utils.Globals;

import android.view.ViewTreeObserver.OnScrollChangedListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MoviesViewFragment extends Fragment {

    //Controller that handles action on the movies
    public MoviesController movieController;

    RecyclerView lst_movies;
    MoviesAdapter moviesAdapter;
    OnScrollChangedListener scrollListener;

    public MoviesViewFragment() {
        movieController = new MoviesController(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_movies_view, container, false);
        lst_movies = (RecyclerView) rootview.findViewById(R.id.lst_movies);

        if (null != savedInstanceState){
            movieController = savedInstanceState.getParcelable(Globals.MOVIE_CONTROLLER_TAG);
            movieController.setMovieView(this);
            setAsWebListener();
            //ArrayList<MovieModel> movies = savedInstanceState.getParcelableArrayList("movies");
            //movieController.setMovies(movies);
        }

        //create the adapter for the list of movies
        moviesAdapter = new MoviesAdapter();
        //set the vertical layout so the list is displayed top down
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        lst_movies.setLayoutManager(layoutManager);
        //set the adapter
        lst_movies.setAdapter(moviesAdapter);
        //create a scroll listener for the reciclerview, that will check if the list reached bottom
        //this will be added to the list when the first elements are received (check receiveMovies(int cant))
        scrollListener = new OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                //verify if the list reached the end and load new elements automatically
                if (layoutManager.findLastCompletelyVisibleItemPosition() >= movieController.getMovies().size()-2) {
                    //remove the scroll listener from the list while is loading
                    lst_movies.getViewTreeObserver().removeOnScrollChangedListener(scrollListener);
                    //request the next set of the movies
                    doMoviesNextPageRequest();
                }
            }
        };

        //if is first load, request the movies
        if (null == savedInstanceState) {
            doMoviesRequest();
        } else {
            //recreates a call where movies are received, and places again the scrolllistener
            receiveMovies();
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

    /**
     * Handle the selecion of an element
     * @param position the position of the element on the list
     */
    public void elementClicked(int position){
        /*Toast.makeText(MoviesViewFragment.this.getActivity(),
                movieController.getMovies().get(position).getTitle()
                        + " " + movieController.getMovies().get(position).getIds().getTrakt()
                        + " " + movieController.getMovies().get(position).getIds().getTmdb(),
                Toast.LENGTH_SHORT).show();*/
        Intent openDetail = new Intent(getActivity(), ActivityDetail.class);
        Bundle extras = new Bundle();
        extras.putParcelable(Globals.MOVIE_OBJ_TAG, movieController.getMovies().get(position));
        openDetail.putExtras(extras);
        startActivity(openDetail);
    }
    /**
     * Handle the long click of an element
     * @param position the position of the element on the list
     */
    public void elementLongClicked(int position){
        Toast.makeText(MoviesViewFragment.this.getActivity(), "LONG: " +
                movieController.getMovies().get(position).getTitle()
                        + " " + movieController.getMovies().get(position).getIds().getTrakt()
                        + " " + movieController.getMovies().get(position).getIds().getTmdb(),
                Toast.LENGTH_SHORT).show();
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
        //clear the current set of movies (this function always calls a new set)
        movieController.clearMovies();
        //remove the scroll listener of the list (if is set)
        if (null != lst_movies)
            lst_movies.getViewTreeObserver().removeOnScrollChangedListener(scrollListener);
        //notify the adapter to refresh the list
        if (null != moviesAdapter) moviesAdapter.notifyData();
        //do the request to the web service
        movieController.setSearch(search);
        movieController.doMoviesRequest();
    }
    /**
     * request the next page of the last movies search
     */
    public void doMoviesNextPageRequest(){
        //do the request of the nezxt set
        movieController.doMoviesNextPageRequest();
    }

    /**
     * Receive the response of a movies search
     */
    public void receiveMovies(){
        //verifiy if there are more loadable items
        if (movieController.canLoadMore()) {
            //add a null element to the list so it can be interpreted as a loading element
            movieController.addLoadingElement();
            //add a listener so when the scroll reaches bottom auto loads another set of movies
            lst_movies.getViewTreeObserver().addOnScrollChangedListener(scrollListener);
        }
        //update the list
        moviesAdapter.notifyData();
    }
    /**
     * Receive a notification that the images of a certain movie has been updated
     * @param position the position of the movie on the list
     */
    public void receiveMovieImages(int position){
        //request the adapter to update the movie in the position "position" of the list
        moviesAdapter.notifyItemChanged(position);
    }

    /**
     * Adapter that handles the display of the movies in the list
     */
    public class MoviesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_LOADING = 1;
        private final int VIEW_TYPE_EMPTY_SEARCH = 2;

        public class MoviesViewHolder extends RecyclerView.ViewHolder {
            public LinearLayout lay_movie_item;
            public ImageView img_poster;
            public TextView txt_name;
            public TextView txt_year;

            public MoviesViewHolder(View view) {
                super(view);
                lay_movie_item = (LinearLayout) view.findViewById(R.id.lay_movie_item);
                img_poster = (ImageView) view.findViewById(R.id.img_poster);
                txt_name = (TextView) view.findViewById(R.id.txt_name);
                txt_year = (TextView) view.findViewById(R.id.txt_year);
            }
        }
        public class LoadingViewHolder extends RecyclerView.ViewHolder {
            public LinearLayout lay_loading;

            public LoadingViewHolder(View view) {
                super(view);
                lay_loading = (LinearLayout) view.findViewById(R.id.lay_loading);
            }
        }
        public class EmptySearchViewHolder extends RecyclerView.ViewHolder {
            public LinearLayout lay_loading;
            public TextView txt_message;

            public EmptySearchViewHolder(View view) {
                super(view);
                txt_message = (TextView) view.findViewById(R.id.txt_message);
            }
        }

        @Override
        public int getItemViewType(int position) {
            //use null object as a reference for a loading object in the list (it must be the last on the list)
            if (movieController.getMovies().size()-1 == position &&
                    null == movieController.getMovies().get(position))
                return VIEW_TYPE_LOADING;
            //if the search returned 0 results, and there is only one dummy movie in the list
            else if (movieController.isSearchEmpty() && movieController.getMovies().size()-1 == position &&
                    null != movieController.getMovies().get(position) &&
                    null == movieController.getMovies().get(position).getTitle())
                return VIEW_TYPE_EMPTY_SEARCH;
            //the element is a valid movie
            return VIEW_TYPE_ITEM;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_movies, parent, false);
                return new MoviesViewHolder(itemView);
            } else if (viewType == VIEW_TYPE_LOADING){
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_loading, parent, false);
                return new LoadingViewHolder(itemView);
            } else if (viewType == VIEW_TYPE_EMPTY_SEARCH){
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_empty_text, parent, false);
                return new EmptySearchViewHolder(itemView);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            //verify the type of element
            if (holder instanceof MoviesViewHolder){
                //cast holder to a movieholder
                final MoviesViewHolder mh = (MoviesViewHolder) holder;
                //set the values
                mh.txt_name.setText(movieController.getMovies().get(position).getTitle());
                mh.txt_year.setText(String.valueOf(movieController.getMovies().get(position).getYear()));
                mh.lay_movie_item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        elementClicked(position);
                    }
                });
                mh.lay_movie_item.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        elementLongClicked(position);
                        return true;
                    }
                });
                //find images (if poster is already in the movie, doesnt fetch it from tmdb, just from the object)
                if (null != movieController.getMovies().get(position).getPoster())
                    mh.img_poster.setImageBitmap(movieController.getMovies().get(position).getPoster());
                else {
                    //place app image as preview of the poster (just placing something)
                    mh.img_poster.setImageResource(R.mipmap.thumbnail);
                    //go fetch the image from tmdb
                    movieController.findImage(position);
                }
            } else if (holder instanceof LoadingViewHolder){
                //do nothing, the loading icon will be shown without trouble
            } else if (holder instanceof EmptySearchViewHolder){
                //cast holder as
                EmptySearchViewHolder eh = (EmptySearchViewHolder) holder;
                //set the text verifying the connection
                if (ApplicationMain.webManager.verifyConn())
                    eh.txt_message.setText(R.string.no_match);
                else
                    eh.txt_message.setText(R.string.no_internet);
            }

        }

        @Override
        public int getItemCount() {
            return movieController.getMovies().size();
        }

        public void notifyData() {
            Log.d("notifyData ", movieController.getMovies().size() + "");
            notifyDataSetChanged();
        }
    }
}
