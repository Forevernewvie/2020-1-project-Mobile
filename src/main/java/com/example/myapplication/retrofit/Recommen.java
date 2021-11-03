package com.example.myapplication;


import retrofit2.Call;
import retrofit2.http.GET;

public interface recommen {
    @GET("/res")
    Call<resList> getList();
}
