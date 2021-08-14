package com.developers.wajbaty.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.developers.wajbaty.R;

public class SliderAdapter extends PagerAdapter {

    private final Context context;
    private final Integer[] images;
    private final String[] titles;
    private final String[] descs;

    public SliderAdapter(Context context, Integer[] images, String[] titles, String[] descs) {
        this.context = context;
        this.images = images;
        this.titles = titles;
        this.descs = descs;
    }

    @Override
    public int getCount() {
        return images.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.slider_pager_layout, null);
        TextView sliderTitleTv = view.findViewById(R.id.sliderTitleTv);
        TextView sliderDescTv = view.findViewById(R.id.sliderDescTv);
        if (position == 3) {
            sliderTitleTv.setVisibility(View.GONE);
            sliderDescTv.setVisibility(View.GONE);
        } else {
            sliderTitleTv.setText(titles[position]);
            sliderDescTv.setText(descs[position]);
        }
        ((ImageView) view.findViewById(R.id.sliderIv)).setImageResource(images[position]);
        container.addView(view, 0);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
