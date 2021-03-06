package com.sandersoft.maximusmovies;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.sandersoft.maximusmovies.models.tmdb.Images;
import com.sandersoft.maximusmovies.utils.Globals;
import com.sandersoft.maximusmovies.interfaces.WebManagerListener;
import com.sandersoft.maximusmovies.models.MovieModel;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
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

import java.util.concurrent.Callable;

import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Sander on 10/11/2016.
 */
public class WebManager {

    //the weblistener at whom this controll will report
    private WebManagerListener webManagerListener;

    //falg that defines if the web requests is done with reactive or async tasks
    boolean reactive = true;

    //the instance of the movie fetch task, so this can be cancelled anytime
    private GetMoviesTask getMoviesTask;
    //instance of the movie fecth observable, so this can be unsubscribed anytime
    Observable<ResponseEntity> moviesObservable;

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
        if (reactive)
            getMoviesReactive(params, search);
        else {
            //if there is a ongoing fetch task, cancell it
            if (null != getMoviesTask) {
                getMoviesTask.cancel(true);
                getMoviesTask = null;
            }
            //create a new fetch task
            getMoviesTask = getTheMoviesTask();
            //execute the task
            getMoviesTask.execute(params, search);
        }
    }
    /**
     * Prepare and execute a single movie fetch
     * @param params the params
     */
    public void doMovieRequest(WebManagerListener controller, String params){
        //define the controller as the webListener
        webManagerListener = controller;
        if (reactive)
            getMoviesReactive(params, null);
        else
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
        if (reactive)
            getImagesReactive(tmdb_id, movie);
        else
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
            executeImageRequest(imageHolder, movie, movie.getImages().getPosters().get(0).getFile_path(), size);
        //verify if the image url exists in the backdrops list
        else if (movie.getImages().getBackdrops().size() > 0)
            executeImageRequest(imageHolder, movie, movie.getImages().getBackdrops().get(0).getFile_path(), size);
    }
    public void doImageRequest(WebManagerListener controller, ImageView imageHolder, String image_url, String size) {
        //define the controller as the webListener
        webManagerListener = controller;
        executeImageRequest(imageHolder, null, image_url, size);
    }
    public void executeImageRequest(ImageView imageHolder, MovieModel movie, String image_url, String size){
        if (reactive)
            getImageReactive(imageHolder, movie, image_url, size);
        else
            new GetImageTask(imageHolder, movie).execute(image_url, size);
    }

    public GetMoviesTask getTheMoviesTask(){
        return new GetMoviesTask();
    }
    public GetImagesTask getTheImagesTask(MovieModel movie){
        return new GetImagesTask(movie);
    }
    public GetImageTask getTheImageTask(ImageView imageHolder, MovieModel movie){
        return new GetImageTask(imageHolder, movie);
    }

    /**
     * AsyncTask that fetches the movies
     */
    public class GetMoviesTask extends AsyncTask<String, Void, ResponseEntity> {
        String search="";
        boolean multiple = false;
        //instantiate the current listener, so the old responses never goes to newlly registered controllers
        WebManagerListener currWebListener = webManagerListener;

        public void defineMultiple(boolean m){
            multiple = m;
        }
        public void defineSearch(String search){
            this.search = search;
        }

        @Override
        protected ResponseEntity doInBackground(String... params) {
            try {
                //verify if the request has a search term (this defines if is a multiple fetch)
                if (params.length > 1) {
                    defineSearch(params[1]);
                    defineMultiple(true);
                }
                //create the url
                String url = Globals.MOVIES_URL + params[0] + search;
                //do the request proccess
                ResponseEntity response = getMoviesProcess(url, multiple);
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
        public void onPostExecute(ResponseEntity response) {
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
     * Function that fetches the movies, using reactive
     * @param params the parameters for the fetch
     * @param search the search
     */
    public void getMoviesReactive(final String params, final String search){
        final WebManagerListener currWebListener = webManagerListener;
        boolean mult = false;
        //verify if the request has a search term (this defines if is a multiple fetch)
        if (search != null) {
            mult = true;
        }
        final boolean multiple = mult;

        //we check if the observable exists, else we unsubscribe so the last call is not called
        if (moviesObservable != null)
            moviesObservable.unsubscribeOn(Schedulers.io());
        //we create the observable (this is in charge of retrieving the movie list)
        moviesObservable = Observable.fromCallable(new Callable<ResponseEntity>() {
                    @Override
                    public ResponseEntity call() throws Exception {
                        try {
                            String url = Globals.MOVIES_URL + params + (search != null ? search : "");
                            return getMoviesProcess(url, multiple);
                        } catch (HttpClientErrorException e){
                            //return the error response
                            return new ResponseEntity(e.getMessage(), e.getStatusCode());
                        } catch (Exception e) {
                            Log.e("WebRequest", e.getMessage(), e);
                        }
                        return null;
                    }
                });
        //we create the observer (this is in charge of observing the task and receiving the response of the movies)
        Observer<ResponseEntity> moviesObserver = new Observer<ResponseEntity>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.v("WebReactive", "Subscrito");
            }

            @Override
            public void onNext(ResponseEntity response) {
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

            @Override
            public void onError(Throwable e) {}

            @Override
            public void onComplete() { }
        };
        //we create the subscrition (this is in charge of creating the task in background and starts it
        //at .subscribe(observer))
        moviesObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(moviesObserver);
    }
    /**
     * The process that does the fetch of the movies from Trakt.tv
     * @param url The url to fetch
     * @return
     */
    public ResponseEntity getMoviesProcess(String url, boolean multiple) throws HttpClientErrorException {
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
    }

    /**
     * AsyncTask that fetches the list ulr_images
     */
    public class GetImagesTask extends AsyncTask<String, Void, Images> {
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
                //do the request
                Images response = getImagesProcess(url);
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
     * Function that fetches the list ulr_images, using reactive
     * @param tmdb_id
     * @param movie
     */
    public void getImagesReactive(final String tmdb_id, final MovieModel movie){
        final WebManagerListener currWebListener = webManagerListener;

        //we create the observable (this is in charge of retrieving the movie list)
        Observable<Images> imagesObservable = Observable.fromCallable(new Callable<Images>() {
                    @Override
                    public Images call() throws Exception {
                        try {
                            String url = Globals.TMSB_IMAGES_URL.replace("***", tmdb_id) + Globals.TMDB_ID;
                            return getImagesProcess(url);
                        } catch (Exception e) {
                            Log.e("WebRequest", e.getMessage(), e);
                        }
                        return null;
                    }
                });
        //we create the observer (this is in charge of observing the task and receiving the response of the movies)
        Observer<Images> imagesObserver = new Observer<Images>() {
            @Override
            public void onSubscribe(Disposable d) {}

            @Override
            public void onNext(Images response) {
                //verify if there is a weblistener registered
                if (null != currWebListener) {
                    //return the image list with the movie object
                    currWebListener.onReceiveHttpTMDB(response, movie);
                }
            }

            @Override
            public void onError(Throwable e) {}

            @Override
            public void onComplete() { }
        };
        //we create the subscrition (this is in charge of creating the task in background and starts it
        //at .subscribe(observer))
        imagesObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(imagesObserver);
    }
    /**
     * Process that does the fetch of the list of images urls from TMDB
     * @param url the url of the images service
     * @return list of image urls from TMDB
     */
    public Images getImagesProcess(String url){
        //define the spring request
        RestTemplate restTemplate = new RestTemplate();
        //we add the converter
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        //do the request
        Images response = restTemplate.getForObject(url, Images.class);
        return response;
    }

    /**
     * AsyncTask that fetches a bitmap image from the web
     */
    public class GetImageTask extends AsyncTask<String, Void, Bitmap> {
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
                //do the request process
                ResponseEntity<Resource> response = getImageProcess(url);
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
            //verify if there is a weblistener
            if (null != currWebListener) {
                //return the bitmap image with the movie object
                currWebListener.onReceiveHttpTMDBImage(image, movie, imageHolder);
            }
        }
    }
    /**
     * Function that fetches a bitmap image from the web, using reactive
     * @param imageHolder
     * @param movie
     * @param image_url
     * @param size
     */
    public void getImageReactive(final ImageView imageHolder, final MovieModel movie, final String image_url, final String size){
        final WebManagerListener currWebListener = webManagerListener;

        //we create the observable (this is in charge of retrieving the movie list)
        Observable<Bitmap> imageObservable = Observable.fromCallable(new Callable<Bitmap>() {
            @Override
            public Bitmap call() throws Exception {
                try {
                    //generate the url
                    final String url = Globals.TMSB_IMAGE_URL.replace("***", size) + image_url;
                    //do the request process
                    ResponseEntity<Resource> response = getImageProcess(url);
                    //convert the response to bitmap
                    if (null != response.getBody())
                        return BitmapFactory.decodeStream(response.getBody().getInputStream());
                } catch (Exception e) {
                    Log.e("WebRequest", e.getMessage(), e);
                }
                return null;
            }
        });
        //we create the observer (this is in charge of observing the task and receiving the response of the movies)
        Observer<Bitmap> imageObserver = new Observer<Bitmap>() {
            @Override
            public void onSubscribe(Disposable d) {}

            @Override
            public void onNext(Bitmap image) {
                //verify if there is a weblistener
                if (null != currWebListener) {
                    //return the bitmap image with the movie object
                    currWebListener.onReceiveHttpTMDBImage(image, movie, imageHolder);
                }
            }

            @Override
            public void onError(Throwable e) {}

            @Override
            public void onComplete() { }
        };
        //we create the subscrition (this is in charge of creating the task in background and starts it
        //at .subscribe(observer))
        imageObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(imageObserver);
    }
    /**
     * Process that fetches an image bitmap from TMDB
     * @param url the url of the image
     * @return ResponseEntity with the type Resource, and the image must be in the body
     */
    public ResponseEntity<Resource> getImageProcess(String url){
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
        return response;
    }

}
