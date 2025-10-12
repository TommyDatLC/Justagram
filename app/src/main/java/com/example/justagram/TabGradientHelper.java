package com.example.justagram;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;

import android.graphics.Color;
import java.util.Random;
import java.util.HashSet;
import java.util.Set;

/**
 * Gradient helper for bottom navigation:
 * - Moves the gradient behind tabs with bounce
 * - Each tab has its own gradient color
 * - Tab icons blend smoothly between inactive and active gradient color
 */
public class TabGradientHelper {

    private final TabLayout tabLayout;
    private final ImageView tabGradient;
    private final ViewPager2 viewPager;
    private final int[] tabColors; // color for each tab
    private final int inactiveColor;
    private int tabWidth;

    public TabGradientHelper(TabLayout tabLayout, ImageView tabGradient, ViewPager2 viewPager,
                             int inactiveColor, int[] tabColors) {
        this.tabLayout = tabLayout;
        this.tabGradient = tabGradient;
        this.viewPager = viewPager;
        this.inactiveColor = inactiveColor;
        this.tabColors = tabColors;
    }

    /** Initialize after layout is ready */
    public void init() {
        tabLayout.post(() -> {
            int count = tabLayout.getTabCount();
            if (count == 0) return;

            tabWidth = tabLayout.getWidth() / count;

            // Place gradient under first tab
            float startX = (tabWidth / 2f) - (tabGradient.getWidth() / 2f);
            tabGradient.setTranslationX(startX);

            setupListeners();
        });
    }

    private void setupListeners() {
        // Tab clicks
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                moveGradientWithBounce(pos);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Swipe events
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                float translationX = (position + positionOffset) * tabWidth + (tabWidth / 2f) - (tabGradient.getWidth() / 2f);
                tabGradient.setTranslationX(translationX);

                // Blend icon colors based on positionOffset
                for (int i = 0; i < tabLayout.getTabCount(); i++) {
                    TabLayout.Tab tab = tabLayout.getTabAt(i);
                    if (tab != null && tab.getIcon() != null) {
                        int fromColor = inactiveColor;
                        int toColor = tabColors[i];

                        // Compute relative influence of the gradient passing this tab
                        float distance = Math.abs(i - (position + positionOffset));
                        float blendRatio = 1f - Math.min(distance, 1f);

                        tab.getIcon().setTint(blendColors(fromColor, toColor, blendRatio));
                    }
                }
            }

            @Override
            public void onPageSelected(int position) {
                TabLayout.Tab tab = tabLayout.getTabAt(position);
                if (tab != null) tab.select();
            }
        });
    }

    /** Gradient moves with gentle overshoot */
    private void moveGradientWithBounce(int tabPosition) {
        float targetX = tabPosition * tabWidth + (tabWidth / 2f) - (tabGradient.getWidth() / 2f);
        ObjectAnimator animator = ObjectAnimator.ofFloat(tabGradient, View.TRANSLATION_X,
                tabGradient.getTranslationX(), targetX + 20, targetX); // overshoot pass
        animator.setInterpolator(new OvershootInterpolator(0.6f));
        animator.setDuration(400);
        animator.start();
    }

    /** Simple color blend */
    private int blendColors(int from, int to, float ratio) {
        float inverse = 1f - ratio;
        int a = (int)((Color.alpha(from) * inverse) + (Color.alpha(to) * ratio));
        int r = (int)((Color.red(from) * inverse) + (Color.red(to) * ratio));
        int g = (int)((Color.green(from) * inverse) + (Color.green(to) * ratio));
        int b = (int)((Color.blue(from) * inverse) + (Color.blue(to) * ratio));
        return Color.argb(a, r, g, b);
    }
}
