package com.example.e_library.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "ELibrarySession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_AVATAR = "avatar";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void saveUserSession(String userId, String username, String email, String avatarBase64) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_AVATAR, avatarBase64);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUserId() {
        return pref.getString(KEY_USER_ID, null);
    }

    public String getUsername() {
        return pref.getString(KEY_USERNAME, null);
    }

    public String getEmail() {
        return pref.getString(KEY_EMAIL, null);
    }

    public String getAvatar() {
        return pref.getString(KEY_AVATAR, null);
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}