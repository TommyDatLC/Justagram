package com.example.justagram.LoginAuth;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
public class SignUpButton implements View.OnClickListener {
    private final Context context;

    public SignUpButton(Context context){
        this.context = context;
    }

    @Override
    public void onClick(View v){
        String url = "https://www.instagram.com/accounts/emailsignup/";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(intent);
    }
}
/**
 Onclick function để handle việc redirect đến official web của facebook

 dùng trong main activity:
 Button signUp = findViewById(ID NUT SIGN UP O XML);
 signUp.setOnClickListener(new SignUpButton(this));
 */

