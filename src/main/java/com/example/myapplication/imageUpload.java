package com.example.myapplication;

import javax.xml.transform.Result;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface imageUpload {
    @Multipart
    @POST("/image")
    Call<ResponseBody> uploadImage(
            @Part MultipartBody.Part image);

}
