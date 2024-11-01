package com.example.stablediffusion.appactivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
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
        // Load the saved theme preference before calling super
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = preferences.getString("theme", "light"); // default to light theme
        if (theme.equals("dark")) {
            setTheme(R.style.AppTheme_Dark); // Ensure you have a defined dark theme
        } else {
            setTheme(R.style.AppTheme); // Ensure you have a defined light theme
        }

        super.onCreate(savedInstanceState); // Call super after setting the theme
        setContentView(R.layout.activity_settings);

        TextView aboutOption = findViewById(R.id.setting_about);
        aboutOption.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, AboutActivity.class);
            startActivity(intent);
        });

        // API Key Settings option
        TextView apiKeyOption = findViewById(R.id.setting_api_key);
        apiKeyOption.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, ApiKeyActivity.class);
            startActivity(intent);
        });

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
        // Setup RadioGroup for theme selection
        RadioGroup themeSelection = findViewById(R.id.theme_selection);
        RadioButton lightTheme = findViewById(R.id.light_theme);
        RadioButton darkTheme = findViewById(R.id.dark_theme);

        // Set checked state based on saved preference
        if (theme.equals("dark")) {
            darkTheme.setChecked(true);
        } else {
            lightTheme.setChecked(true);
        }

        themeSelection.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.light_theme) {
                    changeTheme("light");
                } else if (checkedId == R.id.dark_theme) {
                    changeTheme("dark");
                }
            }
        });
    }

    private void changeTheme(String theme) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.edit().putString("theme", theme).apply();

        // Recreate the activity to apply the new theme
        recreate();
    }
}
