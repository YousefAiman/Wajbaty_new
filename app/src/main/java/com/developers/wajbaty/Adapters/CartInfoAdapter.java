package com.developers.wajbaty.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.developers.wajbaty.Models.CartItem;
import com.developers.wajbaty.Models.CartItemRestaurantHeader;
import com.developers.wajbaty.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CartInfoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 1;
    private static CartClickListener cartClickListener;
    private static int orangeColor;
    private final ArrayList<CartItem> cartItems;

    public CartInfoAdapter(ArrayList<CartItem> cartItems,
                           CartClickListener cartClickListener,
                           Context context) {

        this.cartItems = cartItems;
        CartInfoAdapter.cartClickListener = cartClickListener;
        orangeColor = ResourcesCompat.getColor(context.getResources(), R.color.orange, null);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == TYPE_HEADER)
            return new RestaurantHeaderVH(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_cart_items_restaurant_header, parent, false));


        return new CartInfoItemVH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart_info_layout, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {


        if (holder.getItemViewType() == TYPE_HEADER) {
            ((RestaurantHeaderVH) holder).bind(((CartItemRestaurantHeader) cartItems.get(position)).getHeader());
        } else {
            ((CartInfoItemVH) holder).bind(cartItems.get(position));
        }


    }

    @Override
    public int getItemViewType(int position) {

        if (cartItems.get(position) instanceof CartItemRestaurantHeader) {
            return TYPE_HEADER;
        }

        return super.getItemViewType(position);

    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public interface CartClickListener {
        void showMenuItem(int position);
    }

    public static class CartInfoItemVH extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ImageView cartInfoItemImageIv;
        private final TextView cartInfoItemNameTv, cartInfoItemPriceTv, cartInfoItemCountTv;
        private CircularProgressDrawable progressDrawable;

        public CartInfoItemVH(@NonNull View itemView) {
            super(itemView);
            cartInfoItemImageIv = itemView.findViewById(R.id.cartInfoItemImageIv);
            cartInfoItemNameTv = itemView.findViewById(R.id.cartInfoItemNameTv);
            cartInfoItemPriceTv = itemView.findViewById(R.id.cartInfoItemPriceTv);
            cartInfoItemCountTv = itemView.findViewById(R.id.cartInfoItemCountTv);

            itemView.setOnClickListener(this);
        }

        private void bind(CartItem cartItem) {

            if (progressDrawable == null) {
                progressDrawable = new CircularProgressDrawable(itemView.getContext());
                progressDrawable.setColorSchemeColors(orangeColor);
                progressDrawable.setStyle(CircularProgressDrawable.LARGE);
            }
            if (!progressDrawable.isRunning()) {
                progressDrawable.start();
            }
            Picasso.get().load(cartItem.getImageUrl()).fit().centerCrop()
                    .placeholder(progressDrawable).into(cartInfoItemImageIv);
            cartInfoItemNameTv.setText(cartItem.getName());
            cartInfoItemPriceTv.setText(cartItem.getPrice() + cartItem.getCurrency());
            cartInfoItemCountTv.setText("X" + cartItem.getCount());

        }

        @Override
        public void onClick(View v) {
            cartClickListener.showMenuItem(getAdapterPosition());
        }
    }


    public static class RestaurantHeaderVH extends RecyclerView.ViewHolder {

        private final TextView cartHeaderRestaurantNameTv;

        public RestaurantHeaderVH(@NonNull View itemView) {
            super(itemView);
            cartHeaderRestaurantNameTv = itemView.findViewById(R.id.cartHeaderRestaurantNameTv);

        }

        private void bind(String header) {
            cartHeaderRestaurantNameTv.setText(header);
        }
    }

}
