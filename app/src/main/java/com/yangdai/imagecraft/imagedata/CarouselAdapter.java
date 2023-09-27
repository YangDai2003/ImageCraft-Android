package com.yangdai.imagecraft.imagedata;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.yangdai.imagecraft.R;

import java.util.List;


public class CarouselAdapter extends RecyclerView.Adapter<CarouselAdapter.ViewHolder> {
    private final List<Uri> uriList;
    private OnItemClickListener onItemClickListener;

    public CarouselAdapter(List<Uri> uriList) {
        this.uriList = uriList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.carousel_recycler_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Uri uri = uriList.get(holder.getBindingAdapterPosition());
        RequestBuilder<Drawable> requestBuilder = Glide.with(holder.itemView.getContext())
                .asDrawable().sizeMultiplier(0.3f);
        Glide.with(holder.itemView.getContext())
                .load(uri)
                .thumbnail(requestBuilder)
                .centerCrop()
                .into(holder.imageView);
        holder.imageView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(uri, v);
            }
        });
    }

    @Override
    public int getItemCount() {
        return uriList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.carousel_image_view);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Uri uri, View view);
    }
}
