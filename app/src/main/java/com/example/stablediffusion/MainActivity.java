package com.example.stablediffusion;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import com.example.stablediffusion.appactivity.Img2ImgActivity;
import com.example.stablediffusion.appactivity.T2tActivity;
import com.example.stablediffusion.login.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
        FirebaseUser user = mAuth.getCurrentUser();

        // Check if user is logged in
        if (user == null) {
            // User is not logged in; redirect to LoginActivity
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish(); // Close MainActivity
            return; // Exit onCreate to avoid proceeding with BottomNavigation setup
        }

        // Set up BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Handle navigation item selection
                int id = item.getItemId();
                if (id == R.id.navigation_img2img) {
                    Log.d("MainActivity", "Img2Img selected");
                    Toast.makeText(MainActivity.this, "Img2Img clicked", Toast.LENGTH_SHORT).show();
                    Intent intentImg2Img = new Intent(MainActivity.this, Img2ImgActivity.class);
                    startActivity(intentImg2Img);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                } else if (id == R.id.navigation_t2t) {
                    Log.d("MainActivity", "T2t selected");
                    Intent intentT2t = new Intent(MainActivity.this, T2tActivity.class);
                    startActivity(intentT2t);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                }
                // Handle more items as necessary
                return false;
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Ensure the bottom navigation is properly set when returning
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        // Optionally, set the selected item back to the default or current state
        // bottomNavigationView.setSelectedItemId(R.id.navigation_home); // or any other item
    }
}
