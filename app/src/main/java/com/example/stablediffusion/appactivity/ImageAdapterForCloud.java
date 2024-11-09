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
import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageAdapterForCloud extends RecyclerView.Adapter<ImageAdapterForCloud.ViewHolder> {
    private List<ImageModelForCloud> imageList;
    private Context context;
    private OnImageClickListener onImageClickListener;

    public ImageAdapterForCloud(List<ImageModelForCloud> imageList, Context context, OnImageClickListener onImageClickListener) {
        this.imageList = imageList;
        this.context = context;
        this.onImageClickListener = onImageClickListener;
    }
    public interface OnImageClickListener {
        void onImageClick(Uri imageUri);
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.img2img_cloud_gallery, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageModelForCloud imageModel = imageList.get(position);
        Picasso.get().load(imageModel.getImageUrl()).into(holder.imageView);
        // Set up click listener to call the callback method
        holder.imageView.setOnClickListener(v -> {
            if (onImageClickListener != null) {
                onImageClickListener.onImageClick(Uri.parse(imageModel.getImageUrl()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_image_view_for_cloud);
        }
    }
}
