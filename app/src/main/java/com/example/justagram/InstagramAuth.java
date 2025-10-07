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

/**
 * dùng trong main activity
 * button onclick -> openInstagramLogin 
 * callback về main activity
 * đổi code lấy access token
 * check account type = hàm checkBusinessAccount
 * */ 

/**
 Instagram Basic Display API đã ngừng hoạt động, nên phải sử dụng Facebook Login để truy cập Instagram Business Account
 */
public class InstagramAuth {
    private static final String TAG = "InstagramAuth";
    private static final String PREF_NAME = "instagram_prefs";
    private static final String KEY_TOKEN = "access_token";
    private static final String KEY_IG_USER_ID = "ig_user_id";

    private static final OkHttpClient client = new OkHttpClient();

    /**
     * xin quyenfef đăn nhập fbđ
     @param context Android context
     @param appId Facebook App ID
     @param redirectUri URL để Facebook redirect sau khi đăng nhập xong
     */
    public static void openInstagramLogin(Context context, String appId, String redirectUri) {
        String authUrl = "https://www.facebook.com/v21.0/dialog/oauth" +
                "?client_id=" + appId +
                "&redirect_uri=" + redirectUri +
                "&scope=instagram_basic,pages_show_list,pages_read_engagement" +
                "&response_type=code";
        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl)));
    }

    /**
     * Lấy authorization code từ callback URL sau khi đăng nhập Facebook
     @param uri Callback URI từ Facebook
     @return Authorization code hoặc null nếu không có
     */
    public static String getCodeFromCallback(Uri uri) {
        return uri != null ? uri.getQueryParameter("code") : null;
    }

    /**
     * Đổi authorization code thành access token và kiểm tra tài khoản
     @param code Authorization code từ Facebook
     @param appId Facebook App ID
     @param appSecret Facebook App Secret
     @param redirectUri URL redirect 
     */
    public static LoginResult exchangeCodeForToken(
            String code,
            String appId,
            String appSecret,
            String redirectUri
    ) {
        try {
            // code -> access token
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

                    // đưa token vào để kiểm tra tài khoản Business/Creator
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

    /**
     * Kiểm tra tài khoản có phải Instagram Business/Creator Account không
     * @param fbToken Facebook access token
     */
    private static LoginResult checkBusinessAccount(String fbToken) {
        try {
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

                if (!json.has("data") || json.getJSONArray("data").length() == 0) {
                    return new LoginResult(false, "no facebook page", fbToken, false, null, null);
                }

                JSONObject page = json.getJSONArray("data").getJSONObject(0);
                String pageId = page.getString("id");
                String pageToken = page.getString("access_token");

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

                    // Kiểm tra Page có kết nối Instagram Business Account không
                    if (!pageData.has("instagram_business_account")) {
                        return new LoginResult(false, "facebook chưa kết nối instagram business account", pageToken, false, null, null);
                    }

                    String igUserId = pageData.getJSONObject("instagram_business_account").getString("id");

                    // Bước 4: Lấy thông tin Instagram account (username và account_type)
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

                        //  Kiểm tra account_type có phải BUSINESS hoặc MEDIA_CREATOR không
                        boolean isProfessional = "BUSINESS".equalsIgnoreCase(accountType) ||
                                "MEDIA_CREATOR".equalsIgnoreCase(accountType);

                        // Từ chối đăng nhập nếu không phải Professional Account
                        if (!isProfessional) {
                            return new LoginResult(
                                    false, 
                                    "not professional account",  
                                    pageToken,
                                    false,  
                                    accountType,
                                    igUserId
                            );
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

    /**
     * Lưu access token và Instagram User ID vào SharedPreferences
     */
    public static void saveToken(Context context, String token, String igUserId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_IG_USER_ID, igUserId)
                .apply();
    }

    /**
     * Lấy access token đã lưu từ SharedPreferences
     */
    public static String getToken(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_TOKEN, null);
    }

    /**
     * Lấy Instagram User ID đã lưu từ SharedPreferences
     */
    public static String getIgUserId(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_IG_USER_ID, null);
    }

    /**
     * Kiểm tra user đã đăng nhập chưa
     */
    public static boolean isLoggedIn(Context context) {
        return getToken(context) != null && getIgUserId(context) != null;
    }
}