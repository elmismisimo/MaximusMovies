package com.sandersoft.maximusmovies;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sandersoft.maximusmovies.controlers.MoviesController;

/**
 * Created by Sander on 14/11/2016.
 */
public class BcReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //if its a connectivity change
        if(intent.getAction().contains("android.net.conn.CONNECTIVITY_CHANGE")){
            //if the application is not null and there is internet connection
            if (null != ApplicationMain.instance && ApplicationMain.webManager.verifyConn())
                //if the current controller of the weblistener is the MoviesController
                if (ApplicationMain.webManager.getwebManagerListener() instanceof MoviesController)
                    //request the first 10 results of the query
                    ((MoviesController)ApplicationMain.webManager.getwebManagerListener()).doMoviesRequest();
        }
    }
}
