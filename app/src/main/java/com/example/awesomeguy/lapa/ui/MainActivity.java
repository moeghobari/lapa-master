package com.example.awesomeguy.lapa.ui;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.awesomeguy.lapa.R;
import com.example.awesomeguy.lapa.adapter.PlacesRecyclerView;
import com.example.awesomeguy.lapa.api.Client;
import com.example.awesomeguy.lapa.api.Interface;
import com.example.awesomeguy.lapa.model.StoreModel;
import com.example.awesomeguy.lapa.service.PlacesPOJO;
import com.example.awesomeguy.lapa.util.ResultDistanceMatrix;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.nlopez.smartlocation.SmartLocation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * Created by Mohanad on 09/08/19.
 */

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, NavigationView.OnNavigationItemSelectedListener {

    private static Context context;

    private Spinner spinner;
    private ProgressDialog mProgressDialog;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private static final String[] places = {
            "Hotel", "Restaurant", "Shopping Mall", "Casino", "Art Gallery", "Stadium", "Zoo", "Education Area"
    };
    private static final String[] placesArray = {
            "lodging", "restaurant", "shopping_mall", "casino", "art_gallery", "stadium", "zoo", "education"
    };

    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();
    private final static int ALL_PERMISSIONS_RESULT = 101;

    List<StoreModel> storeModels;
    Interface apiService;

    String latLngString;
    LatLng latLng;

    RecyclerView recyclerView;
    List<PlacesPOJO.CustomA> results;

    NavigationView navigationView;
    View hView;
    ImageView imageView;
    String username;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        context = this;
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("recent_places");

        mProgressDialog = new ProgressDialog(MainActivity.context);

        initialize();

        findViewById(R.id.button).setOnClickListener(v -> {
            showProgressIndicator("Loading...");

            EditText editText = findViewById(R.id.editText);
            String searchText = editText.getText().toString().trim();

            int spinner_pos = spinner.getSelectedItemPosition();
            String placeType = placesArray[spinner_pos];

            editText.clearFocus();
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

            fetchStores(placeType, searchText);
        });

        findViewById(R.id.fab).setOnClickListener(view -> {
            Intent intent = new Intent(this.getApplicationContext(), MapActivity.class);
            intent.putExtra("places", (Serializable) storeModels);
            intent.putExtra("previous", "main");
            intent.putExtra("lastLoc", latLngString);
            startActivity(intent);

            overridePendingTransitionExit();
        });
    }

    private void initialize() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);

        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        hView = navigationView.getHeaderView(0);

        hView.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapActivity.class);
            startActivity(intent);
            finish();
        });

        permissions.add(ACCESS_FINE_LOCATION);
        permissions.add(ACCESS_COARSE_LOCATION);
        permissionsToRequest = findUnAskedPermissions(permissions);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size() > 0)
                requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
            else fetchLocationZero();
        } else fetchLocationZero();


        apiService = Client.getClient().create(Interface.class);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        CoordinatorLayout mainLayout;

        // Get your layout set up, this is just an example
        mainLayout = findViewById(R.id.activity_main);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(mainLayout.getWindowToken(), 0);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        spinner = findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, places);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        int placeTypePosition = adapter.getPosition("Restaurant");
        spinner.setSelection(placeTypePosition);

    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) logout();
        else updateUI(Objects.requireNonNull(currentUser));
    }


    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }


    private void updateUI(FirebaseUser currentUser) {

        String user_id = currentUser.getUid();
        DatabaseReference profile = FirebaseDatabase.getInstance().getReference().child("profile").child(user_id);

        TextView nameView = (TextView) hView.findViewById(R.id.nameTextView);
        TextView emailView = (TextView) hView.findViewById(R.id.emailTextView);
        imageView = (ImageView) hView.findViewById(R.id.profileImageView);


        profile.addValueEventListener(new ValueEventListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                if (map != null) {
                    try {
                        if (map.get("name") != null) {
                            nameView.setText(Objects.requireNonNull(map.get("name")).toString());
                            username = Objects.requireNonNull(map.get("name")).toString();
                        }
                        if (map.get("email") != null) {
                            emailView.setText(Objects.requireNonNull(map.get("email")).toString());
                        }
                        if (map.get("imagePath") != null) {
                            String imagePath = Objects.requireNonNull(map.get("imagePath")).toString();
                            Glide.with(getBaseContext()).load(imagePath).into(imageView);
                        }
                    } catch (Exception e) {
                        System.out.println("something went hell wrong... ");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
            }
        });
    }

    /**
     * Go to location, view
     */
    public void locationClicked(StoreModel store) {
        saveToRecentLocation(store);


        Intent intent = new Intent(context, MapViewActivity.class);
        intent.putExtra("place", (Serializable) store);
        intent.putExtra("previous", "main");
        intent.putExtra("username", username);
        context.startActivity(intent);

        overridePendingTransitionExit();
    }

    private void saveToRecentLocation(StoreModel store) {
        String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference current_user_db = mDatabase.child(user_id).child(store.getId());

        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss - dd/MM/yyyy");
        Date date = new Date();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        current_user_db.child("id").setValue(store.getId());
        current_user_db.child("name").setValue(store.getName());
        current_user_db.child("address").setValue(store.getAddress());
        current_user_db.child("distance").setValue(store.getDistance());
        current_user_db.child("duration").setValue(store.getDuration());
        current_user_db.child("icon").setValue(store.getIcon());
        current_user_db.child("rating").setValue(store.getRating());
        current_user_db.child("longitude").setValue(store.getLongitude());
        current_user_db.child("latitude").setValue(store.getLatitude());
        current_user_db.child("timestamp").setValue(timestamp.getTime());
        current_user_db.child("date").setValue(formatter.format(date));
    }

    /**
     * For Locations in Kuala Lumpur for McDonalds stores aren't returned accurately
     */
    private void fetchStores(String placeType, String searchText) {
        fetchLocation();

        Call<PlacesPOJO.Root> call = apiService.doPlaces(placeType, latLngString, searchText, true, "distance", Client.GOOGLE_PLACE_API_KEY);
        call.enqueue(new Callback<PlacesPOJO.Root>() {
            @Override
            public void onResponse(Call<PlacesPOJO.Root> call, Response<PlacesPOJO.Root> response) {
                PlacesPOJO.Root root = response.body();

                if (response.isSuccessful()) {
                    if (root.status.equals("OK")) {
                        results = root.customA;
                        storeModels = new ArrayList<StoreModel>();

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

    private ArrayList<String> findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<>();

        for (String perm : wanted) {
            if (hasPermission(perm)) result.add(perm);
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED);
            }
        }
        return false;
    }

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == ALL_PERMISSIONS_RESULT) {
            for (String perms : permissionsToRequest) {
                if (hasPermission(perms)) permissionsRejected.add(perms);
            }

            if (permissionsRejected.size() > 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {

                        String msg = "These permissions are mandatory for the application. Please allow access.";
                        showMessageOKCancel(msg, (dialog, which) -> {
                            // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                            requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                        });
                    }
                }

            } else {
                fetchLocation();
            }
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void fetchLocationZero() {
        showProgressIndicator("Loading...");

        SmartLocation.with(this).location().oneFix().start(location -> {
            latLngString = location.getLatitude() + "," + location.getLongitude();
            latLng = new LatLng(location.getLatitude(), location.getLongitude());

            new android.os.Handler().postDelayed(() -> {
                EditText editText = findViewById(R.id.editText);
                String searchText = editText.getText().toString().trim();

                int spinner_pos = spinner.getSelectedItemPosition();
                String placeType = placesArray[spinner_pos];

                fetchStores(placeType, searchText);
            }, 100);
        });
    }

    private void fetchLocation() {
        SmartLocation.with(this).location().oneFix().start(location -> {
            latLngString = location.getLatitude() + "," + location.getLongitude();
            latLng = new LatLng(location.getLatitude(), location.getLongitude());
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

                        storeModels.add(new StoreModel(info.id, info.name, info.icon, info.vicinity, info.rating, info.geometry.locationA.lng, info.geometry.locationA.lat, totalDistance, totalDuration));
                        if (storeModels.size() == 10 || storeModels.size() == results.size()) {
                            // results,
                            PlacesRecyclerView adapterStores = new PlacesRecyclerView(storeModels, context, "main");
                            recyclerView.setAdapter(adapterStores);

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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // do nothing...
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // do nothing...
    }

    public void showProgressIndicator(final String message) {
        mProgressDialog.dismiss();
        mProgressDialog.setTitle(getString(R.string.searching_places));
        mProgressDialog.setMessage(message);
        mProgressDialog.show();
    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransitionExit();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
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
        overridePendingTransition(R.anim.slide_to_right, R.anim.slide_from_left);
    }

    /**
     * Overrides the pending Activity transition by performing the "Exit" animation.
     */
    protected void overridePendingTransitionExit() {
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();

        switch (id) {
            case R.id.nav_profile:
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
                overridePendingTransitionExit();
                break;
            case R.id.nav_suggestion:
                Intent sug_intent = new Intent(this, SuggestionActivity.class);
                sug_intent.putExtra("username", username);
                sug_intent.putExtra("lastLoc", latLngString);
                startActivity(sug_intent);
                overridePendingTransitionExit();
                break;
            case R.id.nav_feedback:
                startActivity(new Intent(this, FeedbackActivity.class));
                overridePendingTransitionExit();
                //finish();
                break;
            case R.id.nav_logout:
                logout();
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    public void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


}
