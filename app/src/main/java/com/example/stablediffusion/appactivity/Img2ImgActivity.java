package com.example.stablediffusion.appactivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.stablediffusion.R;
import com.example.stablediffusion.api.InpaintRequest;
import com.example.stablediffusion.api.InpaintResponse;
import com.example.stablediffusion.login.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import androidx.recyclerview.widget.RecyclerView;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Img2ImgActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 1;
    private static final int CAPTURE_IMAGE = 2;
    private static final int PICK_IMAGE_FROM_CLOUD = 3;
    private ImageAdapterForCloud imageAdapter;
    private List<ImageModelForCloud> imageList = new ArrayList<>();
    private Uri imageUri;
    private ImageView selectedImageView; // ImageView to display the selected image
    private ProgressDialog progressDialog;
    private StorageReference storageReference;

    private String maskImageUrl;
    private String prompt;
    private Button generateButton;
    private String initImageUrl;
    private RecyclerView recyclerView;

    // Activity Result Launcher for camera permission request
    private final ActivityResultLauncher<String> cameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if (result) {
                    // Permission granted, open the camera
                    openCamera();
                } else {
                    Toast.makeText(Img2ImgActivity.this, "Camera permission is required to take pictures.", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_img2img);

        imageAdapter = new ImageAdapterForCloud(imageList, Img2ImgActivity.this, this::onImageClick);

        storageReference = FirebaseStorage.getInstance().getReference();

        selectedImageView = findViewById(R.id.selectedImageView);
        Button chooseButtonLocal = findViewById(R.id.chooseButtonLocal);
        Button chooseButtonCloud = findViewById(R.id.chooseButtonCloud);
        Button captureButton = findViewById(R.id.captureButton);
        Button uploadButton = findViewById(R.id.uploadButton); // Initialize upload button

        progressDialog = new ProgressDialog(Img2ImgActivity.this);
        progressDialog.setMessage("Uploading...");

        chooseButtonLocal.setOnClickListener(v -> openGallery());
        captureButton.setOnClickListener(v -> requestCameraPermission());

        // Set OnClickListener for the upload button
        uploadButton.setOnClickListener(v -> {
            if (imageUri != null) {
                uploadImageToFirebase(imageUri);
            } else {
                Toast.makeText(Img2ImgActivity.this, "No image selected to upload.", Toast.LENGTH_SHORT).show();
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_img2img) {
                    // Stay in SettingsActivity
                    Toast.makeText(Img2ImgActivity.this, "You are already in Img2ImgActivity", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.navigation_t2i) {
                    // Switch to SettingsActivity
                    Intent intent = new Intent(Img2ImgActivity.this, T2iActivity.class);
                    startActivity(intent);
                    finish(); // Optional
                    return true;
                } else if (itemId == R.id.navigation_settings) {
                    // Switch to SettingsActivity
                    Intent intent = new Intent(Img2ImgActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.navigation_gallery) {
                    // Switch to GalleryActivity
                    Intent intent = new Intent(Img2ImgActivity.this, GalleryActivity.class);
                    startActivity(intent);
                    finish(); // Optional
                    return true;
                }else if (itemId == R.id.navigation_logout) {
                    // Perform logout
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(Img2ImgActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish(); // Close the current activity
                    Toast.makeText(Img2ImgActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false; // Unhandled cases
            }
        });
        initImageUrl = "https://firebasestorage.googleapis.com/v0/b/cse5324-group5.firebasestorage.app/o/images%2FnsDuY1wGz3O3ZQ1ae9its7rOLyk2%2Fdog_on_the_bench.png?alt=media&token=c72bd84f-ef83-4caa-a801-bdc6671767a5";
        maskImageUrl = "https://firebasestorage.googleapis.com/v0/b/cse5324-group5.firebasestorage.app/o/images%2FnsDuY1wGz3O3ZQ1ae9its7rOLyk2%2Fdot_on_the_bench_mask.png?alt=media&token=eca71884-2b0b-45eb-961f-663d9da4b505";
        prompt = "a cat sitting on the bench";
        generateButton = findViewById(R.id.generateInpaint);

        // Set OnClickListener for the generate button
        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Validate prompt text
                progressDialog.show();
                progressDialog.setMessage("Generate...Inpaint");
                // Call to generate images
                new InpaintResponse(Img2ImgActivity.this).inpaint(
                        initImageUrl,
                        maskImageUrl,
                        prompt,
                        arrayList -> {
                            progressDialog.dismiss();
                            InpaintRequest request = new InpaintRequest(Img2ImgActivity.this, arrayList);
                            recyclerView.setAdapter(request);
                        }
                );
            }
        });
        // Initialize UI elements
        selectedImageView = findViewById(R.id.selectedImageView);

        // Set OnClickListener for the choose button
        chooseButtonLocal.setOnClickListener(v -> openGallery());

        // Set OnClickListener for the choose button
        chooseButtonCloud.setOnClickListener(v -> fetchImagesFromCloud());

        // Set OnClickListener for the capture button
        captureButton.setOnClickListener(v -> {
            requestCameraPermission(); // Request permission before opening the camera
        });
    }
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    private void fetchImagesFromCloud() {
        // Launch GalleryActivity for result
        Intent intent = new Intent(this, ImageGalleryForCloud.class);
        startActivityForResult(intent, PICK_IMAGE_FROM_CLOUD);
    }
    private void requestCameraPermission() {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Create a file to save the image
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Handle the error
                Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                imageUri = FileProvider.getUriForFile(this,
                        "com.example.stablediffusion.fileprovider",
                        photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, CAPTURE_IMAGE);
            }
        }
    }
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(null);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void onImageClick(Uri imageUri) {
        Intent intent = new Intent(Img2ImgActivity.this, FullImageActivity.class);
        intent.putExtra("imageUri", imageUri.toString());
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("Img2ImgActivity", "Received Intent Data: " + data);

        Log.d("PICK_IMAGE_FROM_CLOUD", "PICK_IMAGE_FROM_CLOUD: " + PICK_IMAGE_FROM_CLOUD);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE && data != null) {
                imageUri = data.getData();
                selectedImageView.setImageURI(imageUri); // Display the selected image from either Local or Cloud storage
                Toast.makeText(this, "Operation PICK_IMAGE", Toast.LENGTH_SHORT).show();
                return; // Exit after handling PICK_IMAGE
            }
            if (requestCode == PICK_IMAGE_FROM_CLOUD && data != null && data.hasExtra("selectedImageUri")) {
                String imageUriString = data.getStringExtra("selectedImageUri");
                imageUri = Uri.parse(imageUriString);
                Log.d("ImageGalleryForCloud", "Selected image URI: " + imageUri.toString());
                //selectedImageView.setImageURI(imageUri);
                if (imageUri != null) {
                    Glide.with(this)
                            .load(imageUri)
                            .into(selectedImageView);
                } else {
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                }

                Toast.makeText(this, "Operation PICK_IMAGE_FROM_CLOUD", Toast.LENGTH_SHORT).show();
                // Exit after handling PICK_IMAGE
            }
            else if (requestCode == CAPTURE_IMAGE) {
                selectedImageView.setImageURI(imageUri); // Display the captured image
                Toast.makeText(this, "Operation CAPTURE_IMAGE", Toast.LENGTH_SHORT).show();
                // Exit after handling PICK_IMAGE
            }
            else {
                Toast.makeText(this, "Operation canceled", Toast.LENGTH_SHORT).show();
            }
        }
    }
    // TODO::Add the ability to mask the image, aka image processing
    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri == null) {
            Toast.makeText(Img2ImgActivity.this, "No image selected to upload.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        // Get the current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid(); // Get the current user's ID
            // Create a unique filename
            String fileName = "images/" + userId + "/" + System.currentTimeMillis() + ".jpg";

            StorageReference imageRef = storageReference.child(fileName);

            // Upload image to Firebase Storage
            UploadTask uploadTask = imageRef.putFile(imageUri);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                progressDialog.dismiss();
                Toast.makeText(Img2ImgActivity.this, "Image uploaded successfully!", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(Img2ImgActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            progressDialog.dismiss();
            Toast.makeText(Img2ImgActivity.this, "User not logged in.", Toast.LENGTH_SHORT).show();
        }
    }
}
