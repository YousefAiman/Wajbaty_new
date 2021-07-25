package com.developers.wajbaty.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.developers.wajbaty.Models.CartItem;
import com.developers.wajbaty.Models.DeliveryCourse;
import com.developers.wajbaty.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class DeliveryCourseAdapter extends RecyclerView.Adapter<DeliveryCourseAdapter.DeliverCourseVH> {

  private final ArrayList<DeliveryCourse> deliveryCourses;
  private int orangeColor,blackColor;

  public DeliveryCourseAdapter(ArrayList<DeliveryCourse> deliveryCourses) {
    this.deliveryCourses = deliveryCourses;
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

  public class DeliverCourseVH extends RecyclerView.ViewHolder {

    private final View courseLocationIndicatorView;
    private final TextView courseLocationNameTv,courseLocationItemCountTv,
            courseLocationCoordinatesTv;
    private final View courseLocationRouteLineView;

    public DeliverCourseVH(@NonNull View itemView) {
      super(itemView);

      courseLocationIndicatorView = itemView.findViewById(R.id.courseLocationIndicatorView);
      courseLocationNameTv = itemView.findViewById(R.id.courseLocationNameTv);
      courseLocationItemCountTv = itemView.findViewById(R.id.courseLocationItemCountTv);
      courseLocationCoordinatesTv = itemView.findViewById(R.id.courseLocationCoordinatesTv);
      courseLocationRouteLineView = itemView.findViewById(R.id.courseLocationRouteLineView);

    }

    private void bind(DeliveryCourse deliveryCourse) {

      if(deliveryCourse.isWasPassed()){
        courseLocationIndicatorView.setBackgroundResource(R.drawable.orange_circle);
      }else{
        courseLocationIndicatorView.setBackgroundResource(R.drawable.outlined_orange_circle);
      }

      courseLocationNameTv.setText(deliveryCourse.getLocationName());
      courseLocationCoordinatesTv.setText("["+ deliveryCourse.getLocation().getLatitude()+" , "+
              deliveryCourse.getLocation().getLongitude()+"]");

      courseLocationItemCountTv.setText(deliveryCourse.getItemCount()+ "items");

      if(deliveryCourse.isActive()){

        if(orangeColor == 0){
          orangeColor = ResourcesCompat.getColor(itemView.getResources(),R.color.orange,null);
        }

        courseLocationItemCountTv.setVisibility(View.VISIBLE);
        courseLocationCoordinatesTv.setTextColor(orangeColor);

      }else {

        if(blackColor == 0){
          blackColor = ResourcesCompat.getColor(itemView.getResources(),R.color.light_black,null);
        }

        courseLocationItemCountTv.setVisibility(View.GONE);
        courseLocationCoordinatesTv.setTextColor(blackColor);

      }

      if(deliveryCourses.indexOf(deliveryCourse) == deliveryCourses.size() - 1){
        courseLocationRouteLineView.setVisibility(View.GONE);
      }

    }

  }
}
