package com.example.justagram;

import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;

public class TabAnimationHelper {

    private final TabLayout tabLayout;
    private final ImageView tabGradient;
    private final ViewPager2 viewPager;
    private final int activeColor;
    private final int inactiveColor;
    private int tabWidth;

    public TabAnimationHelper(TabLayout tabLayout, ImageView tabGradient, ViewPager2 viewPager,
                              int activeColor, int inactiveColor) {
        this.tabLayout = tabLayout;
        this.tabGradient = tabGradient;
        this.viewPager = viewPager;
        this.activeColor = activeColor;
        this.inactiveColor = inactiveColor;
    }

    public void init() {
        tabLayout.post(() -> {
            tabWidth = tabLayout.getWidth() / tabLayout.getTabCount();
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
                if (pos == 2) return; // sign placeholder
                moveGradientWithBounce(pos);
                scaleIcon(tab, true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                scaleIcon(tab, false);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Swipe events with explicit per-case handling
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float offset, int offsetPixels) {
                float gradientX = tabGradient.getTranslationX();
                int startTab = position;
                int endTab = position + 1;

                // Map real swipe transitions skipping placeholder (index 2)
                if (position == 0 && offset > 0) { // 0 -> 1
                    startTab = 0; endTab = 1;
                } else if (position == 1 && offset > 0) { // 1 -> 3
                    startTab = 1; endTab = 3;
                } else if (position == 3 && offset > 0) { // 3 -> 4
                    startTab = 3; endTab = 4;
                } else if (position == 4 && offset < 0) { // 4 -> 3
                    startTab = 4; endTab = 3; offset = -offset;
                } else if (position == 3 && offset < 0) { // 3 -> 1
                    startTab = 3; endTab = 1; offset = -offset;
                } else if (position == 1 && offset < 0) { // 1 -> 0
                    startTab = 1; endTab = 0; offset = -offset;
                } else {
                    startTab = position; endTab = position; offset = 0;
                }

                // Linear interpolate gradient position
                float startX = startTab * tabWidth + tabWidth / 2f - tabGradient.getWidth() / 2f;
                float endX = endTab * tabWidth + tabWidth / 2f - tabGradient.getWidth() / 2f;
                gradientX = startX + (endX - startX) * offset;
                tabGradient.setTranslationX(gradientX);

                // Update icon colors for these two tabs
                for (int i = 0; i < tabLayout.getTabCount(); i++) {
                    TabLayout.Tab tab = tabLayout.getTabAt(i);
                    if (tab != null && tab.getIcon() != null) {
                        float alpha = 0f;
                        if (i == startTab) alpha = 1f - offset;
                        else if (i == endTab) alpha = offset;
                        tab.getIcon().setTint(blendColors(inactiveColor, activeColor, alpha));
                        tab.getIcon().setAlpha((int)((0.7f + 0.5f * alpha) * 255));
                    }
                }
            }
        });
    }

    private void moveGradientWithBounce(int tabPosition) {
        float targetX = tabPosition * tabWidth + (tabWidth / 2f) - (tabGradient.getWidth() / 2f);
        float currentX = tabGradient.getTranslationX();
        float direction = Math.signum(targetX - currentX);
        float distance = Math.abs(targetX - currentX);
        float maxOvershoot = tabWidth * 0.4f;
        float overshoot = Math.min(distance * 0.5f, maxOvershoot) * direction;
        float endX = targetX + overshoot;

        ObjectAnimator animator = ObjectAnimator.ofFloat(tabGradient, View.TRANSLATION_X,
                currentX, endX, targetX);
        animator.setDuration(600);
        animator.setInterpolator(new OvershootInterpolator(0.8f));
        animator.start();
    }

    private void scaleIcon(TabLayout.Tab tab, boolean selected) {
        if (tab == null || tab.getIcon() == null) return;
        View tabView = tab.view;
        if (tabView != null) {
            float scale = selected ? 1.2f : 1f;
            tabView.animate().scaleX(scale).scaleY(scale).setDuration(150).start();
        }
    }

    private int blendColors(int from, int to, float ratio) {
        final float inv = 1f - ratio;
        int a = (int)((android.graphics.Color.alpha(from) * inv) + (android.graphics.Color.alpha(to) * ratio));
        int r = (int)((android.graphics.Color.red(from) * inv) + (android.graphics.Color.red(to) * ratio));
        int g = (int)((android.graphics.Color.green(from) * inv) + (android.graphics.Color.green(to) * ratio));
        int b = (int)((android.graphics.Color.blue(from) * inv) + (android.graphics.Color.blue(to) * ratio));
        return android.graphics.Color.argb(a, r, g, b);
    }
}
