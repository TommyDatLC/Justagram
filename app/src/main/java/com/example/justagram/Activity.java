package com.example.justagram;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Activity extends AppCompatActivity {
    private PostAdapter adapter;
    private final List<Post> postList = new ArrayList<>();
    private final Handler handler = new Handler();

    private static final String ACCESS_TOKEN = "IGAAS2qCIE595BZAFJ0SmVNaHBUbFFCM0NqOFBOYkdNOHhBdC1PR1hNTHV6ZAEtLZAm5RVTNZAa3lweFdqM0xxNVcwY2xLVlBadFdDUm54QkFBd0Jvdl8zRkJEMFFBNEtMZAkhyX2hfQUtIZAzNnVGdSa2pVYmtoX1I2bkZAxOFZAuOGp6VQZDZD";
    private static final String USER_ID = "17841474853201686";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        Button btnReload = findViewById(R.id.btnReload);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PostAdapter(this, postList);
        recyclerView.setAdapter(adapter);

        btnReload.setOnClickListener(v -> new Thread(this::fetchInstagramData).start());
        new Thread(this::fetchInstagramData).start();

        // Auto refresh mỗi 60 giây
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                new Thread(Activity.this::fetchInstagramData).start();
                handler.postDelayed(this, 60000);
            }
        }, 60000);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchInstagramData() {
        try {
            String urlStr = "https://graph.instagram.com/" + USER_ID +
                    "/media?fields=id,media_type,media_url,caption,like_count,comments_count&access_token=" + ACCESS_TOKEN;
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                response.append(inputLine);
            in.close();

            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray data = jsonResponse.getJSONArray("data");

            postList.clear();
            for (int i = 0; i < data.length(); i++) {
                JSONObject obj = data.getJSONObject(i);
                Post post = new Post();
                post.mediaType = obj.optString("media_type");
                post.mediaUrl = obj.optString("media_url");
                post.caption = obj.optString("caption");
                post.likeCount = obj.optInt("like_count");
                post.commentsCount = obj.optInt("comments_count");
                postList.add(post);
            }

            runOnUiThread(() -> adapter.notifyDataSetChanged());
        } catch (Exception e) {
            Log.e("InstagramAPI", "Error fetching data", e);
        }
    }
}


