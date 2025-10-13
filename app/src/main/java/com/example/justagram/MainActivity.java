package com.example.justagram;

import android.os.Bundle;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.FragmentTransaction;

import android.transition.TransitionInflater;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

//        IntroAnimator.start(logo,glow1,glow2,content);
//        InstagramAccountFragment test = new InstagramAccountFragment();
//        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
//        t.add(R.id.test_fragment,test).commit();
        PostFeedFragment test = new PostFeedFragment();
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.add(R.id.test_fragment,test).commit();
    }
}