package com.developers.wajbaty.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.developers.wajbaty.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumVH> {

    private final List<String> albumImages;
    private static AlbumClickListener albumClickListener;

    public interface AlbumClickListener{
        void onImageClicked(int position);
    }

    public AlbumAdapter(List<String> albumImages, AlbumClickListener albumClickListener) {
        this.albumImages = albumImages;
        AlbumAdapter.albumClickListener = albumClickListener;
    }


    @NonNull
    @Override
    public AlbumVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AlbumVH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_restaruant_album_image,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumVH holder, int position) {
        holder.bind(albumImages.get(position));
    }

    @Override
    public int getItemCount() {
        return albumImages.size();
    }

    public static class AlbumVH extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ImageView albumImageIV;

        public AlbumVH(@NonNull View itemView) {
            super(itemView);
            albumImageIV = itemView.findViewById(R.id.albumImageIV);
            itemView.setOnClickListener(this);

        }

        private void bind(String imageURL){
            Picasso.get().load(imageURL).fit().centerCrop().into(albumImageIV);
        }

        @Override
        public void onClick(View v) {
            albumClickListener.onImageClicked(getAdapterPosition());
        }
    }

}
