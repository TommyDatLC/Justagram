package com.example.justagram;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class DashboardActivity extends AppCompatActivity {

    private final int[] TAB_ICONS = {
            R.drawable.ic_dashboard,
            R.drawable.ic_analytics,
            R.drawable.ic_calendar,
            R.drawable.ic_settings
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        ImageView tabGradient = findViewById(R.id.tabGradient);
        ImageView signButton = findViewById(R.id.signButton);

        // ViewPager Adapter
        viewPager.setAdapter(new DashboardPagerAdapter(this));

        // Attach TabLayout with icons
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setIcon(TAB_ICONS[position])).attach();

        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                LinearLayout tabStrip = (LinearLayout) tabLayout.getChildAt(0);

                Space spacer = new Space(DashboardActivity.this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, 0, 1f); // weight fills remaining space
                spacer.setLayoutParams(params);

                tabStrip.addView(spacer, 2); // insert at index 2 (between tab 2 and 3)
            }
        });

        // Tab animation helper
        int activeColor = getColor(R.color.sunset_orange);
        int inactiveColor = getColor(R.color.hint_gray);
        TabAnimationHelper tabHelper = new TabAnimationHelper(tabLayout, tabGradient, viewPager, activeColor, inactiveColor);
        tabLayout.post(tabHelper::init);

        // Floating sign button click
        signButton.setOnClickListener(v -> {
            // Launch your AddPostActivity or fragment
            Intent intent = new Intent(this, AddPostActivity.class);
            startActivity(intent);
        });

        viewPager.setCurrentItem(0, false);
    }

    /** Minimal PagerAdapter for 4 fragments */
    private static class DashboardPagerAdapter extends FragmentStateAdapter {
        public DashboardPagerAdapter(@NonNull FragmentActivity fa) {
            super(fa);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0: return DashboardFragment.newInstance();
                case 1: return AnalyticsFragment.newInstance();
                case 2: return CalendarFragment.newInstance();
                case 3: return SettingsFragment.newInstance();
                default: return DashboardFragment.newInstance();
            }
        }

        @Override
        public int getItemCount() { return 4; }
    }
}
