package com.example.justagram;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import com.example.justagram.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class InstagramPublishAPI {
    private static final String SERVER_URL = "https://catechistical-questingly-na.ngrok-free.dev";
    private static final String ACCESS_TOKEN = "IGAAS2qCIE595BZAFJ0SmVNaHBUbFFCM0NqOFBOYkdNOHhBdC1PR1hNTHV6ZAEtLZAm5RVTNZAa3lweFdqM0xxNVcwY2xLVlBadFdDUm54QkFBd0Jvdl8zRkJEMFFBNEtMZAkhyX2hfQUtIZAzNnVGdSa2pVYmtoX1I2bkZAxOFZAuOGp6VQZDZD";
    private static final String IG_USER_ID = "17841474853201686";
    private static final String API_VERSION = "v23.0";
    private static final OkHttpClient http = new OkHttpClient();

    public interface UploadVideoCallback { void onDone(String videoUrl, String err); }
    public interface TerminalCallback { void onDone(String error); }
    public static void publishReelToInstagram(String igUserId, String videoUrl, String caption, String accessToken, IgPublisherActivity.UploadVideoCallback callback) {
        OkHttpClient client = new OkHttpClient();

        // URL
        String createUrl = String.format("https://graph.instagram.com/v23.0/%s/media", igUserId);

        // TAO BODY
        RequestBody createBody = new FormBody.Builder(Charset.forName("UTF-8"))
                .add("media_type", "REELS")
                .add("video_url", videoUrl)
                .add("caption", caption)
                .add("share_to_feed", "true")
                .add("access_token", accessToken)
                .build();

        // hàm tổng thể để gửi request
        Request createRequest = new Request.Builder()
                .url(createUrl)
                .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .post(createBody)
                .build();


        // Log để debug
        Log.e("IG_DEBUG", "Create URL: " + createUrl);

        // lấy 20 ký tự đầu của access token
        Log.e("IG_DEBUG", "AccessToken (first 20): " + accessToken.substring(0, Math.min(20, accessToken.length())) + "...");
        Log.e("IG_DEBUG", "Video URL: " + videoUrl);
        Log.e("IG_DEBUG", "Caption: " + caption);



        client.newCall(createRequest).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                callback.onDone(null, "Create failed: " + e.getMessage());
            }

            // nếu phản hồi thành công
            @Override public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                Log.e("IG_CREATE_RESPONSE", responseData);

                if (!response.isSuccessful()) {
                    callback.onDone(null, "Create error: " + response.code() + " - " + responseData);
                    return;
                }
                // nếu phản hồi không có id -> lỗi
                try {
                    JSONObject json = new JSONObject(responseData);
                    if (!json.has("id")) {
                        callback.onDone(null, "Create response missing ID");
                        return;
                    }


                    // thành công lấy id -> log ra id
                    String creationId = json.getString("id");
                    Log.e("IG_DEBUG", " Creation ID: " + creationId);

                    // đợi 15s trước khi post để instagram xử lí
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {

                        // link publish
                        String publishUrl = String.format("https://graph.instagram.com/v23.0/%s/media_publish", igUserId);

                        /**
                         * @parameter creation_id: id sau khi tạo thành công container
                         * @parameter access_token: very self explainatory
                         */
                        RequestBody publishBody = new FormBody.Builder(Charset.forName("UTF-8"))
                                .add("creation_id", creationId)
                                .add("access_token", accessToken)
                                .build();

                        Request publishRequest = new Request.Builder()
                                .url(publishUrl)
                                .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                                .post(publishBody)
                                .build();
                        // log để debug
                        Log.e("IG_DEBUG", "Publish URL: " + publishUrl);

                        client.newCall(publishRequest).enqueue(new Callback() {
                            @Override public void onFailure(Call call, IOException e) {
                                callback.onDone(null, "Publish failed: " + e.getMessage());
                            }


                            // nếu phản hồi thành công
                            @Override public void onResponse(Call call, Response response) throws IOException {
                                String publishResponse = response.body().string();
                                Log.e("IG_PUBLISH_RESPONSE", publishResponse);

                                if (!response.isSuccessful()) {
                                    callback.onDone(null, "Publish error: " + publishResponse);
                                } else {
                                    callback.onDone(publishResponse, null);
                                }
                            }
                        });

                    }, 15000); // delay 20s

                } catch (JSONException e) {
                    callback.onDone(null, "Invalid JSON: " + e.getMessage());
                }
            }
        });
    }

    private static void createContainersForUrlsThenPublish(String igId, String token, List<String> urls, String caption, boolean asReel, IgPublisherActivity.TerminalCallback terminal) {
        List<String> creationIds = new ArrayList<>();
        AtomicInteger remaining = new AtomicInteger(urls.size());
        if (urls.isEmpty()) { if (terminal != null) terminal.onDone("No urls to create containers"); return; }

        for (String u : urls) {
            FormBody.Builder fb = new FormBody.Builder()
                    .add("image_url", u)
                    .add("caption", caption)
                    .add("access_token", token);
            if (urls.size() > 1) fb.add("is_carousel_item", "true");
            Request req = new Request.Builder().url("https://graph.instagram.com/" + API_VERSION + "/" + igId + "/media").post(fb.build()).build();
            http.newCall(req).enqueue(new Callback() {
                @Override public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                    runOnUiThread(() -> {
                        if (terminal != null) terminal.onDone("Create container failed: " + e.getMessage());
                    });
                }
                @Override public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                    try {
                        if (!response.isSuccessful()) {
                            String body = response.body() != null ? response.body().string() : "";
                            runOnUiThread(() -> { if (terminal != null) terminal.onDone("Create container HTTP error: " + response.code() + " " + body); });
                        } else {
                            JSONObject jo = new JSONObject(response.body().string());
                            String id = jo.optString("id", null);
                            if (!TextUtils.isEmpty(id)) synchronized (creationIds) { creationIds.add(id); }
                        }
                    } catch (Exception ex) {
                        runOnUiThread(() -> { if (terminal != null) terminal.onDone("Parse error: " + ex.getMessage()); });
                    } finally {
                        if (remaining.decrementAndGet() == 0) {
                            runOnUiThread(() -> {
                                if (creationIds.isEmpty()) {
                                    if (terminal != null) terminal.onDone("No containers created.");
                                    return;
                                }
                                if (creationIds.size() == 1) {
                                    publishContainer(igId, token, creationIds.get(0), (err) -> {
                                        if (terminal != null) terminal.onDone(err);
                                    });
                                } else {
                                    createCarouselAndPublish(igId, token, creationIds, caption, (err) -> {
                                        if (terminal != null) terminal.onDone(err);
                                    });
                                }
                            });
                        }
                    }
                }
            });
        }
    }

    private static void runOnUiThread(Object o) {
    }

    private static void createVideoContainerThenPublish(String igId, String token, String videoUrl, String caption, boolean asReel, IgPublisherActivity.TerminalCallback terminal) {
        FormBody.Builder fb = new FormBody.Builder()
                .add("video_url", videoUrl)
                .add("caption", caption)
                .add("access_token", token);
        if (asReel) fb.add("media_type", "REELS");
        Request req = new Request.Builder().url("https://graph.instagram.com/" + API_VERSION + "/" + igId + "/media").post(fb.build()).build();
        http.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                runOnUiThread(() -> { if (terminal != null) terminal.onDone("Create video container failed: " + e.getMessage()); });
            }
            @Override public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) {
                        String body = response.body() != null ? response.body().string() : "";
                        runOnUiThread(() -> { if (terminal != null) terminal.onDone("Create video container HTTP error: " + response.code() + " " + body); });
                        return;
                    }
                    JSONObject jo = new JSONObject(response.body().string());
                    String id = jo.optString("id", null);
                    if (!TextUtils.isEmpty(id)) {
                        publishContainer(igId, token, id, (err) -> {
                            if (terminal != null) terminal.onDone(err);
                        });
                    } else {
                        runOnUiThread(() -> { if (terminal != null) terminal.onDone("Video container returned no id"); });
                    }
                } catch (Exception ex) {
                    runOnUiThread(() -> { if (terminal != null) terminal.onDone("Parse error video container: " + ex.getMessage()); });
                }
            }
        });
    }

    private static void createCarouselAndPublish(String igId, String token, List<String> children, String caption, IgPublisherActivity.TerminalCallback terminal) {
        String childrenCsv = TextUtils.join(",", children);
        FormBody fb = new FormBody.Builder()
                .add("caption", caption)
                .add("media_type", "CAROUSEL")
                .add("children", childrenCsv)
                .add("access_token", token)
                .build();
        Request req = new Request.Builder().url("https://graph.instagram.com/" + API_VERSION + "/" + igId + "/media").post(fb).build();
        http.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                runOnUiThread(() -> { if (terminal != null) terminal.onDone("Create carousel failed: " + e.getMessage()); });
            }
            @Override public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) {
                        runOnUiThread(() -> { if (terminal != null) terminal.onDone("Create carousel HTTP error: " + response.code()); });
                        return;
                    }
                    JSONObject jo = new JSONObject(response.body().string());
                    String id = jo.optString("id", null);
                    if (!TextUtils.isEmpty(id)) {
                        publishContainer(igId, token, id, (err) -> {
                            if (terminal != null) terminal.onDone(err);
                        });
                    } else {
                        runOnUiThread(() -> { if (terminal != null) terminal.onDone("Carousel creation returned no id"); });
                    }
                } catch (Exception ex) {
                    runOnUiThread(() -> { if (terminal != null) terminal.onDone("Parse error: " + ex.getMessage()); });
                }
            }
        });
    }

    private static void publishContainer(String igId, String token, String creationId, IgPublisherActivity.TerminalCallback terminal) {
        RequestBody fb = new FormBody.Builder().add("creation_id", creationId).add("access_token", token).build();
        Request req = new Request.Builder().url("https://graph.instagram.com/" + API_VERSION + "/" + igId + "/media_publish").post(fb).build();
        http.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                runOnUiThread(() -> { if (terminal != null) terminal.onDone("Publish failed: " + e.getMessage()); });
            }
            @Override public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    String body = response.body() != null ? response.body().string() : "";
                    runOnUiThread(() -> { if (terminal != null) terminal.onDone("Publish HTTP error: " + response.code() + " " + body); });
                    return;
                }
                runOnUiThread(() -> { if (terminal != null) terminal.onDone(null); });
            }
        });
    }
}
