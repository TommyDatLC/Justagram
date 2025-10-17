package com.example.justagram.etc;


import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.example.justagram.LoginAuth.LoginActivity;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.function.Consumer;

import okhttp3.Request;

public class Utility {
    public static void showMessageBox(String content, Context t) {

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

    public static void OpenWebsite(Context ctx, String URL) {

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL));
        ctx.startActivity(browserIntent);
    }

    public static void SimpleGetRequest(String URL, TommyDatCallBack callback) {
        Request r = new Request.Builder().url(URL).build();
        LoginActivity.client.newCall(r).enqueue(callback);
    }

    public static void SimpleGetRequest(String URL, Consumer<Hashtable<String, Object>> callback) {
        TommyDatCallBack TDcallback = new TommyDatCallBack();
        TDcallback.onResponeJson = callback;
        Request r = new Request.Builder().url(URL).build();
        LoginActivity.client.newCall(r).enqueue(TDcallback);
    }

    public static TommyDatPostRequest SimplePostRequest(String URL, boolean isForm) {
        return new TommyDatPostRequest(URL, isForm);
    }

    public static void Save(Context ctx, Object obj, String FileName) {
        var jsonString = LoginActivity.gson.toJson(obj);
        File f = new File(ctx.getFilesDir(), FileName);
        try (FileWriter writer = new FileWriter(f)) {
            if (!f.exists()) {
                f.createNewFile();
            }
            writer.write(jsonString);
            writer.flush();
            Log.i("File", "Saved file to" + f.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static <T> T Load(Context ctx, Class<T> c, String FileName) {
        File f = new File(ctx.getFilesDir(), FileName);
        try (FileReader r = new FileReader(f)) {
            return LoginActivity.gson.fromJson(r, c);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Runnable CreateRunnable(Consumer<Object> a) {
        return new Runnable() {
            @Override
            public void run() {
                a.accept(null);
            }
        };
    }


}
