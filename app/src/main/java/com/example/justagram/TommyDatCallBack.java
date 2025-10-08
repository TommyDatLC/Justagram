package com.example.justagram;

import androidx.annotation.NonNull;

import java.io.IOException;

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

    }
}
