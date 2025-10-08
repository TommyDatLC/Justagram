package com.example.justagram;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.SystemClock;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Response;


// get the link
// attach the link within the project

// implement the webhoook from the kaggle server
// client gui 1 request genHashID , doi cho request duoc hoan thanh
// sau khi dang nhap xong browser request vao client
//
public class MainActivity extends AppCompatActivity {

    public static OkHttpClient client = new OkHttpClient();
    public static String OurBackendServer = "https://catechistical-questingly-na.ngrok-free.dev";
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
       home_page();
       // ScheduleTask(getApplicationContext(),3);
    }
    String GenerateLoginLink()
    {
        final String[] hashKey = new String[1];
        Utility.SimpleGetRequest(OurBackendServer,new TommyDatCallBack()
        {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response respone) throws IOException {
                hashKey[0] = respone.body().string();
            }
        });
        return "https://www.instagram.com/oauth/authorize?force_reauth=true&client_id=1326733148350430&redirect_uri=https://catechistical-questingly-na.ngrok-free.dev/returnCode/&ClientHash=" +
                hashKey[0] +
                "&response_type=code&scope=instagram_business_basic%2Cinstagram_business_manage_messages%2Cinstagram_business_manage_comments%2Cinstagram_business_content_publish%2Cinstagram_business_manage_insights";
    }
    void home_page()
    {
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
                String link =  GenerateLoginLink();
                Utility.OpenWebsite(thisContext,link);
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
