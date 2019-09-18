package com.example.awesomeguy.lapa.service;


import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mohanad on 09/08/19.
 */
public class PlacesPOJO {

    public class Root implements Serializable {
        @SerializedName("results")
        public List<CustomA> customA = new ArrayList<>();
        @SerializedName("status")
        public String status;
    }

    public class CustomA implements Serializable {
        @SerializedName("id")
        public String id;
        @SerializedName("name")
        public String name;
        @SerializedName("icon")
        public String icon;
        @SerializedName("rating")
        public String rating;
        @SerializedName("geometry")
        public Geometry geometry;
        @SerializedName("vicinity")
        public String vicinity;
    }

    public class Geometry implements Serializable {
        @SerializedName("location")
        public LocationA locationA;
    }

    public class LocationA implements Serializable {
        @SerializedName("lat")
        public String lat;
        @SerializedName("lng")
        public String lng;
    }

}
