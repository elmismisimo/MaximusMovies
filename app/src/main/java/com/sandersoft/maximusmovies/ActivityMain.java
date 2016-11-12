package com.sandersoft.maximusmovies;

import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.sandersoft.maximusmovies.views.MoviesViewFragment;

public class ActivityMain extends AppCompatActivity {

    SearchView searchView;
    String searchPreval = "";

    MoviesViewFragment mainFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //place the fragment in the container
        mainFragment = new MoviesViewFragment();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.main_fragment, mainFragment);
        //ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        //ft.addToBackStack(null);
        ft.commit();

        mainFragment.setAsWebListener();
        if (savedInstanceState == null) {
            mainFragment.doMoviesRequest();
        } else {
            //mainFragment.movieController
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putParcelableArrayList("movies", mainFragment.movieController.getMovies());
    }

    @Override
    protected void onResume() {
        super.onResume();
        //we define the controller of the fragment as the web listener for all the incoming requests
        mainFragment.setAsWebListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);

        defineSearchView(menu);
        //searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return true;
    }
    public void defineSearchView(final Menu menu){

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        if (searchView != null) {
            searchView.setIconifiedByDefault(false);
            //searchView.setFocusable(true);
            //searchView.setIconified(false);
            searchView.requestFocusFromTouch();
            searchView.setOnQueryTextListener(new OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    searchView.clearFocus();
                    //menu.findItem(R.id.search).collapseActionView();
                    return false;
                }
                @Override
                public boolean onQueryTextChange(String newText) {
                    if (newText.equals(searchPreval)) return false;
                    searchPreval = newText;
                    //do request with search value (even if its empty
                    mainFragment.doMoviesRequest(searchPreval);
                    return false;
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.search) {
            searchView.requestFocus();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
