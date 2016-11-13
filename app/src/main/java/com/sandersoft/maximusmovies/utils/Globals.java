package com.sandersoft.maximusmovies.utils;

/**
 * Created by Sander on 10/11/2016.
 */
public class Globals {
    //IDS
    public static final String TRAKT_ID = "019a13b1881ae971f91295efc7fdecfa48b32c2a69fe6dd03180ff59289452b8";
    public static final String TMDB_ID = "a04c1584cba00eff07ec744a26320b6d";

    //URLS
    public static final String MOVIES_URL = "https://api.trakt.tv/movies/";
    //replace the *** from the url with the tmdb_id of the movie
    public static final String TMSB_IMAGES_URL = "https://api.themoviedb.org/3/movie/***/images?api_key=";
    //this url may change, to get it again use https://api.themoviedb.org/3/configuration?api_key=***
    //and get the field "base_url". Replace the *** for the size width (w185, w500, original) <- prefered
    public static final String TMSB_IMAGE_URL = "http://image.tmdb.org/t/p/***/";

    //TAGS
    public static final String TAG_MOVIE_FRAGMENT = "movieDetailFragment";
    public static final String TAG_MOVIE_DETAIL_FRAGMENT = "movieDetailFragment";
    public static final String MOVIE_CONTROLLER_TAG = "movieController";
    public static final String MOVIE_OBJ_TAG = "movieObject";
}
