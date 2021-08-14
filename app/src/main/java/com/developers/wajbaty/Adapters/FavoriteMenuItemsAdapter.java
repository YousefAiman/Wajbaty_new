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

import com.developers.wajbaty.Models.MenuItem;
import com.developers.wajbaty.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class FavoriteMenuItemsAdapter extends RecyclerView.Adapter<FavoriteMenuItemsAdapter.FavMenuItemVH> {

    private static FavoriteMenuItemsListener favoriteRestaurantsListener;
    private static int orangeColor;
    private final ArrayList<MenuItem.MenuItemSummary> menuItemSummaries;

    public FavoriteMenuItemsAdapter(ArrayList<MenuItem.MenuItemSummary> menuItemSummaries,
                                    FavoriteMenuItemsListener favoriteRestaurantsListener,
                                    Context context) {
        this.menuItemSummaries = menuItemSummaries;
        FavoriteMenuItemsAdapter.favoriteRestaurantsListener = favoriteRestaurantsListener;
        orangeColor = ResourcesCompat.getColor(context.getResources(), R.color.orange, null);
    }

    @NonNull
    @Override
    public FavMenuItemVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FavMenuItemVH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite_menu_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FavMenuItemVH holder, int position) {
        holder.bind(menuItemSummaries.get(position));
    }

    @Override
    public int getItemCount() {
        return menuItemSummaries.size();
    }


    public interface FavoriteMenuItemsListener {
        void removeFromFav(int position);

        void showMenuItem(int position);
    }

    public static class FavMenuItemVH extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ImageView favoriteMenuItemImageIv, favoriteMenuItemFavIv;
        private final TextView favoriteMenuItemNameTv, favoriteMenuItemPriceTv;
        private CircularProgressDrawable progressDrawable;

        public FavMenuItemVH(@NonNull View itemView) {
            super(itemView);
            favoriteMenuItemImageIv = itemView.findViewById(R.id.favoriteMenuItemImageIv);
            favoriteMenuItemFavIv = itemView.findViewById(R.id.favoriteMenuItemFavIv);
            favoriteMenuItemNameTv = itemView.findViewById(R.id.favoriteMenuItemNameTv);
            favoriteMenuItemPriceTv = itemView.findViewById(R.id.favoriteMenuItemPriceTv);

            itemView.setOnClickListener(this);
            favoriteMenuItemFavIv.setOnClickListener(this);
        }

        private void bind(MenuItem.MenuItemSummary menuItemSummary) {

            if (progressDrawable == null) {
                progressDrawable = new CircularProgressDrawable(itemView.getContext());
                progressDrawable.setColorSchemeColors(orangeColor);
                progressDrawable.setStyle(CircularProgressDrawable.LARGE);
                progressDrawable.start();
            }

            if (!progressDrawable.isRunning()) {
                progressDrawable.start();
            }

            Picasso.get().load(menuItemSummary.getImageUrls().get(0)).fit().centerCrop()
                    .placeholder(progressDrawable).into(favoriteMenuItemImageIv);

            favoriteMenuItemNameTv.setText(menuItemSummary.getName());

            favoriteMenuItemPriceTv.setText(menuItemSummary.getPrice() +
                    (menuItemSummary.getCurrency() != null ? menuItemSummary.getCurrency() : "ILS"));

        }

        @Override
        public void onClick(View v) {

            if (v.getId() == favoriteMenuItemFavIv.getId()) {
                favoriteRestaurantsListener.removeFromFav(getAdapterPosition());
            } else {
                favoriteRestaurantsListener.showMenuItem(getAdapterPosition());
            }

        }
    }
}
