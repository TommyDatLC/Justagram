package com.example.justagram.fragment.PostFeed;
// if u need to change something, here r the related files
// postItem.java
// layout : items_post_feed, fragment_post_feed, activity_main, activity_post_detail

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.justagram.Activity;
import com.example.justagram.R;

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

public class PostFeedFragment extends Fragment {

    private final OkHttpClient client = new OkHttpClient();
    private TextView totalStats;
    private PostAdapter adapter;
    private List<PostItem> postList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_post_feed, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        postList = new ArrayList<>();

        String access_token = com.example.justagram.Activity.ACCESS_TOKEN;
        adapter = new PostAdapter(requireContext(), postList, access_token);
        recyclerView.setAdapter(adapter);

        totalStats = view.findViewById(R.id.totalStats);
        updateTotalStats();

        adapter.setOnSelectionChangedListener(this::updateTotalStats);

        // 🆕 Gọi API để lấy data thật
        fetchInstagramPosts();

        return view;
    }

    // 🆕 Method gọi Instagram API
    private void fetchInstagramPosts() {
        String apiUrl = "https://graph.instagram.com/me/media?fields=id,caption,media_url,media_type,like_count,comments_count&access_token=" + Activity.ACCESS_TOKEN;

        Request request = new Request.Builder()
                .url(apiUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Lỗi khi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("API_ERROR", "Error: " + e.getMessage());
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Lỗi kết nối API: " + response.code(), Toast.LENGTH_SHORT).show();
                        });
                    }
                    return;
                }

                String responseData = response.body().string();
                Log.d("API_RESPONSE", responseData);

                try {
                    JSONObject jsonObject = new JSONObject(responseData);
                    JSONArray dataArray = jsonObject.getJSONArray("data");

                    postList.clear();
                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject postObj = dataArray.getJSONObject(i);

                        String id = postObj.optString("id");
                        String mediaUrl = postObj.optString("media_url");
                        String mediaType = postObj.optString("media_type", "IMAGE");
                        int likeCount = postObj.optInt("like_count", 0);
                        int commentCount = postObj.optInt("comments_count", 0);

                        // 🧩 CHỈ LẤY ẢNH - BỎ VIDEO / REELS
                        if (mediaType.equalsIgnoreCase("IMAGE")) {
                            postList.add(new PostItem(id, mediaUrl, mediaType, likeCount, commentCount));
                        }
                    }

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            adapter.notifyDataSetChanged();
                            updateTotalStats();
                            Toast.makeText(getContext(),
                                    "Đã tải " + postList.size() + " bài viết (ảnh)",
                                    Toast.LENGTH_SHORT).show();
                        });
                    }

                } catch (JSONException e) {
                    Log.e("JSON_ERROR", "Error parsing: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }


    private void updateTotalStats() {
        int totalLikes = adapter.getTotalLikes();
        int totalComments = adapter.getTotalComments();
        totalStats.setText("♥ " + totalLikes + "  💬 " + totalComments);
    }
}