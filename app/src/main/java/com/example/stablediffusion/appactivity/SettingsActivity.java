package com.example.stablediffusion.appactivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.stablediffusion.R;
import com.example.stablediffusion.login.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_img2img) {
                    // Switch to Img2ImgActivity
                    Intent intent = new Intent(SettingsActivity.this, Img2ImgActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.navigation_t2i) {
                    // Switch to SettingsActivity
                    Intent intent = new Intent(SettingsActivity.this, T2iActivity.class);
                    startActivity(intent);
                    finish(); // Optional
                    return true;
                } else if (itemId == R.id.navigation_settings) {
                    // Stay in SettingsActivity
                    Toast.makeText(SettingsActivity.this, "You are already in SettingsActivity", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.navigation_gallery) {
                    // Switch to GalleryActivity
                    Intent intent = new Intent(SettingsActivity.this, GalleryActivity.class);
                    startActivity(intent);
                    finish(); // Optional
                    return true;
                }else if (itemId == R.id.navigation_logout) {
                    // Perform logout
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish(); // Close the current activity
                    Toast.makeText(SettingsActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false; // Unhandled cases
            }
        });
    }
}