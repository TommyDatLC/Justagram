package com.example.justagram;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

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
        ImageView navAdd = findViewById(R.id.nav_add);

        // Add button action
        navAdd.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, AddPostActivity.class)));

        // Setup ViewPager2 adapter
        viewPager.setAdapter(new DashboardPagerAdapter(this));

        // Attach TabLayout with icons
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setIcon(TAB_ICONS[position])).attach();

        // Initialize the animation helper
        int activeColor = getColor(R.color.sunset_orange);
        int inactiveColor = getColor(R.color.hint_gray);
        TabAnimationHelper tabHelper = new TabAnimationHelper(tabLayout, tabGradient, viewPager,
                activeColor, inactiveColor);
        tabLayout.post(tabHelper::init);

        // Optional: Set default tab
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
