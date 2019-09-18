package com.example.awesomeguy.lapa.ui;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.awesomeguy.lapa.R;
import com.example.awesomeguy.lapa.adapter.PlacesRecyclerView;
import com.example.awesomeguy.lapa.api.Client;
import com.example.awesomeguy.lapa.api.Interface;
import com.example.awesomeguy.lapa.model.StoreModel;
import com.example.awesomeguy.lapa.service.PlacesPOJO;
import com.example.awesomeguy.lapa.util.ResultDistanceMatrix;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SuggestionActivity extends AppCompatActivity {

    @SuppressLint("StaticFieldLeak")
    private static Context context;

    private ProgressDialog mProgressDialog;
    private ToggleButton recentSuggest, googleSuggest;


    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    // private PlacesRecyclerView placeAdapter;

    List<StoreModel> placeModel, recentPlaceModel;
    Interface apiService;

    String latLngString;
    LatLng latLng;

    RecyclerView recyclerView, grecyclerView;
    List<PlacesPOJO.CustomA> results;

    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestion);

        context = this;
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("recent_places");

        mProgressDialog = new ProgressDialog(this);

        Toolbar toolbar = findViewById(R.id.suggestion_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_chevron_left_white_32dp);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(view -> goBack());

//        findViewById(R.id.fab).setOnClickListener(view -> {
//            Intent intent = new Intent(this.getApplicationContext(), MapActivity.class);
//            intent.putExtra("places", (Serializable) placeModel);
//            intent.putExtra("lastLoc", latLngString);
//            intent.putExtra("previous", "suggestion");
//            startActivity(intent);
//
//            overridePendingTransitionExit();
//        });

        apiService = Client.getClient().create(Interface.class);

        googleSuggest = findViewById(R.id.googlePlaces);
        recentSuggest = findViewById(R.id.suggestPlace);

        googleSuggest.setOnClickListener(view -> {
            if (googleSuggest.isChecked()) grecyclerView.setVisibility(View.VISIBLE);
            else grecyclerView.setVisibility(View.INVISIBLE);
        });

        recentSuggest.setOnClickListener(view -> {
            if (recentSuggest.isChecked()) recyclerView.setVisibility(View.VISIBLE);
            else recyclerView.setVisibility(View.INVISIBLE);
        });

        grecyclerView = findViewById(R.id.googleSuggestRecyclerView);
        grecyclerView.setNestedScrollingEnabled(false);
        grecyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        grecyclerView.setLayoutManager(layoutManager);

        Intent i = getIntent();
        username = Objects.requireNonNull(i.getExtras()).getString("username");
        latLngString = Objects.requireNonNull(i.getExtras()).getString("lastLoc");

        showProgressIndicator("Loading...");
        suggestRecentLocations();
        fetchStores();
    }

    private void suggestRecentLocations() {
        recentPlaceModel = new ArrayList<StoreModel>();
        PlacesRecyclerView placeAdapter = new PlacesRecyclerView(recentPlaceModel, context, "suggestion");

        recyclerView = findViewById(R.id.suggestRecyclerView);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(placeAdapter);

        recyclerData(placeAdapter);
    }

    private void recyclerData(PlacesRecyclerView placeAdapter) {
        recentPlaceModel.clear();

        String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("recent_places").child(user_id);

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // progressDialog.dismiss();
                // do nothing...
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // do nothing...
            }
        });
        mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                // if (placeModel.size() <= 5) {
                allListData(dataSnapshot);
                placeAdapter.notifyDataSetChanged();
                // }
            }

            @SuppressWarnings({"unchecked", "ConstantConditions"})
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
                String _mapId = dataSnapshot.getKey();

                Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                try {
                    String _mapName = map.get("name").toString();
                    String _mapIcon = map.get("icon").toString();
                    String _mapAddress = map.get("address").toString();
                    String _mapRating = map.get("rating").toString();
                    String _mapLongitude = map.get("longitude").toString();
                    String _mapLatitude = map.get("latitude").toString();
                    String _mapDistance = map.get("distance").toString();
                    String _mapDuration = map.get("duration").toString();

                    for (StoreModel placeList : recentPlaceModel) {
                        if (placeList.getId().equals(_mapId)) {
                            recentPlaceModel.set(recentPlaceModel.indexOf(placeList), new StoreModel(_mapId, _mapName, _mapIcon, _mapAddress, _mapRating, _mapLongitude, _mapLatitude, _mapDistance, _mapDuration));
                        }
                    }
                    placeAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    System.out.println("Error:" + e.getMessage());
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                // do nothing...
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {
                // do nothing...
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // do nothing...
            }
        });
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    @SuppressLint("NewApi")
    public void allListData(final DataSnapshot dataSnapshot) {
        if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
            Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

            if (map != null) {
                try {
                    String _mapId = dataSnapshot.getKey();
                    String _mapName = map.get("name").toString();
                    String _mapIcon = map.get("icon").toString();
                    String _mapAddress = map.get("address").toString();
                    String _mapRating = map.get("rating").toString();
                    String _mapLongitude = map.get("longitude").toString();
                    String _mapLatitude = map.get("latitude").toString();
                    String _mapDistance = map.get("distance").toString();
                    String _mapDuration = map.get("duration").toString();

                    recentPlaceModel.add(0, new StoreModel(_mapId, _mapName, _mapIcon, _mapAddress, _mapRating, _mapLongitude, _mapLatitude, _mapDistance, _mapDuration));
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }

            }

        }
    }

    private void fetchStores() {
        Call<PlacesPOJO.Root> call = apiService.doPlaces("point_of_interest", latLngString, "", true, "distance", Client.GOOGLE_PLACE_API_KEY);
        call.enqueue(new Callback<PlacesPOJO.Root>() {
            @Override
            public void onResponse(Call<PlacesPOJO.Root> call, Response<PlacesPOJO.Root> response) {
                PlacesPOJO.Root root = response.body();

                if (response.isSuccessful()) {
                    if (root.status.equals("OK")) {
                        results = root.customA;
                        placeModel = new ArrayList<StoreModel>();

                        for (int i = 0; i < results.size(); i++) {
                            if (i == 20) break;

                            PlacesPOJO.CustomA info = results.get(i);
                            fetchDistance(info);
                        }
                    } else {
                        mProgressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "No matches found near you", Toast.LENGTH_SHORT).show();
                    }

                } else if (response.code() != 200) {
                    mProgressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Error " + response.code() + " found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PlacesPOJO.Root> call, Throwable t) {
                // Log error here since request failed
                mProgressDialog.dismiss();
                call.cancel();
            }
        });

    }

    private void fetchDistance(final PlacesPOJO.CustomA info) {
        Call<ResultDistanceMatrix> call = apiService.getDistance(Client.GOOGLE_PLACE_API_KEY, latLngString, info.geometry.locationA.lat + "," + info.geometry.locationA.lng);
        call.enqueue(new Callback<ResultDistanceMatrix>() {
            @Override
            public void onResponse(Call<ResultDistanceMatrix> call, Response<ResultDistanceMatrix> response) {
                ResultDistanceMatrix resultDistance = response.body();
                if ("OK".equalsIgnoreCase(resultDistance.status)) {

                    ResultDistanceMatrix.InfoDistanceMatrix infoDistanceMatrix = resultDistance.rows.get(0);
                    ResultDistanceMatrix.InfoDistanceMatrix.DistanceElement distanceElement = infoDistanceMatrix.elements.get(0);
                    if ("OK".equalsIgnoreCase(distanceElement.status)) {
                        ResultDistanceMatrix.InfoDistanceMatrix.ValueItem itemDuration = distanceElement.duration;
                        ResultDistanceMatrix.InfoDistanceMatrix.ValueItem itemDistance = distanceElement.distance;
                        String totalDistance = String.valueOf(itemDistance.text);
                        String totalDuration = String.valueOf(itemDuration.text);

                        placeModel.add(new StoreModel(info.id, info.name, info.icon, info.vicinity, info.rating, info.geometry.locationA.lng, info.geometry.locationA.lat, totalDistance, totalDuration));
                        if (placeModel.size() == 20 || placeModel.size() == results.size()) {
                            // results,
                            PlacesRecyclerView adapterStores = new PlacesRecyclerView(placeModel, context, "suggestion");
                            grecyclerView.setAdapter(adapterStores);

                            // placeAdapter.notifyDataSetChanged();
                            try {
                                mProgressDialog.dismiss();
                            } catch (Exception e) {
                                System.out.println("Something went wrong here...");
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ResultDistanceMatrix> call, Throwable t) {
                call.cancel();
            }
        });

    }

    /**
     * Go to location, view
     */
    public void locationClicked(StoreModel store) {
        Intent intent = new Intent(context, MapViewActivity.class);
        intent.putExtra("place", store);
        intent.putExtra("previous", "suggestion");
        intent.putExtra("username", username);
        context.startActivity(intent);

        overridePendingTransitionExit();
    }

    public void showProgressIndicator(final String message) {
        mProgressDialog.dismiss();
        mProgressDialog.setTitle(getString(R.string.suggesting_places));
        mProgressDialog.setMessage(message);
        mProgressDialog.show();
    }


    @Override
    public void onBackPressed() {
        goBack();
    }

    public void goBack() {
        Intent intent = new Intent(this.getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        this.finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransitionExit();
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransitionEnter();
    }

    /**
     * Overrides the pending Activity transition by performing the "Enter" animation.
     */
    protected void overridePendingTransitionEnter() {
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    /**
     * Overrides the pending Activity transition by performing the "Exit" animation.
     */
    protected void overridePendingTransitionExit() {
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
}
