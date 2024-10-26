package com.example.stablediffusion.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface StableDiffusionAPI {
    @Headers("Content-Type: application/json")
    @POST("v6/images/text2img")
    Call<ImageResponse> generateImage(@Body ImageRequest request);
}
