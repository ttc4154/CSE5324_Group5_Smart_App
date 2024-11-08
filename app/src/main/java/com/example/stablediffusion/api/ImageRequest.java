package com.example.stablediffusion.api;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.stablediffusion.R;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ImageRequest extends RecyclerView.Adapter<ImageRequest.ViewHolder> {
    Context context;
    ArrayList<String> arrayList;

    public ImageRequest(Context context, ArrayList<String> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.image_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(context).asBitmap().load(arrayList.get(position)).addListener(new RequestListener<Bitmap>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                holder.imageView.setImageBitmap(resource);

                // Download Button
                holder.download.setOnClickListener(v -> {
                    if (resource != null) {
                        // Create a file name using the position
                        String fileName = "image" + position + ".png";
                        //saveImageUsingMediaStore(resource, context, fileName);
                        saveImageToExternalStorage(resource, position);
                    }
                });

                // Share Button
                holder.share.setOnClickListener(v -> {
                    if (resource != null) {
                        shareImage(resource, position, context);
                    }
                });

                // Cloud Upload Button
                holder.upload.setOnClickListener(v -> uploadImageToCloud(resource, position));

                return true;
            }
        }).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    // For Android 10 and above, using MediaStore for Scoped Storage
    public void saveImageUsingMediaStore(Bitmap bitmap, Context context, String fileName) {
        try {
            // Check if the Android version is Q (API 29) or higher for scoped storage
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES); // Save in Pictures folder

                // Get a content resolver and insert the image into the MediaStore
                Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                if (uri != null) {
                    // Open an output stream and save the bitmap to the MediaStore
                    try (OutputStream outputStream = context.getContentResolver().openOutputStream(uri)) {
                        if (outputStream != null) {
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                            outputStream.flush();
                            Toast.makeText(context, "Image Saved Successfully", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show();
                }
            } else {
                // For older Android versions, can save directly to the Downloads folder using the old method
                File downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File file = new File(downloadsDirectory, fileName);
                try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    outputStream.close();
                    Toast.makeText(context, "Image Saved at: " + file.getPath(), Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error saving image", Toast.LENGTH_SHORT).show();
        }
    }

    // For Android below Android 10 (external storage write)
    private void saveImageToExternalStorage(Bitmap resource, int position) {
        // Create directory SDA-Group5 in Downloads if it doesn't exist
        File downloadsDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "SDA-Group5");
        if (!downloadsDirectory.exists()) {
            if (!downloadsDirectory.mkdirs()) {
                Toast.makeText(context, "Failed to create directory", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String timestamp = sdf.format(new Date());
        // Create a new file for the image
        File file = new File(downloadsDirectory, "image_" + timestamp + ".png");

        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
            resource.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.close();
            Toast.makeText(context, "Image Saved at: " + file.getPath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }


    public void shareImage(Bitmap bitmap, int position, Context context) {
        try {
            // Create a directory in the cache named "images"
            File cachePath = new File(context.getCacheDir(), "images");
            cachePath.mkdirs(); // Create the directory if it doesn't exist

            // Generate a unique file name to prevent overwriting
            File file = new File(cachePath, "image" + position + ".png");
            FileOutputStream stream = new FileOutputStream(file);

            // Compress the bitmap and save it as PNG in the specified directory
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            // Use FileProvider to get a URI for the file
            Uri contentUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);


            // Create a share intent
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Start the share activity
            context.startActivity(Intent.createChooser(shareIntent, "Share Image"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Upload Image to Firebase Cloud Storage
    private void uploadImageToCloud(Bitmap bitmap, int position) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && bitmap != null) {
            try {
                File tempFile = File.createTempFile("upload_image", ".png", context.getCacheDir());
                try (OutputStream fos = new FileOutputStream(tempFile)) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.flush();
                }

                Uri imageUri = Uri.fromFile(tempFile);
                StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                StorageReference userImagesRef = storageRef.child("user_images/" + user.getUid() + "/image" + position + ".png");

                UploadTask uploadTask = userImagesRef.putFile(imageUri);
                uploadTask.addOnSuccessListener(taskSnapshot -> {
                    Toast.makeText(context, "Image uploaded to Firebase", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show();
                });
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageButton download, share, upload;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.list_item_image);
            download = itemView.findViewById(R.id.list_item_download);
            share = itemView.findViewById(R.id.list_item_share); // Share Button
            upload = itemView.findViewById(R.id.list_item_upload); // Upload Button
        }
    }
}
