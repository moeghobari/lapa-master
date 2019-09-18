package com.example.awesomeguy.lapa.model;

import android.content.Context;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Mohanad on 09/08/19.
 */
public class ReviewModel implements Serializable {

    private String id, subject, desc, date, username, rating;

    public ReviewModel(String id, String subject, String desc, String date, String username, String rating) {
        this.id = id;
        this.subject = subject;
        this.desc = desc;
        this.date = date;
        this.username = username;
        this.rating = rating;
    }

    public String getId() {
        return id;
    }

    public String getSubject() {
        return subject;
    }

    public String getDesc() {
        return desc;
    }

    public String getDate() {
        return date;
    }

    public String getUsername() {
        return username;
    }

    public String getRating() {
        return rating;
    }

}