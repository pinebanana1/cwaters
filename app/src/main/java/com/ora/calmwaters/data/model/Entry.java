package com.ora.calmwaters.data.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Entry implements Parcelable {

    private String name;
    private float rating;
    private float minRating;
    private float maxRating;
    private String description;


    public Entry(String name, float minRating, float maxRating, String description) {
        this.name = name;
        this.rating = 0;
        this.minRating = minRating;
        this.maxRating = maxRating;
        this.description = description;
    }

    public Entry(String name, float rating, float minRating, float maxRating, String description) {
        this.name = name;
        this.rating = rating;
        this.minRating = minRating;
        this.maxRating = maxRating;
        this.description = description;
    }

    protected Entry(Parcel in) {
        name = in.readString();
        rating = in.readFloat();
        minRating = in.readFloat();
        maxRating = in.readFloat();
        description = in.readString();
    }

    public static final Creator<Entry> CREATOR = new Creator<Entry>() {
        @Override
        public Entry createFromParcel(Parcel in) {
            return new Entry(in);
        }

        @Override
        public Entry[] newArray(int size) {
            return new Entry[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public float getMinRating() {
        return minRating;
    }

    public void setMinRating(int minRating) {
        this.minRating = minRating;
    }

    public float getMaxRating() {
        return maxRating;
    }

    public void setMaxRating(int maxRating) {
        this.maxRating = maxRating;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeFloat(rating);
        dest.writeFloat(minRating);
        dest.writeFloat(maxRating);
        dest.writeString(description);
    }
}
