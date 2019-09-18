package com.example.awesomeguy.lapa.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.awesomeguy.lapa.R;
import com.example.awesomeguy.lapa.model.ReviewModel;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.MyViewHolder> {

    private List<ReviewModel> reviews;


    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView subject, desc, date, price, username;

        MyViewHolder(View view) {
            super(view);
            subject = view.findViewById(R.id.review_subject);
            desc = view.findViewById(R.id.review_desc);
            date = view.findViewById(R.id.review_date);
            username = view.findViewById(R.id.review_username);
        }
    }

    public ReviewAdapter(List<ReviewModel> reviews, Context context) {
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public ReviewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_list, parent, false);

        return new ReviewAdapter.MyViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ReviewAdapter.MyViewHolder holder, final int position) {
        ReviewModel review_list = reviews.get(position);

        holder.subject.setText(review_list.getSubject());
        holder.desc.setText(review_list.getDesc());

        holder.date.setText(review_list.getDate());
        String username = "reviewed by: " + review_list.getUsername();
        holder.username.setText(username);
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

}