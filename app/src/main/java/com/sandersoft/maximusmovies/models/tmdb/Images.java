package com.sandersoft.maximusmovies.models.tmdb;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sander on 10/11/2016.
 */
public class Images implements Parcelable {
    private Integer id;
    private List<Image> backdrops = new ArrayList<Image>();
    private List<Image> posters = new ArrayList<Image>();

    /**
     * Empty constructor for the json serialization
     */
    public Images(){}

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

    // Parcelling part
    public Images(Parcel in){
        id = in.readInt();
        backdrops = in.readArrayList(Image.class.getClassLoader());
        posters = in.readArrayList(Image.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeList(backdrops);
        dest.writeList(posters);
    }
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Images createFromParcel(Parcel in) {
            return new Images(in);
        }

        public Images[] newArray(int size) {
            return new Images[size];
        }
    };
}
