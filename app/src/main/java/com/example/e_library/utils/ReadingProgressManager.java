package com.example.e_library.utils;

import android.content.Context;
import android.util.Log;
import com.example.e_library.network.ApiService;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReadingProgressManager {
    private final Context context;
    private final ApiService apiService;
    private final SessionManager sessionManager;

    // Add the callback interface
    public interface ProgressCallback {
        void onSuccess(JsonObject response);
        void onError(String message);
    }

    public ReadingProgressManager(Context context, ApiService apiService) {
        this.context = context;
        this.apiService = apiService;
        this.sessionManager = new SessionManager(context);
    }

    public void updateProgress(long bookId, int lastPage, int totalPages, ProgressCallback callback) {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            callback.onError("User not logged in");
            return;
        }

        Map<String, Object> progressData = new HashMap<>();
        progressData.put("bookId", bookId);
        progressData.put("lastPage", lastPage);
        progressData.put("totalPages", totalPages);
        progressData.put("userId", Integer.parseInt(userId));

        apiService.updateReadingProgress(progressData).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Unknown error";
                        Log.e("ReadingProgress", "Error response: " + errorBody);
                        callback.onError(errorBody);
                    } catch (IOException e) {
                        callback.onError("Error reading error response");
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("ReadingProgress", "Network error: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }

    public void getProgress(long bookId, ProgressCallback callback) {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            callback.onError("User not logged in");
            return;
        }

        Map<String, Object> requestData = new HashMap<>();
        requestData.put("bookId", bookId);
        requestData.put("userId", Integer.parseInt(userId));

        apiService.getReadingProgress(requestData.size()).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Unknown error";
                        Log.e("ReadingProgress", "Error response: " + errorBody);
                        callback.onError(errorBody);
                    } catch (IOException e) {
                        callback.onError("Error reading error response");
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("ReadingProgress", "Network error: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }
}