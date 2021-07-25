package com.developers.wajbaty.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.developers.wajbaty.Models.offer.DiscountOffer;
import com.developers.wajbaty.Models.offer.Offer;
import com.developers.wajbaty.R;
import com.developers.wajbaty.Utils.FullScreenImagesUtil;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class DiscountOffersPagerAdapter extends PagerAdapter {

    private final ArrayList<DiscountOffer> discountOffers;
    private final int pagerLayout;
    private final OfferClickListener offerClickListener;

    public interface OfferClickListener{
        void onOfferClicked(int position);
    }

    public DiscountOffersPagerAdapter(ArrayList<DiscountOffer> discountOffers, int pagerLayout,
                                      OfferClickListener offerClickListener){
        this.discountOffers = discountOffers;
        this.pagerLayout = pagerLayout;
        this.offerClickListener = offerClickListener;
    }

    @Override
    public int getCount() {
        return discountOffers.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
       final View view = LayoutInflater.from(container.getContext()).inflate(pagerLayout,null);

       final DiscountOffer discountOffer = discountOffers.get(position);

       final TextView discountOfferNameTv = view.findViewById(R.id.discountOfferNameTv);
       final ImageView discountOfferImageIv = view.findViewById(R.id.discountOfferImageIv);
        final TextView discountOfferOldPriceTv = view.findViewById(R.id.discountOfferOldPriceTv);
        final TextView discountOfferNewPriceTv = view.findViewById(R.id.discountOfferNewPriceTv);


        discountOfferNameTv.setText(discountOffer.getTitle());
        Picasso.get().load(discountOffer.getImageUrl()).fit().centerCrop().into(discountOfferImageIv);
        discountOfferOldPriceTv.setText(String.valueOf(discountOffer.getPreviousPrice()));
        discountOfferNewPriceTv.setText(String.valueOf(discountOffer.getNewPrice()));


        view.setOnClickListener(v-> offerClickListener.onOfferClicked(position));
        container.addView(view);

       return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}