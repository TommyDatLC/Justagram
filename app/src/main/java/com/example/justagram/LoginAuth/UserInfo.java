package com.example.justagram.LoginAuth;

import java.util.function.Consumer;

public class UserInfo {
    public transient Consumer<Object> onAccessTokenChange;
    public transient String UserID = "";
    String AccessToken = "";

    public void SetAccessToken(String newAccessToken) {
        AccessToken = newAccessToken;
        onAccessTokenChange.accept(null);
    }

    public String GetAccessToken() {
        return AccessToken;
    }
}
