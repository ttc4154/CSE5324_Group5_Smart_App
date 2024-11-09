package com.example.stablediffusion.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiService {
    @Headers("Content-Type: application/json")
    @POST("v1/chat/completions")
    Call<ChatResponse> sendChatRequest(@Body ChatRequest chatRequest);
}
