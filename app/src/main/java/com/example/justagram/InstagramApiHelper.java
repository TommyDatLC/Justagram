/*
    Nhat Anh
*/

package com.example.justagram;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class InstagramApiHelper {

    private static final OkHttpClient client = new OkHttpClient();

    public interface SuccessCallback {
        void onSuccess(String result);
    }

    public interface ErrorCallback {
        void onError(String error);
    }

    public static void getInstagramPosts(
            String userId,
            String accessToken,
            SuccessCallback onSuccess,
            ErrorCallback onError
    ) {
        String url = "https://graph.instagram.com/" + userId +
                "/media?fields=id,caption,like_count,comments_count&access_token=" +
                accessToken;

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error";
                onError.onError(errorMessage);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ResponseBody responseBody = response.body();

                if (responseBody == null) {
                    onError.onError("Empty response from Instagram API");
                    return;
                }

                String body = responseBody.string();

                if (body == null || body.isEmpty()) {
                    onError.onError("Empty response from Instagram API");
                    return;
                }

                try {
                    JSONObject json = new JSONObject(body);
                    JSONArray data = json.getJSONArray("data");

                    StringBuilder builder = new StringBuilder();

                    for (int i = 0; i < data.length(); i++) {
                        JSONObject post = data.getJSONObject(i);

                        String id = post.getString("id");
                        String caption = post.optString("caption", "No caption");
                        int likes = post.optInt("like_count", 0);
                        int comments = post.optInt("comments_count", 0);

                        builder.append("Post ID: ").append(id).append("\n");
                        builder.append("Caption: ").append(caption).append("\n");
                        builder.append("Likes: ").append(likes)
                                .append(" | Comments: ").append(comments)
                                .append("\n\n");
                    }

                    onSuccess.onSuccess(builder.toString());

                } catch (Exception ex) {
                    String errorMessage = "Parsing error: " +
                            (ex.getMessage() != null ? ex.getMessage() : "Unknown parsing error");
                    onError.onError(errorMessage);
                }
            }
        });
    }
}