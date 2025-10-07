package com.example.justagram;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
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

/**
 * IgPublisherFragment.java
 *
 * Single file:
 * - preview up to 10 local images
 * - upload selected images to a Python server (user provides server URL)
 * - create IG containers for uploaded image URLs, handle carousel, publish now or schedule
 * - schedule uses AlarmManager; AlarmReceiver must be declared in AndroidManifest with android:exported="false"
 *
 * All user-visible messages use Utility.showMessageBox(...) executed on the main thread.
 *
 * NOTE: This code uses single-chunk upload to rupload only if you want to upload local files directly to Meta.
 *       But here we upload to our Python server first (so we use image_url creation path).
 */
public class IgPublisherFragment extends Fragment {

    private static final int REQUEST_PICK_MEDIA = 3001;
    private static final int MAX_IMAGES = 10;
    private static final String PREFS = "ig_publisher_prefs";
    private static final String PREF_SCHEDULED = "scheduled_posts";

    private EditText etIgId, etAccessToken, etServerUrl, etCaption;

    private Button btnPickMedia, btnUploadAndPost, btnSchedule, btnPostStory;
    private RecyclerView rvPreview, rvScheduled;

    private final List<Uri> selectedUris = new ArrayList<>();
    private final List<String> selectedNames = new ArrayList<>();
    private final OkHttpClient http = new OkHttpClient();
    private static final String ACCESS_TOKEN = "IGAAS2qCIE595BZAFJ0SmVNaHBUbFFCM0NqOFBOYkdNOHhBdC1PR1hNTHV6ZAEtLZAm5RVTNZAa3lweFdqM0xxNVcwY2xLVlBadFdDUm54QkFBd0Jvdl8zRkJEMFFBNEtMZAkhyX2hfQUtIZAzNnVGdSa2pVYmtoX1I2bkZAxOFZAuOGp6VQZDZD";
    private static final String IG_USER_ID = "17841474853201686";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle saved) {
        View v = inflater.inflate(R.layout.fragment_ig_publisher, parent, false);
        etIgId = v.findViewById(R.id.etIgId);
        etIgId.setText(IG_USER_ID);
        etAccessToken = v.findViewById(R.id.etAccessToken);
        etAccessToken.setText(ACCESS_TOKEN);
        etServerUrl = v.findViewById(R.id.etServerUrl);
        etCaption = v.findViewById(R.id.etCaption);
        btnPickMedia = v.findViewById(R.id.btnPickMedia);
        btnUploadAndPost = v.findViewById(R.id.btnUploadAndPost);
        btnSchedule = v.findViewById(R.id.btnSchedule);
        btnPostStory = v.findViewById(R.id.btnPostStory);
        rvPreview = v.findViewById(R.id.rvPreview);
        rvScheduled = v.findViewById(R.id.rvScheduled);

        btnPickMedia.setOnClickListener(ch -> pickMedia());
        btnUploadAndPost.setOnClickListener(ch -> uploadThenPublish(false, false));
        btnPostStory.setOnClickListener(ch -> uploadThenPublish(true, false));
        btnSchedule.setOnClickListener(ch -> uploadThenPublish(false, true));

        rvPreview.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvPreview.setAdapter(new PreviewAdapter());

        rvScheduled.setLayoutManager(new LinearLayoutManager(getContext()));
        rvScheduled.setAdapter(new ScheduledAdapter(( jsonArrayToList(loadScheduledRaw()))));

        // optional in-app receiver
        if (getContext() != null)
            ContextCompat.registerReceiver(getContext(), new InAppAlarmReceiver(), new IntentFilter(AlarmReceiver.ACTION_PERFORM_POST), ContextCompat.RECEIVER_NOT_EXPORTED);
        return v;
    }

    // ------------------ pick images ------------------
    private void pickMedia() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
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
            rvPreview.getAdapter().notifyDataSetChanged();
        }
    }

    private void addPersistUri(Uri u) {
        if (u == null || getContext() == null) return;
        try {
            // persist permission
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

    // ------------------ upload images to Python server then create containers ------------------
    private void uploadThenPublish(boolean asStory, boolean schedule) {
        String igId = etIgId.getText().toString().trim();
        String token = etAccessToken.getText().toString().trim();
        String serverUrl = "https://catechistical-questingly-na.ngrok-free.dev";
        String caption = etCaption.getText().toString();

        if (TextUtils.isEmpty(igId) || TextUtils.isEmpty(token) ) {
            showMsg("Nhập IG ID, Access Token và Upload server URL (ví dụ https://abcd.ngrok.io)");
            return;
        }
        if (selectedUris.isEmpty()) {
            showMsg("Chọn ít nhất 1 ảnh để upload");
            return;
        }

        // 1) upload all images to server -> get list of public urls
        uploadAllImagesToServer(serverUrl, selectedUris, (urls, err) -> {
            if (err != null) { showMsg("Upload server error: " + err); return; }
            if (urls == null || urls.isEmpty()) { showMsg("Server không trả về URL nào"); return; }
            // 2) create IG containers for each url (is_carousel_item=true)
            createContainersForUrlsThenPublishOrSchedule(igId, token, urls, caption, asStory, schedule);
        });
    }

    // Upload all images (multipart) sequentially to keep things simple
    private interface UploadAllCallback { void onDone(List<String> urls, String err); }

    private void uploadAllImagesToServer(String serverUrl, List<Uri> uris, UploadAllCallback cb) {
        List<String> resultUrls = new ArrayList<>();
        AtomicInteger remaining = new AtomicInteger(uris.size());

        // normalize serverUrl: remove trailing slash if present
        if (serverUrl.endsWith("/")) serverUrl = serverUrl.substring(0, serverUrl.length() - 1);

        for (Uri u : uris) {
            try {
                InputStream in = getContext().getContentResolver().openInputStream(u);
                if (in == null) { runOnMain(() -> cb.onDone(null, "Cannot open file")); return; }
                byte[] bytes = new byte[in.available()];
                in.read(bytes); in.close();

                String name = queryName(getContext(), u);
                if (name == null || name.isEmpty()) name = "file_" + System.currentTimeMillis();

                // Build multipart body
                RequestBody fileBody = RequestBody.create(bytes, MediaType.parse("application/octet-stream"));
                RequestBody rb = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", name, fileBody)
                        .build();

                Request req = new Request.Builder()
                        .url(serverUrl + "/upload")
                        .post(rb)
                        .build();

                String finalServerUrl = serverUrl;
                String finalName = name;
                http.newCall(req).enqueue(new Callback() {
                    @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        // if any upload fails, report error immediately on main thread
                        runOnMain(() -> cb.onDone(null, e.getMessage()));
                    }
                    @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            runOnMain(() -> cb.onDone(null, "HTTP " + response.code()));
                            return;
                        }
                        // We assume server saved the file as uploads/<filename>
                        // Construct public URL deterministically:
                        // Example: serverUrl = https://abcd.ngrok.io => public url = https://abcd.ngrok.io/uploads/<filename>
                        String publicUrl = finalServerUrl + "/uploads/" + urlEncodePathSegment(finalName);
                        synchronized (resultUrls) { resultUrls.add(publicUrl); }

                        if (remaining.decrementAndGet() == 0) {
                            // All done: return list on main thread
                            runOnMain(() -> cb.onDone(resultUrls, null));
                        }
                    }
                });

            } catch (Exception ex) {
                runOnMain(() -> cb.onDone(null, ex.getMessage()));
                return;
            }
        }
    }
    private String urlEncodePathSegment(String s) {
        try {
            // encode but keep slashes if any (filenames shouldn't have slashes)
            return java.net.URLEncoder.encode(s, "UTF-8").replace("+", "%20");
        } catch (Exception e) {
            return s;
        }
    }
    // Create containers for remote image URLs (image_url) then publish or schedule
    private void createContainersForUrlsThenPublishOrSchedule(String igId, String token, List<String> urls, String caption, boolean asStory, boolean schedule) {
        List<String> creationIds = new ArrayList<>();
        AtomicInteger remaining = new AtomicInteger(urls.size());
        for (String u : urls) {
            FormBody.Builder fb = new FormBody.Builder()
                    .add("image_url", u)
                    .add("caption", caption)
                    .add("access_token", token);
            if (urls.size() > 1)
            {
                fb.add("is_carousel_item", "true");
            }
            if (asStory) fb.add("media_type", "STORIES");
            Request req = new Request.Builder()
                    .url("https://graph.instagram.com/v23.0/" + etIgId.getText().toString().trim() + "/media")
                    .post(fb.build())
                    .build();

            http.newCall(req).enqueue(new Callback() {
                @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnMain(() -> showMsg("Create container failed: " + e.getMessage()));
                    if (remaining.decrementAndGet() == 0) proceedAfterContainers(creationIds, igId, token, caption, schedule);
                }
                @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        runOnMain(() -> {
                            try {
                                showMsg("Create container HTTP error: " + response.body().string() + " : " + response.code());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                        if (remaining.decrementAndGet() == 0) proceedAfterContainers(creationIds, igId, token, caption, schedule);
                        return;
                    }
                    try {
                        JSONObject jo = new JSONObject(response.body().string());
                        String id = jo.optString("id");
                        if (!TextUtils.isEmpty(id)) synchronized (creationIds) { creationIds.add(id); }
                    } catch (Exception ex) {
                        runOnMain(() -> showMsg("Parse error: " + ex.getMessage()));
                    } finally {
                        if (remaining.decrementAndGet() == 0) proceedAfterContainers(creationIds, igId, token, caption, schedule);
                    }
                }
            });
        }
    }

    private void proceedAfterContainers(List<String> creationIds, String igId, String token, String caption, boolean schedule) {
        if (creationIds.isEmpty()) { showMsg("Không có container nào được tạo"); return; }
        if (schedule) {
            // schedule: save creation_ids and set Alarm
            scheduleWithCreationIds(igId, token, caption, creationIds);
        } else {
            if (creationIds.size() == 1) publishContainer(igId, token, creationIds.get(0));
            else createCarouselAndPublish(igId, token, creationIds, caption);
        }
    }

    private void createCarouselAndPublish(String igId, String token, List<String> children, String caption) {
        String childrenCsv = TextUtils.join(",", children);
        FormBody fb = new FormBody.Builder()
                .add("caption", caption)
                .add("media_type", "CAROUSEL")
                .add("children", childrenCsv)
                .add("access_token", token)
                .build();
        Request req = new Request.Builder().url("https://graph.instagram.com/v23.0/" + igId + "/media").post(fb).build();
        http.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnMain(() -> showMsg("Create carousel failed: " + e.getMessage()));
            }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) { runOnMain(() -> {
                    try {
                        showMsg("Create carousel HTTP error: " + response.body().string());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }); return; }
                try {
                    JSONObject jo = new JSONObject(response.body().string());
                    String id = jo.optString("id");
                    if (TextUtils.isEmpty(id)) { runOnMain(() -> showMsg("Carousel creation returned no id")); return; }
                    publishContainer(igId, token, id);
                } catch (Exception ex) { runOnMain(() -> showMsg("Parse error: " + ex.getMessage())); }
            }
        });
    }

    private void publishContainer(String igId, String token, String creationId) {
        FormBody fb = new FormBody.Builder()
                .add("creation_id", creationId)
                .add("access_token", token)
                .build();
        Request req = new Request.Builder().url("https://graph.instagram.com/v23.0/" + igId + "/media_publish").post(fb).build();
        http.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnMain(() -> showMsg("Publish failed: " + e.getMessage()));
            }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) { runOnMain(() -> {
                    try {
                        showMsg("Publish HTTP error: " + response.body().string() + ":" + response.code());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }); return; }
                String s = response.body().string();
                runOnMain(() -> showMsg("Đăng thành công: " + s));
            }
        });
    }

    // ------------------ Scheduling with creation_ids ------------------
    private void scheduleWithCreationIds(String igId, String token, String caption, List<String> creationIds) {
        try {
            long when = System.currentTimeMillis() + 60_000; // demo +60s; replace with pickers
            JSONObject job = new JSONObject();
            job.put("igId", igId);
            job.put("token", token);
            job.put("caption", caption);
            job.put("timeMillis", when);
            JSONArray arr = new JSONArray();
            for (String s : creationIds) arr.put(s);
            job.put("creation_ids", arr);
            String uid = "sched_" + System.currentTimeMillis();
            job.put("id", uid);

            JSONArray saved = loadScheduledRaw();
            saved.put(job);
            saveScheduledRaw(saved);
            runOnMain(() -> rvScheduled.setAdapter(new ScheduledAdapter(jsonArrayToList(saved)) ));

            Intent i = new Intent(getContext(), AlarmReceiver.class);
            i.setAction(AlarmReceiver.ACTION_PERFORM_POST);
            i.putExtra("payload", job.toString());
            PendingIntent pi = PendingIntent.getBroadcast(getContext(), uid.hashCode(), i, PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT>=23?PendingIntent.FLAG_IMMUTABLE:0));
            AlarmManager am = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
            if (am != null) am.setExact(AlarmManager.RTC_WAKEUP, when, pi);
            runOnMain(() -> showMsg("Đã hẹn giờ publish tại: " + when));
        } catch (Exception ex) {
            runOnMain(() -> showMsg("Schedule error: " + ex.getMessage()));
        }
    }

    // ------------------ AlarmReceiver ------------------
    public static class AlarmReceiver extends android.content.BroadcastReceiver {
        public static final String ACTION_PERFORM_POST = "com.example.igpublisher.ACTION_PERFORM_POST";
        @Override public void onReceive(Context context, Intent intent) {
            if (!ACTION_PERFORM_POST.equals(intent.getAction())) return;
            String payload = intent.getStringExtra("payload");
            if (payload == null) return;
            new Thread(() -> {
                try {
                    JSONObject job = new JSONObject(payload);
                    String igId = job.optString("igId");
                    String token = job.optString("token");
                    JSONArray ids = job.optJSONArray("creation_ids");
                    if (ids == null) return;
                    OkHttpClient client = new OkHttpClient();
                    for (int i = 0; i < ids.length(); ++i) {
                        String creationId = ids.getString(i);
                        FormBody fb = new FormBody.Builder()
                                .add("creation_id", creationId)
                                .add("access_token", token).build();
                        Request req = new Request.Builder().url("https://graph.instagram.com/v23.0/" + igId + "/media_publish").post(fb).build();
                        Response r = client.newCall(req).execute();
                        // no UI here; production: use Notification to inform user
                    }
                } catch (Exception ignored) {}
            }).start();
        }
    }

    private class InAppAlarmReceiver extends android.content.BroadcastReceiver {
        @Override public void onReceive(Context context, Intent intent) {
            AlarmReceiver ar = new AlarmReceiver();
            ar.onReceive(context, intent);
        }
    }

    // ------------------ Scheduled storage ------------------
    private JSONArray loadScheduledRaw() {
        SharedPreferences p = getContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String s = p.getString(PREF_SCHEDULED, "[]");
        try { return new JSONArray(s); } catch (JSONException e) { return new JSONArray(); }
    }
    private List<JSONObject> jsonArrayToList(JSONArray arr) {
        List<JSONObject> list = new ArrayList<>();
        for (int i = 0; i < arr.length(); ++i) try { list.add(arr.getJSONObject(i)); } catch (JSONException ignored) {}
        return list;
    }
    private void saveScheduledRaw(JSONArray a) {
        SharedPreferences p = getContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        p.edit().putString(PREF_SCHEDULED, a.toString()).apply();
    }

    // ------------------ Preview adapter (shows thumbnails via ImageView using URI) ------------------
    private class PreviewAdapter extends RecyclerView.Adapter<PreviewViewHolder> {
        @NonNull @Override public PreviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView iv = new ImageView(parent.getContext());
            int pad = 8;
            iv.setPadding(pad, pad, pad, pad);
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(220, 220);
            iv.setLayoutParams(lp);
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return new PreviewViewHolder(iv);
        }
        @Override public void onBindViewHolder(@NonNull PreviewViewHolder holder, int position) {
            Uri u = selectedUris.get(position);
            holder.img.setImageURI(u);
        }
        @Override public int getItemCount() { return selectedUris.size(); }
    }
    private static class PreviewViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        PreviewViewHolder(@NonNull View v) { super(v); img = (ImageView)v; }
    }

    // ------------------ Scheduled adapter ------------------
    private class ScheduledAdapter extends RecyclerView.Adapter<ScheduledVH> {
        private final List<JSONObject> items;
        ScheduledAdapter(List<JSONObject> items) { this.items = items; }
        @NonNull @Override public ScheduledVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            EditText t = new EditText(parent.getContext());
            t.setEnabled(false);
            t.setPadding(12,12,12,12);
            return new ScheduledVH(t);
        }
        @Override public void onBindViewHolder(@NonNull ScheduledVH holder, int position) { holder.bind(items.get(position)); }
        @Override public int getItemCount() { return items.size(); }
    }
    private static class ScheduledVH extends RecyclerView.ViewHolder {
        EditText tv;
        ScheduledVH(@NonNull View v) { super(v); tv = (EditText)v; }
        void bind(JSONObject j) {
            long t = j.optLong("timeMillis");
            JSONArray arr = j.optJSONArray("creation_ids");
            tv.setText("At: " + t + "\nIds: " + (arr==null?"[]":arr.toString()));
        }
    }

    // ------------------ Helpers ------------------
    private void runOnMain(Runnable r) {
        if (getActivity() != null) getActivity().runOnUiThread(r); else r.run();
    }
    private void showMsg(String s) {
        runOnMain(() -> {
            try { Utility.showMessageBox(s, getContext()); } catch (Exception e) { Toast.makeText(getContext(), s, Toast.LENGTH_LONG).show(); }
        });
    }
}
