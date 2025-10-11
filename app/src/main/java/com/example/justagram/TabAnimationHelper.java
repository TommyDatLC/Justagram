package com.example.justagram;

import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;

/**
 * Handles gradient movement, icon scaling, and color blending for bottom navigation.
 */
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

    /** Initialize helper after layout is ready */
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

    /** Setup tab clicks and swipe callbacks */
    private void setupListeners() {
        // Tab clicks
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
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

        // Swipe events
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float offset, int offsetPixels) {
                float translationX = (position + offset) * tabWidth + (tabWidth / 2f) - (tabGradient.getWidth() / 2f);
                tabGradient.setTranslationX(translationX);

                // Optional: update icon colors while swiping
                for (int i = 0; i < tabLayout.getTabCount(); i++) {
                    TabLayout.Tab tab = tabLayout.getTabAt(i);
                    if (tab != null && tab.getIcon() != null) {
                        float alphaActive = 1f - Math.min(Math.abs(i - (position + offset)), 1f);
                        tab.getIcon().setTint(blendColors(inactiveColor, activeColor, alphaActive));
                        tab.getIcon().setAlpha((int)((0.7f + 0.5f * alphaActive) * 255));
                    }
                }
            }
        });
    }

    /** Move gradient with slight overshoot and bounce back */
    private void moveGradientWithBounce(int tabPosition) {
        float targetX = tabPosition * tabWidth + (tabWidth / 2f) - (tabGradient.getWidth() / 2f);

        // Current position
        float currentX = tabGradient.getTranslationX();

        // Direction of movement: +1 right, -1 left
        float direction = Math.signum(targetX - currentX);

        // Compute overshoot proportional to tab distance but capped
        float distance = Math.abs(targetX - currentX);
        float maxOvershoot = tabWidth * 0.4f; // max 15% of tab width
        float overshoot = Math.min(distance * 0.5f, maxOvershoot) * direction; // 25% of distance or cap

        float endX = targetX + overshoot;

        // Animate gradient: go slightly past target then settle
        ObjectAnimator animator = ObjectAnimator.ofFloat(tabGradient, View.TRANSLATION_X,
                currentX, endX, targetX);
        animator.setDuration(600);
        animator.setInterpolator(new OvershootInterpolator(0.8f));
        animator.start();
    }


    /** Scale tab icon when selected/unselected */
    private void scaleIcon(TabLayout.Tab tab, boolean selected) {
        if (tab == null || tab.getIcon() == null) return;
        View tabView = tab.view;
        if (tabView != null) {
            float scale = selected ? 1.2f : 1f;
            tabView.animate().scaleX(scale).scaleY(scale).setDuration(150).start();
        }
    }

    /** Blend two colors based on ratio (0..1) */
    private int blendColors(int from, int to, float ratio) {
        final float inverseR = 1f - ratio;
        int a = (int)((android.graphics.Color.alpha(from) * inverseR) + (android.graphics.Color.alpha(to) * ratio));
        int r = (int)((android.graphics.Color.red(from) * inverseR) + (android.graphics.Color.red(to) * ratio));
        int g = (int)((android.graphics.Color.green(from) * inverseR) + (android.graphics.Color.green(to) * ratio));
        int b = (int)((android.graphics.Color.blue(from) * inverseR) + (android.graphics.Color.blue(to) * ratio));
        return android.graphics.Color.argb(a, r, g, b);
    }
}
