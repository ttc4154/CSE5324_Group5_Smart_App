package com.example.stablediffusion.api;
import android.content.Context;
import android.util.Base64;
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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InpaintResponse {
    private final String url = "https://stablediffusionapi.com/api/v3/inpaint"; // Change to inpainting API
    private final Context context;

    public InpaintResponse(Context context) {
        this.context = context;
    }

    public void inpaint(String initImageUrl, String maskImageUrl, String prompt, OnLoaded onLoaded) {
        ArrayList<String> arrayList = new ArrayList<>();
        JSONObject js = new JSONObject();
        Log.d("InpaintResponse", "initImageUrl: " + initImageUrl);
        Log.d("InpaintResponse", "maskImageUrl: " + maskImageUrl);
        Log.d("InpaintResponse", "prompt: " + prompt);
        try {
            // Validate the URLs before encoding
            if (initImageUrl == null || initImageUrl.isEmpty()) {
                Toast.makeText(context, "Init image URL is required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (maskImageUrl == null || maskImageUrl.isEmpty()) {
                Toast.makeText(context, "Mask image URL is required", Toast.LENGTH_SHORT).show();
                return;
            }
            String key = "your_key_here";
            js.put("key", key);
            //js.put("init_image", encodeImageToBase64(initImageUrl)); // Encode initial image
            js.put("init_image", initImageUrl); // Encode initial image
            //js.put("mask_image", encodeImageToBase64(maskImageUrl)); // Encode mask image
            js.put("mask_image", maskImageUrl); // Encode mask image
            js.put("prompt", prompt);
            js.put("width", 512);
            js.put("height", 512);
            js.put("samples", 1);
            js.put("num_inference_steps", 30);
            js.put("guidance_scale", 7.5);
            js.put("strength", 0.7);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, js, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    // Log the entire response
                    Log.d("InpaintResponse", "Response: " + response.toString());
                    // Handle the response
                    if (response.has("image_url")) {
                        String inpaintedImageUrl = response.getString("image_url");
                        arrayList.add(inpaintedImageUrl);
                        onLoaded.loaded(arrayList);
                    } else if (response.has("error")) {
                        String errorMessage = response.getString("error");
                        Toast.makeText(context, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "No image returned", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
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
