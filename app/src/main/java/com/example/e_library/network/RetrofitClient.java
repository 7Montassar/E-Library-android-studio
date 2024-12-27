package com.example.e_library.network;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


// app/java/com.example.e_library/network/RetrofitClient.java
public class RetrofitClient {
    private static final String BASE_URL = "http://10.0.2.2:3000/";  // localhost for Android emulator
    private static RetrofitClient instance;
    private Retrofit retrofit;

    private RetrofitClient() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    public ApiService getApi() {
        return retrofit.create(ApiService.class);
    }
}