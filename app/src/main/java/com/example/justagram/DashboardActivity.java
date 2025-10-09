package com.example.justagram;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.content.Intent;

public class DashboardActivity extends AppCompatActivity {

    private FragmentTransition fragmentTransition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Use the nav_indicator id (the sliding gradient)
        ImageView gradient = findViewById(R.id.nav_indicator);
        ImageView navAdd = findViewById(R.id.nav_add);
        ImageButton navDashboard = findViewById(R.id.nav_dashboard);
        ImageButton navAnalytics = findViewById(R.id.nav_analytics);
        ImageButton navCalendar = findViewById(R.id.nav_calendar);
        ImageButton navSettings = findViewById(R.id.nav_settings);

        fragmentTransition = new FragmentTransition(gradient);

        // Default: put gradient under Dashboard

        if (savedInstanceState == null) {
            loadFragment(new DashboardFragment());
            navDashboard.post(() -> fragmentTransition.animateTo(navDashboard));
        }

        // Animate + switch fragments
        navDashboard.setOnClickListener(v -> {
            fragmentTransition.animateTo(navDashboard);
            loadFragment(new DashboardFragment());
        });

        navAnalytics.setOnClickListener(v -> {
            fragmentTransition.animateTo(navAnalytics);
            loadFragment(new AnalyticsFragment());
        });

        navCalendar.setOnClickListener(v -> {
            fragmentTransition.animateTo(navCalendar);
            loadFragment(new CalendarFragment());
        });

        navSettings.setOnClickListener(v -> {
            fragmentTransition.animateTo(navSettings);
            loadFragment(new SettingsFragment());
        });

        navAdd.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, AddPostActivity.class);
            startActivity(intent);
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

}
