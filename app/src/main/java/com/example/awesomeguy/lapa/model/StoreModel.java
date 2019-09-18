package com.example.awesomeguy.lapa.model;

import java.io.Serializable;

/**
 * Created by Mohanad on 09/08/19.
 */
public class StoreModel implements Serializable {

    private String id, name, icon, address, rating, distance, duration, longitude, latitude;

    public StoreModel(String id, String name, String icon, String address, String rating, String longitude, String latitude, String distance, String duration) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.address = address;
        this.rating = rating;
        this.distance = distance;
        this.duration = duration;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }

    public String getAddress() {
        return address;
    }

    public String getDistance() {
        return distance;
    }

    public String getRating() {
        return rating;
    }

    public String getDuration() {
        return duration;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getLatitude() {
        return latitude;
    }
}