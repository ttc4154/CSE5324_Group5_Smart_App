package com.example.stablediffusion.appactivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stablediffusion.OnLoaded;
import com.example.stablediffusion.R;
import com.example.stablediffusion.api.ImageRequest; // Import ImageRequest model
import com.example.stablediffusion.api.ImageResponse; // Import ImageResponse model
import com.example.stablediffusion.login.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Objects;

public class T2iActivity extends AppCompatActivity {
    private TextInputLayout promptLayout;
    private TextInputEditText promptET;
    private SeekBar width;
    private SeekBar height;
    private SeekBar imageCount;
    private RecyclerView recyclerView;
    private Button generateButton;
    //private Button logoutButton; // Add a logout button
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth; // Firebase Auth instance

    // Activity Result Launcher for permission request
    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if (result) {
                        Toast.makeText(T2iActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(T2iActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_t2i);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_img2img) {
                    // Switch to Img2ImgActivity
                    Intent intent = new Intent(T2iActivity.this, Img2ImgActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.navigation_t2i) {
                    // Stay in T2iActivity
                    Toast.makeText(T2iActivity.this, "You are already in T2i", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.navigation_settings) {
                    // Switch to SettingsActivity
                    Intent intent = new Intent(T2iActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    finish(); // Optional
                    return true;
                } else if (itemId == R.id.navigation_gallery) {
                    // Switch to GalleryActivity
                    Intent intent = new Intent(T2iActivity.this, GalleryActivity.class);
                    startActivity(intent);
                    finish(); // Optional
                    return true;
                }else if (itemId == R.id.navigation_logout) {
                    // Perform logout
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(T2iActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish(); // Close the current activity
                    Toast.makeText(T2iActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false; // Unhandled cases
            }
        });
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI elements
        promptLayout = findViewById(R.id.promptLayout);
        promptET = findViewById(R.id.promptET);
        width = findViewById(R.id.width);
        height = findViewById(R.id.height);
        imageCount = findViewById(R.id.imageCount);
        generateButton = findViewById(R.id.generate);
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
                                new OnLoaded() {
                                    @Override
                                    public void loaded(ArrayList<String> arrayList) {
                                        progressDialog.dismiss();
                                        ImageRequest request = new ImageRequest(T2iActivity.this, arrayList);
                                        recyclerView.setAdapter(request);
                                    }
                                }
                        );
                    }
                }
            }
        });
    }
    @Override
    public void onBackPressed() {
        // Finish the activity and exit the application
        super.onBackPressed();
        finishAffinity(); // This will close all activities and exit the app
        // Alternatively, can use finishAndRemoveTask();
        // finishAndRemoveTask();
    }
}
