package com.example.justagram;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.justagram.Fragments.PostFeedFragment;

public class MainActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

//     IntroAnimator.start(logo,glow1,glow2,content);
//      InstagramAccountFragment test = new InstagramAccountFragment();
//        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
//        t.add(R.id.test_fragment,test).commit();
        PostFeedFragment test = new PostFeedFragment();
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.add(R.id.test_fragment,test).commit();
    }
}