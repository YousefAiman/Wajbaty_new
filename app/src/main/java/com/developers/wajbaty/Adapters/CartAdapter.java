package com.developers.wajbaty.Adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.developers.wajbaty.Models.CartItem;
import com.developers.wajbaty.Models.MenuItem;
import com.developers.wajbaty.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartItemVH> {

  private final ArrayList<CartItem> cartItems;
  private static CartClickListener cartClickListener;

  public interface CartClickListener{
    void removeCartItem(int position);
    void showCartItemInfo(int position);
    void increasedCartItemCount(int position);
    void decreasedCartItemCount(int position);
  }

  public CartAdapter(ArrayList<CartItem> cartItems,
                     CartClickListener cartClickListener) {
    this.cartItems = cartItems;
    CartAdapter.cartClickListener = cartClickListener;
  }

  @NonNull
  @Override
  public CartItemVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

      return new CartItemVH(LayoutInflater.from(parent.getContext())
              .inflate(R.layout.item_cart_layout, parent, false));

  }

  @Override
  public void onBindViewHolder(@NonNull CartItemVH holder, int position) {

      holder.bind(cartItems.get(position));

  }

  @Override
  public int getItemCount() {
    return cartItems.size();
  }

  public class CartItemVH extends RecyclerView.ViewHolder implements View.OnClickListener {

    private final ImageView cartItemImageIv,cartItemRemoveIv,cartItemPlusIv,cartItemMinusIv;
    private final TextView cartItemNameTv,cartItemPriceTv,cartItemCountTv;

    public CartItemVH(@NonNull View itemView) {
      super(itemView);
      cartItemImageIv = itemView.findViewById(R.id.cartItemImageIv);
      cartItemRemoveIv = itemView.findViewById(R.id.cartItemRemoveIv);
      cartItemPlusIv = itemView.findViewById(R.id.cartItemPlusIv);
      cartItemMinusIv = itemView.findViewById(R.id.cartItemMinusIv);
      cartItemNameTv = itemView.findViewById(R.id.cartItemNameTv);
      cartItemPriceTv = itemView.findViewById(R.id.cartItemPriceTv);
      cartItemCountTv = itemView.findViewById(R.id.cartItemCountTv);

      itemView.setOnClickListener(this);
      cartItemRemoveIv.setOnClickListener(this);
      cartItemPlusIv.setOnClickListener(this);
      cartItemMinusIv.setOnClickListener(this);
    }

    private void bind(CartItem cartItem){

      Picasso.get().load(cartItem.getImageUrl()).fit().centerCrop().into(cartItemImageIv);
      cartItemNameTv.setText(cartItem.getName());
      cartItemPriceTv.setText(cartItem.getPrice() + cartItem.getCurrency());
      cartItemCountTv.setText(String.valueOf(cartItem.getCount()));

    }

    @Override
    public void onClick(View v) {

      if(v.getId() == cartItemRemoveIv.getId()){

        cartClickListener.removeCartItem(getAdapterPosition());

      }else if(v.getId() == cartItemPlusIv.getId()){

        final CartItem cartItem = cartItems.get(getAdapterPosition());
        cartItem.setCount(cartItem.getCount()+1);
        cartItemCountTv.setText(String.valueOf(cartItem.getCount()));

        cartClickListener.increasedCartItemCount(getAdapterPosition());

      }else if(v.getId() == cartItemMinusIv.getId()){

        final CartItem cartItem = cartItems.get(getAdapterPosition());

        if(cartItem.getCount() > 1){
          cartItem.setCount(cartItem.getCount()-1);
          cartItemCountTv.setText(String.valueOf(cartItem.getCount()));
          cartClickListener.decreasedCartItemCount(getAdapterPosition());
        }

      }else {

        cartClickListener.showCartItemInfo(getAdapterPosition());

      }

    }
  }

}
