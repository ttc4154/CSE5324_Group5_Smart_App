package com.example.stablediffusion;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stablediffusion.api.ImageRequest; // Import our ImageRequest model
import com.example.stablediffusion.api.ImageResponse; // Import our ImageResponse model
import com.example.stablediffusion.api.StableDiffusionAPI; // Import our API interface

import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private static final String BASE_URL = "https://modelslab.com/api/";
    private Retrofit retrofit;
    private StableDiffusionAPI apiService; // Use the StableDiffusionAPI interface

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up Retrofit with OkHttpClient
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL) // Ensure this matches our API base URL
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(StableDiffusionAPI.class); // Create an instance of our API service

        // Create the API request object
        ImageRequest request = new ImageRequest(
                "3syYMIsTtVvMGJh3PNQ6YnSkWegyh0dcJ0btZwSg4GhqmXCZxRUyo2HElTiw", // Add my API key
                "midjourney",
                "actual 8K portrait photo of gareth person, portrait, happy colors, bright eyes, clear eyes, warm smile, smooth soft skin, big dreamy eyes, beautiful intricate colored hair, symmetrical, anime wide eyes, soft lighting, detailed face, by makoto shinkai, stanley artgerm lau, wlop, rossdraws, concept art, digital painting, looking into camera",
                "painting, extra fingers, mutated hands, poorly drawn hands, poorly drawn face, deformed, ugly, blurry, bad anatomy, bad proportions, extra limbs, cloned face, skinny, glitchy, double torso, extra arms, extra hands, mangled fingers, missing lips, ugly face, distorted face, extra legs, anime",
                "512", "512", "1", "30",
                "no", "yes", null, 7.5,
                "no", "no", "no", "no",
                null, null, "yes", "yes",
                null, null, "DDPMScheduler",
                null, null
        );

        // Make the API call
        apiService.generateImage(request).enqueue(new Callback<ImageResponse>() {
            @Override
            public void onResponse(Call<ImageResponse> call, Response<ImageResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ImageResponse imageResponse = response.body();
                    Log.d("MainActivity", "API Response: " + imageResponse); // Log the entire response

                    if (imageResponse.getOutput() != null && !imageResponse.getOutput().isEmpty()) {
                        List<String> images = imageResponse.getOutput();
                        String imageUrl = images.get(0); // Assuming we're using the first URL
                        Log.d("MainActivity", "Generated Image URL: " + imageUrl);
                    } else {
                        Log.e("MainActivity", "Image list is null or empty");
                    }
                } else {
                    Log.e("MainActivity", "API Response unsuccessful: " + response.message());
                }
            }


            @Override
            public void onFailure(Call<ImageResponse> call, Throwable t) {
                Log.e("MainActivity", "API call failed: " + t.getMessage());
            }
        });
    }
}
