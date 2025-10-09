package com.example.justagram;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TestInstagramPostFragment();
    }

    void home_page() {
        setContentView(R.layout.login_activity);
        ImageView logo = findViewById(R.id.justagram);
        ImageView glow1 = findViewById(R.id.glow1);
        ImageView glow2 = findViewById(R.id.glow2);
        LinearLayout content = findViewById(R.id.content);

        IntroAnimator.start(logo, glow1, glow2, content);

        var forget = findViewById(R.id.tvForgot);
        forget.setOnClickListener(new forgetPassword(this));
        var signUp = findViewById(R.id.signUp);
        signUp.setOnClickListener(new SignUpButton(this));
    }

    void TestInstagramAccountFragment() {
        setContentView(R.layout.home_page);
        InstagramAccountFragment test = new InstagramAccountFragment();
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.add(R.id.test_fragment, test).commit();
    }

    void TestInstagramPostFragment() {
        setContentView(R.layout.home_page);
        IgPublisherFragment test = new IgPublisherFragment();
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.add(R.id.test_fragment, test).commit();
        // Open the Activity version instead of fragment so buttons interact immediately
        Intent i = new Intent(MainActivity.this, IgPublisherActivity.class);
        startActivity(i);
    }


}
