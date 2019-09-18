package com.example.awesomeguy.lapa.ui;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.example.awesomeguy.lapa.R;

// import com.example.awesomeguy.lapa.ui.fragment.dashboard.DashboardFragment;
import com.example.awesomeguy.lapa.ui.fragment.home.HomeFragment;
import com.example.awesomeguy.lapa.ui.fragment.notifications.NotificationsFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


public class ProfileActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    BottomNavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.profile_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_chevron_left_white_32dp);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(view -> goBack());

        navView = findViewById(R.id.nav_view_profile);
        navView.setOnNavigationItemSelectedListener(this);

        View view = navView.findViewById(R.id.navigation_home);
        view.performClick();

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    }

    protected void selectFragment(String item) {
        System.out.println("item.getItemId() ==== " + item);

        switch (item) {
            case "Home":
                // Action to perform when Home Menu item is selected.
                pushFragment(new HomeFragment());
                break;
//            case "Dashboard":
//                // Action to perform when Bag Menu item is selected.
//                pushFragment(new DashboardFragment());
//                break;
            case "Notifications":
                // Action to perform when Account Menu item is selected.
                pushFragment(new NotificationsFragment());
                break;
        }
    }

    protected void pushFragment(Fragment fragment) {
        if (fragment == null)
            return;

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.nav_host_fragment, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        selectFragment((String) menuItem.getTitle());
        menuItem.setChecked(true);

        return false;
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
