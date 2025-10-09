package com.example.justagram;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Hashtable;
import java.util.function.Consumer;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class TommyDatCallBack implements Callback {

    @Override
    public void onFailure(@NonNull Call call, @NonNull IOException e) {
        e.printStackTrace();
    }
    @Override
    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
        Type type_hashtable = new TypeToken<Hashtable<String,Object>>() {}.getType();
        String jsonString = response.body().string();
        Log.i("jsonReqString",jsonString);
        Hashtable<String,Object> hashtable =  MainActivity.gson.fromJson(jsonString,type_hashtable);
        onResponeJson.accept(hashtable);
    }
    public Consumer<Hashtable<String,Object>> onResponeJson;
}
