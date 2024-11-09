package com.example.stablediffusion.appactivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stablediffusion.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ImageGalleryForCloud extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ImageAdapterForCloud imageAdapter;
    private List<ImageModelForCloud> imageList = new ArrayList<>();
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.img2img_cloud_gallery);

        recyclerView = findViewById(R.id.recycler_view_for_cloud);
        int numberOfColumns = 1; // Set 3 columns for grid layout
        recyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        imageAdapter = new ImageAdapterForCloud(imageList, this, this::onImageClick);
        recyclerView.setAdapter(imageAdapter);

        // Initialize Firebase Storage reference
        storageReference = FirebaseStorage.getInstance().getReference();

        // Fetch images from Firebase
        fetchImagesFromCloud();
    }

    private void fetchImagesFromCloud() {
        Log.d("GalleryActivityForCloud", "Fetching images from Firebase Storage.");

        // Get the current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        StorageReference userImagesRef = null;
        if (user != null) {
            String userId = user.getUid();
            userImagesRef = storageReference.child("user_images/" + userId);
        }

        // Attempt to list all items in the storage reference
        assert userImagesRef != null;
        userImagesRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                Log.d("GalleryActivityForCloud", "Successfully fetched list of images.");
                imageList.clear(); // Clear previous images

                for (StorageReference item : listResult.getItems()) {
                    item.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            imageList.add(new ImageModelForCloud(uri.toString()));
                            imageAdapter.notifyItemInserted(imageList.size() - 1);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ImageGalleryForCloud.this, "Failed to load image: " + item.getName(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                imageAdapter.notifyDataSetChanged();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ImageGalleryForCloud.this, "Failed to list images", Toast.LENGTH_SHORT).show();
                Log.e("GalleryActivityForCloud", "Failed to list images: " + e.getMessage());
            }
        });
    }
    // Method to be called when an image is clicked
    private void onImageClick(Uri selectedUri) {
        onImageSelected(selectedUri); // Call the method to handle image selection
    }
    private void onImageSelected(Uri selectedImageUriFromCloud) {
        // Pass the selected image URI back to Img2ImgActivity
        if (selectedImageUriFromCloud != null) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("selectedImageUri", selectedImageUriFromCloud.toString()); // Ensure the URI is not null
            setResult(Img2ImgActivity.RESULT_OK, resultIntent);
            finish(); // Finish the activity
        } else {
            Log.e("ImageGalleryForCloud", "Image URI is null");
        }
    }
}
