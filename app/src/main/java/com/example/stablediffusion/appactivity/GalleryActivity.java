package com.example.stablediffusion.appactivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stablediffusion.R;
import com.example.stablediffusion.login.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_READ_MEDIA_IMAGES = 101;
    private RecyclerView recyclerView;
    private GalleryAdapter adapter;
    private List<Uri> imageUris;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        recyclerView = findViewById(R.id.recycler_view);

        // Check for permissions
        checkPermissionAndLoadImages();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_img2img) {
                    startActivity(new Intent(GalleryActivity.this, Img2ImgActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.navigation_t2i) {
                    startActivity(new Intent(GalleryActivity.this, T2iActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.navigation_settings) {
                    startActivity(new Intent(GalleryActivity.this, SettingsActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.navigation_gallery) {
                    Toast.makeText(GalleryActivity.this, "You are already in GalleryActivity", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.navigation_logout) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(GalleryActivity.this, LoginActivity.class));
                    finish();
                    Toast.makeText(GalleryActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });
    }

    private void checkPermissionAndLoadImages() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        REQUEST_CODE_READ_MEDIA_IMAGES);
            } else {
                // Permission granted, load images
                loadImages();
            }
        } else {
            // For older versions, request WRITE_EXTERNAL_STORAGE if needed
            loadImages();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_READ_MEDIA_IMAGES) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadImages();
            } else {
                Toast.makeText(this, "Permission denied to read media images", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadImages() {
        imageUris = new ArrayList<>();
        Uri collection;

        // Use MediaStore based on the Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        String[] projection = new String[]{
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATA // This column gives the file path
        };

        // Define the selection criteria to filter for images in the Downloads folder
        String selection = MediaStore.Images.Media.DATA + " LIKE ?";
        String[] selectionArgs = new String[]{Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "%"};

        try (Cursor cursor = getContentResolver().query(
                collection,
                projection,
                selection,
                selectionArgs,
                null)) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                    long id = cursor.getLong(idColumn);
                    Uri contentUri = Uri.withAppendedPath(collection, String.valueOf(id));
                    imageUris.add(contentUri);
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading images: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        // Set up the RecyclerView
        if (imageUris.isEmpty()) {
            Toast.makeText(this, "No images found in Downloads folder.", Toast.LENGTH_SHORT).show();
        } else {
            adapter = new GalleryAdapter(this, imageUris, this::onImageClick);
            recyclerView.setLayoutManager(new GridLayoutManager(this, 3)); // 3 columns
            recyclerView.setAdapter(adapter);
        }
    }

    private void onImageClick(Uri imageUri) {
        Intent intent = new Intent(GalleryActivity.this, FullImageActivity.class);
        intent.putExtra("imageUri", imageUri.toString());
        startActivity(intent);
    }
}
