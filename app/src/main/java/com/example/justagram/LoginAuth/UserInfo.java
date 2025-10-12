package com.example.justagram.LoginAuth;

import java.util.function.Consumer;

public class UserInfo {
    String AccessToken = "";
    public transient Consumer<Object> onAccessTokenChange;
    public void SetAccessToken(String newAccessToken)
    {
        AccessToken = newAccessToken;
        onAccessTokenChange.accept(null);
    }
    public String GetAccessToken()
    {
        return AccessToken;
    }
    public transient String UserID = "";
}
