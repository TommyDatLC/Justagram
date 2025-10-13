package com.example.justagram.LoginAuth;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.justagram.HomeActivity;
import com.example.justagram.IgPublisherFragment;
import com.example.justagram.InstagramAccountFragment;
import com.example.justagram.R;
import com.example.justagram.etc.TommyDatCallBack;
import com.example.justagram.etc.Utility;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Response;



public class LoginActivity extends AppCompatActivity {

    public static OkHttpClient client = new OkHttpClient();
    public static String OurBackendServer = "https://catechistical-questingly-na.ngrok-free.dev";

    public static Gson gson = new Gson();
    public final String InstagramLoginAuthLink = "https://www.instagram.com/oauth/authorize?force_reauth=true&client_id=1326733148350430&redirect_uri=https://catechistical-questingly-na.ngrok-free.dev/returnCode&response_type=code&scope=instagram_business_basic%2Cinstagram_business_manage_messages%2Cinstagram_business_manage_comments%2Cinstagram_business_content_publish%2Cinstagram_business_manage_insights";
    public static UserInfo userInfo;
    public final String UserFileName = "UserInfo.json";

    public EditText txtbox_accessToken;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        userInfo = Utility.Load(this,UserInfo.class,UserFileName);

        super.onCreate(savedInstanceState);
        login_actvity();

    }
    // Exchange the short term code for the the long term code
    // Save the code into the device disk


    void login_actvity()
    {
        Intent intent = getIntent();
        Uri data = intent.getData();
        setContentView(R.layout.activity_login);
        var btn_login = findViewById(R.id.btn_Login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_LoginAction();
            }
        } );

        if (data != null)
        {
            String access_code = data.getQueryParameter("code");
          ExchangeForToken(access_code);
        }

        txtbox_accessToken = findViewById(R.id.txtbox_accessToken);
        ImageView logo = findViewById(R.id.justagram);
        ImageView glow1 = findViewById(R.id.glow1);
        ImageView glow2 = findViewById(R.id.glow2);
        var forget = findViewById(R.id.tvForgot);
        var signUp = findViewById(R.id.signUp);
        LinearLayout content = findViewById(R.id.content);

        if (userInfo != null)
        {
            txtbox_accessToken.setText(userInfo.GetAccessToken());
        }
        else
            userInfo = new UserInfo();
        txtbox_accessToken.setText(userInfo.GetAccessToken());
        userInfo.onAccessTokenChange = obj -> {
            txtbox_accessToken.setText(userInfo.GetAccessToken());
        };

        IntroAnimator.start(logo,glow1,glow2,content);
        forget.setOnClickListener(new forgetPassword(this));
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

    void ExchangeForToken(String Code)
    {
        String endpoint_code2Stoken = "https://api.instagram.com/oauth/access_token";
        String endpoint_Stoken2token = "";
        var req = Utility.SimplePostRequest(endpoint_code2Stoken,true);

        req.Add("client_id","1326733148350430");
        req.Add("client_secret","7eaeafa2ad6e548c65b0bc79ae2457d1");
        req.Add("grant_type","authorization_code");
        req.Add("code",Code);
        req.Add("redirect_uri",OurBackendServer + "/returnCode");
        final String[] Stoken = new String[1];

        req.onResponeJson = hash ->
        {
//            if ((Double)hash.get("code") > 299)
//            {
//                Utility.showMessageBox("Fail to request",this);
//                return;
//            }
            Stoken[0] = hash.get("access_token").toString();
            ExchangeForLongtoken(Stoken[0]);
        };

        req.Send(new TommyDatCallBack() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                super.onFailure(call, e);
            }
        });
    }
    void ExchangeForLongtoken(String Stoken)
    {

        String endpoint_code2Stoken = "https://graph.instagram.com/access_token?grant_type=ig_exchange_token&client_secret=7eaeafa2ad6e548c65b0bc79ae2457d1&access_token=" + Stoken;
        final String[] ltoken = new String[1];
        var t = new TommyDatCallBack() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                super.onFailure(call, e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                super.onResponse(call, response);
            }
        };
        t.onResponeJson = hashTable -> {
            ltoken[0] = hashTable.get("access_token").toString();
            userInfo.SetAccessToken(ltoken[0]);
            Utility.Save(this,userInfo,UserFileName);
        };
        Utility.SimpleGetRequest(endpoint_code2Stoken,t);
    }
    void TestInstagramAccountFragment()
    {
        setContentView(R.layout.activity_home_page);
        InstagramAccountFragment test = new InstagramAccountFragment();
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.add(R.id.test_fragment,test).commit();
    }
    void TestInstagramPostFragment()
    {
        setContentView(R.layout.activity_home_page);
        IgPublisherFragment test = new IgPublisherFragment();
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.add(R.id.test_fragment,test).commit();

    }

    void btn_LoginAction()
    {
        userInfo.SetAccessToken(txtbox_accessToken.getText().toString());
        var callback = new TommyDatCallBack();
        var onFailure = Utility.CreateRunnable(obj ->
        Utility.showMessageBox("Cannot not fetch user info, please check the access_token is valid or not",this));
        callback.onResponeJson = hashTable ->
        {
            if ((int)hashTable.get("request_code") > 299)
            {
                runOnUiThread(onFailure);
            }
            else
            {
                userInfo.UserID =  hashTable.get("id").toString();
                OpenHomeActivity();

            }
        };
        // Sending request to the server
        Utility.SimpleGetRequest(" https://graph.instagram.com/v24.0/me?" +
                                        "fields=id&access_token="  +
                userInfo.GetAccessToken(),callback);

    }
    void OpenHomeActivity()
    {
        Intent i = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(i);
    }
}
