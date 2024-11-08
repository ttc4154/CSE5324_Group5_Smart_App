package com.example.stablediffusion.login;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.stablediffusion.R;
import com.example.stablediffusion.appactivity.GalleryActivity;
import com.example.stablediffusion.appactivity.Img2ImgActivity;
import com.example.stablediffusion.appactivity.SettingsActivity;
import com.example.stablediffusion.appactivity.T2iActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int STORAGE_PERMISSION_CODE = 2;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView displayNameText, emailText;
    private ImageView profileImage;
    private Button editProfileButton, changeProfilePictureButton;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        displayNameText = findViewById(R.id.display_name);
        emailText = findViewById(R.id.email);
        profileImage = findViewById(R.id.profile_image);
        editProfileButton = findViewById(R.id.edit_profile_button);
        changeProfilePictureButton = findViewById(R.id.change_profile_picture_button);

        if (currentUser != null) {
            loadUserProfile();
        }

        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        changeProfilePictureButton.setOnClickListener(v -> openImagePicker());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestStoragePermission();
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void loadUserProfile() {
        String userId = currentUser.getUid();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        displayNameText.setText(documentSnapshot.getString("displayName"));
                        emailText.setText(currentUser.getEmail());
                        String photoUrl = documentSnapshot.getString("photoUrl");
                        if (photoUrl != null) {
                            Glide.with(this).load(photoUrl).into(profileImage);
                        }
                    } else {
                        Toast.makeText(this, "User profile does not exist", Toast.LENGTH_SHORT).show();
                        createDefaultUserProfile();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show());
    }

    private void uploadProfileImageToFirebase(Uri imageUri) {
        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference("profile_images/" + currentUser.getUid());

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    db.collection("users").document(currentUser.getUid())
                            .update("photoUrl", uri.toString())
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to update profile picture", Toast.LENGTH_SHORT).show());
                }))
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show());
    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            profileImage.setImageURI(imageUri);
            uploadProfileImageToFirebase(imageUri);
        }
    }


    private void createDefaultUserProfile() {
        String userId = currentUser.getUid();
        Map<String, Object> defaultProfile = new HashMap<>();
        defaultProfile.put("displayName", "New User");
        defaultProfile.put("email", currentUser.getEmail());
        defaultProfile.put("photoUrl", null); // or provide a default image URL

        db.collection("users").document(userId)
                .set(defaultProfile)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Default profile created", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to create default profile", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_user_profile);
    }
    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        Class<?> activityClass = null;

        if (itemId == R.id.navigation_img2img) activityClass = Img2ImgActivity.class;
        else if (itemId == R.id.navigation_t2i) activityClass = T2iActivity.class;
        else if (itemId == R.id.navigation_settings) activityClass = SettingsActivity.class;
        else if (itemId == R.id.navigation_gallery) activityClass = GalleryActivity.class;
        else if (itemId == R.id.navigation_user_profile) return true;

        if (activityClass != null) {
            startActivity(new Intent(this, activityClass));
            finish();
        }
        return true;
    }
}
