package com.example.stablediffusion.appactivity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.stablediffusion.R;
import com.example.stablediffusion.api.InpaintRequest;
import com.example.stablediffusion.api.InpaintResponse;
import com.example.stablediffusion.login.LoginActivity;
import com.example.stablediffusion.login.UserProfileActivity;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Img2ImgActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_FROM_LOCAL = 1;
    private static final int CAPTURE_IMAGE = 2;
    private static final int PICK_IMAGE_FROM_CLOUD = 3;
    private ImageAdapterForCloud imageAdapter;
    private List<ImageModelForCloud> imageList = new ArrayList<>();
    private Uri imageUri;
    private ImageView selectedImageView; // ImageView to display the selected image
    private ProgressDialog progressDialog;
    private StorageReference storageReference;

    private String maskImageUrl;
    private TextInputLayout promptLayout;
    private TextInputEditText promptET;
    private Button generateButton;
    private String initImageUrl;
    private RecyclerView recyclerView;
    private PhotoView selectedImageViewForPhotoView;
    private DrawingView drawingView; // Your custom drawing view
    private Uri imageUriForPhotoView; // The URI of the selected image
    private SeekBar brushSizeSeekBar;
    private ImageButton brushSizeButton;

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

        selectedImageView = findViewById(R.id.selectedImageView);
        drawingView = findViewById(R.id.drawingView);
        ImageButton cropButton = findViewById(R.id.cropButton);
        ImageButton rotateButton = findViewById(R.id.rotateButton);
        //Button maskButton = findViewById(R.id.maskButton);
        brushSizeSeekBar = findViewById(R.id.brushSizeSeekBar);
        brushSizeButton = findViewById(R.id.brushSizeButton);
        ImageButton saveImageButton = findViewById(R.id.SaveImageButton);

        // Set default brush size
        drawingView.setBrushSize(10); // Set a default value, e.g., 10

        // Toggle SeekBar visibility on button click
        brushSizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (brushSizeSeekBar.getVisibility() == View.GONE) {
                    brushSizeSeekBar.setVisibility(View.VISIBLE);
                } else {
                    brushSizeSeekBar.setVisibility(View.GONE);
                }
            }
        });

        // Set up SeekBar listener to adjust brush size
        brushSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float brushSize = progress;
                drawingView.setBrushSize(brushSize);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Optional: actions when user starts adjusting the SeekBar
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Hide the SeekBar after user stops adjusting
                brushSizeSeekBar.setVisibility(View.GONE);
            }
        });

        saveImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve the bitmap from DrawingView
                Bitmap maskImage = drawingView.getDrawingBitmap();
                if (maskImage != null) {
                    // Convert the mask to black and white (grayscale)
                    Bitmap blackAndWhiteMask = convertToBlackAndWhite(maskImage);
                    // Save the bitmap to a file
                    String savedPath = saveBitmapToFile(blackAndWhiteMask);
                    if (savedPath != null) {
                        maskImageUrl = savedPath; // Update the maskImageUrl with the saved path

                        // Convert the saved file path to Uri
                        File file = new File(savedPath);
                        Uri imageUri = Uri.fromFile(file);

                        // Call the uploadImageToFirebase method
                        uploadImageToFirebase(imageUri, true); // Pass `true` for isMaskImageUrl

                        Toast.makeText(Img2ImgActivity.this, "Mask saved and uploading!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Img2ImgActivity.this, "Failed to save mask image", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Img2ImgActivity.this, "No mask image to save", Toast.LENGTH_SHORT).show();
                }
            }
        });


        // Set the image URI to the PhotoView
        //setImageUri(imageUri);
        // Set up button click listeners
        cropButton.setOnClickListener(v -> cropImage());
        rotateButton.setOnClickListener(v -> rotateImage());
        //maskButton.setOnClickListener(v -> applyMask());

        imageAdapter = new ImageAdapterForCloud(imageList, Img2ImgActivity.this, this::onImageClick);

        storageReference = FirebaseStorage.getInstance().getReference();

        selectedImageView = findViewById(R.id.selectedImageView);
        //drawingView.setOnDrawingCompleteListener(this); // Set the listener
        ImageButton chooseButtonLocal = findViewById(R.id.chooseButtonLocal);
        ImageButton chooseButtonCloud = findViewById(R.id.chooseButtonCloud);
        ImageButton captureButton = findViewById(R.id.captureButton);
        //Button uploadButton = findViewById(R.id.uploadButton); // Initialize upload button

        progressDialog = new ProgressDialog(Img2ImgActivity.this);
        progressDialog.setMessage("Uploading...");

        chooseButtonLocal.setOnClickListener(v -> openGallery());
        captureButton.setOnClickListener(v -> requestCameraPermission());

        // Set OnClickListener for the upload button
        /*uploadButton.setOnClickListener(v -> {
            if (imageUri != null) {
                uploadImageToFirebase(imageUri);
            } else {
                Toast.makeText(Img2ImgActivity.this, "No image selected to upload.", Toast.LENGTH_SHORT).show();
            }
        });*/

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);

        //initImageUrl = "https://firebasestorage.googleapis.com/v0/b/cse5324-group5.firebasestorage.app/o/images%2FnsDuY1wGz3O3ZQ1ae9its7rOLyk2%2Fdog_on_the_bench.png?alt=media&token=c72bd84f-ef83-4caa-a801-bdc6671767a5";
        maskImageUrl = "https://firebasestorage.googleapis.com/v0/b/cse5324-group5.firebasestorage.app/o/images%2FnsDuY1wGz3O3ZQ1ae9its7rOLyk2%2Fdot_on_the_bench_mask.png?alt=media&token=eca71884-2b0b-45eb-961f-663d9da4b505";
        // Initialize UI elements
        promptLayout = findViewById(R.id.inpaintPromptLayout);
        promptET = findViewById(R.id.inpaintPromptET);
        // Assuming promptET is a TextInputEditText
        String promptText = promptET.getText().toString();
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
                        promptText,
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
        startActivityForResult(intent, PICK_IMAGE_FROM_LOCAL);
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

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_FROM_LOCAL && data != null) {
                imageUri = data.getData();
                //selectedImageView.setImageURI(imageUri); // Display the selected image from either Local or Cloud storage
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Select Image Option");
                builder.setMessage("Would you like to use this image?");
                // Positive button to upload the selected image
                builder.setPositiveButton("Yes", (dialog, which) -> {
                    if (imageUri != null) {
                        uploadImageToFirebase(imageUri, false); //false for notMaskImageUrl
                    } else {
                        Toast.makeText(this, "No image selected to upload.", Toast.LENGTH_SHORT).show();
                    }
                });
                // Neutral button to discard and close the dialog
                builder.setNeutralButton("Cancel", (dialog, which) -> {
                    // Handle cancel, simply dismissing the dialog without taking any action
                    dialog.dismiss();
                    Toast.makeText(this, "Image selection canceled.", Toast.LENGTH_SHORT).show();
                });
                builder.show();

                Log.d("ImageGalleryForCloud", "initImageUrl: " + initImageUrl);
                Toast.makeText(this, "Operation PICK_IMAGE_FROM_LOCAL", Toast.LENGTH_SHORT).show();
                return; // Exit after handling PICK_IMAGE
            }
            if (requestCode == PICK_IMAGE_FROM_CLOUD && data != null && data.hasExtra("selectedImageUri")) {
                String imageUriString = data.getStringExtra("selectedImageUri");
                imageUri = Uri.parse(imageUriString);
                Log.d("ImageGalleryForCloud", "Selected image URI: " + imageUri.toString());
                setImageUri(imageUri);
                selectedImageView.setImageURI(imageUri);
                initImageUrl = imageUri.toString();
                Log.d("ImageGalleryForCloud", "initImageUrl: " + initImageUrl);
                if (imageUri != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Select Image Option");
                    builder.setMessage("Would you like to use this image?");
                    // Positive button to upload the selected image
                    builder.setPositiveButton("Yes", (dialog, which) -> {
                        if (imageUri != null) {
                            Glide.with(this)
                                    .load(imageUri)
                                    .into(selectedImageView);
                        } else {
                            Toast.makeText(this, "No image selected to upload.", Toast.LENGTH_SHORT).show();
                        }
                    });
                    // Neutral button to discard and close the dialog
                    builder.setNeutralButton("Cancel", (dialog, which) -> {
                        // Handle cancel, simply dismissing the dialog without taking any action
                        dialog.dismiss();
                        Toast.makeText(this, "Image selection canceled.", Toast.LENGTH_SHORT).show();
                    });
                    builder.show();
                } else {
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                }

                Toast.makeText(this, "Operation PICK_IMAGE_FROM_CLOUD", Toast.LENGTH_SHORT).show();
                // Exit after handling PICK_IMAGE
            }
            else if (requestCode == CAPTURE_IMAGE) {
                //selectedImageView.setImageURI(imageUri); // Display the captured image
                Log.d("ImageGalleryForCloud", "initImageUrl: " + initImageUrl);
                Toast.makeText(this, "Operation CAPTURE_IMAGE", Toast.LENGTH_SHORT).show();
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Select Image Option");
                builder.setMessage("Would you like to use this image?");
                // Positive button to upload the selected image
                builder.setPositiveButton("Yes", (dialog, which) -> {
                    if (imageUri != null) {
                        uploadImageToFirebase(imageUri, false); // false for notMaskImageUrl
                    } else {
                        Toast.makeText(this, "No image selected to upload.", Toast.LENGTH_SHORT).show();
                    }
                });
                // Neutral button to discard and close the dialog
                builder.setNeutralButton("Cancel", (dialog, which) -> {
                    // Handle cancel, simply dismissing the dialog without taking any action
                    dialog.dismiss();
                    Toast.makeText(this, "Image selection canceled.", Toast.LENGTH_SHORT).show();
                });
                builder.show();
            }
            else {
                Toast.makeText(this, "Operation canceled", Toast.LENGTH_SHORT).show();
            }
        }
    }
    // TODO::Add the ability to mask the image, aka image processing
    private void uploadImageToFirebase(Uri imageUri, boolean isMaskImageUrl) {
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
            String fileName = "user_images/" + userId + "/" + System.currentTimeMillis() + ".jpg";
            StorageReference imageRef = storageReference.child(fileName);

            /*// Upload image to Firebase Storage
            UploadTask uploadTask = imageRef.putFile(imageUri);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                progressDialog.dismiss();
                Toast.makeText(Img2ImgActivity.this, "Image uploaded successfully!", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(Img2ImgActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });*/
            // Upload the image and retrieve the download URL
            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            progressDialog.dismiss();
                            String downloadUrl = uri.toString();
                            if (isMaskImageUrl){
                                maskImageUrl = downloadUrl;
                                Log.d("ImageGalleryForCloud", "maskImageUrl: " + maskImageUrl);
                            }else{
                                initImageUrl = downloadUrl; // Set the URL for later use
                                // Use Glide to load the image URL into the ImageView
                                Glide.with(this)
                                        .load(downloadUrl)
                                        .into(selectedImageView);
                                Log.d("ImageGalleryForCloud", "initImageUrl: " + initImageUrl);
                                Toast.makeText(Img2ImgActivity.this, "Image uploaded and URL retrieved!", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(e -> {
                            progressDialog.dismiss();
                            Toast.makeText(Img2ImgActivity.this, "Failed to get URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }).addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(Img2ImgActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            progressDialog.dismiss();
            Toast.makeText(Img2ImgActivity.this, "User not logged in.", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to update the image after drawing is completed
    public void updateImageAfterDrawing() {
        // Get the bitmap from the drawing view
        Bitmap maskBitmap = drawingView.getDrawingBitmap(); // Assuming this method exists
        if (maskBitmap != null) {
            // Update the selected image view with the new masked image
            Bitmap selectedBitmap = ((BitmapDrawable) selectedImageView.getDrawable()).getBitmap();
            Bitmap combinedBitmap = combineBitmaps(selectedBitmap, maskBitmap); // Combine the original image with the mask
            selectedImageView.setImageBitmap(combinedBitmap);
        } else {
            Toast.makeText(this, "Failed to retrieve the mask bitmap.", Toast.LENGTH_SHORT).show();
        }
    }

    // Assuming a method that combines two bitmaps (original and mask)
    private Bitmap combineBitmaps(Bitmap original, Bitmap mask) {
        Bitmap result = Bitmap.createBitmap(original.getWidth(), original.getHeight(), original.getConfig());
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(original, 0, 0, null);
        canvas.drawBitmap(mask, 0, 0, null);
        return result;
    }

    private void setImageUri(Uri imageUri) {
        if (imageUri == null) {
            Log.e("Img2ImgActivity", "Image URI is null");
            Toast.makeText(this, "Failed to load image. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            selectedImageViewForPhotoView.setImageBitmap(bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading image.", Toast.LENGTH_SHORT).show();
        }
    }

    private void applyMask() {
        Bitmap maskedBitmap = drawingView.getMaskedBitmap();
        selectedImageView.setImageBitmap(maskedBitmap); // Update the image with the mask applied
    }

    private void rotateImage() {
        Bitmap bitmap = ((BitmapDrawable) selectedImageView.getDrawable()).getBitmap();
        Matrix matrix = new Matrix();
        matrix.postRotate(90); // Rotate by 90 degrees
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        selectedImageView.setImageBitmap(rotatedBitmap); // Set the rotated bitmap
    }

    // Add method to crop the image
    private void cropImage() {
        Uri destinationUri = Uri.fromFile(new File(getCacheDir(), "cropped_image.jpg"));
        /*UCrop.of(imageUri, destinationUri)
                .withAspectRatio(1, 1)
                .withMaxResultSize(800, 800)
                .start(this);*/
    }

    private String saveBitmapToFile(Bitmap bitmap) {
        File directory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (directory != null && !directory.exists()) {
            directory.mkdirs();
        }

        String fileName = "maskImage_" + System.currentTimeMillis() + ".png";
        File file = new File(directory, fileName);

        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return file.getAbsolutePath();
    }
    private Bitmap convertToBlackAndWhite(Bitmap originalBitmap) {
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();
        Bitmap bwBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixelColor = originalBitmap.getPixel(x, y);

                // Get the red, green, and blue values
                int red = Color.red(pixelColor);
                int green = Color.green(pixelColor);
                int blue = Color.blue(pixelColor);

                // Calculate the brightness using the grayscale formula
                int gray = (int) (0.3 * red + 0.59 * green + 0.11 * blue);

                // Set the pixel color to black or white based on brightness
                int blackOrWhite = (gray < 128) ? Color.BLACK : Color.WHITE;
                bwBitmap.setPixel(x, y, blackOrWhite);
            }
        }
        return bwBitmap;
    }


    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_img2img); // Set the default selection
    }
    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        Class<?> activityClass = null;

        if (itemId == R.id.navigation_img2img) return true;
        else if (itemId == R.id.navigation_t2i) activityClass = T2iActivity.class;
        else if (itemId == R.id.navigation_settings) activityClass = SettingsActivity.class;
        else if (itemId == R.id.navigation_gallery) activityClass = GalleryActivity.class;
        else if (itemId == R.id.navigation_user_profile) activityClass = UserProfileActivity.class;

        if (activityClass != null) {
            startActivity(new Intent(this, activityClass));
            finish();
        }
        return true;
    }
}