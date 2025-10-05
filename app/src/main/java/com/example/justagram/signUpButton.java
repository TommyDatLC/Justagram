package com.example.justagram;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
public class signUpButton implements View.OnClickListener {
    private final Context context;
    public signUpButton(Context context){
        this.context = context;
    }

    @Override
    public void onClick(View v){
        String url = "https://www.facebook.com/r.php?entry_point=login";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(intent);
    }


}
