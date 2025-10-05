package com.example.justagram;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class InstagramAuth {
    private static final String TAG = "InstagramAuth";
    private static final String PREF_NAME = "instagram_prefs";
    private static final String KEY_TOKEN = "access_token";
    private static final String KEY_IG_USER_ID = "ig_user_id";

    private static final OkHttpClient client = new OkHttpClient();

    // dùng facebook vì instagram basic display api đã ngừng hoạt động
    public static void openInstagramLogin(Context context, String appId, String redirectUri) {
        String authUrl = "https://www.facebook.com/v21.0/dialog/oauth" +
                "?client_id=" + appId +
                "&redirect_uri=" + redirectUri +
                "&scope=instagram_basic,pages_show_list,pages_read_engagement" +
                "&response_type=code";
        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl)));
    }

    public static String getCodeFromCallback(Uri uri) {
        return uri != null ? uri.getQueryParameter("code") : null;
    }

    public static LoginResult exchangeCodeForToken(
            String code,
            String appId,
            String appSecret,
            String redirectUri
    ) {
        try {
            // exchange code -> Facebook access_token
            String url = "https://graph.facebook.com/v21.0/oauth/access_token" +
                    "?client_id=" + appId +
                    "&client_secret=" + appSecret +
                    "&redirect_uri=" + redirectUri +
                    "&code=" + code;

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    JSONObject json = new JSONObject(responseBody);
                    String token = json.getString("access_token");

                    // gửi về hàm checkBusinessAccount để kiểm tra loại tài khoản
                    return checkBusinessAccount(token);
                } else {
                    return new LoginResult(false, "HTTP " + response.code(), null, false, null, null);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "login failed", e);
            return new LoginResult(false, e.getMessage(), null, false, null, null);
        }
    }

    private static LoginResult checkBusinessAccount(String fbToken) {
        try {
            // Bước 1: Lấy danh sách Facebook Pages
            String url = "https://graph.facebook.com/v21.0/me/accounts?access_token=" + fbToken;

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    return new LoginResult(false, "HTTP " + response.code(), fbToken, false, null, null);
                }

                String responseBody = response.body().string();
                JSONObject json = new JSONObject(responseBody);

                // kiểm tra có facebook page không
                if (!json.has("data") || json.getJSONArray("data").length() == 0) {
                    return new LoginResult(false, "no facebook page", fbToken, false, null, null);
                }

                // obtain page id and page access token
                JSONObject page = json.getJSONArray("data").getJSONObject(0);
                String pageId = page.getString("id");
                String pageToken = page.getString("access_token");

                // Bước 2: Kiểm tra Instagram Business Account
                url = "https://graph.facebook.com/v21.0/" + pageId +
                        "?fields=instagram_business_account&access_token=" + pageToken;

                request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();

                try (Response pageResponse = client.newCall(request).execute()) {
                    if (!pageResponse.isSuccessful() || pageResponse.body() == null) {
                        return new LoginResult(false, "HTTP " + pageResponse.code(), pageToken, false, null, null);
                    }

                    String pageResponseBody = pageResponse.body().string();
                    JSONObject pageData = new JSONObject(pageResponseBody);

                    // Kiểm tra có kết nối Instagram không
                    if (!pageData.has("instagram_business_account")) {
                        return new LoginResult(false, "facebook chưa kết nối instagram business account", pageToken, false, null, null);
                    }

                    String igUserId = pageData.getJSONObject("instagram_business_account").getString("id");

                    // Bước 3: Lấy thông tin Instagram account
                    url = "https://graph.facebook.com/v21.0/" + igUserId +
                            "?fields=username,account_type&access_token=" + pageToken;

                    request = new Request.Builder()
                            .url(url)
                            .get()
                            .build();

                    try (Response igResponse = client.newCall(request).execute()) {
                        if (!igResponse.isSuccessful() || igResponse.body() == null) {
                            return new LoginResult(false, "Cannot get Instagram account info", pageToken, false, null, igUserId);
                        }

                        String igResponseBody = igResponse.body().string();
                        JSONObject igData = new JSONObject(igResponseBody);
                        String accountType = igData.optString("account_type", "UNKNOWN");

                        // so sánh account type có phải business không
                        boolean isBusiness = "BUSINESS".equalsIgnoreCase(accountType) ||
                                "MEDIA_CREATOR".equalsIgnoreCase(accountType);

                        if (!isBusiness) {
                            return new LoginResult(false, "Account type: " + accountType +
                                    ", not business nor media creator", pageToken, false, accountType, igUserId);
                        }

                        return new LoginResult(true, "login successfully", pageToken, true, accountType, igUserId);
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error", e);
            return new LoginResult(false, "Network error: " + e.getMessage(), fbToken, false, null, null);
        } catch (Exception e) {
            Log.e(TAG, "Error checking business account", e);
            return new LoginResult(false, e.getMessage(), fbToken, false, null, null);
        }
    }

    // Lưu token
    public static void saveToken(Context context, String token, String igUserId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_IG_USER_ID, igUserId)
                .apply();
    }

    // Lấy token đã lưu
    public static String getToken(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_TOKEN, null);
    }

    // get IgUserId đã lưu
    public static String getIgUserId(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_IG_USER_ID, null);
    }

    // check user đã loggin chưa
    public static boolean isLoggedIn(Context context) {
        return getToken(context) != null && getIgUserId(context) != null;
    }
}