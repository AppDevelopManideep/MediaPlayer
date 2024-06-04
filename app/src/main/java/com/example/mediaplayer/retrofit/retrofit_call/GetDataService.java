package com.example.mediaplayer.retrofit.retrofit_call;

import com.example.mediaplayer.retrofit.RetroPhoto;

import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

//for end point
public interface GetDataService {
    @GET("/photos")
    Call<List<RetroPhoto>> getAllPhotos();

    @POST("/photos")
    Call<String> sendPhotos(@Body RequestBody requestBody);
}
