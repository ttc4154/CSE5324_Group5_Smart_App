package com.example.stablediffusion.appactivity;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.stablediffusion.R;

import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {
    private Context context;
    private List<Uri> imageUris;
    private OnImageClickListener clickListener;

    public interface OnImageClickListener {
        void onImageClick(Uri imageUri);
    }

    public GalleryAdapter(Context context, List<Uri> imageUris, OnImageClickListener clickListener) {
        this.context = context;
        this.imageUris = imageUris;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_gallery_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Uri imageUri = imageUris.get(position);
        // Load the image using Glide or any other library here
        holder.imageView.setImageURI(imageUri);

        // Set click listener for the image
        holder.imageView.setOnClickListener(v -> clickListener.onImageClick(imageUri));
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.gallery_image);
        }
    }
}
