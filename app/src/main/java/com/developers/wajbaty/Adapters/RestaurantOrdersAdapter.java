package com.developers.wajbaty.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.developers.wajbaty.Models.RestaurantOrder;
import com.developers.wajbaty.R;
import com.developers.wajbaty.Utils.TimeFormatter;

import java.util.ArrayList;

public class RestaurantOrdersAdapter extends RecyclerView.Adapter<RestaurantOrdersAdapter.RestaurantOrdersVH> {

    private static RestaurantsOrdersListener restaurantsOrdersListener;
    private final ArrayList<RestaurantOrder> restaurantOrders;

    public RestaurantOrdersAdapter(ArrayList<RestaurantOrder> restaurantOrders,
                                   RestaurantsOrdersListener restaurantsOrdersListener) {
        this.restaurantOrders = restaurantOrders;
        RestaurantOrdersAdapter.restaurantsOrdersListener = restaurantsOrdersListener;
    }

    @NonNull
    @Override
    public RestaurantOrdersVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RestaurantOrdersVH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_restaurant_order, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantOrdersVH holder, int position) {

        holder.bind(restaurantOrders.get(position));

    }

    @Override
    public int getItemCount() {
        return restaurantOrders.size();
    }

    public interface RestaurantsOrdersListener {
        void onRestaurantOrderClicked(int position);
    }

    public static class RestaurantOrdersVH extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView restaurantOrderTimeTv, restaurantOrderCostTv,
                restaurantOrderDriverNameTv, restaurantOrderStatusTv;

        public RestaurantOrdersVH(@NonNull View itemView) {
            super(itemView);
            restaurantOrderTimeTv = itemView.findViewById(R.id.restaurantOrderTimeTv);
            restaurantOrderCostTv = itemView.findViewById(R.id.restaurantOrderCostTv);
            restaurantOrderDriverNameTv = itemView.findViewById(R.id.restaurantOrderDriverNameTv);
            restaurantOrderStatusTv = itemView.findViewById(R.id.restaurantOrderStatusTv);

            itemView.setOnClickListener(this);
        }

        private void bind(RestaurantOrder restaurantOrder) {

            restaurantOrderTimeTv.setText(TimeFormatter.formatTime(restaurantOrder.getOrderTimeInMillis()));
            restaurantOrderCostTv.setText(restaurantOrder.getTotalCost() + " " + restaurantOrder.getCurrency());
            restaurantOrderDriverNameTv.setText(restaurantOrder.getDriverName());

            String status = "";

            switch (restaurantOrder.getStatus()) {

                case RestaurantOrder.TYPE_PENDING:
                    status = "Pending";
                    break;
                case RestaurantOrder.TYPE_DONE:
                    status = "Done";
                    break;
                case RestaurantOrder.TYPE_CANCELLED:
                    status = "Cancelled";
                    break;

            }

            restaurantOrderStatusTv.setText(status);

        }

        @Override
        public void onClick(View v) {

            restaurantsOrdersListener.onRestaurantOrderClicked(getAdapterPosition());

        }
    }

}
