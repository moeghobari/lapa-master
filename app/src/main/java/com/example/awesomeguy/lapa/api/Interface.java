package com.example.awesomeguy.lapa.api;

import com.example.awesomeguy.lapa.service.PlacesPOJO;
import com.example.awesomeguy.lapa.util.ResultDistanceMatrix;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface Interface {
    @GET("place/nearbysearch/json?")
    Call<PlacesPOJO.Root> doPlaces(
            @Query(value = "type", encoded = true) String type,
            @Query(value = "location", encoded = true) String location,
            @Query(value = "name", encoded = true) String name,
            @Query(value = "opennow", encoded = true) boolean opennow,
            @Query(value = "rankby", encoded = true) String rankby,
            @Query(value = "key", encoded = true) String key
    );

    // origins/destinations:  LatLng as string
    @GET("distancematrix/json")
    Call<ResultDistanceMatrix> getDistance(
            @Query("key") String key,
            @Query("origins") String origins,
            @Query("destinations") String destinations
    );


}
