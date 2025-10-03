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
    public final boolean isBusinessAccount;
    public final String accountType;
    public final String igUserId;

    public LoginResult(boolean success, String message, String token, boolean isBusinessAccount, String accountType, String igUserId) {
        this.success = success;
        this.message = message;
        this.token = token;
        this.isBusinessAccount = isBusinessAccount;
        this.accountType = accountType;
        this.igUserId = igUserId;
    }
}

public class InstagramAuth {
    private static final String TAG = "InstagramAuth";
    private static final String PREF_NAME = "instagram_prefs";
    private static final String KEY_TOKEN = "access_token";

    public static void openInstagramLogin(Context context, String appId, String redirectUri) {
        String authUrl = "https://api.instagram.com/oauth/authorize" +
            "?client_id=" + appId +
            "&redirect_uri=" + redirectUri +
            "&scope=user_profile,user_media" +
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
        HttpURLConnection conn = null;
        try {
            //exchange code -> access_token
            // connection initialization
            URL url = new URL("https://api.instagram.com/oauth/access_token");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String data = "client_id=" + appId +
                "&client_secret=" + appSecret +
                "&grant_type=authorization_code" +
                "&redirect_uri=" + redirectUri +
                "&code=" + code;

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

                // check account info
                return checkBusinessAccount(token);

            } else {
                return new LoginResult(false, "HTTP " + conn.getResponseCode(), null, false, null, null);
            }
        } catch (Exception e) {
            Log.e(TAG, "login failed", e);
            return new LoginResult(false, e.getMessage(), null, false, null, null);
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private static LoginResult checkBusinessAccount(String token) {
        HttpURLConnection conn = null;
        try {

            // connection initialization
            URL url = new URL("https://graph.instagram.com/me?fields=id,account_type&access_token=" + token);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // code 200 -> success  
            if (conn.getResponseCode() == 200) {
                // đọc dữ liệu JSON
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                // chuyển chuỗi json
                JSONObject json = new JSONObject(sb.toString());
                String igUserId = json.optString("id");
                String accountType = json.optString("account_type");

                // so sánh account type có phải business không
                boolean isBusiness = "BUSINESS".equalsIgnoreCase(accountType);

                if (!isBusiness) {
                    return new LoginResult(false, "Not business account, cant access", token, false, accountType, igUserId);
                }

                return new LoginResult(true, "login successfully", token, true, accountType, igUserId);

            } else {
                return new LoginResult(false, "HTTP " + conn.getResponseCode(), token, false, null, null);
            }
        } catch (Exception e) {
            return new LoginResult(false, e.getMessage(), token, false, null, null);
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    // Lưu token 
    public static void saveToken(Context context, String token) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    // Lấy token đã lưu 
    public static String getToken(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TOKEN, null);
    }

    // check user đã loggin chưa
    public static boolean isLoggedIn(Context context) {
        return getToken(context) != null;
    }
}