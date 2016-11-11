package com.sandersoft.maximusmovies.models.tmdb;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sander on 10/11/2016.
 */
public class Images {
    private Integer id;
    private List<Image> backdrops = new ArrayList<Image>();
    private List<Image> posters = new ArrayList<Image>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<Image> getBackdrops() {
        return backdrops;
    }

    public void setBackdrops(List<Image> backdrops) {
        this.backdrops = backdrops;
    }

    public List<Image> getPosters() {
        return posters;
    }

    public void setPosters(List<Image> posters) {
        this.posters = posters;
    }
}
