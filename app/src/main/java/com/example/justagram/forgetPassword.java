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
        String url = "https://www.facebook.com/login/identify/?ctx=recover&ars=facebook_login&from_login_screen=0";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(intent);
    }
}
