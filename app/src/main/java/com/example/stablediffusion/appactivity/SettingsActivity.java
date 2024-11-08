package com.example.stablediffusion.appactivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.stablediffusion.R;
import com.example.stablediffusion.login.LoginActivity;
import com.example.stablediffusion.login.UserProfileActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {
    private EditText apiKeyInput;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "AppSettingsPrefs";
    private static final String API_KEY = "ApiKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Load the saved theme preference before calling super
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = preferences.getString("theme", "light"); // default to light theme

        if (theme.equals("dark")) {
            setTheme(R.style.AppTheme_Dark); // Ensure to have a defined dark theme
        } else {
            setTheme(R.style.AppTheme); // Ensure to have a defined light theme
        }
        super.onCreate(savedInstanceState); // Call super after setting the theme
        setContentView(R.layout.activity_settings); // Set content view at the beginning
        // Initialize views after setting content view
        Button logoutButton = findViewById(R.id.logout_button);

        if (logoutButton != null) {
            logoutButton.setOnClickListener(v -> {
                // Log out the user
                FirebaseAuth.getInstance().signOut();

                // Redirect to LoginActivity (or any other login screen)
                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();

                Toast.makeText(SettingsActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            });
        }

        TextView aboutOption = findViewById(R.id.setting_about);
        aboutOption.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, AboutActivity.class);
            startActivity(intent);
        });

        // Initialize views
        apiKeyInput = findViewById(R.id.api_key_input);
        Button saveApiKeyButton = findViewById(R.id.save_api_settings_button);
        Button editApiKeyButton = findViewById(R.id.edit_api_key_button);

        // Load and display the saved API key
        sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        String savedApiKey = sharedPreferences.getString(API_KEY, "");
        apiKeyInput.setText(savedApiKey);

        // If API Key is already set, disable the EditText
        if (!savedApiKey.isEmpty()) {
            apiKeyInput.setEnabled(false);
            editApiKeyButton.setText("Edit API Key");
            saveApiKeyButton.setVisibility(Button.GONE); // Hide save button if key is set
        }

        // Set save button click listener
        saveApiKeyButton.setOnClickListener(v -> {
            String apiKey = apiKeyInput.getText().toString();
            if (!apiKey.isEmpty()) {
                saveApiKey(apiKey);
                Toast.makeText(SettingsActivity.this, "API Key saved!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SettingsActivity.this, "Please enter a valid API Key", Toast.LENGTH_SHORT).show();
            }
        });

        // Set edit button click listener
        editApiKeyButton.setOnClickListener(v -> {
            if (apiKeyInput.isEnabled()) {
                // If EditText is enabled, save the new API key
                String newApiKey = apiKeyInput.getText().toString();
                if (!newApiKey.isEmpty()) {
                    saveApiKey(newApiKey);
                    Toast.makeText(SettingsActivity.this, "API Key saved!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SettingsActivity.this, "Please enter a valid API Key", Toast.LENGTH_SHORT).show();
                }

                // Disable EditText and update button text
                apiKeyInput.setEnabled(false);
                editApiKeyButton.setText("Edit API Key");
                saveApiKeyButton.setVisibility(Button.GONE); // Hide save button after saving
            } else {
                // If EditText is disabled, enable it for editing
                apiKeyInput.setEnabled(true);
                apiKeyInput.setSelection(apiKeyInput.getText().length()); // Move cursor to the end
                editApiKeyButton.setText("Save API Key");
                saveApiKeyButton.setVisibility(Button.VISIBLE); // Show save button
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);

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

    // Method to save the API key in SharedPreferences
    private void saveApiKey(String apiKey) {
        // Use "AppPreferences" for SharedPreferences file
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(API_KEY, apiKey); // Store the API key using API_KEY constant
        editor.apply();
        Log.d("SettingsActivity", "API Key saved successfully: " + apiKey);
    }

    private void changeTheme(String theme) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.edit().putString("theme", theme).apply();
        recreate(); // Recreate the activity to apply the new theme
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_settings); // Set the default selection
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        Class<?> activityClass = null;

        if (itemId == R.id.navigation_img2img) activityClass = Img2ImgActivity.class;
        else if (itemId == R.id.navigation_t2i) activityClass = T2iActivity.class;
        else if (itemId == R.id.navigation_settings) return true;
        else if (itemId == R.id.navigation_gallery) activityClass = GalleryActivity.class;
        else if (itemId == R.id.navigation_user_profile) activityClass = UserProfileActivity.class;

        if (activityClass != null) {
            startActivity(new Intent(this, activityClass));
            finish();
        }
        return true;
    }
}
