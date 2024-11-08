package com.example.stablediffusion.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.stablediffusion.OnLoaded;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ImageResponse {
    private final String url = "https://stablediffusionapi.com/api/v3/text2img";
    private final Context context;

    public ImageResponse(Context context) {
        this.context = context;
    }

    public void generate(String prompt, int width, int height, int count, OnLoaded onLoaded) {
        ArrayList<String> arrayList = new ArrayList<>();
        JSONObject js = new JSONObject();

        // Retrieve API key from SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        String apiKey = sharedPreferences.getString("ApiKey", null);

        if (apiKey == null || apiKey.isEmpty()) {
            Toast.makeText(context, "API key not set. Please set the API key in settings.", Toast.LENGTH_SHORT).show();
            Log.e("ImageResponse", "API key is null or empty.");
            return;
        } else {
            Log.d("ImageResponse", "Retrieved API Key: " + apiKey);
        }

        try {
            boolean DEBUG = true;
            if(DEBUG){
                js.put("key", "DEBUG");
            }else{
                js.put("key", apiKey);
            }
            js.put("prompt", prompt);
            js.put("samples", count);
            js.put("width", width);
            js.put("height", height);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, js, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                boolean DEBUG = true;
                if (DEBUG)
                {
                    if (true) {
                        //Log.d("ImageGenerator", "Full Response: " + response.toString()); // Log the full JSON response
                        //JSONArray dataArray;
                        //try {
//                        JSONArray linksArray = response.getJSONArray("links"); // Fetch the "links" array from the JSON response
                        //dataArray = response.getJSONArray("output");
                        for (int i = 0; i < count; i++) {
                            //arrayList.add(dataArray.getString(i));
                            arrayList.add("https://pub-3626123a908346a7a8be8d9295f44e26.r2.dev/generations/dd174091-53aa-43d8-afc4-ea5f56d9c3f7-2.png"); //hardcode them for now, later will get it from the api
                            //Log.d("ImageGenerator", "Image URL: " + dataArray.getString(i)); // Log each individual image URL
                        }

                        onLoaded.loaded(arrayList);
                        //} //catch (JSONException e) {
                        //e.printStackTrace();
                        //}
                    }
                }
                else{
                    if (response != null) {
                        Log.d("ImageGenerator", "Full Response: " + response.toString()); // Log the full JSON response
                        try {
                            // Check if "output" array exists in the response
                            if (response.has("output")) {
                                JSONArray outputArray = response.getJSONArray("output");
                                for (int i = 0; i < Math.min(count, outputArray.length()); i++) {
                                    String imageUrl = outputArray.getString(i);
                                    arrayList.add(imageUrl);
                                    //Image URL: https://pub-3626123a908346a7a8be8d9295f44e26.r2.dev/generations/dd174091-53aa-43d8-afc4-ea5f56d9c3f7-0.png
                                    //Image URL: https://pub-3626123a908346a7a8be8d9295f44e26.r2.dev/generations/dd174091-53aa-43d8-afc4-ea5f56d9c3f7-1.png
                                    //Image URL: https://pub-3626123a908346a7a8be8d9295f44e26.r2.dev/generations/dd174091-53aa-43d8-afc4-ea5f56d9c3f7-2.png
                                    Log.d("ImageGenerator", "Image URL: " + imageUrl); // Log each individual image URL
                                }
                                onLoaded.loaded(arrayList);
                            } else {
                                Log.e("ImageGenerator", "No 'output' field in response.");
                                Toast.makeText(context, "Image URL not found in response.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(context, "Error processing the API response.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("ImageGenerator", "Error: " + error.getMessage());
                Toast.makeText(context, "There was an error while getting images", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-type", "application/json");
                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);
    }
}
