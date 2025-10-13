package com.example.justagram.fragment;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.justagram.R;
import com.example.justagram.etc.Utility;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * IgPublisherFragment (single-file)
 *
 * Changes:
 * - removed scheduling functionality entirely
 * - publish buttons are disabled while corresponding publish flows run and re-enabled after success/failure
 *
 * Note: Replace SERVER_URL, ACCESS_TOKEN, IG_USER_ID with real values.
 */
public class IgPublisherFragment extends Fragment {

    private static final int REQUEST_PICK_MEDIA = 4001;
    private static final int MAX_IMAGES = 10;

    // >>> CONFIG CONSTANTS (not user input)
    private static final String SERVER_URL = "https://catechistical-questingly-na.ngrok-free.dev"; // your upload server
    private static final String ACCESS_TOKEN = "IGAAS2qCIE595BZAFJ0SmVNaHBUbFFCM0NqOFBOYkdNOHhBdC1PR1hNTHV6ZAEtLZAm5RVTNZAa3lweFdqM0xxNVcwY2xLVlBadFdDUm54QkFBd0Jvdl8zRkJEMFFBNEtMZAkhyX2hfQUtIZAzNnVGdSa2pVYmtoX1I2bkZAxOFZAuOGp6VQZDZD";
    private static final String IG_USER_ID = "17841474853201686";
    private static final String API_VERSION = "v23.0";
    // <<<

    // UI
    private TabLayout tabLayout;
    private Button btnPickMedia_reel, btnPublishNow_reel;
    private Button btnPickMedia_post, btnPublishNow_post;
    private EditText etCaption_reel, etCaption_post;
    private RecyclerView rvPreview_reel, rvPreview_post;

    // data
    private final List<Uri> selectedUris = new ArrayList<>();
    private final List<String> selectedNames = new ArrayList<>();
    private final OkHttpClient http = new OkHttpClient();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle saved) {
        View v = inflater.inflate(R.layout.fragment_ig_publisher_tabs, parent, false);

        tabLayout = v.findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText("Reel"));
        tabLayout.addTab(tabLayout.newTab().setText("Post"));

        // Reel views
        btnPickMedia_reel = v.findViewById(R.id.btnPickMedia_reel);
        btnPublishNow_reel = v.findViewById(R.id.btnPublishNow_reel);
        etCaption_reel = v.findViewById(R.id.etCaption_reel);
        rvPreview_reel = v.findViewById(R.id.rvPreview_reel);

        // Post views
        btnPickMedia_post = v.findViewById(R.id.btnPickMedia_post);
        btnPublishNow_post = v.findViewById(R.id.btnPublishNow_post);
        etCaption_post = v.findViewById(R.id.etCaption_post);
        rvPreview_post = v.findViewById(R.id.rvPreview_post);

        // set up preview recyclers
        rvPreview_reel.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvPreview_post.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        PreviewAdapter previewAdapter = new PreviewAdapter();

        rvPreview_reel.setAdapter(previewAdapter);
        rvPreview_post.setAdapter(previewAdapter);

        // pick handlers (each respects current tab)
        btnPickMedia_reel.setOnClickListener(ch -> pickMedia(true));
        btnPickMedia_post.setOnClickListener(ch -> pickMedia(false));

        // publish now
        btnPublishNow_reel.setOnClickListener(ch -> publishNow(true));
        btnPublishNow_post.setOnClickListener(ch -> publishNow(false));

        return v;
    }

    // ---------------- pick media ----------------
    // if isReel=true -> only pick video (single)
    private void pickMedia(boolean isReel) {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, isReel ? false : true);
        if (isReel) i.setType("video/*");
        else i.setType("image/*");
        startActivityForResult(i, REQUEST_PICK_MEDIA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
            // refresh previews
            if (rvPreview_reel.getAdapter() != null) rvPreview_reel.getAdapter().notifyDataSetChanged();
            if (rvPreview_post.getAdapter() != null) rvPreview_post.getAdapter().notifyDataSetChanged();
        }
    }

    private void addPersistUri(Uri u) {
        if (u == null || getContext() == null) return;
        try {
            final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
            getContext().getContentResolver().takePersistableUriPermission(u, takeFlags);
        } catch (Exception ignore) {}
        selectedUris.add(u);
        selectedNames.add(queryName(getContext(), u));
    }

    private static String queryName(Context ctx, Uri uri) {
        String res = null;
        Cursor c = ctx.getContentResolver().query(uri, null, null, null, null);
        try {
            if (c != null && c.moveToFirst()) {
                int idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) res = c.getString(idx);
            }
        } finally { if (c != null) c.close(); }
        return res == null ? "file" : res;
    }

    // ---------------- publish now ----------------
    private void publishNow(boolean asReel) {
        String caption = asReel ? etCaption_reel.getText().toString() : etCaption_post.getText().toString();
        if (selectedUris.isEmpty()) { showMsg("Pick at least one media"); return; }

        // disable both publish buttons while any publish is in progress
        setAllPublishButtonsEnabled(false);

        if (asReel) {
            // only one video allowed
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
                // create reel container using video_url and publish
                createVideoContainerThenPublish(IG_USER_ID, ACCESS_TOKEN, videoUrl, caption, true, new TerminalCallback() {
                    @Override public void onDone(String error) {
                        if (error != null) showMsg("Reel publish error: " + error);
                        setAllPublishButtonsEnabled(true);
                    }
                });
            });
        } else {
            // images (one or many)
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
                    @Override public void onDone(String error) {
                        if (error != null) showMsg("Post publish error: " + error);
                        setAllPublishButtonsEnabled(true);
                    }
                });
            });
        }
    }

    private void setAllPublishButtonsEnabled(boolean enabled) {
        runOnMain(() -> {
            if (btnPublishNow_post != null) btnPublishNow_post.setEnabled(enabled);
            if (btnPublishNow_reel != null) btnPublishNow_reel.setEnabled(enabled);
        });
    }

    // ---------------- upload helpers (images) ----------------
    private interface UploadAllCallback { void onDone(List<String> urls, String err); }

    private void uploadAllImagesToServer(String serverUrl, List<Uri> uris, UploadAllCallback cb) {
        List<String> resultUrls = new ArrayList<>();
        AtomicInteger remaining = new AtomicInteger(uris.size());
        if (uris.isEmpty()) { runOnMain(() -> cb.onDone(null, "No URIs")); return; }
        if (serverUrl.endsWith("/")) serverUrl = serverUrl.substring(0, serverUrl.length() - 1);

        for (Uri u : uris) {
            try {
                InputStream in = getContext().getContentResolver().openInputStream(u);
                if (in == null) { runOnMain(() -> cb.onDone(null, "Cannot open file")); return; }
                byte[] bytes = new byte[in.available()];
                in.read(bytes); in.close();

                String name = queryName(getContext(), u);
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
                    @Override public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                        runOnMain(() -> cb.onDone(null, e.getMessage()));
                    }
                    @Override public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            runOnMain(() -> cb.onDone(null, "HTTP " + response.code()));
                            return;
                        }
                        try {
                            String body = response.body().string();
                            String url = null;
                            try {
                                JSONObject jo = new JSONObject(body);
                                url = jo.optString("url", null);
                            } catch (Exception ignored) {}
                            if (url == null || url.isEmpty()) {
                                url = finalServerUrl + "/uploads/" + urlEncodePathSegment(finalName);
                            }
                            synchronized (resultUrls) { resultUrls.add(url); }
                        } finally {
                            if (remaining.decrementAndGet() == 0) runOnMain(() -> cb.onDone(resultUrls, null));
                        }
                    }
                });
            } catch (Exception ex) {
                runOnMain(() -> cb.onDone(null, ex.getMessage()));
                return;
            }
        }
    }

    // ---------------- upload video helper (single) ----------------
    private interface UploadVideoCallback { void onDone(String videoUrl, String err); }

    private void uploadVideoToServer(String serverUrl, Uri videoUri, UploadVideoCallback cb) {
        if (serverUrl.endsWith("/")) serverUrl = serverUrl.substring(0, serverUrl.length() - 1);
        try {
            InputStream in = getContext().getContentResolver().openInputStream(videoUri);
            if (in == null) { runOnMain(() -> cb.onDone(null, "Cannot open video file")); return; }
            byte[] bytes = new byte[in.available()];
            in.read(bytes); in.close();

            String name = queryName(getContext(), videoUri);
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
                @Override public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                    runOnMain(() -> cb.onDone(null, e.getMessage()));
                }
                @Override public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        runOnMain(() -> cb.onDone(null, "HTTP " + response.code()));
                        return;
                    }
                    try {
                        String body = response.body().string();
                        String url = null;
                        try {
                            JSONObject jo = new JSONObject(body);
                            url = jo.optString("url", null);
                        } catch (Exception ignored) {}
                        if (url == null || url.isEmpty()) {
                            url = finalServerUrl + "/uploads/" + urlEncodePathSegment(finalName);
                        }
                        String finalUrl = url;
                        runOnMain(() -> cb.onDone(finalUrl, null));
                    } catch (Exception ex) {
                        runOnMain(() -> cb.onDone(null, ex.getMessage()));
                    }
                }
            });
        } catch (Exception e) {
            runOnMain(() -> cb.onDone(null, e.getMessage()));
        }
    }

    private String urlEncodePathSegment(String s) {
        try { return java.net.URLEncoder.encode(s, "UTF-8").replace("+", "%20"); }
        catch (Exception e) { return s; }
    }

    // ---------------- create containers & publish (immediate, images) ----------------
    // TerminalCallback invoked when entire publish flow completes (null error = success, non-null = error message)
    private interface TerminalCallback { void onDone(String error); }

    private void createContainersForUrlsThenPublish(String igId, String token, List<String> urls, String caption, boolean asReel, TerminalCallback terminal) {
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
                    // one of the container creations failed -> we treat whole publish as failed
                    runOnMain(() -> {
                        if (terminal != null) terminal.onDone("Create container failed: " + e.getMessage());
                    });
                }
                @Override public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                    try {
                        if (!response.isSuccessful()) {
                            String body = response.body() != null ? response.body().string() : "";
                            runOnMain(() -> { if (terminal != null) terminal.onDone("Create container HTTP error: " + response.code() + " " + body); });
                        } else {
                            JSONObject jo = new JSONObject(response.body().string());
                            String id = jo.optString("id", null);
                            if (!TextUtils.isEmpty(id)) synchronized (creationIds) { creationIds.add(id); }
                        }
                    } catch (Exception ex) {
                        runOnMain(() -> { if (terminal != null) terminal.onDone("Parse error: " + ex.getMessage()); });
                    } finally {
                        if (remaining.decrementAndGet() == 0) {
                            // all container calls finished successfully (or added ids). Proceed.
                            runOnMain(() -> {
                                if (creationIds.isEmpty()) {
                                    if (terminal != null) terminal.onDone("No containers created.");
                                    return;
                                }
                                // publish
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

    // ---------------- create video container (reel) and publish immediately ----------------
    private void createVideoContainerThenPublish(String igId, String token, String videoUrl, String caption, boolean asReel, TerminalCallback terminal) {
        FormBody.Builder fb = new FormBody.Builder()
                .add("video_url", videoUrl)
                .add("caption", caption)
                .add("access_token", token);
        if (asReel) fb.add("media_type", "REELS");
        Request req = new Request.Builder().url("https://graph.instagram.com/" + API_VERSION + "/" + igId + "/media").post(fb.build()).build();
        http.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                runOnMain(() -> { if (terminal != null) terminal.onDone("Create video container failed: " + e.getMessage()); });
            }
            @Override public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) {
                        String body = response.body() != null ? response.body().string() : "";
                        runOnMain(() -> { if (terminal != null) terminal.onDone("Create video container HTTP error: " + response.code() + " " + body); });
                        return;
                    }
                    JSONObject jo = new JSONObject(response.body().string());
                    String id = jo.optString("id", null);
                    if (!TextUtils.isEmpty(id)) {
                        // publish container
                        publishContainer(igId, token, id, (err) -> {
                            if (terminal != null) terminal.onDone(err);
                        });
                    } else {
                        runOnMain(() -> { if (terminal != null) terminal.onDone("Video container returned no id"); });
                    }
                } catch (Exception ex) {
                    runOnMain(() -> { if (terminal != null) terminal.onDone("Parse error video container: " + ex.getMessage()); });
                }
            }
        });
    }

    // ---------------- create carousel & publish helper ----------------
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
            @Override public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                runOnMain(() -> { if (terminal != null) terminal.onDone("Create carousel failed: " + e.getMessage()); });
            }
            @Override public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) {
                        runOnMain(() -> { if (terminal != null) terminal.onDone("Create carousel HTTP error: " + response.code()); });
                        return;
                    }
                    JSONObject jo = new JSONObject(response.body().string());
                    String id = jo.optString("id", null);
                    if (!TextUtils.isEmpty(id)) {
                        // publish the carousel container
                        publishContainer(igId, token, id, (err) -> {
                            if (terminal != null) terminal.onDone(err);
                        });
                    } else {
                        runOnMain(() -> { if (terminal != null) terminal.onDone("Carousel creation returned no id"); });
                    }
                } catch (Exception ex) {
                    runOnMain(() -> { if (terminal != null) terminal.onDone("Parse error: " + ex.getMessage()); });
                }
            }
        });
    }

    // publish a single container and call terminal when done
    private void publishContainer(String igId, String token, String creationId, TerminalCallback terminal) {
        RequestBody fb = new FormBody.Builder().add("creation_id", creationId).add("access_token", token).build();
        Request req = new Request.Builder().url("https://graph.instagram.com/" + API_VERSION + "/" + igId + "/media_publish").post(fb).build();
        http.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                runOnMain(() -> { if (terminal != null) terminal.onDone("Publish failed: " + e.getMessage()); });
            }
            @Override public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    String body = response.body() != null ? response.body().string() : "";
                    runOnMain(() -> { if (terminal != null) terminal.onDone("Publish HTTP error: " + response.code() + " " + body); });
                    return;
                }
                // success
                runOnMain(() -> { if (terminal != null) terminal.onDone(null); });
            }
        });
    }

    // ---------------- preview adapter ----------------
    private class PreviewAdapter extends RecyclerView.Adapter<PreviewVH> {
        @NonNull @Override public PreviewVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView iv = new ImageView(parent.getContext());
            int pad = 8; iv.setPadding(pad,pad,pad,pad);
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(220,220);
            iv.setLayoutParams(lp); iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return new PreviewVH(iv);
        }
        @Override public void onBindViewHolder(@NonNull PreviewVH holder, int position) { holder.img.setImageURI(selectedUris.get(position)); }
        @Override public int getItemCount() { return selectedUris.size(); }
    }
    private static class PreviewVH extends RecyclerView.ViewHolder { ImageView img; PreviewVH(@NonNull View v){ super(v); img=(ImageView)v; } }

    // ---------------- util ----------------
    private void runOnMain(Runnable r) { if (getActivity()!=null) getActivity().runOnUiThread(r); else r.run(); }
    private void showMsg(String s) { runOnMain(() -> { try { Utility.showMessageBox(s, getContext()); } catch (Exception e) { Toast.makeText(getContext(), s, Toast.LENGTH_LONG).show(); } }); }
}
