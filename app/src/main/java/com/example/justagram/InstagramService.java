package com.example.justagram;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InstagramService {
    private static final String TAG = "InstagramService";
    private static final String BASE_URL = "https://graph.instagram.com";
    private String accessToken;

    public InstagramService(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * Lấy reels từ user hiện tại
     */
    public void fetchMyReels(ReelsCallback callback) {
        new Thread(() -> {
            try {
                // Lấy user ID trước
                String meUrl = BASE_URL + "/me?fields=id,username&access_token=" + accessToken;
                String meResponse = makeHttpRequest(meUrl);
                JSONObject meJson = new JSONObject(meResponse);
                String userId = meJson.getString("id");

                Log.d(TAG, "User ID: " + userId);

                // Lấy media của user
                fetchUserReels(userId, callback);
            } catch (Exception e) {
                Log.e(TAG, "Error fetching user info: " + e.getMessage());
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        }).start();
    }

    /**
     * Lấy reels từ một user cụ thể
     */
    public void fetchUserReels(String userId, ReelsCallback callback) {
        new Thread(() -> {
            try {
                String fields = "id,media_type,media_url,thumbnail_url,caption,timestamp,like_count,comments_count,permalink";
                String mediaUrl = BASE_URL + "/" + userId + "/media?fields=" + fields + "&access_token=" + accessToken;

                Log.d(TAG, "Fetching reels from: " + mediaUrl);

                String response = makeHttpRequest(mediaUrl);
                List<Reel_video> reels = parseReelsResponse(response);

                Log.d(TAG, "Found " + reels.size() + " reels");

                if (callback != null) {
                    callback.onSuccess(reels);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching reels: " + e.getMessage());
                e.printStackTrace();
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        }).start();
    }

    /**
     * Lấy insights của một media (cần business account)
     */
    public void fetchMediaInsights(String mediaId, InsightsCallback callback) {
        new Thread(() -> {
            try {
                String insightsUrl = BASE_URL + "/" + mediaId + "/insights?metric=plays,reach,saved&access_token=" + accessToken;
                String response = makeHttpRequest(insightsUrl);

                JSONObject jsonObject = new JSONObject(response);
                JSONArray dataArray = jsonObject.getJSONArray("data");

                int plays = 0;
                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject insight = dataArray.getJSONObject(i);
                    if (insight.getString("name").equals("plays")) {
                        JSONArray values = insight.getJSONArray("values");
                        if (values.length() > 0) {
                            plays = values.getJSONObject(0).getInt("value");
                        }
                        break;
                    }
                }

                if (callback != null) {
                    callback.onSuccess(plays);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching insights: " + e.getMessage());
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        }).start();
    }

    private String makeHttpRequest(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);

        int responseCode = conn.getResponseCode();
        Log.d(TAG, "Response Code: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } else {
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            String errorLine;
            StringBuilder errorResponse = new StringBuilder();
            while ((errorLine = errorReader.readLine()) != null) {
                errorResponse.append(errorLine);
            }
            errorReader.close();
            throw new IOException("HTTP " + responseCode + ": " + errorResponse);
        }
    }

    private List<Reel_video> parseReelsResponse(String jsonResponse) throws JSONException {
        List<Reel_video> reels = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray dataArray = jsonObject.getJSONArray("data");

        for (int i = 0; i < dataArray.length(); i++) {
            JSONObject mediaObject = dataArray.getJSONObject(i);
            String mediaType = mediaObject.optString("media_type", "");

            // Chỉ lấy VIDEO hoặc REELS
            if (mediaType.equals("VIDEO") || mediaType.equals("REELS")) {
                Reel_video reel = new Reel_video();

                reel.setId(mediaObject.optString("id", ""));
                reel.setMediaType(mediaType);
                reel.setVideoUrl(mediaObject.optString("media_url", ""));
                reel.setThumbnailUrl(mediaObject.optString("thumbnail_url", ""));
                reel.setPermalink(mediaObject.optString("permalink", ""));

                // Caption
                String caption = mediaObject.optString("caption", "");
                reel.setCaption(caption);

                // Title (50 ký tự đầu của caption)
                String title = caption;
                if (caption.length() > 50) {
                    title = caption.substring(0, 50) + "...";
                }
                reel.setTitle(title);
                reel.setDescription(caption);

                // Timestamp
                String timestamp = mediaObject.optString("timestamp", "");
                reel.setTimestamp(timestamp);
                reel.setDate(formatDate(timestamp));

                // Stats
                reel.setLikes(mediaObject.optInt("like_count", 0));
                reel.setComments(mediaObject.optInt("comments_count", 0));

                // Views - Instagram Graph API không cung cấp trực tiếp
                // Cần dùng Insights API cho business accounts
                reel.setViews(0);

                reels.add(reel);

                Log.d(TAG, "Parsed reel: " + reel.getTitle());
            }
        }

        return reels;
    }

    private String formatDate(String timestamp) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM d, yyyy", Locale.US);
            Date date = inputFormat.parse(timestamp);
            return outputFormat.format(date);
        } catch (Exception e) {
            Log.e(TAG, "Error formatting date: " + e.getMessage());
            return "Unknown date";
        }
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public interface ReelsCallback {
        void onSuccess(List<Reel_video> reels);

        void onError(String error);
    }

    public interface InsightsCallback {
        void onSuccess(int views);

        void onError(String error);
    }
}