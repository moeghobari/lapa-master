package com.example.awesomeguy.lapa.ui;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.awesomeguy.lapa.R;
import com.example.awesomeguy.lapa.adapter.ReviewAdapter;
import com.example.awesomeguy.lapa.model.ReviewModel;
import com.example.awesomeguy.lapa.model.StoreModel;
import com.example.awesomeguy.lapa.service.OnMapAndViewReadyListener;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class MapViewActivity extends AppCompatActivity implements OnMapAndViewReadyListener.OnGlobalLayoutAndMapReadyListener, OnMapReadyCallback {

    private GoogleMap mMap;

    private EditText subject, desc;
    private DatabaseReference mDatabase;
    private ReviewAdapter rAdapter;

    private ArrayList<ReviewModel> rRecycler = new ArrayList<ReviewModel>();

    ProgressDialog progressDialog;
    private BottomSheetDialog mBottomSheetDialog;
    StoreModel storeModel;
    String username, previous;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapview);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_chevron_left_white_32dp);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(view -> goBack());

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(false);
        progressDialog.show();

        Intent i = getIntent();
        storeModel = (StoreModel) i.getSerializableExtra("place");
        username = Objects.requireNonNull(i.getExtras()).getString("username");
        previous = Objects.requireNonNull(i.getExtras()).getString("previous");

        // View bottomSheet = findViewById(R.id.framelayout_bottom_sheet);
        View bottomSheetLayout = getLayoutInflater().inflate(R.layout.bottom_sheet_dialog, null);

        subject = (bottomSheetLayout.findViewById(R.id.feedback_subject));
        desc = (bottomSheetLayout.findViewById(R.id.feedback_desc));

        (bottomSheetLayout.findViewById(R.id.cancelReviewBtn)).setOnClickListener(view -> mBottomSheetDialog.dismiss());
        (bottomSheetLayout.findViewById(R.id.submitReviewBtn)).setOnClickListener(v -> {
            String _subject = subject.getText().toString(), _desc = desc.getText().toString();
            if (_subject.isEmpty() || _desc.isEmpty())
                Toast.makeText(getApplicationContext(), "All fields are required!", Toast.LENGTH_SHORT).show();
            else {
                submitReview(_subject, _desc);
                mBottomSheetDialog.dismiss();
            }
        });

        mBottomSheetDialog = new BottomSheetDialog(this);
        mBottomSheetDialog.setContentView(bottomSheetLayout);

        findViewById(R.id.fab).setOnClickListener(view -> mBottomSheetDialog.show());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
            new OnMapAndViewReadyListener(mapFragment, this);
        }

        mDatabase = FirebaseDatabase.getInstance().getReference().child("review").child(storeModel.getId());
        loadReviews();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Hide the zoom controls as the button panel will cover it.
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        LatLng relativeLng = new LatLng(Double.parseDouble(storeModel.getLatitude()), Double.parseDouble(storeModel.getLongitude()));
        mMap.addMarker(new MarkerOptions().position(relativeLng).snippet(storeModel.getAddress()).title(storeModel.getName()));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(relativeLng, 16));

        mMap.setContentDescription("Map with different places...");
    }

    private void submitReview(String _subject, String _desc) {
        String user_id = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DatabaseReference current_user_db = mDatabase.push();

        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss - dd/MM/yyyy");
        Date date = new Date();
        System.out.println(formatter.format(date));

        current_user_db.child("subject").setValue(_subject);
        current_user_db.child("description").setValue(_desc);
        current_user_db.child("user_id").setValue(user_id);
        current_user_db.child("username").setValue(username);
        current_user_db.child("location_name").setValue(storeModel.getName());
        current_user_db.child("rating").setValue(storeModel.getRating());
        current_user_db.child("date").setValue(formatter.format(date));

        Toast.makeText(this, "Review sent successfully...", Toast.LENGTH_SHORT).show();
        subject.setText("");
        desc.setText("");
    }

    private void loadReviews() {
        RecyclerView recyclerView = findViewById(R.id.reviewRecyclerView);
        rAdapter = new ReviewAdapter(rRecycler, getApplicationContext());

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(rAdapter);
        recyclerData();
    }

    private void recyclerData() {
        rRecycler.clear();
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                allListData(dataSnapshot);
                rAdapter.notifyDataSetChanged();
            }

            @SuppressWarnings({"unchecked", "ConstantConditions"})
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
                String _mapID = dataSnapshot.getKey();

                Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                String _mapSubject = null, _mapDesc = null, _mapDate = null, _mapRating = null, _mapUser = null;
                if (map != null) {
                    try {
                        _mapSubject = map.get("subject").toString();
                        _mapDesc = map.get("description").toString();

                        if (map.get("username") != null)
                            _mapUser = map.get("username").toString();
                        if (map.get("rating") != null)
                            _mapRating = map.get("rating").toString();
                        if (map.get("date") != null)
                            _mapDate = map.get("date").toString();
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                    }
                }

                for (ReviewModel reviews : rRecycler) {
                    if (reviews.getId().equals(_mapID)) {
                        rRecycler.set(rRecycler.indexOf(reviews), new ReviewModel(_mapID, _mapSubject, _mapDesc, _mapDate, _mapUser, _mapRating));
                    }
                }
                rAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String _mapID = dataSnapshot.getKey();

                for (Iterator<ReviewModel> itr = rRecycler.iterator(); itr.hasNext(); ) {
                    ReviewModel bookings = itr.next();
                    if (bookings.getId().equals(_mapID)) {
                        itr.remove();
                    }
                }
                rAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    @SuppressLint("NewApi")
    public void allListData(final DataSnapshot dataSnapshot) {
        if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
            Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

            String _mapID = dataSnapshot.getKey();
            String _mapSubject = null, _mapDesc = null, _mapDate = null, _mapRating = null;
            String _mapUser = null;

            if (map != null) {
                try {
                    _mapSubject = map.get("subject").toString();
                    _mapDesc = map.get("description").toString();

                    if (map.get("username") != null)
                        _mapUser = map.get("username").toString();
                    if (map.get("rating") != null)
                        _mapRating = map.get("rating").toString();
                    if (map.get("date") != null)
                        _mapDate = map.get("date").toString();

                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
                rRecycler.add(0, new ReviewModel(_mapID, _mapSubject, _mapDesc, _mapDate, _mapUser, _mapRating));
            }

            findViewById(R.id.no_review).setVisibility(View.INVISIBLE);
        }
        progressDialog.dismiss();
    }


    @Override
    public void onBackPressed() {
        goBack();
    }

    public void goBack() {
        Intent intent;

        if (previous.equals("suggestion"))
            intent = new Intent(this.getApplicationContext(), SuggestionActivity.class);
        else
            intent = new Intent(this.getApplicationContext(), MainActivity.class);

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
