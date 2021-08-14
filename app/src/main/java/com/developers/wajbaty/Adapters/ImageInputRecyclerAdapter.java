package com.developers.wajbaty.Adapters;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
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

public class ImageInputRecyclerAdapter extends RecyclerView.Adapter<ImageInputRecyclerAdapter.ImageInputViewHolder> {

    public static final int TYPE_BANNER = 1, TYPE_ALBUM = 2;
    private static final int TYPE_LOCAL = 1, TYPE_CLOUD = 2;
    private static int orangeColor;
    private final Context context;
    private final List<Object> images;
    private final CloudImageRemoveListener cloudImageRemoveListener;
    private final LocalImageListener localImageListener;
    private final int adapterType;

    public ImageInputRecyclerAdapter(Context context, List<Object> images,
                                     CloudImageRemoveListener cloudImageRemoveListener,
                                     LocalImageListener localImageListener,
                                     int adapterType) {
        this.images = images;
        this.context = context;
        this.cloudImageRemoveListener = cloudImageRemoveListener;
        this.localImageListener = localImageListener;
        this.adapterType = adapterType;
        orangeColor = ResourcesCompat.getColor(context.getResources(), R.color.orange, null);
    }

    @NonNull
    @Override
    public ImageInputViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

//        switch (viewType){
//
//            case TYPE_LOCAL:
//
//                return new LocalImageInputViewHolder(LayoutInflater.from(context).inflate(R.layout.item_image_input_layout,parent,false));
//
//            case TYPE_CLOUD:
//
//                return new CloudImageInputViewHolder(LayoutInflater.from(context).inflate(R.layout.item_image_input_layout,parent,false));
//
//            default:
        return new ImageInputViewHolder(LayoutInflater.from(context).inflate(R.layout.item_image_input_layout, parent, false));
//        }

    }

    @Override
    public int getItemViewType(int position) {

        final Object image = images.get(position);
        if (image instanceof Uri) {

            return TYPE_LOCAL;
        } else if (image instanceof String) {
            return TYPE_CLOUD;
        }

        return 0;
    }

    @Override
    public void onBindViewHolder(@NonNull ImageInputViewHolder holder, int position) {


        final Object object = images.get(position);

        if (object instanceof Uri) {

            holder.bind((Uri) object);

        } else if (object instanceof String) {

            holder.bind((String) object);

        } else if (object == null) {

            holder.bindEmpty();

        }


//        if(holder instanceof LocalImageInputViewHolder){
//
//            holder.bind((Uri) images.get(position));
//
//        }else if(holder instanceof CloudImageInputViewHolder){
//            holder.bind((String) images.get(position));
//        }

    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public interface CloudImageRemoveListener {
        void removeCloudImage(int index, int adapterType);
    }

    public interface LocalImageListener {
        void removeLocaleImage(int index, int adapterType);

        void addLocalImage(int index, int adapterType);
    }

    public class ImageInputViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ImageView imageIv, cancelIv, addImageIv;
        private CircularProgressDrawable progressDrawable;

        public ImageInputViewHolder(@NonNull View itemView) {
            super(itemView);
            imageIv = itemView.findViewById(R.id.imageIv);
            cancelIv = itemView.findViewById(R.id.cancelIv);
            addImageIv = itemView.findViewById(R.id.addImageIv);
        }

        public void bind(Uri imageUri) {

            if (imageUri != null) {

                if (progressDrawable == null) {
                    progressDrawable = new CircularProgressDrawable(itemView.getContext());
                    progressDrawable.setColorSchemeColors(orangeColor);
                    progressDrawable.setStyle(CircularProgressDrawable.LARGE);
                    progressDrawable.start();
                }

                if (!progressDrawable.isRunning()) {
                    progressDrawable.start();
                }


                Picasso.get().load(imageUri).fit().centerCrop().placeholder(progressDrawable)
                        .into(imageIv);
                cancelIv.setVisibility(View.VISIBLE);
                addImageIv.setVisibility(View.GONE);
                cancelIv.setOnClickListener(this);

            }

        }

        public void bind(String imageUrl) {

            if (imageUrl != null) {

                if (progressDrawable == null) {
                    progressDrawable = new CircularProgressDrawable(itemView.getContext());
                    progressDrawable.setColorSchemeColors(orangeColor);
                    progressDrawable.setStyle(CircularProgressDrawable.LARGE);
                    progressDrawable.start();
                }

                if (!progressDrawable.isRunning()) {
                    progressDrawable.start();
                }

                Picasso.get().load(imageUrl).fit().centerCrop()
                        .placeholder(progressDrawable).into(imageIv);
                cancelIv.setVisibility(View.VISIBLE);
                addImageIv.setVisibility(View.GONE);

                cancelIv.setOnClickListener(this);

            }
        }

        public void bindEmpty() {


            cancelIv.setVisibility(View.GONE);
            addImageIv.setVisibility(View.VISIBLE);


            cancelIv.setOnClickListener(this);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {

            Log.d("ttt", "clicked");

            final Object image = images.get(getAdapterPosition());


            if (v.getId() == R.id.cancelIv) {

                if (image != null) {

                    if (image instanceof Uri) {

                        localImageListener.removeLocaleImage(getAdapterPosition(), adapterType);

                    } else if (image instanceof String) {

                        cloudImageRemoveListener.removeCloudImage(getAdapterPosition(), adapterType);

                    }

                }

            } else {

                Log.d("ttt", "clicked itemview");

                if (image != null)
                    return;

                localImageListener.addLocalImage(getAdapterPosition(), adapterType);


            }


        }
    }

    private class CloudImageInputViewHolder extends ImageInputViewHolder implements View.OnClickListener {

        private CloudImageInputViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.cancelIv) {

                cloudImageRemoveListener.removeCloudImage(images.indexOf(getAdapterPosition()), adapterType);

            }

        }
    }


    private class LocalImageInputViewHolder extends ImageInputViewHolder implements View.OnClickListener {

        private LocalImageInputViewHolder(@NonNull View itemView) {
            super(itemView);
        }


        @Override
        public void onClick(View v) {

            if (v.getId() == R.id.cancelIv) {

                localImageListener.removeLocaleImage(getAdapterPosition(), adapterType);

            } else {

                localImageListener.addLocalImage(getAdapterPosition(), adapterType);

            }

        }
    }


}
