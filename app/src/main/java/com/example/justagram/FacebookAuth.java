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

public class FacebookAuth {
    private static final String TAG = "FacebookAuth";
    private static final String PREF_NAME = "facebook_prefs";
    private static final String KEY_TOKEN = "access_token";
    private static final OkHttpClient client = new OkHttpClient();

    /**
     * Mở giao diện đăng nhập Facebook.
     * Sau khi người dùng đăng nhập xong, Facebook sẽ redirect về redirectUri với ?code=...
     */
    public static void openFacebookLogin(Context context, String appId, String redirectUri) {
        String authUrl = "https://www.facebook.com/v21.0/dialog/oauth" +
                "?client_id=" + appId +
                "&redirect_uri=" + redirectUri +
                "&scope=email,public_profile" +   // quyền cơ bản
                "&response_type=code";
        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl)));
    }

    /**
     * Lấy "code" trả về từ Facebook sau khi người dùng login thành công.
     */
    public static String getCodeFromCallback(Uri uri) {
        return uri != null ? uri.getQueryParameter("code") : null;
    }

    /**
     * Đổi "code" sang "access_token"
     */
    public static LoginResult exchangeCodeForToken(
            String code,
            String appId,
            String appSecret,
            String redirectUri
    ) {
        try {
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

                    return new LoginResult(true, "Facebook login successful", token, true, null, null);
                } else {
                    return new LoginResult(false, "HTTP " + response.code(), null, false, null, null);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error", e);
            return new LoginResult(false, "Network error: " + e.getMessage(), null, false, null, null);
        } catch (Exception e) {
            Log.e(TAG, "Login error", e);
            return new LoginResult(false, e.getMessage(), null, false, null, null);
        }
    }

    public static void saveToken(Context context, String token) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .apply();
    }

    public static String getToken(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_TOKEN, null);
    }

    public static boolean isLoggedIn(Context context) {
        return getToken(context) != null;
    }
}
