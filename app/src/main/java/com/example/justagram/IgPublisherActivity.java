package com.example.justagram;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.justagram.etc.Utility;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class IgPublisherActivity extends AppCompatActivity {
    private static final int REQUEST_PICK_MEDIA = 4001;
    private static final int MAX_IMAGES = 10;

    private static final String SERVER_URL = "https://catechistical-questingly-na.ngrok-free.dev";
    private static final String ACCESS_TOKEN = "IGAAS2qCIE595BZAFJ0SmVNaHBUbFFCM0NqOFBOYkdNOHhBdC1PR1hNTHV6ZAEtLZAm5RVTNZAa3lweFdqM0xxNVcwY2xLVlBadFdDUm54QkFBd0Jvdl8zRkJEMFFBNEtMZAkhyX2hfQUtIZAzNnVGdSa2pVYmtoX1I2bkZAxOFZAuOGp6VQZDZD";
    private static final String IG_USER_ID = "17841474853201686";
    private static final String API_VERSION = "v23.0";
    private final List<Uri> selectedUris = new ArrayList<>();
    private final List<String> selectedNames = new ArrayList<>();
    private final OkHttpClient http = new OkHttpClient();
    private TabLayout tabLayout;
    private Button btnPickMedia_reel, btnPublishNow_reel;
    private Button btnPickMedia_post, btnPublishNow_post;
    private Button btnSchedule_reel, btnSchedule_post;
    private EditText etCaption_reel, etCaption_post;
    private RecyclerView rvPreview_reel, rvPreview_post;
    private List<String> scheduledList = new ArrayList<>();
    private ArrayAdapter<String> scheduledAdapter;

    private static String queryName(Context ctx, Uri uri) {
        String res = null;
        Cursor c = ctx.getContentResolver().query(uri, null, null, null, null);
        try {
            if (c != null && c.moveToFirst()) {
                int idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) res = c.getString(idx);
            }
        } finally {
            if (c != null) c.close();
        }
        return res == null ? "file" : res;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_ig_publisher_tabs);

        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText("Reel"));
        tabLayout.addTab(tabLayout.newTab().setText("Post"));

        btnPickMedia_reel = findViewById(R.id.btnPickMedia_reel);
        btnPublishNow_reel = findViewById(R.id.btnPublishNow_reel);
        etCaption_reel = findViewById(R.id.etCaption_reel);
        rvPreview_reel = findViewById(R.id.rvPreview_reel);

        btnPickMedia_post = findViewById(R.id.btnPickMedia_post);
        btnPublishNow_post = findViewById(R.id.btnPublishNow_post);
        etCaption_post = findViewById(R.id.etCaption_post);
        rvPreview_post = findViewById(R.id.rvPreview_post);
        btnSchedule_reel = findViewById(R.id.btnSchedule_reel);
        btnSchedule_post = findViewById(R.id.btnSchedule_post);

        rvPreview_reel.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvPreview_post.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        PreviewAdapter previewAdapter = new PreviewAdapter();
        rvPreview_reel.setAdapter(previewAdapter);
        rvPreview_post.setAdapter(previewAdapter);

        btnPickMedia_reel.setOnClickListener(ch -> pickMedia(true));
        btnPickMedia_post.setOnClickListener(ch -> pickMedia(false));

        btnPublishNow_reel.setOnClickListener(ch -> publishNow(true));
        btnPublishNow_post.setOnClickListener(ch -> publishNow(false));


    }

    private void pickMedia(boolean isReel) {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, isReel ? false : true);
        if (isReel) i.setType("video/*");
        else i.setType("image/*");
        startActivityForResult(i, REQUEST_PICK_MEDIA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_MEDIA && data != null) {
            selectedUris.clear();
            selectedNames.clear();
            if (data.getClipData() != null) {
                ClipData cd = data.getClipData();
                for (int idx = 0; idx < cd.getItemCount() && selectedUris.size() < MAX_IMAGES; ++idx) {
                    Uri u = cd.getItemAt(idx).getUri();
                    addPersistUri(u);
                }
            } else if (data.getData() != null) {
                addPersistUri(data.getData());
            }
            if (rvPreview_reel.getAdapter() != null)
                rvPreview_reel.getAdapter().notifyDataSetChanged();
            if (rvPreview_post.getAdapter() != null)
                rvPreview_post.getAdapter().notifyDataSetChanged();
        }
    }

    private void addPersistUri(Uri u) {
        if (u == null) return;
        try {
            final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
            getContentResolver().takePersistableUriPermission(u, takeFlags);
        } catch (Exception ignore) {
        }
        selectedUris.add(u);
        selectedNames.add(queryName(this, u));
    }

    private void publishNow(boolean asReel) {
        String caption = asReel ? etCaption_reel.getText().toString() : etCaption_post.getText().toString();
        if (!checkCaptionLength(caption)) {
            showMsg("Caption must be at least 8 characters long");
            return;
        }
        if (selectedUris.isEmpty()) {
            showMsg("Pick at least one media");
            return;
        }

        setAllPublishButtonsEnabled(false);

        if (asReel) {
            if (selectedUris.size() > 1) {
                showMsg("Reel accepts only one video. Please pick a single video.");
                setAllPublishButtonsEnabled(true);
                return;
            }
            Uri videoUri = selectedUris.get(0);
            uploadVideoToServer(SERVER_URL, videoUri, (videoUrl, err) -> {
                if (err != null) {
                    showMsg("Video upload error: " + err);
                    setAllPublishButtonsEnabled(true);
                    return;
                }
                /**
                 * sau khi upload video lên server thành công thì up lên instagram thông qua hàm publishReelToInstagram
                 * @parameter igUserId: id instagram
                 * @parameter videoUrl: url video ở server
                 * @parameter caption: caption
                 * @parameter accessToken: self explainatory
                 *
                 *
                 */

                publishReelToInstagram(IG_USER_ID, videoUrl, caption, ACCESS_TOKEN, (url, error) -> {
                    runOnUiThread(() -> {
                        if (error != null) {
                            showMsg("Reel publish error: " + error);
                        } else {
                            showMsg("Reel published successfully!");
                        }
                        setAllPublishButtonsEnabled(true);
                    });
                });
            });
        } else {
            // mảng url chứa server trả về
            uploadAllImagesToServer(SERVER_URL, selectedUris, (urls, err) -> {
                if (err != null) {
                    showMsg("Upload server error: " + err);
                    setAllPublishButtonsEnabled(true);
                    return;
                }
                if (urls == null || urls.isEmpty()) {
                    showMsg("No URLs returned by server");
                    setAllPublishButtonsEnabled(true);
                    return;
                }
                createContainersForUrlsThenPublish(IG_USER_ID, ACCESS_TOKEN, urls, caption, false, new TerminalCallback() {
                    @Override
                    public void onDone(String error) {
                        if (error != null) showMsg("Post publish error: " + error);
                        setAllPublishButtonsEnabled(true);
                    }
                });
            });
        }
    }

    private void publishToInstagram() {

    }

    private void setAllPublishButtonsEnabled(boolean enabled) {
        runOnUiThread(() -> {
            if (btnPublishNow_post != null) btnPublishNow_post.setEnabled(enabled);
            if (btnPublishNow_reel != null) btnPublishNow_reel.setEnabled(enabled);
        });
    }

    private void uploadAllImagesToServer(String serverUrl, List<Uri> uris, UploadAllCallback cb) {
        List<String> resultUrls = new ArrayList<>();
        AtomicInteger remaining = new AtomicInteger(uris.size());
        if (uris.isEmpty()) {
            runOnUiThread(() -> cb.onDone(null, "No URIs"));
            return;
        }
        if (serverUrl.endsWith("/")) serverUrl = serverUrl.substring(0, serverUrl.length() - 1);

        for (Uri u : uris) {
            try {
                InputStream in = getContentResolver().openInputStream(u);
                if (in == null) {
                    runOnUiThread(() -> cb.onDone(null, "Cannot open file"));
                    return;
                }
                byte[] bytes = new byte[in.available()];
                in.read(bytes);
                in.close();

                String name = queryName(this, u);
                if (name == null || name.isEmpty()) name = "file_" + System.currentTimeMillis();

                RequestBody fileBody = RequestBody.create(bytes, MediaType.parse("application/octet-stream"));
                RequestBody rb = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", name, fileBody)
                        .build();

                Request req = new Request.Builder().url(serverUrl + "/upload").post(rb).build();
                String finalServerUrl = serverUrl;
                String finalName = name;
                http.newCall(req).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                        runOnUiThread(() -> cb.onDone(null, e.getMessage()));
                    }

                    @Override
                    public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            runOnUiThread(() -> cb.onDone(null, "HTTP " + response.code()));
                            return;
                        }
                        try {
                            String body = response.body().string();
                            String url = null;
                            try {
                                JSONObject jo = new JSONObject(body);
                                url = jo.optString("url", null);
                            } catch (Exception ignored) {
                            }
                            if (url == null || url.isEmpty()) {
                                url = finalServerUrl + "/uploads/" + urlEncodePathSegment(finalName);
                            }
                            synchronized (resultUrls) {
                                resultUrls.add(url);
                            }
                        } finally {
                            if (remaining.decrementAndGet() == 0)
                                runOnUiThread(() -> cb.onDone(resultUrls, null));
                        }
                    }
                });
            } catch (Exception ex) {
                runOnUiThread(() -> cb.onDone(null, ex.getMessage()));
                return;
            }
        }
    }

    private void uploadVideoToServer(String serverUrl, Uri videoUri, UploadVideoCallback cb) {
        if (serverUrl.endsWith("/")) serverUrl = serverUrl.substring(0, serverUrl.length() - 1);
        try {
            InputStream in = getContentResolver().openInputStream(videoUri);
            if (in == null) {
                runOnUiThread(() -> cb.onDone(null, "Cannot open video file"));
                return;
            }
            byte[] bytes = new byte[in.available()];
            in.read(bytes);
            in.close();

            String name = queryName(this, videoUri);
            if (name == null || name.isEmpty()) name = "video_" + System.currentTimeMillis();

            RequestBody fileBody = RequestBody.create(bytes, MediaType.parse("video/mp4"));
            RequestBody rb = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", name, fileBody)
                    .build();

            Request req = new Request.Builder().url(serverUrl + "/upload").post(rb).build();
            String finalServerUrl = serverUrl;
            String finalName = name;
            http.newCall(req).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                    runOnUiThread(() -> cb.onDone(null, e.getMessage()));
                }

                @Override
                public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        runOnUiThread(() -> cb.onDone(null, "HTTP " + response.code()));
                        return;
                    }
                    try {
                        String body = response.body().string();
                        String url = null;
                        try {
                            JSONObject jo = new JSONObject(body);
                            url = jo.optString("url", null);
                        } catch (Exception ignored) {
                        }
                        if (url == null || url.isEmpty()) {
                            url = finalServerUrl + "/uploads/" + urlEncodePathSegment(finalName);
                        }
                        String finalUrl = url;
                        runOnUiThread(() -> cb.onDone(finalUrl, null));
                    } catch (Exception ex) {
                        runOnUiThread(() -> cb.onDone(null, ex.getMessage()));
                    }
                }
            });
        } catch (Exception e) {
            runOnUiThread(() -> cb.onDone(null, e.getMessage()));
        }
    }

    // function to upload instagram 123
    // check 6-7 charr moi cho up
    // dung dateTime lay schedule
    // hiện thông báo khi upload thành công lên server
    public boolean checkCaptionLength(String caption) {
        if (caption == null || caption.length() < 8) {
            return false;
        }
        return true;
    }

    public void publishReelToInstagram(String igUserId, String videoUrl, String caption, String accessToken, UploadVideoCallback callback) {
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
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onDone(null, "Create failed: " + e.getMessage());
            }

            // nếu phản hồi thành công
            @Override
            public void onResponse(Call call, Response response) throws IOException {
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
                    Log.e("IG_DEBUG", "✅ Creation ID: " + creationId);

                    // đợi 20s trước khi post để instagram xử lí
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
                            @Override
                            public void onFailure(Call call, IOException e) {
                                callback.onDone(null, "Publish failed: " + e.getMessage());
                            }


                            // nếu phản hồi thành công
                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                String publishResponse = response.body().string();
                                Log.e("IG_PUBLISH_RESPONSE", publishResponse);

                                if (!response.isSuccessful()) {
                                    callback.onDone(null, "Publish error: " + publishResponse);
                                } else {
                                    callback.onDone(publishResponse, null);
                                }
                            }
                        });

                    }, 20000); // delay 20s

                } catch (JSONException e) {
                    callback.onDone(null, "Invalid JSON: " + e.getMessage());
                }
            }
        });
    }

    private String urlEncodePathSegment(String s) {
        try {
            return java.net.URLEncoder.encode(s, "UTF-8").replace("+", "%20");
        } catch (Exception e) {
            return s;
        }
    }

    private void createContainersForUrlsThenPublish(String igId, String token, List<String> urls, String caption, boolean asReel, TerminalCallback terminal) {
        List<String> creationIds = new ArrayList<>();
        AtomicInteger remaining = new AtomicInteger(urls.size());
        if (urls.isEmpty()) {
            if (terminal != null) terminal.onDone("No urls to create containers");
            return;
        }

        for (String u : urls) {
            FormBody.Builder fb = new FormBody.Builder()
                    .add("image_url", u)
                    .add("caption", caption)
                    .add("access_token", token);
            if (urls.size() > 1) fb.add("is_carousel_item", "true");
            Request req = new Request.Builder().url("https://graph.instagram.com/" + API_VERSION + "/" + igId + "/media").post(fb.build()).build();
            http.newCall(req).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                    runOnUiThread(() -> {
                        if (terminal != null)
                            terminal.onDone("Create container failed: " + e.getMessage());
                    });
                }

                @Override
                public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                    try {
                        if (!response.isSuccessful()) {
                            String body = response.body() != null ? response.body().string() : "";
                            runOnUiThread(() -> {
                                if (terminal != null)
                                    terminal.onDone("Create container HTTP error: " + response.code() + " " + body);
                            });
                        } else {
                            JSONObject jo = new JSONObject(response.body().string());
                            String id = jo.optString("id", null);
                            if (!TextUtils.isEmpty(id)) synchronized (creationIds) {
                                creationIds.add(id);
                            }
                        }
                    } catch (Exception ex) {
                        runOnUiThread(() -> {
                            if (terminal != null)
                                terminal.onDone("Parse error: " + ex.getMessage());
                        });
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

    private void createVideoContainerThenPublish(String igId, String token, String videoUrl, String caption, boolean asReel, TerminalCallback terminal) {
        FormBody.Builder fb = new FormBody.Builder()
                .add("video_url", videoUrl)
                .add("caption", caption)
                .add("access_token", token);
        if (asReel) fb.add("media_type", "REELS");
        Request req = new Request.Builder().url("https://graph.instagram.com/" + API_VERSION + "/" + igId + "/media").post(fb.build()).build();
        http.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    if (terminal != null)
                        terminal.onDone("Create video container failed: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) {
                        String body = response.body() != null ? response.body().string() : "";
                        runOnUiThread(() -> {
                            if (terminal != null)
                                terminal.onDone("Create video container HTTP error: " + response.code() + " " + body);
                        });
                        return;
                    }
                    JSONObject jo = new JSONObject(response.body().string());
                    String id = jo.optString("id", null);
                    if (!TextUtils.isEmpty(id)) {
                        publishContainer(igId, token, id, (err) -> {
                            if (terminal != null) terminal.onDone(err);
                        });
                    } else {
                        runOnUiThread(() -> {
                            if (terminal != null) terminal.onDone("Video container returned no id");
                        });
                    }
                } catch (Exception ex) {
                    runOnUiThread(() -> {
                        if (terminal != null)
                            terminal.onDone("Parse error video container: " + ex.getMessage());
                    });
                }
            }
        });
    }

    private void createCarouselAndPublish(String igId, String token, List<String> children, String caption, TerminalCallback terminal) {
        String childrenCsv = TextUtils.join(",", children);
        FormBody fb = new FormBody.Builder()
                .add("caption", caption)
                .add("media_type", "CAROUSEL")
                .add("children", childrenCsv)
                .add("access_token", token)
                .build();
        Request req = new Request.Builder().url("https://graph.instagram.com/" + API_VERSION + "/" + igId + "/media").post(fb).build();
        http.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    if (terminal != null)
                        terminal.onDone("Create carousel failed: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) {
                        runOnUiThread(() -> {
                            if (terminal != null)
                                terminal.onDone("Create carousel HTTP error: " + response.code());
                        });
                        return;
                    }
                    JSONObject jo = new JSONObject(response.body().string());
                    String id = jo.optString("id", null);
                    if (!TextUtils.isEmpty(id)) {
                        publishContainer(igId, token, id, (err) -> {
                            if (terminal != null) terminal.onDone(err);
                        });
                    } else {
                        runOnUiThread(() -> {
                            if (terminal != null)
                                terminal.onDone("Carousel creation returned no id");
                        });
                    }
                } catch (Exception ex) {
                    runOnUiThread(() -> {
                        if (terminal != null) terminal.onDone("Parse error: " + ex.getMessage());
                    });
                }
            }
        });
    }

    private void publishContainer(String igId, String token, String creationId, TerminalCallback terminal) {
        RequestBody fb = new FormBody.Builder().add("creation_id", creationId).add("access_token", token).build();
        Request req = new Request.Builder().url("https://graph.instagram.com/" + API_VERSION + "/" + igId + "/media_publish").post(fb).build();
        http.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    if (terminal != null) terminal.onDone("Publish failed: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    String body = response.body() != null ? response.body().string() : "";
                    runOnUiThread(() -> {
                        if (terminal != null)
                            terminal.onDone("Publish HTTP error: " + response.code() + " " + body);
                    });
                    return;
                }
                runOnUiThread(() -> {
                    if (terminal != null) terminal.onDone(null);
                });
            }
        });
    }

    private void showMsg(String s) {
        try {
            Utility.showMessageBox(s, this);
        } catch (Exception e) {
            Toast.makeText(this, s, Toast.LENGTH_LONG).show();
        }
    }

    private interface UploadAllCallback {
        void onDone(List<String> urls, String err);
    }

    private interface UploadVideoCallback {
        void onDone(String videoUrl, String err);
    }

    private interface TerminalCallback {
        void onDone(String error);
    }

    // ĐÃ XÓA SCHEDULED FEATURE SỬ DỤNG ALARM MANAGER
    // --------> DÙNG HÀM DATETIME

    private static class PreviewVH extends RecyclerView.ViewHolder {
        ImageView img;

        PreviewVH(@NonNull View v) {
            super(v);
            img = (ImageView) v;
        }
    }

    // ---------------- preview adapter ----------------
    private class PreviewAdapter extends RecyclerView.Adapter<PreviewVH> {
        @NonNull
        @Override
        public PreviewVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView iv = new ImageView(parent.getContext());
            int pad = 8;
            iv.setPadding(pad, pad, pad, pad);
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(220, 220);
            iv.setLayoutParams(lp);
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return new PreviewVH(iv);
        }


        @Override
        public void onBindViewHolder(@NonNull PreviewVH holder, int position) {
            Uri uri = selectedUris.get(position);
            String type = getContentResolver().getType(uri);

            // Nếu là video, lấy frame đầu tiên
            if (type != null && type.startsWith("video/")) {
                try {
                    android.media.MediaMetadataRetriever retriever = new android.media.MediaMetadataRetriever();
                    retriever.setDataSource(IgPublisherActivity.this, uri);
                    android.graphics.Bitmap frame = retriever.getFrameAtTime(0);
                    retriever.release();

                    if (frame != null) {
                        holder.img.setImageBitmap(frame);
                    } else {
                        holder.img.setImageURI(uri);
                    }
                } catch (Exception e) {
                    holder.img.setImageURI(uri);
                }
            } else {
                // Nếu là ảnh, load bình thường
                holder.img.setImageURI(uri);
            }
        }

        @Override
        public int getItemCount() {
            return selectedUris.size();
        }
    }
}
