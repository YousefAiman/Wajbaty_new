package com.developers.wajbaty.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.developers.wajbaty.Models.DeliveryCourse;
import com.developers.wajbaty.R;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public class DeliveryCourseAdapter extends RecyclerView.Adapter<DeliveryCourseAdapter.DeliverCourseVH> {

    private static DeliverCourseListener deliverCourseListener;
    private final ArrayList<DeliveryCourse> deliveryCourses;
    private int orangeColor, blackColor;

    public DeliveryCourseAdapter(ArrayList<DeliveryCourse> deliveryCourses, DeliverCourseListener deliverCourseListener) {
        this.deliveryCourses = deliveryCourses;
        DeliveryCourseAdapter.deliverCourseListener = deliverCourseListener;
    }

    @NonNull
    @Override
    public DeliverCourseVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new DeliverCourseVH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_map_course_location, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull DeliverCourseVH holder, int position) {

        holder.bind(deliveryCourses.get(position));

    }

    @Override
    public int getItemCount() {
        return deliveryCourses.size();
    }

    public interface DeliverCourseListener {
        void onDeliveryCourseClicked(int position);
    }

    public class DeliverCourseVH extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final View courseLocationIndicatorView;
        private final TextView courseLocationNameTv, courseLocationItemCountTv,
                courseLocationCoordinatesTv;
        private final View courseLocationRouteLineView;

        public DeliverCourseVH(@NonNull View itemView) {
            super(itemView);

            courseLocationIndicatorView = itemView.findViewById(R.id.courseLocationIndicatorView);
            courseLocationNameTv = itemView.findViewById(R.id.courseLocationNameTv);
            courseLocationItemCountTv = itemView.findViewById(R.id.courseLocationItemCountTv);
            courseLocationCoordinatesTv = itemView.findViewById(R.id.courseLocationCoordinatesTv);
            courseLocationRouteLineView = itemView.findViewById(R.id.courseLocationRouteLineView);

            itemView.setOnClickListener(this);
        }

        private void bind(DeliveryCourse deliveryCourse) {

            if (deliveryCourse.isWasPassed()) {
                courseLocationIndicatorView.setBackgroundResource(R.drawable.orange_circle);
            } else {
                courseLocationIndicatorView.setBackgroundResource(R.drawable.outlined_orange_circle);
            }

            courseLocationNameTv.setText(deliveryCourse.getLocationName());

            final float lat = BigDecimal.valueOf(deliveryCourse.getLocation().getLatitude())
                    .setScale(2, RoundingMode.DOWN).floatValue(),
                    lng = BigDecimal.valueOf(deliveryCourse.getLocation().getLongitude())
                            .setScale(2, RoundingMode.DOWN).floatValue();


            courseLocationCoordinatesTv.setText("[" + lat + " , " + lng + "]");

            courseLocationItemCountTv.setText(deliveryCourse.getItemCount() + "items");

            if (deliveryCourse.isActive()) {

                if (orangeColor == 0) {
                    orangeColor = ResourcesCompat.getColor(itemView.getResources(), R.color.orange, null);
                }

                courseLocationItemCountTv.setVisibility(View.VISIBLE);
                courseLocationCoordinatesTv.setTextColor(orangeColor);

            } else {

                if (blackColor == 0) {
                    blackColor = ResourcesCompat.getColor(itemView.getResources(), R.color.light_black, null);
                }

                courseLocationItemCountTv.setVisibility(View.GONE);
                courseLocationCoordinatesTv.setTextColor(blackColor);

            }

            if (deliveryCourses.indexOf(deliveryCourse) == deliveryCourses.size() - 1) {
                courseLocationRouteLineView.setVisibility(View.GONE);
            } else {
                courseLocationRouteLineView.setVisibility(View.VISIBLE);
            }

        }

        @Override
        public void onClick(View v) {

            deliverCourseListener.onDeliveryCourseClicked(getAdapterPosition());

        }
    }
}
