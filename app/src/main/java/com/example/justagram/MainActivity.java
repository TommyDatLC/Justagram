package com.example.justagram;

import android.os.Bundle;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.transition.TransitionInflater;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_design);

        RelativeLayout loginRoot = findViewById(R.id.login_root);

        loginRoot.setAlpha(0f);

        loginRoot.animate().alpha(1f).setStartDelay(200).setDuration(700).setInterpolator(AnimationUtils.loadInterpolator(this, android.R.interpolator.fast_out_slow_in)).start();

        getWindow().setSharedElementEnterTransition(
                TransitionInflater.from(this).inflateTransition(R.transition.shared_element_transition));
        getWindow().setSharedElementExitTransition(
                TransitionInflater.from(this).inflateTransition(R.transition.shared_element_transition));
    }
}
