package com.sandersoft.maximusmovies;

import android.app.Application;
import android.content.Context;

/**
 * Created by Sander on 10/11/2016.
 */
public class ApplicationMain extends Application {

    //the webmanager
    public static WebManager webManager;
    //the instance of this class
    public static ApplicationMain instance;

    public ApplicationMain(){
        instance = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        webManager = new WebManager();
    }

    public static Context getContext(){
        return instance;
    }
}
