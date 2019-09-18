package com.example.awesomeguy.lapa.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.awesomeguy.lapa.R;
import com.example.awesomeguy.lapa.model.StoreModel;
import com.example.awesomeguy.lapa.service.OnMapAndViewReadyListener;
import com.example.awesomeguy.lapa.util.PermissionUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.nlopez.smartlocation.SmartLocation;

/**
 * This shows how to place markers on a map.
 */
public class MapActivity extends AppCompatActivity implements
        OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback,
        OnMapAndViewReadyListener.OnGlobalLayoutAndMapReadyListener {

    private GoogleMap mMap;
    private boolean mPermissionDenied = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    List<StoreModel> storeModels;

    String lastLoc, previous;

    /**
     * Keeps track of the last selected marker (though it may no longer be selected).  This is
     * useful for refreshing the info window.
     */
    private final List<Marker> mMarkerRainbow = new ArrayList<Marker>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
            new OnMapAndViewReadyListener(mapFragment, this);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_chevron_left_white_32dp);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(view -> goBack());

        Intent i = getIntent();
        //noinspection unchecked
        storeModels = (List<StoreModel>) i.getSerializableExtra("places");
        previous = Objects.requireNonNull(i.getExtras()).getString("previous");
        lastLoc = Objects.requireNonNull(i.getExtras()).getString("lastLoc");
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        // Hide the zoom controls as the button panel will cover it.
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        String[] locations = lastLoc.split(",");
        LatLng lastRelativeLng = new LatLng(Double.parseDouble(locations[0]), Double.parseDouble(locations[1]));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastRelativeLng, 14));

        SmartLocation.with(this).location().oneFix().start(location -> {
            LatLng relativeLng = new LatLng(location.getLatitude(), location.getLongitude());
            if (lastRelativeLng != relativeLng)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(relativeLng, 14));
        });

        // Add lots of markers to the map.
        addMarkersToMap();
        enableMyLocation();

        mMap.setContentDescription("Map with different places...");
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE, Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) return;

        if (PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    private void addMarkersToMap() {
        int k = 0;
        if (storeModels != null) {
            while (k < storeModels.size()) {
                StoreModel s = storeModels.get(k);
                LatLng pos = new LatLng(Double.parseDouble(s.getLatitude()), Double.parseDouble(s.getLongitude()));

                Marker marker = mMap.addMarker(new MarkerOptions().position(pos).snippet(s.getAddress()).title(s.getName()));
                mMarkerRainbow.add(marker);
                k++;
            }
        }
    }


    private boolean checkReady() {
        if (mMap == null) {
            Toast.makeText(this, R.string.map_not_ready, Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        goBack();
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog.newInstance(true).show(getSupportFragmentManager(), "dialog");
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
