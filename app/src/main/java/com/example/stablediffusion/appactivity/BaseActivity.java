package com.example.stablediffusion.appactivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.stablediffusion.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_with_bottom_navigation);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_t2i) {
                    startActivity(new Intent(BaseActivity.this, T2iActivity.class));
                    return true;
                } else if (itemId == R.id.navigation_img2img) {
                    startActivity(new Intent(BaseActivity.this, Img2ImgActivity.class));
                    return true;
                } else if (itemId == R.id.navigation_gallery) {
                    startActivity(new Intent(BaseActivity.this, GalleryActivity.class));
                    return true;
                } else if (itemId == R.id.navigation_settings) {
                    startActivity(new Intent(BaseActivity.this, SettingsActivity.class));
                    return true;
                } else {
                    return false; // Return false for unhandled cases
                }
            }
        });
    }
}
