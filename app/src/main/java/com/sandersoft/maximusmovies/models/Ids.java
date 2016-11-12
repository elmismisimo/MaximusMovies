package com.sandersoft.maximusmovies.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Sander on 10/11/2016.
 */
public class Ids implements Parcelable {
    private Integer trakt;
    private String slug;
    private String imdb;
    private Integer tmdb;

    /**
     * empty contructor for the json serialization
     */
    public Ids(){}

    public Integer getTrakt() {
        return trakt;
    }

    public void setTrakt(Integer trakt) {
        this.trakt = trakt;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getImdb() {
        return imdb;
    }

    public void setImdb(String imdb) {
        this.imdb = imdb;
    }

    public Integer getTmdb() {
        return tmdb;
    }

    public void setTmdb(Integer tmdb) {
        this.tmdb = tmdb;
    }


    // Parcelling part
    public Ids(Parcel in){
        trakt = in.readInt();
        slug = in.readString();
        imdb = in.readString();
        tmdb = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(trakt);
        dest.writeString(slug);
        dest.writeString(imdb);
        dest.writeInt(tmdb);
    }
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Ids createFromParcel(Parcel in) {
            return new Ids(in);
        }

        public Ids[] newArray(int size) {
            return new Ids[size];
        }
    };
}
