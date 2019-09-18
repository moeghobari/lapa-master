package com.example.awesomeguy.lapa.adapter;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.awesomeguy.lapa.R;
import com.example.awesomeguy.lapa.model.StoreModel;
import com.example.awesomeguy.lapa.service.PlacesPOJO;
import com.example.awesomeguy.lapa.ui.MainActivity;
import com.example.awesomeguy.lapa.ui.SuggestionActivity;

import java.util.List;


/**
 * Created by Mohanad on 09/08/19.
 */
public class PlacesRecyclerView extends RecyclerView.Adapter<PlacesRecyclerView.MyViewHolder> {
    // private List<PlacesPOJO.CustomA> stLstStores;
    private List<StoreModel> models;

    private LinearLayout linearLayout;
    private Context mContext;

    private String mpage;

    // List<PlacesPOJO.CustomA> stores,
    public PlacesRecyclerView(List<StoreModel> storeModels, Context context, String page) {
        // stLstStores = stores;
        models = storeModels;
        mContext = context;
        mpage = page;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView txtStoreName;
        TextView txtStoreAddr;
        TextView txtStoreDist;
        TextView txtStoreRating;
        StoreModel model;

        MyViewHolder(View itemView) {
            super(itemView);

            this.txtStoreName = itemView.findViewById(R.id.txtStoreName);
            this.txtStoreAddr = itemView.findViewById(R.id.txtStoreAddr);
            this.txtStoreDist = itemView.findViewById(R.id.txtStoreDist);
            this.txtStoreRating = itemView.findViewById(R.id.txtStoreRating);

            linearLayout = itemView.findViewById(R.id.locationRecyclerView);
        }

        @SuppressLint("SetTextI18n")
        private void setData(MyViewHolder holder, StoreModel storeModel) {
            this.model = storeModel;

            holder.txtStoreName.setText(model.getName());
            holder.txtStoreAddr.setText(model.getAddress());
            holder.txtStoreDist.setText(String.format("%s, %s", model.getDistance(), model.getDuration()));
            holder.txtStoreRating.setText("Rating: " + model.getRating());
        }

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.loction_list, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.setData(holder, models.get(holder.getAdapterPosition()));
        StoreModel _model = models.get(position);

        linearLayout.setOnClickListener(v -> {
            if (mpage.equals("main")) ((MainActivity) mContext).locationClicked(_model);
            else ((SuggestionActivity) mContext).locationClicked(_model);
        });
    }


    @Override
    public int getItemCount() {
        // return Math.min(20, models.size());
        return models.size();
    }


}
