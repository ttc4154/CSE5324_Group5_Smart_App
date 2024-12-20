package com.example.stablediffusion.appactivity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stablediffusion.R;
import com.example.stablediffusion.api.ChatGPTService;
import com.example.stablediffusion.api.ImageRequest; // Import ImageRequest model
import com.example.stablediffusion.api.ImageResponse; // Import ImageResponse model
import com.example.stablediffusion.login.UserProfileActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class T2iActivity extends AppCompatActivity {
    private TextInputLayout promptLayout;
    private TextInputEditText promptET;
    private Button clearPromptButton;
    private SeekBar width;
    private SeekBar height;
    private SeekBar imageCount;
    private RecyclerView recyclerView;
    private Button generateButton;
    private Button chatGptButton;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth; // Firebase Auth instance

    // Activity Result Launcher for permission request
    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if (result) {
                    Toast.makeText(T2iActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(T2iActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_t2i);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI elements
        promptLayout = findViewById(R.id.promptLayout);
        promptET = findViewById(R.id.promptET);
        clearPromptButton = findViewById(R.id.clear_prompt);
        width = findViewById(R.id.width);
        height = findViewById(R.id.height);
        imageCount = findViewById(R.id.imageCount);
        generateButton = findViewById(R.id.generate);
        chatGptButton = findViewById(R.id.chat_gpt);
        recyclerView = findViewById(R.id.recycler);

        // ProgressDialog initialization
        progressDialog = new ProgressDialog(T2iActivity.this);
        progressDialog.setMessage("Generating...T2Activity");

        // Set OnClickListener for the generate button
        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check for permission
                if (ActivityCompat.checkSelfPermission(T2iActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    activityResultLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                } else {
                    // Validate prompt text
                    if (Objects.requireNonNull(promptET.getText()).toString().isEmpty()) {
                        promptLayout.setError("This Field is Required");
                    } else {
                        progressDialog.show();
                        // Call to generate images
                        new ImageResponse(T2iActivity.this).generate(
                                promptET.getText().toString(),
                                width.getProgress(),
                                height.getProgress(),
                                imageCount.getProgress(),
                                arrayList -> {
                                    progressDialog.dismiss();
                                    ImageRequest request = new ImageRequest(T2iActivity.this, arrayList);
                                    recyclerView.setAdapter(request);
                                }
                        );
                    }
                }
            }
        });

        clearPromptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear the text from the prompt TextInputEditText
                if (promptET != null) {
                    promptET.setText(""); // Clear the text
                }
            }
        });

        chatGptButton.setOnClickListener(new View.OnClickListener() {
            /*"Generate a random image prompt."
                "Generate a random prompt to create an image."
                "Generate a random image generation prompt."*/
            @Override
            public void onClick(View v) {
                // Define the prompt text to send to ChatGPT
                String promptText = "Generate a random image prompt.";
                // Call the ChatGPT API with the prompt text and handle the response
                Log.d("chatGptButton", "Sending request with prompt: " + promptText);
                // Call the ChatGPT API with the prompt text and handle the response
                ChatGPTService chatService = new ChatGPTService();
                chatService.getChatResponse(promptText, new ChatGPTService.ChatResponseCallback() {
                    @Override
                    public void onResponse(String response) {
                        // Display the response in a TextView (or handle as needed)
                        //runOnUiThread(() -> responseTextView.setText(response));
                        promptET.setText(response);
                        Log.d("chatGptButton", "Response received: " + response);
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.d("chatGptButton", "Error: " + error);
                        // Display the error in case of failure
                        //runOnUiThread(() -> responseTextView.setText("Error: " + error));
                    }
                });
            }
        });
    }
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Exit")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    finishAffinity(); // Close the app
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }
    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        Class<?> activityClass = null;

        if (itemId == R.id.navigation_img2img) activityClass = Img2ImgActivity.class;
        else if (itemId == R.id.navigation_t2i) return true;
        else if (itemId == R.id.navigation_settings) activityClass = SettingsActivity.class;
        else if (itemId == R.id.navigation_gallery) activityClass = GalleryActivity.class;
        else if (itemId == R.id.navigation_user_profile) activityClass = UserProfileActivity.class;

        if (activityClass != null) {
            startActivity(new Intent(this, activityClass));
            finish();
        }
        return true;
    }
}
