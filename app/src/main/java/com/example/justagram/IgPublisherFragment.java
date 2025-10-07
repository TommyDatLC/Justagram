package com.example.justagram;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
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
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
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
 * IgPublisherFragment (single-file)
 *
 * Key behaviors:
 * - Post tab: upload images to SERVER_URL, create containers now, publish (supports carousel).
 * - Reel tab: only accept video (single). Upload video to SERVER_URL, receive video_url, create REELS container and publish.
 * - Schedule: upload local files to server now (to guarantee public URLs exist), but DO NOT create IG containers until the scheduled time.
 *   The scheduled job stores media_urls + is_reel flag; when alarm fires PublishService will create containers and publish.
 *
 * Important: resumable direct upload to Meta (rupload.facebook.com) for large videos is not implemented here.
 */
public class IgPublisherFragment extends Fragment {

    private static final int REQUEST_PICK_MEDIA = 4001;
    private static final int MAX_IMAGES = 10;
    private static final String PREFS = "ig_publisher_prefs";
    private static final String PREF_SCHEDULED = "scheduled_posts";

    // >>> CONFIG CONSTANTS (not user input)
    private static final String SERVER_URL = "https://catechistical-questingly-na.ngrok-free.dev"; // your upload server
    private static final String ACCESS_TOKEN = "IGAAS2qCIE595BZAFJ0SmVNaHBUbFFCM0NqOFBOYkdNOHhBdC1PR1hNTHV6ZAEtLZAm5RVTNZAa3lweFdqM0xxNVcwY2xLVlBadFdDUm54QkFBd0Jvdl8zRkJEMFFBNEtMZAkhyX2hfQUtIZAzNnVGdSa2pVYmtoX1I2bkZAxOFZAuOGp6VQZDZD"; // replace
    private static final String IG_USER_ID = "17841474853201686"; // replace
    private static final String API_VERSION = "v23.0";
    // <<<

    // UI
    private TabLayout tabLayout;
    private Button btnPickMedia_reel, btnPublishNow_reel, btnSchedule_reel;
    private Button btnPickMedia_post, btnPublishNow_post, btnSchedule_post;
    private EditText etCaption_reel, etCaption_post;
    private RecyclerView rvPreview_reel, rvPreview_post, rvScheduled;

    // data
    private final List<Uri> selectedUris = new ArrayList<>();
    private final List<String> selectedNames = new ArrayList<>();
    private final OkHttpClient http = new OkHttpClient();

    // request code constant for exact alarm request (optional)
    private static final int REQ_CODE_REQUEST_EXACT_ALARM = 12345;

    /** ensure permission to set exact alarms on Android S+ */
    private boolean ensureCanScheduleExactAlarms() {
        AlarmManager am = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        if (am != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (am.canScheduleExactAlarms()) return true;
            try {
                Intent i = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                i.setData(Uri.parse("package:" + getContext().getPackageName()));
                startActivityForResult(i, REQ_CODE_REQUEST_EXACT_ALARM);
            } catch (ActivityNotFoundException ex) {
                showMsg("Please enable 'Alarms & reminders' for this app in system settings.");
            }
            return false;
        }
        return true;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle saved) {
        View v = inflater.inflate(R.layout.fragment_ig_publisher_tabs, parent, false);

        // check exact alarm permission early (non-blocking)
        ensureCanScheduleExactAlarms();

        tabLayout = v.findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText("Reel"));
        tabLayout.addTab(tabLayout.newTab().setText("Post"));

        // Reel views
        btnPickMedia_reel = v.findViewById(R.id.btnPickMedia_reel);
        btnPublishNow_reel = v.findViewById(R.id.btnPublishNow_reel);
        btnSchedule_reel = v.findViewById(R.id.btnSchedule_reel);
        etCaption_reel = v.findViewById(R.id.etCaption_reel);
        rvPreview_reel = v.findViewById(R.id.rvPreview_reel);

        // Post views
        btnPickMedia_post = v.findViewById(R.id.btnPickMedia_post);
        btnPublishNow_post = v.findViewById(R.id.btnPublishNow_post);
        btnSchedule_post = v.findViewById(R.id.btnSchedule_post);
        etCaption_post = v.findViewById(R.id.etCaption_post);
        rvPreview_post = v.findViewById(R.id.rvPreview_post);

        rvScheduled = v.findViewById(R.id.rvScheduled);

        // Common adapter for preview (same list)
        rvPreview_reel.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvPreview_post.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        PreviewAdapter previewAdapter = new PreviewAdapter();
        rvPreview_reel.setAdapter(previewAdapter);
        rvPreview_post.setAdapter(previewAdapter);

        rvScheduled.setLayoutManager(new LinearLayoutManager(getContext()));
        rvScheduled.setAdapter(new ScheduledAdapter(loadScheduled()));

        // pick handlers (each respects current tab)
        btnPickMedia_reel.setOnClickListener(ch -> pickMedia(true));
        btnPickMedia_post.setOnClickListener(ch -> pickMedia(false));

        // publish now
        btnPublishNow_reel.setOnClickListener(ch -> publishNow(true));
        btnPublishNow_post.setOnClickListener(ch -> publishNow(false));

        // schedule
        btnSchedule_reel.setOnClickListener(ch -> scheduleFlow(true));
        btnSchedule_post.setOnClickListener(ch -> scheduleFlow(false));

        // register in-app receiver for Alarm (optional)
        if (getContext() != null) {
            try {
                ContextCompat.registerReceiver(getContext(), new InAppAlarmReceiver(),
                        new IntentFilter(AlarmReceiver.ACTION_PERFORM_POST),
                        ContextCompat.RECEIVER_NOT_EXPORTED);
            } catch (Exception ignored) {}
        }

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
            // refresh both previews
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

        if (asReel) {
            // only one video allowed
            if (selectedUris.size() > 1) { showMsg("Reel accepts only one video. Please pick a single video."); return; }
            Uri videoUri = selectedUris.get(0);
            uploadVideoToServer(SERVER_URL, videoUri, (videoUrl, err) -> {
                if (err != null) { showMsg("Video upload error: " + err); return; }
                // create reel container using video_url
                createVideoContainerThenPublish(IG_USER_ID, ACCESS_TOKEN, videoUrl, caption, true);
            });
        } else {
            // images (one or many)
            uploadAllImagesToServer(SERVER_URL, selectedUris, (urls, err) -> {
                if (err != null) { showMsg("Upload server error: " + err); return; }
                if (urls == null || urls.isEmpty()) { showMsg("No URLs returned by server"); return; }
                createContainersForUrlsThenPublish(IG_USER_ID, ACCESS_TOKEN, urls, caption, false);
            });
        }
    }

    // ---------------- schedule flow ----------------
    private void scheduleFlow(boolean asReel) {
        if (selectedUris.isEmpty()) { showMsg("Pick media before scheduling"); return; }

        final Calendar now = Calendar.getInstance();
        DatePickerDialog dp = new DatePickerDialog(getContext(), (DatePicker view, int year, int month, int dayOfMonth) -> {
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, dayOfMonth);
            // after date, pick time (hour + minute)
            TimePickerDialog tp = new TimePickerDialog(getContext(), (TimePicker timePicker, int hourOfDay, int minute) -> {
                cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                cal.set(Calendar.MINUTE, minute);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);

                long chosen = cal.getTimeInMillis();
                long nowMs = System.currentTimeMillis();
                long minAllowed = nowMs + 60_000; // 1 minute
                long maxAllowed = nowMs + 5L * 365 * 24 * 60 * 60 * 1000; // ~5 years

                if (chosen < minAllowed) {
                    showMsg("Chosen time invalid: scheduling must be at least 1 minute from now.");
                    return;
                }
                if (chosen > maxAllowed) {
                    showMsg("Chosen time invalid: scheduling too far in the future.");
                    return;
                }

                String caption = asReel ? etCaption_reel.getText().toString() : etCaption_post.getText().toString();

                // For scheduling: first upload media to server now to ensure public urls exist, but DO NOT create IG containers yet.
                if (asReel) {
                    // upload single video now -> get video_url and schedule with media_urls + is_reel=true
                    if (selectedUris.size() > 1) { showMsg("Reel accepts only one video. Please pick a single video."); return; }
                    uploadVideoToServer(SERVER_URL, selectedUris.get(0), (videoUrl, err) -> {
                        if (err != null) { showMsg("Upload server error: " + err); return; }
                        List<String> mediaUrls = new ArrayList<>(); mediaUrls.add(videoUrl);
                        schedulePublishJobWithMediaUrls(IG_USER_ID, ACCESS_TOKEN, mediaUrls, caption, true, chosen);
                    });
                } else {
                    // upload images, schedule with media_urls
                    uploadAllImagesToServer(SERVER_URL, selectedUris, (urls, err) -> {
                        if (err != null) { showMsg("Upload server error: " + err); return; }
                        if (urls == null || urls.isEmpty()) { showMsg("No URLs returned by server"); return; }
                        schedulePublishJobWithMediaUrls(IG_USER_ID, ACCESS_TOKEN, urls, caption, false, chosen);
                    });
                }

            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true);
            tp.setTitle("Pick hour and minute");
            tp.show();
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        dp.setTitle("Pick date");
        dp.show();
    }

    // ---------------- upload helpers (images) ----------------
    private interface UploadAllCallback { void onDone(List<String> urls, String err); }

    private void uploadAllImagesToServer(String serverUrl, List<Uri> uris, UploadAllCallback cb) {
        List<String> resultUrls = new ArrayList<>();
        AtomicInteger remaining = new AtomicInteger(uris.size());
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
                    @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        runOnMain(() -> cb.onDone(null, e.getMessage()));
                    }
                    @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
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
                @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnMain(() -> cb.onDone(null, e.getMessage()));
                }
                @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
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
    private void createContainersForUrlsThenPublish(String igId, String token, List<String> urls, String caption, boolean asReel) {
        List<String> creationIds = new ArrayList<>();
        AtomicInteger remaining = new AtomicInteger(urls.size());
        for (String u : urls) {
            FormBody.Builder fb = new FormBody.Builder()
                    .add("image_url", u)
                    .add("caption", caption)
                    .add("access_token", token);
            if (urls.size() > 1) fb.add("is_carousel_item", "true");
            Request req = new Request.Builder().url("https://graph.instagram.com/" + API_VERSION + "/" + igId + "/media").post(fb.build()).build();
            http.newCall(req).enqueue(new Callback() {
                @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnMain(() -> showMsg("Create container failed: " + e.getMessage()));
                    if (remaining.decrementAndGet() == 0) { /* nothing */ }
                }
                @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    try {
                        if (!response.isSuccessful()) {
                            String body = response.body() != null ? response.body().string() : "";
                            runOnMain(() -> showMsg("Create container HTTP error: " + response.code() + " " + body));
                        } else {
                            JSONObject jo = new JSONObject(response.body().string());
                            String id = jo.optString("id", null);
                            if (!TextUtils.isEmpty(id)) synchronized (creationIds) { creationIds.add(id); }
                        }
                    } catch (Exception ex) {
                        runOnMain(() -> showMsg("Parse error: " + ex.getMessage()));
                    } finally {
                        if (remaining.decrementAndGet() == 0) {
                            if (creationIds.isEmpty()) { runOnMain(() -> showMsg("No containers created.")); return; }
                            if (creationIds.size() == 1) publishContainer(igId, token, creationIds.get(0));
                            else createCarouselAndPublish(igId, token, creationIds, caption);
                        }
                    }
                }
            });
        }
    }

    // ---------------- create video container (reel) and publish immediately ----------------
    private void createVideoContainerThenPublish(String igId, String token, String videoUrl, String caption, boolean asReel) {
        // media_type=REELS for reel, or MEDIA_TYPE other for plain video if needed
        FormBody.Builder fb = new FormBody.Builder()
                .add("video_url", videoUrl)
                .add("caption", caption)
                .add("access_token", token);
        if (asReel) fb.add("media_type", "REELS");
        Request req = new Request.Builder().url("https://graph.instagram.com/" + API_VERSION + "/" + igId + "/media").post(fb.build()).build();
        http.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) { runOnMain(() -> showMsg("Create video container failed: " + e.getMessage())); }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) {
                        String body = response.body() != null ? response.body().string() : "";
                        runOnMain(() -> showMsg("Create video container HTTP error: " + response.code() + " " + body));
                        return;
                    }
                    JSONObject jo = new JSONObject(response.body().string());
                    String id = jo.optString("id", null);
                    if (!TextUtils.isEmpty(id)) publishContainer(igId, token, id);
                    else runOnMain(() -> showMsg("Video container returned no id"));
                } catch (Exception ex) { runOnMain(() -> showMsg("Parse error video container: " + ex.getMessage())); }
            }
        });
    }

    // ---------------- create carousel & publish helper ----------------
    private void createCarouselAndPublish(String igId, String token, List<String> children, String caption) {
        String childrenCsv = TextUtils.join(",", children);
        FormBody fb = new FormBody.Builder()
                .add("caption", caption)
                .add("media_type", "CAROUSEL")
                .add("children", childrenCsv)
                .add("access_token", token)
                .build();
        Request req = new Request.Builder().url("https://graph.instagram.com/" + API_VERSION + "/" + igId + "/media").post(fb).build();
        http.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) { runOnMain(() -> showMsg("Create carousel failed: " + e.getMessage())); }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) { runOnMain(() -> showMsg("Create carousel HTTP error: " + response.code())); return; }
                    JSONObject jo = new JSONObject(response.body().string());
                    String id = jo.optString("id", null);
                    if (!TextUtils.isEmpty(id)) publishContainer(igId, token, id);
                    else runOnMain(() -> showMsg("Carousel creation returned no id"));
                } catch (Exception ex) { runOnMain(() -> showMsg("Parse error: " + ex.getMessage())); }
            }
        });
    }

    private void publishContainer(String igId, String token, String creationId) {
        FormBody fb = new FormBody.Builder().add("creation_id", creationId).add("access_token", token).build();
        Request req = new Request.Builder().url("https://graph.instagram.com/" + API_VERSION + "/" + igId + "/media_publish").post(fb).build();
        http.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) { runOnMain(() -> showMsg("Publish failed: " + e.getMessage())); }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    String body = response.body() != null ? response.body().string() : "";
                    runOnMain(() -> showMsg("Publish HTTP error: " + response.code() + " " + body));
                    return;
                }
                String s = response.body().string();
                runOnMain(() -> showMsg("Published: " + s));
            }
        });
    }

    // ---------------- schedule storing function (store media_urls NOT creation_ids) ----------------
    private void schedulePublishJobWithMediaUrls(String igId, String token, List<String> mediaUrls, String caption, boolean isReel, long whenMillis) {
        try {
            JSONObject job = new JSONObject();
            job.put("igId", igId);
            job.put("token", token);
            job.put("timeMillis", whenMillis);
            job.put("is_reel", isReel);
            job.put("caption", caption == null ? "" : caption);
            JSONArray ja = new JSONArray();
            for (String s : mediaUrls) ja.put(s);
            job.put("media_urls", ja);
            String uid = "sched_" + System.currentTimeMillis();
            job.put("id", uid);

            JSONArray saved = loadScheduledRaw();
            saved.put(job);
            saveScheduledRaw(saved);
            runOnMain(() -> rvScheduled.setAdapter(new ScheduledAdapter(jsonArrayToList(saved))));

            Intent i = new Intent(getContext(), AlarmReceiver.class);
            i.setAction(AlarmReceiver.ACTION_PERFORM_POST);
            i.putExtra("payload", job.toString());
            PendingIntent pi = PendingIntent.getBroadcast(getContext(), uid.hashCode(), i, PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT>=23?PendingIntent.FLAG_IMMUTABLE:0));
            AlarmManager am = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);

            // prefer exact alarm if available
            if (am != null) {
                if (ensureCanScheduleExactAlarms()) {
                    am.setExact(AlarmManager.RTC_WAKEUP, whenMillis, pi);
                } else {
                    am.set(AlarmManager.RTC_WAKEUP, whenMillis, pi);
                }
            }
            runOnMain(() -> showMsg("Scheduled publish at " + whenMillis));
        } catch (Exception ex) {
            runOnMain(() -> showMsg("Schedule error: " + ex.getMessage()));
        }
    }

    // ---------------- scheduled storage ----------------
    private JSONArray loadScheduledRaw() {
        SharedPreferences p = getContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String s = p.getString(PREF_SCHEDULED, "[]");
        try { return new JSONArray(s); } catch (JSONException e) { return new JSONArray(); }
    }
    private void saveScheduledRaw(JSONArray a) {
        SharedPreferences p = getContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        p.edit().putString(PREF_SCHEDULED, a.toString()).apply();
    }
    private List<JSONObject> loadScheduled() { return jsonArrayToList(loadScheduledRaw()); }
    private List<JSONObject> jsonArrayToList(JSONArray arr) {
        List<JSONObject> list = new ArrayList<>();
        for (int i = 0; i < arr.length(); ++i) try { list.add(arr.getJSONObject(i)); } catch (JSONException ignored) {}
        return list;
    }

    // ---------------- AlarmReceiver -> starts PublishService ----------------
    public static class AlarmReceiver extends android.content.BroadcastReceiver {
        public static final String ACTION_PERFORM_POST = "com.example.justagram.ACTION_PERFORM_POST";
        @Override public void onReceive(Context context, Intent intent) {
            if (!ACTION_PERFORM_POST.equals(intent.getAction())) return;
            String payload = intent.getStringExtra("payload");
            if (payload == null) return;
            Intent svc = new Intent(context, PublishService.class);
            svc.putExtra("payload", payload);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(svc);
            } else {
                context.startService(svc);
            }
        }
    }

    // ---------------- PublishService: creates containers at runtime and publishes ----------------
    public static class PublishService extends Service {
        private OkHttpClient client = new OkHttpClient();
        @Override public int onStartCommand(Intent intent, int flags, int startId) {
            final String payload = intent.getStringExtra("payload");
            new Thread(() -> {
                try {
                    JSONObject job = new JSONObject(payload);
                    String igId = job.optString("igId");
                    String token = job.optString("token");
                    boolean isReel = job.optBoolean("is_reel", false);
                    String caption = job.optString("caption", "");
                    JSONArray mediaUrls = job.optJSONArray("media_urls");
                    if (mediaUrls == null || mediaUrls.length() == 0) return;

                    List<String> mediaList = new ArrayList<>();
                    for (int i = 0; i < mediaUrls.length(); ++i) mediaList.add(mediaUrls.getString(i));

                    // Create containers for each media url (image_url or video_url depending on isReel)
                    List<String> creationIds = new ArrayList<>();
                    for (String mediaUrl : mediaList) {
                        FormBody.Builder fb = new FormBody.Builder().add("access_token", token).add("caption", caption);
                        Request.Builder reqBuilder;
                        if (isReel) {
                            fb.add("video_url", mediaUrl);
                            fb.add("media_type", "REELS");
                        } else {
                            fb.add("image_url", mediaUrl);
                            if (mediaList.size() > 1) fb.add("is_carousel_item", "true");
                        }
                        Request req = new Request.Builder()
                                .url("https://graph.instagram.com/" + API_VERSION + "/" + igId + "/media")
                                .post(fb.build())
                                .build();
                        Response resp = client.newCall(req).execute();
                        if (!resp.isSuccessful()) {
                            // log and continue
                            // you could notify via Notification; here we just skip
                            continue;
                        }
                        JSONObject jo = new JSONObject(resp.body().string());
                        String id = jo.optString("id", null);
                        if (id != null) creationIds.add(id);
                    }

                    if (creationIds.isEmpty()) return;

                    if (!isReel) {
                        if (creationIds.size() == 1) {
                            // publish single
                            RequestBody pb = new FormBody.Builder().add("creation_id", creationIds.get(0)).add("access_token", token).build();
                            Request pr = new Request.Builder().url("https://graph.instagram.com/" + API_VERSION + "/" + igId + "/media_publish").post(pb).build();
                            client.newCall(pr).execute();
                        } else {
                            // create carousel
                            String childrenCsv = TextUtils.join(",", creationIds);
                            RequestBody fb2 = new FormBody.Builder()
                                    .add("caption", caption)
                                    .add("media_type", "CAROUSEL")
                                    .add("children", childrenCsv)
                                    .add("access_token", token)
                                    .build();
                            Request req2 = new Request.Builder().url("https://graph.instagram.com/" + API_VERSION + "/" + igId + "/media").post(fb2).build();
                            Response r2 = client.newCall(req2).execute();
                            if (!r2.isSuccessful()) return;
                            JSONObject jo2 = new JSONObject(r2.body().string());
                            String carouselId = jo2.optString("id", null);
                            if (carouselId != null) {
                                RequestBody pb = new FormBody.Builder().add("creation_id", carouselId).add("access_token", token).build();
                                Request pr = new Request.Builder().url("https://graph.instagram.com/" + API_VERSION + "/" + igId + "/media_publish").post(pb).build();
                                client.newCall(pr).execute();
                            }
                        }
                    } else {
                        // Reels: assume single video container or multiple video containers (rare). Publish each container (Meta expects publish on container)
                        for (String cid : creationIds) {
                            RequestBody pb = new FormBody.Builder().add("creation_id", cid).add("access_token", token).build();
                            Request pr = new Request.Builder().url("https://graph.instagram.com/" + API_VERSION + "/" + igId + "/media_publish").post(pb).build();
                            client.newCall(pr).execute();
                        }
                    }

                } catch (Exception ignored) {}
                stopSelf();
            }).start();
            return START_NOT_STICKY;
        }
        @Override public android.os.IBinder onBind(Intent intent) { return null; }
    }

    // In-app receiver to forward when running
    private class InAppAlarmReceiver extends android.content.BroadcastReceiver {
        @Override public void onReceive(Context context, Intent intent) {
            AlarmReceiver ar = new AlarmReceiver();
            ar.onReceive(context, intent);
        }
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

    // ---------------- scheduled adapter (with delete X) ----------------
    private class ScheduledAdapter extends RecyclerView.Adapter<ScheduledVH> {
        private final List<JSONObject> items;
        ScheduledAdapter(List<JSONObject> items){ this.items = items; }
        @NonNull @Override public ScheduledVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LinearLayout container = new LinearLayout(parent.getContext());
            container.setOrientation(LinearLayout.HORIZONTAL);
            container.setPadding(12,12,12,12);
            container.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            TextView tv = new TextView(parent.getContext());
            LinearLayout.LayoutParams tvLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            tv.setLayoutParams(tvLp);
            tv.setTextSize(14);
            Button btn = new Button(parent.getContext());
            btn.setText("X");
            LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            btnLp.gravity = Gravity.CENTER_VERTICAL;
            btn.setLayoutParams(btnLp);
            container.addView(tv);
            container.addView(btn);
            return new ScheduledVH(container, tv, btn);
        }
        @Override public void onBindViewHolder(@NonNull ScheduledVH holder, int position) { holder.bind(items.get(position), position); }
        @Override public int getItemCount() { return items.size(); }
    }
    private class ScheduledVH extends RecyclerView.ViewHolder {
        TextView tv;
        Button del;
        ScheduledVH(@NonNull View v, TextView tv, Button del){ super(v); this.tv = tv; this.del = del; }
        void bind(JSONObject jobObj, int position) {
            try {
                long t = jobObj.optLong("timeMillis");
                String caption = jobObj.optString("caption", "");
                JSONArray urls = jobObj.optJSONArray("media_urls");
                boolean isReel = jobObj.optBoolean("is_reel", false);
                String urlsStr = urls == null ? "[]" : urls.toString();

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                String when = sdf.format(t);
                String info = when + " — " + (isReel ? "Reel" : "Post") + " — Caption: " + (caption.isEmpty() ? "(no caption)" : caption) + "\nURLs: " + urlsStr;
                tv.setText(info);

                del.setOnClickListener(ch -> {
                    String uid = jobObj.optString("id", null);
                    if (uid != null) {
                        try {
                            Intent i = new Intent(getContext(), AlarmReceiver.class);
                            i.setAction(AlarmReceiver.ACTION_PERFORM_POST);
                            i.putExtra("payload", jobObj.toString());
                            PendingIntent pi = PendingIntent.getBroadcast(getContext(), uid.hashCode(), i, PendingIntent.FLAG_NO_CREATE | (Build.VERSION.SDK_INT>=23?PendingIntent.FLAG_IMMUTABLE:0));
                            AlarmManager am = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
                            if (pi != null && am != null) {
                                am.cancel(pi);
                                pi.cancel();
                            }
                        } catch (Exception e) {
                            // ignore cancel errors
                        }
                    }
                    try {
                        JSONArray arr = loadScheduledRaw();
                        JSONArray newArr = new JSONArray();
                        for (int i = 0; i < arr.length(); ++i) {
                            JSONObject o = arr.getJSONObject(i);
                            String id = o.optString("id", null);
                            if (id == null || !id.equals(jobObj.optString("id"))) newArr.put(o);
                        }
                        saveScheduledRaw(newArr);
                        runOnMain(() -> rvScheduled.setAdapter(new ScheduledAdapter(jsonArrayToList(newArr))));
                        showMsg("Schedule removed");
                    } catch (Exception ex) {
                        showMsg("Failed to remove schedule: " + ex.getMessage());
                    }
                });
            } catch (Exception e) {
                tv.setText("Invalid job");
                del.setOnClickListener(null);
            }
        }
    }

    // ---------------- util ----------------
    private void runOnMain(Runnable r) { if (getActivity()!=null) getActivity().runOnUiThread(r); else r.run(); }
    private void showMsg(String s) { runOnMain(() -> { try { Utility.showMessageBox(s, getContext()); } catch (Exception e) { Toast.makeText(getContext(), s, Toast.LENGTH_LONG).show(); } }); }
}
