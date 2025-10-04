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
    private static final String KEY_IG_USER_ID = "ig_user_id"; 



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
        HttpURLConnection conn = null;
        try {
            // exchange code -> Facebook access_token
            // connection initialization
            URL url = new URL("https://graph.facebook.com/v21.0/oauth/access_token" +
                "?client_id=" + appId +
                "&client_secret=" + appSecret +
                "&redirect_uri=" + redirectUri +
                "&code=" + code);
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

                JSONObject json = new JSONObject(sb.toString());
                String token = json.getString("access_token");

                // gửi về hàm checkBusinessAccount để kiểm tra loại tài khoản
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

    private static LoginResult checkBusinessAccount(String fbToken) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL("https://graph.facebook.com/v21.0/me/accounts?access_token=" + fbToken);
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
                
                // kiểm tra có facebook page không
                if (!json.has("data") || json.getJSONArray("data").length() == 0) {
                    return new LoginResult(false, "no facebook page", fbToken, false, null, null);
                }

                // obtain page id and page access token
                JSONObject page = json.getJSONArray("data").getJSONObject(0);
                String pageId = page.getString("id");
                String pageToken = page.getString("access_token");

                // check for business account
                conn.disconnect();

                // mở kết nối mới
                url = new URL("https://graph.facebook.com/v21.0/" + pageId +
                    "?fields=instagram_business_account&access_token=" + pageToken);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                if (conn.getResponseCode() == 200) {
                    reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    sb = new StringBuilder();
                    while ((line = reader.readLine()) != null) sb.append(line);
                    reader.close();

                    JSONObject pageData = new JSONObject(sb.toString());

                    // Kiểm tra có kết nối Instagram không
                    if (!pageData.has("instagram_business_account")) {
                        return new LoginResult(false, "facebook chưa kết nối instagram business account", pageToken, false, null, null);
                    }

                    String igUserId = pageData.getJSONObject("instagram_business_account").getString("id");

                    
                    conn.disconnect();
                    url = new URL("https://graph.facebook.com/v21.0/" + igUserId +
                        "?fields=username,account_type&access_token=" + pageToken);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");

                    if (conn.getResponseCode() == 200) {
                        reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        sb = new StringBuilder();
                        while ((line = reader.readLine()) != null) sb.append(line);
                        reader.close();

                        JSONObject igData = new JSONObject(sb.toString());
                        String accountType = igData.optString("account_type", "UNKNOWN"); 

                        // so sánh account type có phải business không
                        boolean isBusiness = "BUSINESS".equalsIgnoreCase(accountType) || "MEDIA_CREATOR".equalsIgnoreCase(accountType);

                        if (!isBusiness) {
                            return new LoginResult(false, "Account type: " + accountType + ", not business nor media creator", pageToken, false, accountType, igUserId);
                        }

                        return new LoginResult(true, "login successfully", pageToken, true, accountType, igUserId); 
                    } else {
                        return new LoginResult(false, "Cannot get Instagram account info", pageToken, false, null, igUserId);
                    }
                } else {
                    return new LoginResult(false, "HTTP " + conn.getResponseCode(), pageToken, false, null, null);
                }

            } else {
                return new LoginResult(false, "HTTP " + conn.getResponseCode(), fbToken, false, null, null);
            }
        } catch (Exception e) {
            return new LoginResult(false, e.getMessage(), fbToken, false, null, null);
        } finally {
            if (conn != null) conn.disconnect();
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