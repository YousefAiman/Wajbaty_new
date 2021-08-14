package com.developers.wajbaty.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.developers.wajbaty.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumVH> {

    private static AlbumClickListener albumClickListener;
    private static int orangeColor;
    private final List<String> albumImages;

    public AlbumAdapter(List<String> albumImages, AlbumClickListener albumClickListener,
                        Context context) {
        this.albumImages = albumImages;
        AlbumAdapter.albumClickListener = albumClickListener;
        orangeColor = ResourcesCompat.getColor(context.getResources(), R.color.orange, null);

    }

    @NonNull
    @Override
    public AlbumVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AlbumVH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_restaruant_album_image, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumVH holder, int position) {
        holder.bind(albumImages.get(position));
    }

    @Override
    public int getItemCount() {
        return albumImages.size();
    }

    public interface AlbumClickListener {
        void onImageClicked(int position);
    }

    public static class AlbumVH extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ImageView albumImageIV;
        private CircularProgressDrawable progressDrawable;

        public AlbumVH(@NonNull View itemView) {
            super(itemView);
            albumImageIV = itemView.findViewById(R.id.albumImageIV);
            itemView.setOnClickListener(this);

        }

        private void bind(String imageURL) {

            if (progressDrawable == null) {
                progressDrawable = new CircularProgressDrawable(itemView.getContext());
                progressDrawable.setColorSchemeColors(orangeColor);
                progressDrawable.setStyle(CircularProgressDrawable.LARGE);
            }

            if (!progressDrawable.isRunning()) {
                progressDrawable.start();
            }

            Picasso.get().load(imageURL).fit().centerCrop().placeholder(progressDrawable)
                    .into(albumImageIV);
        }

        @Override
        public void onClick(View v) {
            albumClickListener.onImageClicked(getAdapterPosition());
        }
    }

}
