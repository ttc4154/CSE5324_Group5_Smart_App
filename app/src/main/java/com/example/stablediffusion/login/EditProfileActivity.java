package com.example.stablediffusion.login;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.stablediffusion.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etDisplayName, etPhoneNumber, etAddress;
    private Button btnSaveChanges;
    private ImageView ivProfilePicture;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Find views by ID
        etDisplayName = findViewById(R.id.etDisplayName);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etAddress = findViewById(R.id.etAddress);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        ivProfilePicture = findViewById(R.id.ivProfilePicture);

        // Load current user data from Firestore
        loadUserProfileData();

        // Set click listener to save changes
        btnSaveChanges.setOnClickListener(v -> saveChanges());
    }

    private void loadUserProfileData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Load user data from Firestore
            DocumentReference userRef = firestore.collection("users").document(user.getUid());
            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // Retrieve and set user data
                    String displayName = documentSnapshot.getString("displayName");
                    String phoneNumber = documentSnapshot.getString("phoneNumber");
                    String address = documentSnapshot.getString("address");
                    String photoUrl = documentSnapshot.getString("photoUrl");

                    etDisplayName.setText(displayName);
                    etPhoneNumber.setText(phoneNumber);
                    etAddress.setText(address);

                    // Load profile picture if available
                    if (photoUrl != null) {
                        Glide.with(this).load(photoUrl).into(ivProfilePicture);
                    }
                } else {
                    Toast.makeText(this, "User profile does not exist.", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void saveChanges() {
        String displayName = etDisplayName.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (TextUtils.isEmpty(displayName) || TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(address)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Update display name in Firebase Authentication
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build();

            user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Save other details to Firestore
                    DocumentReference userRef = firestore.collection("users").document(user.getUid());
                    userRef.update("displayName", displayName,
                            "phoneNumber", phoneNumber,
                            "address", address).addOnCompleteListener(dbTask -> {
                        if (dbTask.isSuccessful()) {
                            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, "Error updating profile in Firestore", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(this, "Error updating display name", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
