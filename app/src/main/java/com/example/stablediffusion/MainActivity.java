package com.example.stablediffusion;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stablediffusion.api.ImageRequest;
import com.example.stablediffusion.api.ImageResponse;
import com.example.stablediffusion.api.StableDiffusionAPI;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    private static final String BASE_URL = "https://modelslab.com/api/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        StableDiffusionAPI api = retrofit.create(StableDiffusionAPI.class);

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

        api.generateImage(request).enqueue(new Callback<ImageResponse>() {
            @Override
            public void onResponse(Call<ImageResponse> call, Response<ImageResponse> response) {
                if (response.isSuccessful()) {
                    ImageResponse imageResponse = response.body();
                    if (imageResponse != null && imageResponse.getOutput() != null && !imageResponse.getOutput().isEmpty()) {
                        List<String> images = imageResponse.getOutput();
                        // Process or display the image URL from the 'output' array
                        String imageUrl = images.get(0); // Assuming we're using the first URL
                        Log.d("MainActivity", "Generated Image URL: " + imageUrl);
                    } else {
                        Log.e("MainActivity", "Image list is null or empty");
                    }
                } else {
                    Log.e("MainActivity", "API Response unsuccessful: " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<ImageResponse> call, Throwable t) {
                Log.e("MainActivity", "API call failed: " + t.getMessage());
            }

        });
    }
}
