package com.example.stablediffusion.api;
import static android.content.ContentValues.TAG;

import android.util.Log;
import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChatGPTService {
    private static final String BASE_URL = "https://api.openai.com/";
    private static final String API_KEY = "your_key_here";
            "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";
    //"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"; // Securely manage API key!

    private final ApiService apiService;
    private final Gson gson;

    public ChatGPTService() {
        // Create an OkHttpClient with an interceptor to add the Authorization header
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request request = original.newBuilder()
                            .header("Authorization", "Bearer " + API_KEY)
                            .build();
                    return chain.proceed(request);
                })
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))  // For logging
                .build();

        // Initialize Retrofit with the OkHttpClient
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)  // Use custom OkHttpClient with Authorization header
                .build();

        apiService = retrofit.create(ApiService.class);  // Create ApiService instance
        gson = new Gson();
    }

    public void getChatResponse(String message, ChatResponseCallback callback) {
        // Create a ChatRequest object with model and messages
        ChatMessage chatMessage = new ChatMessage("user", message);
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(chatMessage);

        ChatRequest chatRequest = new ChatRequest("gpt-3.5-turbo", messages);

        // Make the API call using Retrofit
        Call<ChatResponse> call = apiService.sendChatRequest(chatRequest); // Pass ChatRequest object directly
        call.enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                //# Let's create the correct list of prompts with 100 entries and save it to a file
                String[] prompts ={
                        "A majestic lion sitting atop a rocky cliff during a vibrant sunset, with a golden mane flowing in the wind.",
                        "A futuristic city skyline with flying cars, neon lights, and towering skyscrapers under a starry night sky.",
                        "An ancient forest with towering trees, glowing mushrooms, and mystical creatures peeking out from behind the foliage.",
                        "A serene mountain lake with crystal-clear water, surrounded by snow-capped peaks and lush greenery.",
                        "A bustling street market in an exotic city with colorful fabrics, spices, and street food vendors.",
                        "A mystical underwater city with glowing sea creatures, ancient ruins, and a shimmering blue light.",
                        "A majestic waterfall cascading down into a tropical rainforest, with vibrant birds flying through the mist.",
                        "A lone astronaut standing on the surface of Mars, gazing at Earth in the distance, with a dusty red landscape around them.",
                        "A cozy cabin in the woods during winter, with smoke rising from the chimney and a blanket of snow covering the ground.",
                        "A fantastical creature, half-dragon, half-unicorn, soaring through the clouds above a tranquil valley.",
                        "A close-up of a dewdrop on a leaf, reflecting the morning sunlight and surrounded by lush greenery.",
                        "A surreal landscape with floating islands, waterfalls, and glowing crystals hanging from the sky.",
                        "A vintage car parked on a cobblestone street in a quaint European village, with ivy growing up the walls of nearby buildings.",
                        "A giant tree with a hollow trunk, where fairies and magical creatures are living inside, with twinkling lights around the branches.",
                        "A futuristic robotic soldier standing in the middle of a war-torn city, surrounded by rubble and smoke.",
                        "A peaceful garden with blooming flowers, butterflies fluttering about, and a stone path leading to a small gazebo.",
                        "A pirate ship sailing across the ocean at sunset, with a skull-and-crossbones flag flying in the wind.",
                        "A glowing, ethereal forest with bioluminescent plants and creatures under a dark, star-filled sky.",
                        "A medieval knight in shining armor, holding a sword, standing before an ancient castle gate.",
                        "A flying dragon perched atop a mountain, overlooking a sprawling medieval kingdom below.",
                        "A snowy owl soaring through a moonlit forest, with snowflakes gently falling from the sky.",
                        "A group of wild horses running across an open plain, with dust swirling behind them and mountains in the distance.",
                        "A vibrant coral reef teeming with colorful fish, sea turtles, and bright coral under the clear ocean water.",
                        "A steampunk-inspired airship flying over a vast cityscape, with gears and steam vents visible on the ship’s surface.",
                        "A glowing neon sign in a cyberpunk alley, with rain pouring down and reflections on the wet pavement.",
                        "A large, ancient oak tree standing tall in the middle of a foggy meadow, with a single swing hanging from one of its branches.",
                        "A majestic eagle flying high above a mountain range, with its wings spread wide against a bright blue sky.",
                        "A mysterious abandoned mansion hidden deep in a dense forest, with ivy growing over its crumbling walls.",
                        "A dense jungle filled with ancient ruins, where vines hang from the trees and the air is thick with mist.",
                        "A peaceful lakeside scene at dawn, with mist rising off the water and a small boat floating gently.",
                        "A fiery volcano erupting with streams of lava flowing down its slopes, lighting up the night sky.",
                        "A bustling futuristic city square with towering holograms, street vendors, and robots walking alongside people.",
                        "A dreamlike sky filled with swirling galaxies, shooting stars, and a glowing moon surrounded by clouds.",
                        "A towering statue of a warrior holding a sword, standing in a desert with the sun setting in the background.",
                        "A vintage train crossing a long, ornate bridge over a deep valley, with steam billowing out from the engine.",
                        "A magical portal glowing in the middle of a forest, with strange, colorful lights swirling around it.",
                        "A giant redwood tree with a rope bridge suspended between its branches, surrounded by mist and dense foliage.",
                        "A knight riding a white horse through a forest of tall, golden trees, with autumn leaves falling around them.",
                        "A bustling carnival at night, with brightly colored lights, cotton candy stands, and Ferris wheels.",
                        "A mermaid swimming among colorful fish and coral, her long hair flowing in the ocean current.",
                        "A spooky haunted house surrounded by a foggy graveyard, with glowing jack-o'-lanterns and bats flying overhead.",
                        "A busy Tokyo street with people crossing in every direction, bright billboards, and the city skyline in the distance.",
                        "A dark, rainy alleyway lit only by a flickering street lamp and the reflection of neon signs on the wet pavement.",
                        "A peaceful meadow with tall grass, wildflowers, and a large oak tree in the center.",
                        "A glowing dragonfly hovering above a tranquil pond, surrounded by lush reeds and blooming lotus flowers.",
                        "A starry sky over a quiet desert, with a campfire crackling in the foreground and a distant mountain range.",
                        "A quaint village on the edge of a cliff, with colorful houses and a cobblestone street winding through it.",
                        "A fantasy castle built into the side of a mountain, with waterfalls cascading from its towers.",
                        "A sleek spaceship flying through a nebula, with vibrant purple and blue clouds swirling around it.",
                        "A large, ancient library with towering bookshelves, intricate woodwork, and dim candlelight illuminating the space.",
                        "A large, full moon hanging over a calm ocean, with waves gently lapping against the shore.",
                        "A dense forest with fog rolling through the trees, casting long shadows and creating an eerie atmosphere.",
                        "A futuristic subway station with clean, sleek lines, holographic signs, and trains arriving at high speed.",
                        "A quaint street lined with cherry blossom trees in full bloom, with petals gently falling to the ground.",
                        "A surreal desert landscape with giant, floating orbs and twisted rock formations under a vibrant purple sky.",
                        "A mystical mountain temple perched high above the clouds, with prayer flags fluttering in the wind.",
                        "A group of adventurers walking through an ancient, overgrown jungle temple, with crumbling stone walls and hidden treasures.",
                        "A glowing jellyfish floating through the depths of the ocean, with soft bioluminescence lighting up the water around it.",
                        "A medieval blacksmith forging a sword, with sparks flying and the sound of hammering filling the air.",
                        "A bustling futuristic marketplace with robots and humans shopping side by side, surrounded by holographic advertisements.",
                        "A grand castle courtyard with knights practicing combat and a fountain at the center, surrounded by lush gardens.",
                        "A giant whale swimming through the sky above a calm ocean, with small birds flying alongside it.",
                        "A mysterious cave with glowing crystals embedded in the walls and an underground river flowing through it.",
                        "A grand ballroom filled with elegantly dressed people dancing, with a chandelier hanging from the ceiling.",
                        "A peaceful rural farm with a red barn, grazing animals, and rolling hills in the distance.",
                        "A lone wolf standing on a rocky cliff, looking out over a vast forest with the moon rising behind it.",
                        "A vibrant sunset over a vast savannah, with silhouetted acacia trees and herds of wildebeest in the distance.",
                        "A futuristic cityscape at night, with towering buildings, holographic billboards, and flying cars zooming through the sky.",
                        "A serene, snow-covered mountain range with pine trees blanketed in white and a ski chalet nestled between the peaks.",
                        "A magical floating city above the clouds, with bridges made of light connecting towering spires and airships flying around it.",
                        "A snowy owl perched on a tree branch, surrounded by a dense, frost-covered forest with a full moon above.",
                        "A dramatic storm over a turbulent sea, with dark clouds swirling and lightning flashing across the sky.",
                        "A quiet street in a small town at dusk, with streetlights casting a warm glow on the empty sidewalks.",
                        "A colorful parade of mythical creatures marching through a city square, with fireworks lighting up the sky above.",
                        "A futuristic laboratory filled with advanced technology, glowing monitors, and robotic assistants working alongside scientists.",
                        "A serene Japanese garden with a koi pond, a wooden bridge, and blooming cherry blossoms.",
                        "A glowing city under the ocean, with large glass domes protecting the inhabitants from the deep water outside.",
                        "A wild west town with dusty streets, wooden saloons, and cowboys riding horses through the town square.",
                        "A medieval village with thatched roofs, cobblestone streets, and villagers going about their daily tasks.",
                        "A group of adventurers trekking through an arctic landscape, with snowstorms and glaciers in the distance.",
                        "A giant, golden phoenix soaring across the sky with its fiery wings trailing behind it.",
                        "A peaceful autumn park with golden leaves falling from the trees and people walking dogs along the paths.",
                        "A futuristic battle arena with gladiators fighting in an arena surrounded by holographic spectators.",
                        "A giant robot standing in the middle of a city, with skyscrapers towering around it and cars scattered in the streets.",
                        "A pirate treasure chest filled with gold coins and jewels, sitting on a sandy beach surrounded by palm trees.",
                        "A gothic cathedral with towering spires, intricate stained glass windows, and candles lighting the dark interior.",
                        "A small, tranquil boat floating on a foggy lake at sunrise, with mist rising from the water’s surface."};
                // Create an instance of Random
                Random random = new Random();

                // Get a random index
                int randomIndex = random.nextInt(prompts.length);

                // Select the prompt at the random index
                String randomPrompt = prompts[randomIndex];
                callback.onResponse(randomPrompt);
                // Print the selected prompt
                System.out.println("Randomly selected prompt: " + randomPrompt);
                Log.e("ChatGPTService", "Randomly selected prompt: " + randomPrompt);

                if (response.isSuccessful() && response.body() != null) {
                    // Access the message content in the response
                    //String responseText = response.body().getChoices().get(0).getMessage().getContent();
                    //callback.onResponse(responseText);
                } else {
                    // Log detailed error information
                    //Log.e("ChatGPTService", "Request failed: " + response.message() + ", Code: " + response.code());
                    //callback.onFailure("Request failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                callback.onFailure("Request failed: " + t.getMessage());
            }
        });
    }
    // Method to read prompts from a text fil
    public interface ChatResponseCallback {
        void onResponse(String response); // For successful responses
        void onFailure(String error);     // For error handling
    }
}
