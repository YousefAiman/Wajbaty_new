package com.developers.wajbaty.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;
import androidx.viewpager.widget.PagerAdapter;

import com.developers.wajbaty.R;
import com.developers.wajbaty.Utils.FullScreenImagesUtil;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ImagePagerAdapter extends PagerAdapter {

    private final List<String> imageUrls;
    private final int pagerLayout, orangeColor;
    private final float width;

    public ImagePagerAdapter(List<String> imageUrls, int pagerLayout, float width, Context context) {
        this.imageUrls = imageUrls;
        this.pagerLayout = pagerLayout;
        this.width = width;
        orangeColor = ResourcesCompat.getColor(context.getResources(), R.color.orange, null);
    }

    @Override
    public int getCount() {
        return imageUrls.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public float getPageWidth(int position) {
        return width;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        final View view = LayoutInflater.from(container.getContext()).inflate(pagerLayout, null);

        final ImageView pageImageIv = view.findViewById(R.id.pageImageIv);


        final CircularProgressDrawable progressDrawable = new CircularProgressDrawable(container.getContext());
        progressDrawable.setColorSchemeColors(orangeColor);
        progressDrawable.setStyle(CircularProgressDrawable.LARGE);
        progressDrawable.start();

        if (!progressDrawable.isRunning()) {
            progressDrawable.start();
        }

        Picasso.get().load(imageUrls.get(position)).fit().centerCrop()
                .placeholder(progressDrawable).into(pageImageIv);

        pageImageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FullScreenImagesUtil.showImageFullScreen(v.getContext(), imageUrls.get(position),
                        null);
            }
        });
        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}