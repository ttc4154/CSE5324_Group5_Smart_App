package com.example.stablediffusion.api;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.stablediffusion.R;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class InpaintRequest extends RecyclerView.Adapter<InpaintRequest.ViewHolder> {
    Context context;
    ArrayList<String> arrayList;

    public InpaintRequest(Context context, ArrayList<String> arrayList) {
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
        // Load the inpainted image URL into the ImageView
        Glide.with(context).asBitmap().load(arrayList.get(position)).addListener(new RequestListener<Bitmap>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                // Handle load failure
                Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                holder.imageView.setImageBitmap(resource);
                holder.download.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (resource != null) {
                            // Specify the file path and name
                            File file = new File(context.getExternalFilesDir(null), "Generated/image" + position + ".png");
                            // Create the directory if it doesn't exist
                            file.getParentFile().mkdirs();
                            OutputStream outputStream;
                            try {
                                outputStream = new BufferedOutputStream(new FileOutputStream(file));
                                resource.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                                outputStream.close();
                                Toast.makeText(context, "Image Saved at: " + file.getPath(), Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
                return true;
            }
        }).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageButton download;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.list_item_image);
            download = itemView.findViewById(R.id.list_item_download);
        }
    }
}
