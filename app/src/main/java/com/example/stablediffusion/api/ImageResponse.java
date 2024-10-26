package com.example.stablediffusion.api;
import android.content.Context;
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
        try {
            String key = "key here"; //w9vuSH0efmulpEFaB1qWiXoHOowHhUMw1RyFMWFozpHJyCGiqNFGyg5wWGIA
            js.put("key", key);
            js.put("prompt", prompt);
            js.put("samples", count);
            js.put("width", width);
            js.put("height", height);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Inside your onResponse method:
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, js, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //if (response != null) {
                if (true) {
                    //Log.d("ImageGenerator", "Full Response: " + response.toString()); // Log the full JSON response
					//JSONArray dataArray;
                    //try {
//                        JSONArray linksArray = response.getJSONArray("links"); // Fetch the "links" array from the JSON response
						//dataArray = response.getJSONArray("output");
						for (int i = 0; i < count; i++) {
                            //arrayList.add(dataArray.getString(i));
                            arrayList.add("https://pub-3626123a908346a7a8be8d9295f44e26.r2.dev/generations/ca86c5f2-85df-4872-b70b-839d0ddd2524-2.png"); //hardcode them for now, later will get it from the api
                            //Log.d("ImageGenerator", "Image URL: " + dataArray.getString(i)); // Log each individual image URL
                        }

                        onLoaded.loaded(arrayList);
                    //} //catch (JSONException e) {
                        //e.printStackTrace();
                    //}
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Log.e("ImageGenerator", "Error: " + error.getMessage());
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
