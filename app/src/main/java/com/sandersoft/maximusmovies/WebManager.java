package com.sandersoft.maximusmovies;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.sandersoft.maximusmovies.models.tmdb.Images;
import com.sandersoft.maximusmovies.utils.Globals;
import com.sandersoft.maximusmovies.interfaces.WebManagerListener;
import com.sandersoft.maximusmovies.models.MovieModel;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Created by Sander on 10/11/2016.
 */
public class WebManager {

    //the weblistener at whom this controll will report
    private WebManagerListener webManagerListener;
    //the instance of the movie fetch task, so this can be cancelled anytime
    private GetMoviesTask getMoviesTask;

    public WebManager(){

    }

    /**
     * Define the weblistener that this controll will report
     * @param webManagerListener the controll listener
     */
    public void setWebManagerListener(WebManagerListener webManagerListener) {
        this.webManagerListener = webManagerListener;
    }
    public WebManagerListener getwebManagerListener(){
        return webManagerListener;
    }

    /**
     * Verify if there is an internet connection
     * @return <b>true</b> if there is a connection, <b>false</b> if there isnt
     */
    public boolean verifyConn()
    {
        //check if the instance of the app is null
        if (null == ApplicationMain.getContext())return false;
        //check if there is an internet connection
        ConnectivityManager cm = (ConnectivityManager) ApplicationMain.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (null != netInfo && netInfo.isConnectedOrConnecting())
            return true;
        return false;
    }

    /**
     * Prepare and execute a multiple movie fetch request
     * @param params the params for the request
     * @param search the search of the request (if any)
     */
    public void doMoviesRequest(WebManagerListener controller, String params, String search){
        //define the controller as the webListener
        webManagerListener = controller;
        //if there is a ongoing fetch task, cancell it
        if (null != getMoviesTask){
            getMoviesTask.cancel(true);
            getMoviesTask = null;
        }
        //create a new fetch task
        getMoviesTask = new GetMoviesTask();
        //execute the task
        getMoviesTask.execute(params,search);
    }
    /**
     * Prepare and execute a single movie fetch
     * @param params the params
     */
    public void doMovieRequest(WebManagerListener controller, String params){
        //define the controller as the webListener
        webManagerListener = controller;
        new GetMoviesTask().execute(params);
    }
    /**
     * Prepare and executes a movie images list request
     * @param tmdb_id the id of the movie in TMDB website
     * @param movie the movie object that will receive the image list
     */
    public void doImagesRequest(WebManagerListener controller, String tmdb_id, MovieModel movie){
        //define the controller as the webListener
        webManagerListener = controller;
        new GetImagesTask(movie).execute(tmdb_id);
    }
    /**
     * Prepares and execute a bitmap movie image request
     * @param imageHolder The ImageView that 'could' hold the image
     * @param movie the movie object that will receive the image
     * @param size the size of the image (w185,w500,original are preferred)
     */
    public void doImageRequest(WebManagerListener controller, ImageView imageHolder, MovieModel movie, String size){
        //define the controller as the webListener
        webManagerListener = controller;
        //verify if the image url exists in the poster list
        if (movie.getImages().getPosters().size() > 0)
            new GetImageTask(imageHolder, movie).execute(movie.getImages().getPosters().get(0).getFile_path(), size);
        //verify if the image url exists in the backdrops list
        else if (movie.getImages().getBackdrops().size() > 0)
            new GetImageTask(imageHolder, movie).execute(movie.getImages().getBackdrops().get(0).getFile_path(), size);
    }
    public void doImageRequest(WebManagerListener controller, ImageView imageHolder, String image_url, String size) {
        //define the controller as the webListener
        webManagerListener = controller;
        new GetImageTask(imageHolder, null).execute(image_url, size);
    }

    /**
     * AsyncTask that fetches the movies
     */
    private class GetMoviesTask extends AsyncTask<String, Void, ResponseEntity> {
        String search="";
        boolean multiple = false;
        //instantiate the current listener, so the old responses never goes to newlly registered controllers
        WebManagerListener currWebListener = webManagerListener;

        @Override
        protected ResponseEntity doInBackground(String... params) {
            try {
                //verify if the request has a search term (this defines if is a multiple fetch)
                if (params.length > 1) {
                    search = params[1];
                    multiple = true;
                }
                //create the url
                String url = Globals.MOVIES_URL + params[0] + search;
                //define the spring request
                RestTemplate restTemplate = new RestTemplate();
                //define the headers
                HttpHeaders requestHeaders = new HttpHeaders();
                requestHeaders.add("Content-Type", "application/json");
                requestHeaders.add("trakt-api-version", "2");
                requestHeaders.add("trakt-api-key", Globals.TRAKT_ID);
                HttpEntity<?> requestEntity = new HttpEntity<Object>(requestHeaders);
                //we add the converter
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                //do the request (depending if its multiple or not, the receiving class changes)
                ResponseEntity response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, multiple ? MovieModel[].class : MovieModel.class);
                return response;
            } catch (HttpClientErrorException e){
                //return the error response
                return new ResponseEntity(e.getMessage(), e.getStatusCode());
            } catch (Exception e) {
                Log.e("WebRequest", e.getMessage(), e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(ResponseEntity response) {
            //verify if there is a weblistener and if the response is diferent to null
            if (null != currWebListener && null != response) {
                //verify if the request was successfull
                if (response.getStatusCode().equals(HttpStatus.OK)) {
                    //verify if its aa multiple fetch
                    if (multiple){
                        //get the amount of records
                        int c = 0;
                        try{c = new Integer(response.getHeaders().getFirst("x-pagination-item-count"));
                        } catch (Exception ex){}
                        //get the page number
                        int p = 0;
                        try{p = new Integer(response.getHeaders().getFirst("x-pagination-page"));
                        } catch (Exception ex){}
                        //return the movies
                        currWebListener.onReceiveHttpAnswer((MovieModel[]) response.getBody(), c, p, search);
                    } else{
                        //return the movie
                        currWebListener.onReceiveHttpAnswer(new MovieModel[]{(MovieModel)response.getBody()}, 1, 1, "");
                    }
                } else {
                    //return the error
                    currWebListener.onReceiveHttpAnswerError(response.getStatusCode() + ": " + response.getBody().toString());
                }
            }
        }
    }

    /**
     * AsyncTask that fetches the list ulr_images
     */
    private class GetImagesTask extends AsyncTask<String, Void, Images> {
        MovieModel movie;
        //instantiate the current listener, so the old responses never goes to newlly registered controllers
        WebManagerListener currWebListener = webManagerListener;

        public GetImagesTask(MovieModel movie) {
            this.movie = movie;
        }

        @Override
        protected Images doInBackground(String... params) {
            try {
                //generate the url
                final String url = Globals.TMSB_IMAGES_URL.replace("***", params[0]) + Globals.TMDB_ID;
                //define the spring request
                RestTemplate restTemplate = new RestTemplate();
                //we add the converter
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                //do the request
                Images response = restTemplate.getForObject(url, Images.class);
                return response;
            } catch (Exception e) {
                Log.e("WebRequest", e.getMessage(), e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Images response) {
            //verify if there is a weblistener registered
            if (null != currWebListener) {
                //return the image list with the movie object
                currWebListener.onReceiveHttpTMDB(response, movie);
            }
        }
    }

    /**
     * AsyncTask that fetches a bitmap image from the web
     */
    private class GetImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView imageHolder;
        MovieModel movie;
        //instantiate the current listener, so the old responses never goes to newlly registered controllers
        WebManagerListener currWebListener = webManagerListener;

        public GetImageTask(ImageView imageHolder, MovieModel movie) {
            this.imageHolder = imageHolder;
            this.movie = movie;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                //generate the url
                final String url = Globals.TMSB_IMAGE_URL.replace("***", params[1]) + params[0];
                //define the spring request
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
                restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
                restTemplate.getMessageConverters().add(new ResourceHttpMessageConverter());
                //define the headders
                HttpHeaders requestHeaders = new HttpHeaders();
                HttpEntity<?> requestEntity = new HttpEntity<Object>(requestHeaders);
                //do the request
                ResponseEntity<Resource> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Resource.class);
                //convert the response to bitmap
                if (null != response.getBody())
                    return BitmapFactory.decodeStream(response.getBody().getInputStream());
            } catch (Exception e) {
                Log.e("WebRequest", e.getMessage(), e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Bitmap image) {
            //verify if there is a weblisterner
            if (null != currWebListener) {
                //return the bitmap image with the movie object
                currWebListener.onReceiveHttpTMDBImage(image, movie, imageHolder);
            }
        }
    }
}
