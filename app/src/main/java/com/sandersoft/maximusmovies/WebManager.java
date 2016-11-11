package com.sandersoft.maximusmovies;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.sandersoft.maximusmovies.models.tmdb.Images;
import com.sandersoft.maximusmovies.utils.Globals;
import com.sandersoft.maximusmovies.interfaces.WebManagerListener;
import com.sandersoft.maximusmovies.models.MovieModel;
import com.sandersoft.maximusmovies.models.QueryModel;

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

    private WebManagerListener webManagerListener;

    public WebManager(){

    }

    public void setWebManagerListener(WebManagerListener webManagerListener) {
        this.webManagerListener = webManagerListener;
    }

    public void doMoviesRequest(String params){
        new GetMoviesTask().execute("trending?"+ params);
        //doImagesRequest("308266");
    }
    public void doMovieRequest(String params){
        new GetMoviesTask().execute(params);
    }
    public void doImagesRequest(String tmdb_id){
        new GetImagesTask().execute(tmdb_id);
    }
    public void doImageRequest(ImageView imageHolder, String image){
        new GetImageTask(imageHolder).execute(image);
    }

    private class GetMoviesTask extends AsyncTask<String, Void, ResponseEntity> {

        @Override
        protected ResponseEntity doInBackground(String... params) {
            try {
                //final String url = "http://rest-service.guides.spring.io/greeting";
                //final String url = "https://api.trakt.tv/movies/tron-legacy-2010";
                final String url = "https://api.trakt.tv/movies/" + params[0];
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
                //restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
                //do the request
                //MovieModel movie = restTemplate.getForObject(url, MovieModel.class);
                //ResponseEntity<MovieModel> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, MovieModel.class);
                ResponseEntity response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, QueryModel[].class);
                return response;
            } catch (HttpClientErrorException e){
                return new ResponseEntity(e.getMessage(), e.getStatusCode());
            } catch (Exception e) {
                Log.e("WebRequest", e.getMessage(), e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(ResponseEntity response) {
            if (webManagerListener != null) {
                if (response.getStatusCode().equals(HttpStatus.OK)) {
                    try{
                        //get the amount of records
                        int c = 0;
                        try{c = new Integer(response.getHeaders().getFirst("x-pagination-item-count"));
                        } catch (Exception ex){}
                        //return the movies
                        webManagerListener.onReceiveHttpAnswer((QueryModel[]) response.getBody(), c);
                    } catch (Exception ex){
                        //return the movie
                        webManagerListener.onReceiveHttpAnswer((MovieModel)response.getBody());
                    }
                    //webManagerListener.onReceiveHttpAnswer(movie);
                } else {
                    webManagerListener.onReceiveHttpAnswerError(response.getStatusCode() + ": " + response.getBody().toString());
                }
            }
        }
    }

    private class GetImagesTask extends AsyncTask<String, Void, Images> {

        @Override
        protected Images doInBackground(String... params) {
            try {
                final String url = "https://api.themoviedb.org/3/movie/" + params[0] + "/images?api_key=" + Globals.TMDB_ID;
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
            if (webManagerListener != null) {
                webManagerListener.onReceiveHttpTMDB(response);
            }
        }
    }

    private class GetImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView imageHolder;

        public GetImageTask(ImageView imageHolder) {
            this.imageHolder = imageHolder;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                final String url = Globals.TMSB_IMAGE_URL + params[0];
                //define the spring request
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
                restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
                restTemplate.getMessageConverters().add(new ResourceHttpMessageConverter());
                //do the request
                HttpHeaders requestHeaders = new HttpHeaders();
                HttpEntity<?> requestEntity = new HttpEntity<Object>(requestHeaders);
                ResponseEntity<Resource> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Resource.class);
                Bitmap bitmap = BitmapFactory.decodeStream(response.getBody().getInputStream());
                return bitmap;
            } catch (Exception e) {
                Log.e("WebRequest", e.getMessage(), e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Bitmap response) {
            if (imageHolder != null) {
                imageHolder.setImageBitmap(response);
            }
        }
    }
}
