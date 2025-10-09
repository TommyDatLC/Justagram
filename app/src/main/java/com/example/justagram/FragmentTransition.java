package com.example.justagram;

import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

public class FragmentTransition {

    private final ImageView gradient;

    public FragmentTransition(ImageView gradient) {
        this.gradient = gradient;
    }

    public void animateTo(View target) {
        float targetX = target.getX() + target.getWidth() / 2f - gradient.getWidth() / 2f;

        gradient.animate()
                .x(targetX)
                .setDuration(400)
                .setInterpolator(new OvershootInterpolator(1.4f))
                .start();
    }
}
