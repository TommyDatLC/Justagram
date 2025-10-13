package com.example.justagram;

import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;

import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;

public class TabAnimationHelper {

    private final TabLayout tabLayout;
    private final ImageView tabGradient;
    private final ViewPager2 viewPager;
    private final int activeColor;
    private final int inactiveColor;

    private float lastTranslationX = -1f;

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
            if (tabLayout.getTabCount() == 0) return;

            // Move gradient to initial tab
            moveGradientWithBounce(viewPager.getCurrentItem());

            setupListeners();
        });
    }

    private void setupListeners() {
        // Tab click
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                moveGradientWithBounce(pos);
                scaleIcon(tab, true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { scaleIcon(tab, false); }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Swipe events
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float offset, int offsetPixels) {
                float center1 = getTabCenterX(position);
                float center2 = getTabCenterX(Math.min(position + 1, tabLayout.getTabCount() - 1));

                float translationX = center1 + (center2 - center1) * offset - tabGradient.getWidth() / 2f;

                // Optional: small overshoot effect
                if (lastTranslationX >= 0) {
                    float direction = Math.signum(translationX - lastTranslationX);
                    float distance = Math.abs(translationX - lastTranslationX);
                    float maxOvershoot = (center2 - center1) * 0.2f;
                    float overshoot = Math.min(distance * 0.3f, maxOvershoot) * direction;
                    translationX += overshoot;
                }

                tabGradient.setX(translationX);
                lastTranslationX = translationX;

                // Update icon colors
                for (int i = 0; i < tabLayout.getTabCount(); i++) {
                    TabLayout.Tab t = tabLayout.getTabAt(i);
                    if (t != null && t.getIcon() != null) {
                        float alphaActive = 1f - Math.min(Math.abs(i - (position + offset)), 1f);
                        t.getIcon().setTint(blendColors(inactiveColor, activeColor, alphaActive));
                        t.getIcon().setAlpha((int)((0.7f + 0.5f * alphaActive) * 255));
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager2.SCROLL_STATE_IDLE) lastTranslationX = -1f;
            }
        });
    }

    private void moveGradientWithBounce(int tabPosition) {
        float targetX = getTabCenterX(tabPosition) - tabGradient.getWidth() / 2f;
        float currentX = tabGradient.getTranslationX();
        float direction = Math.signum(targetX - currentX);
        float distance = Math.abs(targetX - currentX);
        float overshoot = Math.min(distance * 0.5f, tabGradient.getWidth()) * direction;
        float endX = targetX + overshoot;

        ObjectAnimator animator = ObjectAnimator.ofFloat(tabGradient, View.TRANSLATION_X, currentX, endX, targetX);
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

    private float getTabCenterX(int position) {
        LinearLayout tabStrip = (LinearLayout) tabLayout.getChildAt(0);
        int index = position;

        // Adjust for spacer: if you added a Space after tab 1, skip it
        for (int i = 0; i < tabStrip.getChildCount(); i++) {
            View child = tabStrip.getChildAt(i);
            if (child instanceof Space && i <= index) {
                index += 1;
                break;
            }
        }

        View tabView = tabStrip.getChildAt(index);
        return tabView.getLeft() + tabView.getWidth() / 2f;
    }

    private int blendColors(int from, int to, float ratio) {
        float inverse = 1f - ratio;
        int a = (int)((android.graphics.Color.alpha(from) * inverse) + (android.graphics.Color.alpha(to) * ratio));
        int r = (int)((android.graphics.Color.red(from) * inverse) + (android.graphics.Color.red(to) * ratio));
        int g = (int)((android.graphics.Color.green(from) * inverse) + (android.graphics.Color.green(to) * ratio));
        int b = (int)((android.graphics.Color.blue(from) * inverse) + (android.graphics.Color.blue(to) * ratio));
        return android.graphics.Color.argb(a, r, g, b);
    }
}
