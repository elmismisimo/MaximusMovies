package com.sandersoft.maximusmovies.models;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.sandersoft.maximusmovies.models.tmdb.Images;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sander on 09/11/2016.
 */
public class MovieModel implements Parcelable {

    private String title;
    private Integer year;
    private Ids ids;
    private String tagline;
    private String overview;
    private String released;
    private Integer runtime;
    private String trailer;
    private Object homepage;
    private Double rating;
    private Integer votes;
    private String updated_at;
    private String language;
    private List<String> available_translations = new ArrayList<String>();
    private List<String> genres = new ArrayList<String>();
    private Object certification;
    private Images images;
    private Bitmap poster;

    /**
     * Empty contructor so the json serialization
     */
    public MovieModel(){}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Ids getIds() {
        return ids;
    }

    public void setIds(Ids ids) {
        this.ids = ids;
    }

    public String getTagline() {
        return tagline;
    }

    public void setTagline(String tagline) {
        this.tagline = tagline;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getReleased() {
        return released;
    }

    public void setReleased(String released) {
        this.released = released;
    }

    public Integer getRuntime() {
        return runtime;
    }

    public void setRuntime(Integer runtime) {
        this.runtime = runtime;
    }

    public String getTrailer() {
        return trailer;
    }

    public void setTrailer(String trailer) {
        this.trailer = trailer;
    }

    public Object getHomepage() {
        return homepage;
    }

    public void setHomepage(Object homepage) {
        this.homepage = homepage;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Integer getVotes() {
        return votes;
    }

    public void setVotes(Integer votes) {
        this.votes = votes;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public List<String> getAvailable_translations() {
        return available_translations;
    }

    public void setAvailable_translations(List<String> available_translations) {
        this.available_translations = available_translations;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public Object getCertification() {
        return certification;
    }

    public void setCertification(Object certification) {
        this.certification = certification;
    }

    public Images getImages() {
        return images;
    }

    public void setImages(Images images) {
        this.images = images;
    }

    public Bitmap getPoster() {
        return poster;
    }

    public void setPoster(Bitmap poster) {
        this.poster = poster;
    }

    // Parcelling part
    public MovieModel(Parcel in){
        title = in.readString();
        year = in.readInt();
        ids = in.readParcelable(Ids.class.getClassLoader());
        //tagline = in.readString();
        overview = in.readString();
        //released = in.readString();
        //runtime = in.readInt();
        trailer = in.readString();
        //homepage = in.readParcelable(Object.class.getClassLoader());
        //rating = in.readDouble();
        //votes = in.readInt();
        //updated_at = in.readString();
        //language = in.readString();
        //available_translations = in.readArrayList(String.class.getClassLoader());
        //genres = in.readArrayList(String.class.getClassLoader());
        //certification = in.readParcelable(Object.class.getClassLoader());
        images = in.readParcelable(Images.class.getClassLoader());
        poster = in.readParcelable(Bitmap.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeInt(year != null ? year : 0);
        dest.writeParcelable(ids, flags);
        dest.writeString(overview);
        dest.writeString(trailer);
        dest.writeParcelable(images, flags);
        dest.writeParcelable(poster, flags);
    }
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public MovieModel createFromParcel(Parcel in) {
            return new MovieModel(in);
        }

        public MovieModel[] newArray(int size) {
            return new MovieModel[size];
        }
    };
}
