package com.example.justagram;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.transition.TransitionInflater;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView ivLogo = findViewById(R.id.iv_logo);

        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.animation);
        ivLogo.startAnimation(fadeIn);
        ivLogo.setAlpha(1f);

        getWindow().setSharedElementEnterTransition(
                TransitionInflater.from(this).inflateTransition(R.transition.shared_element_transition));
        getWindow().setSharedElementExitTransition(
                TransitionInflater.from(this).inflateTransition(R.transition.shared_element_transition));

        ivLogo.setOnClickListener(v -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);

            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                    SplashActivity.this, ivLogo, "app_logo");

            startActivity(intent, options.toBundle());

            ivLogo.postDelayed(() -> finishAfterTransition(), 500);
        });
    }
}