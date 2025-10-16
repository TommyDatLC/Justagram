package com.example.justagram.etc;

import android.util.Log;

import com.example.justagram.LoginAuth.LoginActivity;
import com.google.gson.Gson;

import java.util.Hashtable;
import java.util.function.Consumer;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

public class TommyDatPostRequest {
    public Consumer<Hashtable<String, Object>> onResponeJson;
    Hashtable<String, Object> reqBody = new Hashtable<String, Object>();
    Request.Builder builder;
    MultipartBody.Builder FormBuilder;
    RequestBody requestBody;
    String URL;
    boolean isForm;

    public TommyDatPostRequest(String URL, Boolean isForm) {
        this.URL = URL;
        this.isForm = isForm;
        if (isForm)
            FormBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        builder = new Request.Builder();
    }

    public void Add(String key, Object value) {
        if (isForm)

            FormBuilder.addFormDataPart(key, value.toString());
        else
            reqBody.put(key, value);
    }

    public void Send(TommyDatCallBack c) {
        var gson = new Gson();
        var jsonString = gson.toJson(reqBody);
        Log.i("Sended Req body", jsonString);
        RequestBody requestBody;
        if (isForm) {
            requestBody = FormBuilder.build();
        } else {
            requestBody = RequestBody.create(jsonString, MediaType.get("application/json"));
        }
        Request r = builder.post(requestBody).url(URL).build();
        c.onResponeJson = this.onResponeJson;
        LoginActivity.client.newCall(r).enqueue(c);
    }
}
