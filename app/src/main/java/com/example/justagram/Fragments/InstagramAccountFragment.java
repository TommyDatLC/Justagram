package com.example.justagram.Fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.justagram.LoginAuth.LoginActivity;
import com.example.justagram.R;
import com.example.justagram.etc.Utility;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Fragment hiển thị thông tin tài khoản Instagram bằng Instagram Graph API
 *
 * IMPORTANT:
 * - Cần điền ACCESS_TOKEN và IG_USER_ID bên dưới.
 * - Thêm permission INTERNET vào AndroidManifest.xml
 * - Thêm dependencies: okhttp, gson, glide, androidx.cardview
 */
public class InstagramAccountFragment extends Fragment {

    // TODO: điền vào đây
    // IGAAS2qCIE595BZAFJ0SmVNaHBUbFFCM0NqOFBOYkdNOHhBdC1PR1hNTHV6ZAEtLZAm5RVTNZAa3lweFdqM0xxNVcwY2xLVlBadFdDUm54QkFBd0Jvdl8zRkJEMFFBNEtMZAkhyX2hfQUtIZAzNnVGdSa2pVYmtoX1I2bkZAxOFZAuOGp6VQZDZD
    // 17841474853201686
    private static final String ACCESS_TOKEN = LoginActivity.userInfo.GetAccessToken();
    private static final String IG_USER_ID = LoginActivity.userInfo.UserID;

    private ImageView ivProfile;
    private TextView tvName, tvUsername, tvFollowers, tvFollows, tvMediaCount, tvBiography, tvWebsite, tvStatus;
    // Create a new http client
    private OkHttpClient httpClient = new OkHttpClient();
    private Gson gson = new Gson();

    public InstagramAccountFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_instagram_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ivProfile = view.findViewById(R.id.ivProfile);
        tvName = view.findViewById(R.id.tvName);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvFollowers = view.findViewById(R.id.tvFollowers);
        tvFollows = view.findViewById(R.id.tvFollows);
        tvMediaCount = view.findViewById(R.id.tvMediaCount);
        tvBiography = view.findViewById(R.id.tvBiography);
        tvWebsite = view.findViewById(R.id.tvWebsite);
        tvStatus = view.findViewById(R.id.tvStatus);
        // Start loading
        fetchInstagramAccount();
    }

    private void fetchInstagramAccount() {
        if (TextUtils.isEmpty(ACCESS_TOKEN) || ACCESS_TOKEN.contains("YOUR_ACCESS_TOKEN")) {
            showMessageBox("ERROR: ACCESS_TOKEN not provided");
            return;
        }
        if (TextUtils.isEmpty(IG_USER_ID) || IG_USER_ID.contains("YOUR_IG_USER_ID")) {
            showMessageBox("ERROR: IG_USER_ID not provided");
            return;

        }

        tvStatus.setText("Loading...");

        // Build the Graph API URL
        // Fields: adjust as necessary; some fields may require specific permissions.
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("graph.instagram.com")
                .addPathSegment(IG_USER_ID)
                .addQueryParameter("fields", "id,username,name,profile_picture_url,biography,website,followers_count,follows_count,media_count")
                .addQueryParameter("access_token", ACCESS_TOKEN)
                .build();
        // https://graph.instagram.com/{IG_USER_ID}?feilds=id,username,name,profile_picture_url,biography,website,followers_count,follows_count,media_count,access_token = {ACCESS_TOKEN}
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> showMessageBox("Network error: " + e.getMessage()));
            }

            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (getActivity() == null) return;

                final String body = response.body() != null ? response.body().string() : "";
                if (!response.isSuccessful()) {
                    getActivity().runOnUiThread(() -> showMessageBox("API error: " + response.code() + " — " + body));
                    return;
                }

                try {
                    final IGUser user = gson.fromJson(body, IGUser.class);
                    getActivity().runOnUiThread(() -> applyUserToUI(user));
                } catch (Exception ex) {
                    getActivity().runOnUiThread(() -> showMessageBox("Parse error: " + ex.getMessage()));
                }
            }
        });
    }

    private void applyUserToUI(IGUser user) {
        if (user == null) {
            showMessageBox("No data");
            return;
        }

        tvName.setText(nonEmptyOrDash(user.name));
        tvUsername.setText("@" + nonEmptyOrDash(user.username));
        tvFollowers.setText(String.valueOf(user.followersCount != null ? user.followersCount : 0));
        tvFollows.setText(String.valueOf(user.followsCount != null ? user.followsCount : 0));
        tvMediaCount.setText(String.valueOf(user.mediaCount != null ? user.mediaCount : 0));
        tvBiography.setText(nonEmptyOrDash(user.biography));
        tvWebsite.setText(nonEmptyOrDash(user.website));
        tvStatus.setText("Loaded");

        if (!TextUtils.isEmpty(user.profilePictureUrl)) {
            // Allocate glide with the require context in this case is this fragment
            Glide.with(requireContext())
                    .load(user.profilePictureUrl)
                    .centerCrop() // Fill the image view, crop the unnecessary
                    .into(ivProfile);// put in the image view
        } else {
            // If can't find any profile image set the imageContainer with the castorice image
            ivProfile.setImageResource(R.mipmap.unknow_user);
        }
    }
    // check if s is null or only white space return
    private String nonEmptyOrDash(String s) {
        return (s == null || s.trim().isEmpty()) ? "—" : s;
    }

    // Model for partial fields returned by IG Graph API

    // Sync the request data into this class
    private static class IGUser {
        @SerializedName("id")
        String id;

        @SerializedName("username")
        String username;

        @SerializedName("name")
        String name;

        // field name may vary by API version; handle nullable
        @SerializedName("profile_picture_url")
        String profilePictureUrl;

        @SerializedName("biography")
        String biography;

        @SerializedName("website")
        String website;

        @SerializedName("followers_count")
        Integer followersCount;

        @SerializedName("follows_count")
        Integer followsCount;

        @SerializedName("media_count")
        Integer mediaCount;
    }
    void showMessageBox(String content)
    {
        Utility.showMessageBox(content,getContext());
    }
}
