package com.sandersoft.maximusmovies;

import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.sandersoft.maximusmovies.utils.Globals;
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

        if (null == savedInstanceState) {
            //place the fragment in the container
            mainFragment = new MoviesViewFragment();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.main_fragment, mainFragment, Globals.TAG_MOVIE_FRAGMENT);
            //ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            //ft.addToBackStack(null);
            ft.commit();
        } else {
            //recover the values after a destroy
            searchPreval = savedInstanceState.getString("search");
            mainFragment = (MoviesViewFragment) getFragmentManager().findFragmentByTag(Globals.TAG_MOVIE_FRAGMENT);
        }
        //we define the controller of the fragment as the web listener for all the incoming requests
        mainFragment.setAsWebListener();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //we place the last search term in the outState variable so when the rotation happens
        //the search value is preserved
        outState.putString("search", searchPreval);
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
        return true;
    }

    /**
     * The searchview is defined in this section
     * @param menu the menu where the searchview is gona be placed
     */
    public void defineSearchView(final Menu menu){
        // instanciate the searchview and add the search configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.search);
        searchView = (SearchView) searchItem.getActionView();
        if (null != searchView) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
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
            //verify if there is a previous search, and add it to the searchview (without triggering search)
            if (!searchPreval.isEmpty()){
                searchItem.expandActionView();
                searchView.setQuery(searchPreval, false);
                searchView.clearFocus();
            }
        }
    }

    /*@Override
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
    }*/
}
