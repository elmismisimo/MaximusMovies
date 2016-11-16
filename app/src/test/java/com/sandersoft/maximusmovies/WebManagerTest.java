package com.sandersoft.maximusmovies;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
//import android.support.v4.media.MediaMetadataCompat;
import android.test.AndroidTestCase;
import android.util.Log;
import android.widget.ImageView;

import com.sandersoft.maximusmovies.controlers.MoviesController;
import com.sandersoft.maximusmovies.interfaces.WebManagerListener;
import com.sandersoft.maximusmovies.models.MovieModel;
import com.sandersoft.maximusmovies.models.tmdb.Images;
import com.sandersoft.maximusmovies.utils.Globals;
import com.sandersoft.maximusmovies.views.MoviesViewFragment;

//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mockito;
//import org.mockito.runners.MockitoJUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

//import static org.junit.Assert.*;
import org.robolectric.*;

/**
 * Test class for testing the web manager functionality.
 * This class implement WebManagerListener so it can be used for the callbacks of the WebManager
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class WebManagerTest extends AndroidTestCase implements WebManagerListener {
    String response = "", search = "";
    MovieModel[] mResponse = new MovieModel[0];
    int page = 0;
    int cant = 0;
    Images images;
    Bitmap image;

    @Test
    public void getWebManagerListenerTest(){
        WebManager wm = new WebManager();
        wm.setWebManagerListener(this);
        assertTrue(this == wm.getwebManagerListener());
    }

    @Test
    public void getWebMoviesBasicErrorResponseTest() {
        WebManager wm = new WebManager();
        wm.setWebManagerListener(this);
        WebManager.GetMoviesTask mt = wm.getTheMoviesTask();

        //check error request
        ResponseEntity re = new ResponseEntity("Error request", HttpStatus.BAD_REQUEST);
        mt.onPostExecute(re);

        assertTrue(response.equals("400: Error request"));
    }
    @Test
    public void getWebMoviesBasicSingleResponseTest(){
        WebManager wm = new WebManager();
        wm.setWebManagerListener(this);
        WebManager.GetMoviesTask mt = wm.getTheMoviesTask();

        //check correct return only one
        MovieModel m = new MovieModel();
        ResponseEntity re = new ResponseEntity(m, HttpStatus.OK);
        mt.onPostExecute(re);

        for (MovieModel mm : mResponse)
            assertTrue(mm == null || mm instanceof MovieModel);
    }
    @Test
    public void getWebMoviesBasicMultipleResponseTest(){
        WebManager wm = new WebManager();
        wm.setWebManagerListener(this);
        WebManager.GetMoviesTask mt = wm.getTheMoviesTask();

        //check correct return multiple
        String localSearch = "this is my search";
        String localPage = "3";
        String localCant = "5";
        int localCount = 3;

        mt.defineMultiple(true);
        mt.defineSearch(localSearch);
        MovieModel[] ml = new MovieModel[localCount];
        for (int i = 0; i < localCount; i++)
            ml[i] = new MovieModel();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("x-pagination-item-count", localCant);
        responseHeaders.add("x-pagination-page", localPage);
        ResponseEntity re = new ResponseEntity(ml, responseHeaders, HttpStatus.OK);
        mt.onPostExecute(re);

        assertEquals(localCount, mResponse.length);
        assertEquals(Integer.parseInt(localCant), cant);
        assertEquals(Integer.parseInt(localPage), page);
        assertEquals(localSearch, search);
        for (MovieModel mm : mResponse)
            assertTrue(mm == null || mm instanceof MovieModel);
    }

    @Test
    public void getWebMoviesRealSingleTest(){
        WebManager wm = new WebManager();
        wm.setWebManagerListener(this);
        WebManager.GetMoviesTask mt = wm.getTheMoviesTask();

        //define the url
        String url = Globals.MOVIES_URL + "120?extended=full";
        ResponseEntity response = wm.getMoviesProcess(url, false);
        mt.onPostExecute(response);

        for (MovieModel mm : mResponse)
            assertTrue(mm == null || mm instanceof MovieModel);
    }

    @Test
    public void getWebMoviesRealMultipleTest(){
        WebManager wm = new WebManager();
        wm.setWebManagerListener(this);
        WebManager.GetMoviesTask mt = wm.getTheMoviesTask();
        //mock the build version
        try{setFinalStatic(Build.VERSION.class.getField("SDK_INT"), 19);} catch (Exception ex){}

        //define variables
        int localPage = 1;
        int localCant = 10;
        int localCount = 10;
        //define the url for pupolar search
        String url = Globals.MOVIES_URL + "popular?limit=10&page=" + localPage;
        mt.defineMultiple(true);
        ResponseEntity response = wm.getMoviesProcess(url, true);
        mt.onPostExecute(response);

        assertEquals(localCount, mResponse.length);
        assertTrue(localCant < cant);
        assertEquals(localPage, page);
        for (MovieModel mm : mResponse)
            assertTrue(mm == null || mm instanceof MovieModel);

        String localSearch = "montessori";
        localPage = 1;
        localCant = 1;
        localCount = 1;
        //define the url for pupolar search
        url = Globals.MOVIES_URL + "popular?limit=10&page=" + localPage + "&query=" + localSearch;
        mt.defineMultiple(true);
        mt.defineSearch(localSearch);
        response = wm.getMoviesProcess(url, true);
        mt.onPostExecute(response);

        assertEquals(localCount, mResponse.length);
        assertEquals(localCant, cant);
        assertEquals(localPage, page);
        assertEquals(localSearch, search);
        for (MovieModel mm : mResponse)
            assertTrue(mm == null || mm instanceof MovieModel);
    }

    @Test
    public void getWebImagesRealTest(){
        WebManager wm = new WebManager();
        wm.setWebManagerListener(this);
        WebManager.GetImagesTask it = wm.getTheImagesTask(null);

        //define the url (for batman)
        String url = Globals.TMSB_IMAGES_URL.replace("***", "155") + Globals.TMDB_ID;
        Images localImages = wm.getImagesProcess(url);
        it.onPostExecute(localImages);

        assertTrue(images != null);
        assertTrue(images.getBackdrops() != null);
        assertTrue(images.getPosters() != null);
        assertTrue(images.getBackdrops().size() > 0);
        assertTrue(images.getPosters().size() > 0);
    }

    @Test
    public void getWebImageRealTest(){
        WebManager wm = new WebManager();
        wm.setWebManagerListener(this);
        WebManager.GetImageTask it = wm.getTheImageTask(null, null);

        try {
            //define the url
            String url = Globals.TMSB_IMAGE_URL.replace("***", "w500") + "/2cLndRZy8e3das3vVaK3BdJfRIi.jpg";
            ResponseEntity<Resource> response = wm.getImageProcess(url);
            //convert the response to bitmap
            if (null != response.getBody())
                it.onPostExecute(BitmapFactory.decodeStream(response.getBody().getInputStream()));
        }catch (Exception ex) {
            it.onPostExecute(null);
        }

        assertTrue(image != null);
        assertTrue(image instanceof Bitmap);

        try {
            //define the url of bad image file
            String url = Globals.TMSB_IMAGE_URL.replace("***", "w500") + "/0.jpg";
            ResponseEntity<Resource> response = wm.getImageProcess(url);
            //convert the response to bitmap
            if (null != response.getBody())
                it.onPostExecute(BitmapFactory.decodeStream(response.getBody().getInputStream()));
        }catch (Exception ex) {
            it.onPostExecute(null);
        }

        assertTrue(image == null);
    }


    //----------------------- WebManagerListener Implementation ---------------------

    @Override
    public void onReceiveHttpAnswer(MovieModel[] jsonResponse, int cant, int page, String search) {
        mResponse = jsonResponse;
        this.cant = cant;
        this.page = page;
        this.search = search;
    }

    @Override
    public void onReceiveHttpAnswerError(String error) {
        response = error;
    }

    @Override
    public void onReceiveHttpTMDB(Images images, MovieModel movie) {
        this.images = images;
    }

    @Override
    public void onReceiveHttpTMDBImage(Bitmap image, MovieModel movie, ImageView imageHolder) {
        this.image = image;
    }

    /**
     * Function that allows to mock the sdk version for the tests
     * The HTTPAccesor has a factory that returns a diferent object of the SDK version is
     * less than 16 (Gingerbread), and then crashes because some class that is notfound because android removed them
     * Its a class from the org.apache class, and its known to be very buggy when it comes to web requests
     * @param field The field to mock
     * @param newValue the value that will be instead
     * @throws Exception
     */
    static void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(null, newValue);
    }
}