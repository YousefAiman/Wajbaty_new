package com.developers.wajbaty.Adapters;

import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.developers.wajbaty.Models.PartneredRestaurant;
import com.developers.wajbaty.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class NearbyRestaurantsAdapter extends RecyclerView.Adapter<NearbyRestaurantsAdapter.NearbyRestaurantItemVH> {

    private static final int TYPE_UN_SELECTED = 1, TYPE_SELECTED = 2;
    private static NearbyRestaurantsListener nearbyRestaurantsListener;
    private static Location currentLocation;
    private static int orangeColor;
    private final ArrayList<PartneredRestaurant.NearbyPartneredRestaurant> nearbyRestaurants;
    //    private final RecyclerView.LayoutManager layoutManager;
//    private final Context context;
    //selection
    private int lastSelected = -1;
//    private int defaultHeight = 0;
//    private int defaultWidth = 0;
//    private final int extraHeight;

    public NearbyRestaurantsAdapter(ArrayList<PartneredRestaurant.NearbyPartneredRestaurant> nearbyRestaurants,
                                    NearbyRestaurantsListener nearbyRestaurantsListener,
                                    Location currentLocation,
                                    Context context,
                                    RecyclerView.LayoutManager layoutManager) {
        this.nearbyRestaurants = nearbyRestaurants;
        NearbyRestaurantsAdapter.nearbyRestaurantsListener = nearbyRestaurantsListener;
        NearbyRestaurantsAdapter.currentLocation = currentLocation;
//        this.context = context;
//        extraHeight = (int) (35 * context.getResources().getDisplayMetrics().density);
//        this.layoutManager = layoutManager;
    }

    public static void setCurrentLocation(Location currentLocation) {
        NearbyRestaurantsAdapter.currentLocation = currentLocation;
    }

    private static String getDistanceAway(double lat, double lng) {

        if (currentLocation == null) {
            Log.e("NearbyRestaurants", "current location is null");
            return null;
        }

        final Location restaurantLocation = new Location("nearbyRestaurant");
        restaurantLocation.setLatitude(lat);
        restaurantLocation.setLongitude(lng);

        double distance = currentLocation.distanceTo(restaurantLocation);

        if (distance >= 1000) {
            return Math.round(distance / 1000) + "km ";
        } else {
            return Math.round(distance) + "m ";
        }

    }

    private void updatedCurrentLocation(Location currentLocation, Context context) {

        NearbyRestaurantsAdapter.currentLocation = currentLocation;
        orangeColor = ResourcesCompat.getColor(context.getResources(), R.color.orange, null);
    }

    @NonNull
    @Override
    public NearbyRestaurantItemVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

//        final int layout = viewType == TYPE_UN_SELECTED ? R.layout.item_nearby_map_restaurant :
//                R.layout.item_nearby_map_restaurant_selected;

        return new NearbyRestaurantItemVH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_nearby_map_restaurant, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NearbyRestaurantItemVH holder, int position) {

        holder.bind(nearbyRestaurants.get(position));
    }

    @Override
    public int getItemCount() {
        return nearbyRestaurants.size();
    }

    @Override
    public int getItemViewType(int position) {

//        if (lastSelected == position)
        return TYPE_SELECTED;

//        return TYPE_UN_SELECTED;
    }


    public interface NearbyRestaurantsListener {
        void selectRestaurant(int position);

        void reSelectRestaurant(int position);

        void getRestaurantDirections(int position);
    }
//
//    private void selection(int newPosition, int oldPosition, TextView textView) {
//
//        if (newPosition != oldPosition) {
//            CardView newView = (CardView) layoutManager.findViewByPosition(newPosition);
//
//
//            newView.setCardBackgroundColor(ResourcesCompat.getColor(context.getResources(),
//                    R.color.orange,null));
//
//            ((TextView)newView.findViewById(R.id.nearbyRestaurantNameTv))
//                    .setTextColor(ResourcesCompat.getColor(context.getResources(),
//                    R.color.white,null));
//
//            newView.requestLayout();
//
////            if(oldPosition != -1){
////                CardView oldView = (CardView) layoutManager.findViewByPosition(oldPosition);
////
////                oldView.setCardBackgroundColor(ResourcesCompat.getColor(context.getResources(),
////                        R.color.white,null));
////                textView.setTextColor(ResourcesCompat.getColor(context.getResources(),
////                        R.color.light_black,null));
////            }
//
//            CardView oldView = (CardView) layoutManager.findViewByPosition(oldPosition);
//
//            if (oldView != null) {
//
//                oldView.setCardBackgroundColor(ResourcesCompat.getColor(context.getResources(),
//                        R.color.white,null));
//                textView.setTextColor(ResourcesCompat.getColor(context.getResources(),
//                        R.color.light_black,null));
//
//                oldView.requestLayout();
//
//            } else {
//                notifyItemChanged(oldPosition);
//            }
//
////            if (newView != null) {
////                defaultWidth = card.getWidth();
////                defaultHeight = card.getHeight();
////                selectItem(newView.getChildAt(0));
////            }
////            if (oldView != null) {
////                deSelectItem(oldView.getChildAt(0));
////            } else {
////                notifyItemChanged(oldPosition);
////            }
//        }
//    }
//
//    private void selectItem(View view) {
//
//        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
//        layoutParams.width = defaultWidth;
//        layoutParams.height = extraHeight + defaultHeight;
//        view.requestLayout();
//
////        ValueAnimator select = ValueAnimator.ofInt(defaultWidth, defaultWidth + extraHeight);
////        select.setInterpolator(new AccelerateDecelerateInterpolator());
////        select.setDuration(300);
////        select.start();
////        select.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
////            @Override
////            public void onAnimationUpdate(ValueAnimator animation) {
////                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
////                layoutParams.width = (int) animation.getAnimatedValue();
////                layoutParams.height = (int) (animation.getAnimatedValue()) + defaultHeight - defaultWidth;
////                view.requestLayout();
////            }
////        });
//
////        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
//////        layoutParams.width = (int) animation.getAnimatedValue();
////        layoutParams.height = extraHeight + defaultHeight;
////        view.requestLayout();
//
////        setMarginTop(view,extraHeight);
//
//    }
//
//    private void deSelectItem(View view) {
//
////        ValueAnimator select = ValueAnimator.ofInt(defaultWidth + extraHeight, defaultWidth);
////        select.setInterpolator(new AccelerateDecelerateInterpolator());
////        select.setDuration(300);
////        select.start();
////        select.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
////            @Override
////            public void onAnimationUpdate(ValueAnimator animation) {
////                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
////                layoutParams.width = (int) animation.getAnimatedValue();
////                layoutParams.height = (int) (animation.getAnimatedValue()) + defaultHeight - defaultWidth;
////                view.requestLayout();
////            }
////        });
//
//
//        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
//        layoutParams.height = defaultHeight;
//        layoutParams.width = defaultWidth;
//        view.requestLayout();
////        setMarginTop(view,0);
//
//    }
//
//
//    private static void setMarginTop(View view, int marginTop) {
//
//        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
//
//            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
//            p.setMargins(0, marginTop, 0, 0);
//            view.requestLayout();
//        }
//
//
//    }

    public class NearbyRestaurantItemVH extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ImageView nearbyRestaurantImageIV, nearbyRestaurantDirectionsIv;
        private final TextView nearbyRestaurantNameTv, nearbyRestaurantDistanceTv;
        private final CardView nearbyRestaurantCardView;
        private CircularProgressDrawable progressDrawable;

        public NearbyRestaurantItemVH(@NonNull View itemView) {
            super(itemView);

            nearbyRestaurantImageIV = itemView.findViewById(R.id.nearbyRestaurantImageIV);
            nearbyRestaurantDirectionsIv = itemView.findViewById(R.id.nearbyRestaurantDirectionsIv);
            nearbyRestaurantNameTv = itemView.findViewById(R.id.nearbyRestaurantNameTv);
            nearbyRestaurantDistanceTv = itemView.findViewById(R.id.nearbyRestaurantDistanceTv);
            nearbyRestaurantCardView = itemView.findViewById(R.id.nearbyRestaurantCardView);

            itemView.setOnClickListener(this);
            nearbyRestaurantDirectionsIv.setOnClickListener(this);
        }

        private void bind(PartneredRestaurant.NearbyPartneredRestaurant nearbyRestaurant) {
//

//            ViewGroup.LayoutParams param = itemView.getLayoutParams();
//            if (getAdapterPosition() == lastSelected) {
//                param.width = defaultWidth + extraHeight;
//                param.height = defaultHeight + extraHeight;
//            } else {
//                if (defaultWidth != 0 && defaultHeight != 0) {
//                    param.height = defaultHeight;
//                    param.width = defaultWidth;
//                }
//            }

//      if(nearbyRestaurant.isSelected()){
//        setMarginTop(nearbyRestaurantCardView,0);
//      }else{
//        setMarginTop(nearbyRestaurantCardView,topMargin);
//      }
//      if(lastSelected == getAdapterPosition()){
//        nearbyRestaurantCardView.getLayoutParams().
//
//      }

            if (progressDrawable == null) {
                progressDrawable = new CircularProgressDrawable(itemView.getContext());
                progressDrawable.setColorSchemeColors(orangeColor);
                progressDrawable.setStyle(CircularProgressDrawable.LARGE);
                progressDrawable.start();
            }

            if (!progressDrawable.isRunning()) {
                progressDrawable.start();
            }


            Picasso.get().load(nearbyRestaurant.getMainImage()).fit().centerCrop()
                    .placeholder(progressDrawable).into(nearbyRestaurantImageIV);
            nearbyRestaurantNameTv.setText(nearbyRestaurant.getName());

            if (nearbyRestaurant.getDistanceFormatted() == null) {
                Log.d("ttt", "nearvy adapater lat " + nearbyRestaurant.getLat() + " lng: " + nearbyRestaurant.getLng());

                nearbyRestaurant.setDistanceFormatted(getDistanceAway(nearbyRestaurant.getLat(), nearbyRestaurant.getLng()));
            }

            nearbyRestaurantDistanceTv.setText(nearbyRestaurant.getDistanceFormatted());

        }

        @Override
        public void onClick(View v) {

            if (v.getId() == nearbyRestaurantDirectionsIv.getId()) {

                nearbyRestaurantsListener.getRestaurantDirections(getAdapterPosition());

            } else {
                if (lastSelected != getAdapterPosition()) {
//        if(lastSelected != -1){
//          nearbyRestaurants.get(lastSelected).setSelected(false);
//        }

//                int currentPosition = getAdapterPosition();
//                selection(currentPosition, lastSelected,nearbyRestaurantNameTv);
                    lastSelected = getAdapterPosition();

//                if(lastSelected != -1){
//
//                    CardView oldView = (CardView) layoutManager.findViewByPosition(lastSelected);
////                setMarginTop(oldView,extraHeight);
//
//                    ViewGroup.LayoutParams layoutParams = oldView.getLayoutParams();
//                    layoutParams.height = defaultHeight;
//                    layoutParams.width = defaultWidth;
//                    oldView.requestLayout();
//
//                }
//
//
//                int currentPosition = getAdapterPosition();
////                selection(currentPosition, lastSelected, nearbyRestaurantConstraintLayout);
//                lastSelected = getAdapterPosition();
//
//                CardView newView = (CardView) layoutManager.findViewByPosition(currentPosition);
////
//                ViewGroup.LayoutParams newLayoutParam = newView.getLayoutParams();
//
//                if(defaultWidth == 0){
//                    defaultWidth = newLayoutParam.width;
//                    defaultHeight = newLayoutParam.height;
//                }
//
//                newLayoutParam.height = defaultHeight + extraHeight;
//                newView.requestLayout();
//
//
                    nearbyRestaurantsListener.selectRestaurant(lastSelected);

//        nearbyRestaurants.get(lastSelected).setSelected(true);
//        setMarginTop(nearbyRestaurantCardView,0);
//        notifyItemChanged(lastSelected);

                } else {
                    nearbyRestaurantsListener.reSelectRestaurant(getAdapterPosition());
                }
            }
        }
    }

}
