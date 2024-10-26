package com.example.stablediffusion;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stablediffusion.login.LoginActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import com.example.stablediffusion.api.ImageRequest; // Import our ImageRequest model
import com.example.stablediffusion.api.ImageResponse; // Import our ImageResponse model
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;



import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean result) {
            if (result) {
                Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null)
        {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish(); // Close main activity
        }
        // Initialize logout button
        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });

        TextInputLayout promptLayout = findViewById(R.id.promptLayout);
        TextInputEditText promptET = findViewById(R.id.promptET);

        SeekBar width = findViewById(R.id.width);
        SeekBar height = findViewById(R.id.height);
        SeekBar imageCount = findViewById(R.id.imageCount);

        Button generate = findViewById(R.id.generate);
        RecyclerView recyclerView = findViewById(R.id.recycler);

        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Generating...");

        OnLoaded onLoaded = new OnLoaded() {
            @Override
            public void loaded(ArrayList<String> arrayList) {
                progressDialog.dismiss();
                ImageRequest request = new ImageRequest(MainActivity.this, arrayList);
                recyclerView.setAdapter(request);
            }
        };

        generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    activityResultLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                } else {
                    if (Objects.requireNonNull(promptET.getText()).toString().isEmpty()) {
                        promptLayout.setError("This Field is Required");
                    } else {
                        progressDialog.show();
                        new ImageResponse(MainActivity.this).generate(promptET.getText().toString(), width.getProgress(), height.getProgress(), imageCount.getProgress(), onLoaded);
                    }
                }
            }
        });
    }
    private void logoutUser() {
        mAuth.signOut(); // Sign out from Firebase
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Navigate back to LoginActivity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Clear activity stack
        startActivity(intent);
        finish(); // Close MainActivity
    }
}
