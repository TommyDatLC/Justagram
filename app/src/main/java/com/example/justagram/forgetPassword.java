package com.example.justagram;

import android.view.View;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class forgetPassword implements View.OnClickListener {

    private final Context context;
    public forgetPassword(Context context){
        this.context = context;
    }

    @Override
    public void onClick(View v){
        String url = "https://www.instagram.com/accounts/password/reset/";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(intent);
    }
}
/**
 Onclick function để handle việc redirect đến official web của facebook
 dùng trong main activity:
 Button forget = findViewById(ID NUT SIGN UP O XML);
 forget.setOnClickListener(new forgetPassword(this));
 */
