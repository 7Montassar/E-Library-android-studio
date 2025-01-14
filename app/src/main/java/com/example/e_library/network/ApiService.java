package com.example.e_library.network;

import com.google.gson.JsonObject;
import java.util.Map;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

public interface ApiService {
    @POST("api/signup")
    Call<JsonObject> signup(@Body Map<String, Object> request);

    @POST("api/signin")
    Call<JsonObject> signIn(@Body Map<String, Object> request);

    @Multipart
    @POST("api/books")
    Call<JsonObject> addBook(
            @Part("title") RequestBody title,
            @Part("author") RequestBody author,
            @Part("description") RequestBody description,
            @Part("category") RequestBody category,
            @Part MultipartBody.Part pdf
    );

    @GET("api/books")
    Call<JsonObject> getBooks();

    @GET("api/books/{id}")
    Call<JsonObject> getBookDetails(@Path("id") long bookId);

    @POST("api/books/{id}/favorite")
    Call<JsonObject> toggleFavorite(
            @Path("id") long bookId,
            @Body JsonObject favorite
            );

    @POST("api/books/{bookId}/comments")
    Call<JsonObject> addComment(
            @Path("bookId") long bookId,
            @Body JsonObject comment
    );

    @GET("api/books/{id}/pdf")
    @Streaming
    Call<ResponseBody> downloadBookPdf(@Path("id") long bookId, @Query("userId") String userId);


    @POST("api/books/{id}/download")
    Call<JsonObject> addToDownloads(
            @Path("id") long bookId,
            @Body JsonObject downloadInfo
    );

    @GET("api/books/{userId}/downloads")
    Call<JsonObject> getUserDownloadedBooks(@Path("userId") String userId);

    @POST("api/books/{id}/download/progress")
    Call<JsonObject> updateReadingProgress(
            @Path("id") long bookId,
            @Body JsonObject progressInfo
    );

    @DELETE("api/books/{id}/download")
    Call<JsonObject> removeDownloadedBook(
            @Path("id") long bookId
    );

    @GET("api/reading-progress/{bookId}")
    Call<JsonObject> getReadingProgress(@Path("bookId") int bookId);

    @POST("api/reading-progress")
    Call<JsonObject> updateReadingProgress(@Body Map<String, Object> progressData);


}