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
import com.example.stablediffusion.api.ImageRequest; // Import your ImageRequest model
import com.example.stablediffusion.api.ImageResponse; // Import your ImageResponse model
import com.example.stablediffusion.login.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Objects;

public class T2tActivity extends AppCompatActivity {
    private TextInputLayout promptLayout;
    private TextInputEditText promptET;
    private SeekBar width;
    private SeekBar height;
    private SeekBar imageCount;
    private RecyclerView recyclerView;
    private Button generateButton;
    private Button logoutButton; // Add a logout button
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth; // Firebase Auth instance

    // Activity Result Launcher for permission request
    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if (result) {
                        Toast.makeText(T2tActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(T2tActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_t2t);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.navigation_img2img) {
                    // Start Img2ImgActivity
                    Intent intentImg2Img = new Intent(T2tActivity.this, Img2ImgActivity.class);
                    startActivity(intentImg2Img);
                    finish(); // Optional, depending on your navigation design
                    return true;
                } else if (item.getItemId() == R.id.navigation_t2t) {
                    // Stay in T2tActivity
                    Toast.makeText(T2tActivity.this, "You are already in T2t", Toast.LENGTH_SHORT).show();
                    return true;
                } else {
                    return false; // Return false for unhandled cases
                }
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
        logoutButton = findViewById(R.id.logoutButton); // Initialize logout button
        recyclerView = findViewById(R.id.recycler);

        // ProgressDialog initialization
        progressDialog = new ProgressDialog(T2tActivity.this);
        progressDialog.setMessage("Generating...T2Activity");

        // Set OnClickListener for the generate button
        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check for permission
                if (ActivityCompat.checkSelfPermission(T2tActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    activityResultLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                } else {
                    // Validate prompt text
                    if (Objects.requireNonNull(promptET.getText()).toString().isEmpty()) {
                        promptLayout.setError("This Field is Required");
                    } else {
                        progressDialog.show();
                        // Call to generate images
                        new ImageResponse(T2tActivity.this).generate(
                                promptET.getText().toString(),
                                width.getProgress(),
                                height.getProgress(),
                                imageCount.getProgress(),
                                new OnLoaded() {
                                    @Override
                                    public void loaded(ArrayList<String> arrayList) {
                                        progressDialog.dismiss();
                                        ImageRequest request = new ImageRequest(T2tActivity.this, arrayList);
                                        recyclerView.setAdapter(request);
                                    }
                                }
                        );
                    }
                }
            }
        });

        // Set OnClickListener for the logout button
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });
    }

    private void logoutUser() {
        mAuth.signOut(); // Sign out from Firebase
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Navigate back to LoginActivity
        Intent intent = new Intent(T2tActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Clear activity stack
        startActivity(intent);
        finish(); // Close T2tActivity
    }
}
