package com.example.justagram;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.justagram.fragment.DashboardFragment;
import com.example.justagram.Helper.ScrollAwareFragment;
import com.example.justagram.Helper.TabAnimationHelper;
import com.example.justagram.fragment.InstagramAccountFragment;
import com.example.justagram.fragment.PostFeedFragment;
import com.example.justagram.fragment.ReelPostFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class DashboardActivity extends AppCompatActivity {

    private final int[] TAB_ICONS = {
            R.mipmap.ic_overview,
            R.mipmap.ic_statistics,
            R.mipmap.ic_reel,
            R.mipmap.ic_post1
    };

    private TabLayout tabLayout;
    private boolean isTabHidden = false;
    private View bottomCardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        ImageView tabGradient = findViewById(R.id.tabGradient);
        ImageView signButton = findViewById(R.id.signButton);
        bottomCardView = findViewById(R.id.cardView);
        ImageView exitButton = findViewById(R.id.exit);

        exitButton.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, com.example.justagram.LoginAuth.LoginActivity.class);

            // Clear the back stack so the user cannot press back
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);

            // Finish this activity so it is removed from the stack
            finish();
        });


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
            Intent intent = new Intent(this, IgPublisherActivity.class);
            startActivity(intent);
        });

        viewPager.setCurrentItem(0, false);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                Fragment current = getSupportFragmentManager().findFragmentByTag("f" + position);
                if (current instanceof ScrollAwareFragment) {
                    ((ScrollAwareFragment) current).setOnScrollChangeListener(new ScrollAwareFragment.OnScrollChangeListener() {
                        @Override
                        public void onScrollUp() {
                            showTabLayout();
                        }

                        @Override
                        public void onScrollDown() {
                            hideTabLayout();
                        }
                    });
                }
            }
        });
    }

    private void hideTabLayout() {
        if (!isTabHidden && bottomCardView != null) {
            bottomCardView.animate()
                    .translationY(bottomCardView.getHeight() + 50) // slide all the way down
                    .setDuration(300)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .start();
            isTabHidden = true;
        }
    }

    private void showTabLayout() {
        if (isTabHidden && bottomCardView != null) {
            bottomCardView.animate()
                    .translationY(0)
                    .setDuration(300)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .start();
            isTabHidden = false;
        }
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
                case 0: return new InstagramAccountFragment();
                case 1: return new com.example.justagram.fragment.Statistic.StatisticFragment();
                case 2: return new ReelPostFragment();
                case 3: return new PostFeedFragment();
                default: return DashboardFragment.newInstance();
            }
        }

        @Override
        public int getItemCount() { return 4; }
    }
}
