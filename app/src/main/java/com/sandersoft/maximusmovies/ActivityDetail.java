package com.sandersoft.maximusmovies;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.sandersoft.maximusmovies.utils.Globals;
import com.sandersoft.maximusmovies.views.MoviesViewFragment;

public class ActivityDetail extends AppCompatActivity {

    MoviesViewFragment detailFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        //place the fragment in the container
        if (savedInstanceState == null) {
            detailFragment = new MoviesViewFragment();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.main_fragment, detailFragment, Globals.TAG_MOVIE_DETAIL_FRAGMENT);
            //ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            //ft.addToBackStack(null);
            ft.commit();
        } else {
            detailFragment = (MoviesViewFragment) getFragmentManager().findFragmentByTag(Globals.TAG_MOVIE_DETAIL_FRAGMENT);
        }
        detailFragment.setAsWebListener();

        //if is first load, request the movies
        if (savedInstanceState == null) {
            detailFragment.doMoviesRequest();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //we define the controller of the fragment as the web listener for all the incoming requests
        detailFragment.setAsWebListener();
    }

}
