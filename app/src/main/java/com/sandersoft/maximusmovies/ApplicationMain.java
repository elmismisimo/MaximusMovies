package com.sandersoft.maximusmovies;

import android.app.Application;

/**
 * Created by Sander on 10/11/2016.
 */
public class ApplicationMain extends Application {

    public static WebManager webManager;

    @Override
    public void onCreate() {
        super.onCreate();

        webManager = new WebManager();
    }
}
