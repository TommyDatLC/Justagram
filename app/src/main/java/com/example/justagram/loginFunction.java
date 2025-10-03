import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

class LoginResult {
    public final boolean success;
    public final String message;
    public final String token;
	
    public LoginResult(boolean success, String message, String token) {
        this.success = success;
        this.message = message;
        this.token = token;
    }
}

public class InstagramAuth {
    private static final String TAG = "InstagramAuth";
    private static final String PREF_NAME = "instagram_prefs";
    private static final String KEY_TOKEN = "access_token";

//button interaction
    public static void openInstagramLogin(Context context, String appId, String redirectUri) {
        String authUrl = "https://api.instagram.com/oauth/authorize"
                + "?client_id=" + appId
                + "&redirect_uri=" + redirectUri
                + "&scope=user_profile,user_media"
                + "&response_type=code";
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
        HttpURLConnection conn = null;
        try {
            URL url = new URL("https://api.instagram.com/oauth/access_token");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String data = "client_id=" + appId
                    + "&client_secret=" + appSecret
                    + "&grant_type=authorization_code"
                    + "&redirect_uri=" + redirectUri
                    + "&code=" + code;

            OutputStream os = conn.getOutputStream();
            os.write(data.getBytes());
            os.close();

            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                JSONObject json = new JSONObject(sb.toString());
                String token = json.getString("access_token");
                return new LoginResult(true, "ok", token);
            } else {
                return new LoginResult(false, "http " + conn.getResponseCode(), null);
            }
        } catch (Exception e) {
            Log.e(TAG, "login failed", e);
            return new LoginResult(false, e.getMessage(), null);
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    public static void saveToken(Context context, String token) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    public static String getToken(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_TOKEN, null);
    }

    public static boolean isLoggedIn(Context context) {
        return getToken(context) != null;
    }
}
