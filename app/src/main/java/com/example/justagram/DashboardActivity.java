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
            0,
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

        // Setup ViewPager2 adapter
        viewPager.setAdapter(new DashboardPagerAdapter(this));

        final int[] lastReal = {viewPager.getCurrentItem()};
        final int[] lastPosition = {viewPager.getCurrentItem()};

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                // remember the latest selected position
                lastPosition[0] = position;
                if (position != 2) lastReal[0] = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    int cur = viewPager.getCurrentItem();
                    if (cur == 2) {
                        // detect direction: was previous < current → right swipe, else left
                        if (lastReal[0] < 2) {
                            // going right → jump to index 3
                            viewPager.setCurrentItem(3, true);
                            lastReal[0] = 3;
                        } else {
                            // going left → jump to index 1
                            viewPager.setCurrentItem(1, true);
                            lastReal[0] = 1;
                        }
                    }
                }
            }
        });


        // Attach TabLayout with icons
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (TAB_ICONS[position] != 0) {
                tab.setIcon(TAB_ICONS[position]);
            } else {
                tab.view.setClickable(false); // Disable the placeholder tab
            }
        }).attach();

        // Initialize the animation helper
        int activeColor = getColor(R.color.sunset_orange);
        int inactiveColor = getColor(R.color.hint_gray);
        TabAnimationHelper tabHelper = new TabAnimationHelper(tabLayout, tabGradient, viewPager,
                activeColor, inactiveColor);
        tabLayout.post(tabHelper::init);

        // Optional: Set default tab
        viewPager.setCurrentItem(0, false);

        signButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddPostActivity.class);
            startActivity(intent);
        });

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
                case 2: return new Fragment();
                case 3: return CalendarFragment.newInstance();
                case 4: return SettingsFragment.newInstance();
                default: return DashboardFragment.newInstance();
            }
        }

        @Override
        public int getItemCount() { return 5; }
    }
}
