package com.example.justagram;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.widget.ImageView;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        TestInstagramPostFragment();
    }

    void home_page()
    {
        setContentView(R.layout.login_activity);
        ImageView logo = findViewById(R.id.justagram);
        ImageView glow1 = findViewById(R.id.glow1);
        ImageView glow2 = findViewById(R.id.glow2);
        LinearLayout content = findViewById(R.id.content);

        IntroAnimator.start(logo,glow1,glow2,content);

    }
    void TestInstagramAccountFragment()
    {
        setContentView(R.layout.home_page);
        InstagramAccountFragment test = new InstagramAccountFragment();
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.add(R.id.test_fragment,test).commit();
    }
    void TestInstagramPostFragment()
    {
        setContentView(R.layout.home_page);
        IgPublisherFragment test = new IgPublisherFragment();
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.add(R.id.test_fragment,test).commit();
    }


}
