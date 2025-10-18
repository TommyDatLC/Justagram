package com.example.justagram.Activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.justagram.LoginAuth.LoginActivity;
import com.example.justagram.R;
import com.example.justagram.fragment.PostFeed.PostAdapter;
import com.example.justagram.fragment.Statistic.PostItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PostFeedActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<PostItem> postList;
    private Button btnReload;
    private TextView totalStats;
    private OkHttpClient client = new OkHttpClient();

    //  Your log in token
    public static String ACCESS_TOKEN = "IGAAS2qCIE595BZAFJ0SmVNaHBUbFFCM0NqOFBOYkdNOHhBdC1PR1hNTHV6ZAEtLZAm5RVTNZAa3lweFdqM0xxNVcwY2xLVlBadFdDUm54QkFBd0Jvdl8zRkJEMFFBNEtMZAkhyX2hfQUtIZAzNnVGdSa2pVYmtoX1I2bkZAxOFZAuOGp6VQZDZD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ACCESS_TOKEN = LoginActivity.userInfo.GetAccessToken();
        recyclerView = findViewById(R.id.recyclerView);
        btnReload = findViewById(R.id.btnReload);
        totalStats = findViewById(R.id.totalStats);
        postList = new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        postAdapter = new PostAdapter(this, postList, ACCESS_TOKEN);
        recyclerView.setAdapter(postAdapter);

        fetchInstagramPosts();

        btnReload.setOnClickListener(v -> fetchInstagramPosts());
    }

    private void fetchInstagramPosts() {
        btnReload.setEnabled(false);
        btnReload.setText("Loading...");

        String apiUrl = "https://graph.instagram.com/me/media?fields=id,caption,media_url,media_type,like_count,comments_count&access_token=" + ACCESS_TOKEN;

        Request request = new Request.Builder()
                .url(apiUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(PostFeedActivity.this, "Lỗi khi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnReload.setEnabled(true);
                    btnReload.setText("Renew post");
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(PostFeedActivity.this, "Lỗi kết nối API!", Toast.LENGTH_SHORT).show();
                        btnReload.setEnabled(true);
                        btnReload.setText("Renew Post");
                    });
                    return;
                }

                String responseData = response.body().string();
                Log.d("API_RESPONSE", responseData);

                try {
                    JSONObject jsonObject = new JSONObject(responseData);
                    JSONArray dataArray = jsonObject.optJSONArray("data");
                    if (dataArray == null) dataArray = new JSONArray();

                    // Xóa list cũ trước khi thêm (trên thread background)
                    postList.clear();

                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject postObj = dataArray.getJSONObject(i);

                        String id = postObj.optString("id");
                        String caption = postObj.optString("caption", "");
                        String mediaUrl = postObj.optString("media_url", null);
                        String mediaType = postObj.optString("media_type", "IMAGE");
                        int likeCount = postObj.optInt("like_count", 0);
                        int commentCount = postObj.optInt("comments_count", 0);

                        // --- LỌC: chỉ thêm ảnh (IMAGE) vào My Post ---
                        if (mediaType != null && mediaType.equalsIgnoreCase("IMAGE")) {
                            // Sử dụng đúng thứ tự constructor: (PostID, mediaUrl, mediaType, likeCount, commentCount)
                            PostItem item = new PostItem(id, mediaUrl, mediaType, likeCount, commentCount);

                            // Nếu API trả thumbnail (optional) và PostItem có trường thumbnailUrl, set nó
                            String thumb = postObj.optString("thumbnail_url", null);
                            if (thumb != null && !thumb.isEmpty()) {
                                item.setThumbnailUrl(thumb);
                            }

                            postList.add(item);
                        }
                    }

                    runOnUiThread(() -> {
                        postAdapter.notifyDataSetChanged();
                        btnReload.setEnabled(true);
                        btnReload.setText("Renew Post");
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        Toast.makeText(PostFeedActivity.this, "Parsing JSON error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        btnReload.setEnabled(true);
                        btnReload.setText("Renew Post");
                    });
                }
            }
        });
    }


    //  Added this new method
    private void updateTotalStats() {
        int totalLikes = postAdapter.getTotalLikes();
        int totalComments = postAdapter.getTotalComments();
        totalStats.setText("♥ " + totalLikes + "  💬 " + totalComments);
    }
}