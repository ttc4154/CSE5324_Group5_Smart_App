package com.example.stablediffusion.appactivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.stablediffusion.R;

public class ApiKeyActivity extends AppCompatActivity {

    private EditText apiKeyInput;
    private SharedPreferences sharedPreferences;

    private static final String PREFS_NAME = "AppSettingsPrefs";
    private static final String API_KEY = "ApiKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_api_key);

        apiKeyInput = findViewById(R.id.api_key_input);
        Button saveApiKeyButton = findViewById(R.id.save_api_settings_button);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Load and display the saved API key
        String savedApiKey = sharedPreferences.getString(API_KEY, "");
        apiKeyInput.setText(savedApiKey);

        // Save API Key Button functionality
        saveApiKeyButton.setOnClickListener(v -> {
            String apiKey = apiKeyInput.getText().toString();
            if (!apiKey.isEmpty()) {
                saveApiKey(apiKey);
                Toast.makeText(ApiKeyActivity.this, "API Key saved!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ApiKeyActivity.this, "Please enter a valid API Key", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // TODO::Need to pass this key to ImageRequest/Response API
    // Method to save the API key in SharedPreferences
    private void saveApiKey(String apiKey) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(API_KEY, apiKey);
        editor.apply();
    }
}