package com.developers.wajbaty.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.developers.wajbaty.R;
import com.developers.wajbaty.Utils.FullScreenImagesUtil;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ImagePagerAdapter extends PagerAdapter {

    private final List<String> imageUrls;
    private final int pagerLayout;

    public ImagePagerAdapter(List<String> imageUrls,int pagerLayout){
        this.imageUrls = imageUrls;
        this.pagerLayout = pagerLayout;
    }

    @Override
    public int getCount() {
        return imageUrls.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
       final View view = LayoutInflater.from(container.getContext()).inflate(pagerLayout,null);

        final ImageView pageImageIv = view.findViewById(R.id.pageImageIv);
        Picasso.get().load(imageUrls.get(position)).fit().centerCrop().into(pageImageIv);

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