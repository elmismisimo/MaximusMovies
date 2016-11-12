package com.sandersoft.maximusmovies.views;

import android.app.Fragment;
import android.content.Context;
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

import com.sandersoft.maximusmovies.ActivityMain;
import com.sandersoft.maximusmovies.ApplicationMain;
import com.sandersoft.maximusmovies.R;
import com.sandersoft.maximusmovies.controlers.MoviesController;
import com.sandersoft.maximusmovies.models.MovieModel;
import android.view.ViewTreeObserver.OnScrollChangedListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MoviesViewFragment extends Fragment {

    RecyclerView lst_movies;
    public MoviesController movieController;

    String list = "popular";

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
        //TODO buscamos una imagen (boorar esta instruccion)
        //doImageRequest("aqhAqttDq7zgsTaBHtCD8wmTk6k.jpg");

        moviesAdapter = new MoviesAdapter();
        final LinearLayoutManager layoutmanager
                = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        lst_movies.setLayoutManager(layoutmanager);
        lst_movies.setAdapter(moviesAdapter);

        scrollListener = new OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                //verificamos si llego al final de la lista y carga nuevos elementos automaticamente
                //if (lst_movies.getScrollY() + lst_movies.getHeight() >= main_lista.getHeight()){
                if (layoutmanager.findLastCompletelyVisibleItemPosition() == movieController.getMovies().size()-1) {
                    lst_movies.getViewTreeObserver().removeOnScrollChangedListener(scrollListener);
                    movieController.doMoviesNextPageRequest();
                }
            }
        };

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
        movieController.clearMovies();
        if (moviesAdapter != null)
            moviesAdapter.notifyData();
        movieController.doMoviesRequest(search,list);
    }

    /**
     * request the next page of the last movies search
     */
    public void doMoviesNextPageRequest(){
        movieController.doMoviesNextPageRequest();
    }

    /**
     * Receive the response of a movies search
     */
    public void receiveMovies(int cant){
        moviesAdapter.notifyData();
        //verifiy if there are more loadable items
        if (movieController.getMovies().size() < cant)
            lst_movies.getViewTreeObserver().addOnScrollChangedListener(scrollListener);
    }
    public void receiveMovieImages(int position){
        moviesAdapter.notifyItemChanged(position);
    }

    public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MoviesViewHolder> {

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

        public MoviesAdapter(){
        }

        @Override
        public MoviesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_movies, parent, false);

            return new MoviesViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MoviesViewHolder holder, final int position) {
            holder.txt_name.setText(movieController.getMovies().get(position).getTitle());
            holder.txt_year.setText(String.valueOf(movieController.getMovies().get(position).getYear()));
            holder.lay_movie_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(MoviesViewFragment.this.getActivity(),
                            holder.txt_name.getText().toString()
                                    + " " + movieController.getMovies().get(position).getIds().getTrakt()
                                    + " " + movieController.getMovies().get(position).getIds().getTmdb(),
                            Toast.LENGTH_SHORT).show();
                }
            });
            //find images
            if (movieController.getMovies().get(position).getPoster() != null)
                holder.img_poster.setImageBitmap(movieController.getMovies().get(position).getPoster());
            else {
                holder.img_poster.setImageResource(R.mipmap.ic_launcher);
                movieController.findImage(holder.img_poster, position);
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
