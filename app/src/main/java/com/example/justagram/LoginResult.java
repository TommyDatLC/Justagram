package com.example.justagram;
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

