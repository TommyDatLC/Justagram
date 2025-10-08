package com.example.justagram;

import static androidx.core.content.ContextCompat.startActivity;


import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.ContextThemeWrapper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class Utility {
    public static void showMessageBox(String content,Context t) {
        
        // 1. Instantiate an AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(t);

        // 2. Chain methods to set the dialog characteristics
        builder.setTitle("Justagram");
        builder.setMessage(content);

        // Add a single button
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Code to run when OK is clicked.
                // For this example, we just close the dialog.
                dialog.dismiss();
            }
        });

        // 3. Create the AlertDialog
        AlertDialog dialog = builder.create();

        // 4. Show the dialog
        dialog.show();
    }
    public static void OpenWebsite(Context ctx, String URL)
    {

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL));
        ctx.startActivity(browserIntent);

    }
    public static void SimpleGetRequest(String URL,TommyDatCallBack callback)
    {
        Request r = new Request.Builder().url(URL).build();
        MainActivity.client.newCall(r).enqueue(callback);
    }
}
