package com.example.stablediffusion.appactivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.stablediffusion.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Img2ImgActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 1;
    private static final int CAPTURE_IMAGE = 2;

    private Uri imageUri;
    private ImageView selectedImageView; // ImageView to display the selected image
    private ProgressDialog progressDialog;

    // Activity Result Launcher for camera permission request
    private final ActivityResultLauncher<String> cameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if (result) {
                        // Permission granted, open the camera
                        openCamera();
                    } else {
                        Toast.makeText(Img2ImgActivity.this, "Camera permission is required to take pictures.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_img2img);

        // Initialize UI elements
        selectedImageView = findViewById(R.id.selectedImageView); // Make sure to have this ImageView in your layout
        Button chooseButton = findViewById(R.id.chooseButton); // Button to choose image
        Button captureButton = findViewById(R.id.captureButton); // Button to capture image

        // ProgressDialog initialization
        progressDialog = new ProgressDialog(Img2ImgActivity.this);
        progressDialog.setMessage("Generating...");

        // Set OnClickListener for the choose button
        chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        // Set OnClickListener for the capture button
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestCameraPermission(); // Request permission before opening the camera
            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE && data != null) {
                imageUri = data.getData();
                selectedImageView.setImageURI(imageUri); // Display the selected image
            } else if (requestCode == CAPTURE_IMAGE) {
                selectedImageView.setImageURI(imageUri); // Display the captured image
            }
        }
    }
}
