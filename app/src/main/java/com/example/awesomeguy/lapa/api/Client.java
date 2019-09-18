package com.example.awesomeguy.lapa.api;


import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Mohanad on 09/08/19.
 */
public class Client {

    private static Retrofit retrofit = null;

    private static String base_url = "https://maps.googleapis.com/maps/api/";
    public static final String GOOGLE_PLACE_API_KEY = "AIzaSyC2DCKZARG6owQoun9lGzcA2MND9CHf4Cw";

    public static Retrofit getClient() {

        //HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        //interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                //.addInterceptor(interceptor)
                .build();

        retrofit = null;
        retrofit = new Retrofit.Builder()
                .baseUrl(base_url)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();


        return retrofit;
    }

}
