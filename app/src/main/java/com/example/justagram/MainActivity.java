package com.example.justagram;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Response;



public class MainActivity extends AppCompatActivity {

    public static OkHttpClient client = new OkHttpClient();
    public static String OurBackendServer = "https://catechistical-questingly-na.ngrok-free.dev";
    public final String InstagramLoginAuthLink = "https://www.instagram.com/oauth/authorize?force_reauth=true&client_id=1326733148350430&redirect_uri=https://catechistical-questingly-na.ngrok-free.dev/returnCode&response_type=code&scope=instagram_business_basic%2Cinstagram_business_manage_messages%2Cinstagram_business_manage_comments%2Cinstagram_business_content_publish%2Cinstagram_business_manage_insights";
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
       home_page();

    }
    // OpenURL (done)
    // when login done -> send to our server the code (done)
    // our server return javascript code to send the code to the app
    // our app will handle the code
    void home_page()
    {
        Intent intent = getIntent();
        Uri data = intent.getData();
        if (data != null)
        {
            String access_token = data.getQueryParameter("code");
            Utility.showMessageBox(access_token,this);
        }

        setContentView(R.layout.login_activity);
        ImageView logo = findViewById(R.id.justagram);
        ImageView glow1 = findViewById(R.id.glow1);
        ImageView glow2 = findViewById(R.id.glow2);
        LinearLayout content = findViewById(R.id.content);

        IntroAnimator.start(logo,glow1,glow2,content);
        var forget = findViewById(R.id.tvForgot);
        forget.setOnClickListener(new forgetPassword(this));
        var signUp = findViewById(R.id.signUp);
        signUp.setOnClickListener(new SignUpButton(this));
        var btn_loginWInstagram = findViewById(R.id.btn_loginWInstagram);
        Context thisContext = this;
        btn_loginWInstagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utility.OpenWebsite(thisContext,InstagramLoginAuthLink);
            }
        });
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
