package com.developers.wajbaty.Adapters;

import android.content.Context;
import android.graphics.Paint;
import android.util.Log;
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
import java.util.List;

public class MenuItemsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_NORMAL = 1, TYPE_EDITABLE = 2, TYPE_DISCOUNTED = 3;

    private static final int OUTLINED_HEART = 1, FILLED_HEART = 2;
    private static CustomerMenuItemClickListener customerClickListener;
    private static AdminMenuItemClickListener adminClickListener;
    private static int orangeColor;
    private final ArrayList<MenuItem.MenuItemSummary> menuItems;
    private final List<String> likedMenuItems;
    private boolean areEditable;

    public MenuItemsAdapter(ArrayList<MenuItem.MenuItemSummary> menuItems,
                            CustomerMenuItemClickListener CustomerMenuItemClickListener,
                            List<String> likedMenuItems,
                            Context context) {
        this.menuItems = menuItems;
        this.likedMenuItems = likedMenuItems;
        MenuItemsAdapter.customerClickListener = CustomerMenuItemClickListener;
        orangeColor = ResourcesCompat.getColor(context.getResources(), R.color.orange, null);
    }

    public MenuItemsAdapter(ArrayList<MenuItem.MenuItemSummary> menuItems,
                            AdminMenuItemClickListener adminMenuItemClickListener,
                            List<String> likedMenuItems,
                            boolean areEditable) {
        this.menuItems = menuItems;
        this.likedMenuItems = likedMenuItems;
        MenuItemsAdapter.adminClickListener = adminMenuItemClickListener;
        this.areEditable = areEditable;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        switch (viewType) {

            case TYPE_EDITABLE:
                return new EditableMenuItemVH(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_editable_menu_item, parent, false));

            case TYPE_DISCOUNTED:
                return new DiscountedMenuItemVH(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_discounted_menu_item, parent, false));

            default:
                return new MenuItemVH(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_menu_item, parent, false));
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        final MenuItem.MenuItemSummary menuItem = menuItems.get(position);

        switch (holder.getItemViewType()) {

            case TYPE_EDITABLE:
                ((EditableMenuItemVH) holder).bind(menuItem);
                break;

            case TYPE_DISCOUNTED:

                ((DiscountedMenuItemVH) holder).bind(menuItem,
                        likedMenuItems != null && likedMenuItems.contains(menuItem.getID()));

                break;

            default:
                ((MenuItemVH) holder).bind(menuItem,
                        likedMenuItems != null && likedMenuItems.contains(menuItem.getID()));

        }


    }

    @Override
    public int getItemCount() {
        return menuItems.size();
    }

    @Override
    public int getItemViewType(int position) {

        if (areEditable) {
            return TYPE_EDITABLE;
        }

        final MenuItem.MenuItemSummary menuItem = menuItems.get(position);

        return menuItem.isDiscounted() ? TYPE_DISCOUNTED : TYPE_NORMAL;

    }


    public interface CustomerMenuItemClickListener {
        void showMenuItem(int position);

        void favMenuItem(int position);
    }

    public interface AdminMenuItemClickListener {
        void showEditingMenu(ImageView menuIv, int position);

        void showMenuItem(int position);
    }

    public static class MenuItemVH extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ImageView menuItemImageIv, menuFavIv;
        private final TextView menuItemNameTv, menuItemPriceTv;
        private CircularProgressDrawable progressDrawable;

        public MenuItemVH(@NonNull View itemView) {
            super(itemView);
            menuItemImageIv = itemView.findViewById(R.id.menuItemImageIv);
            menuItemNameTv = itemView.findViewById(R.id.menuItemNameTv);
            menuItemPriceTv = itemView.findViewById(R.id.menuItemPriceTv);
            menuFavIv = itemView.findViewById(R.id.menuFavIv);

            itemView.setOnClickListener(this);
            menuFavIv.setOnClickListener(this);
            menuFavIv.setTag(OUTLINED_HEART);
        }

        private void bind(MenuItem.MenuItemSummary menuItem, boolean hasLiked) {

            Log.d("ttt", menuItem.getName() + " is liked: " + hasLiked);

            if (progressDrawable == null) {
                progressDrawable = new CircularProgressDrawable(itemView.getContext());
                progressDrawable.setColorSchemeColors(orangeColor);
                progressDrawable.setStyle(CircularProgressDrawable.LARGE);
                progressDrawable.start();
            }

            if (!progressDrawable.isRunning()) {
                progressDrawable.start();
            }

            Picasso.get().load(menuItem.getImageUrls().get(0))
                    .fit().centerCrop().placeholder(progressDrawable).into(menuItemImageIv);

            menuItemNameTv.setText(menuItem.getName());
            menuItemPriceTv.setText(menuItem.getPrice() + (menuItem.getCurrency() != null ? menuItem.getCurrency() : "ILS"));

            final int tag = ((int) menuFavIv.getTag());

            if (hasLiked && tag != FILLED_HEART) {
                Log.d("ttt", "making heart filled");
                menuFavIv.setImageResource(R.drawable.heart_filled_icon);
                menuFavIv.setTag(FILLED_HEART);
            } else if (!hasLiked && tag != OUTLINED_HEART) {
                Log.d("ttt", "making heart outlined");
                menuFavIv.setImageResource(R.drawable.heart_outlined_icon);
                menuFavIv.setTag(OUTLINED_HEART);
            }
        }

        @Override
        public void onClick(View v) {

            if (v.getId() == menuFavIv.getId()) {

                customerClickListener.favMenuItem(getAdapterPosition());

            } else {

                customerClickListener.showMenuItem(getAdapterPosition());
            }

        }
    }

    public static class DiscountedMenuItemVH extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ImageView menuItemImageIv, menuFavIv;
        private final TextView menuItemNameTv, menuItemPriceTv, menuItemDiscountedPriceTv;
        private CircularProgressDrawable progressDrawable;

        public DiscountedMenuItemVH(@NonNull View itemView) {
            super(itemView);
            menuItemImageIv = itemView.findViewById(R.id.menuItemImageIv);
            menuFavIv = itemView.findViewById(R.id.menuFavIv);
            menuItemNameTv = itemView.findViewById(R.id.menuItemNameTv);
            menuItemPriceTv = itemView.findViewById(R.id.menuItemPriceTv);
            menuItemDiscountedPriceTv = itemView.findViewById(R.id.menuItemDiscountedPriceTv);

            menuItemPriceTv.setPaintFlags(menuItemPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);


            itemView.setOnClickListener(this);
            menuFavIv.setOnClickListener(this);
            menuFavIv.setTag(OUTLINED_HEART);
        }

        private void bind(MenuItem.MenuItemSummary menuItem, boolean hasLiked) {

            if (progressDrawable == null) {
                progressDrawable = new CircularProgressDrawable(itemView.getContext());
                progressDrawable.setColorSchemeColors(orangeColor);
                progressDrawable.setStyle(CircularProgressDrawable.LARGE);
                progressDrawable.start();
            }

            if (!progressDrawable.isRunning()) {
                progressDrawable.start();
            }

            Picasso.get().load(menuItem.getImageUrls().get(0)).fit().centerCrop()
                    .placeholder(progressDrawable).into(menuItemImageIv);
            menuItemNameTv.setText(menuItem.getName());

            final String currency = menuItem.getCurrency() != null ? menuItem.getCurrency() : "ILS";

            menuItemPriceTv.setText(menuItem.getPrice() + currency);

            menuItemDiscountedPriceTv.setText(((float) menuItem.getDiscountMap().get("discountedPrice")) + currency);


            final int tag = ((int) menuFavIv.getTag());

            if (hasLiked && tag != FILLED_HEART) {
                Log.d("ttt", "making heart filled");
                menuFavIv.setImageResource(R.drawable.heart_filled_icon);
                menuFavIv.setTag(FILLED_HEART);
            } else if (!hasLiked && tag != OUTLINED_HEART) {
                Log.d("ttt", "making heart outlined");
                menuFavIv.setImageResource(R.drawable.heart_outlined_icon);
                menuFavIv.setTag(OUTLINED_HEART);
            }
        }

        @Override
        public void onClick(View v) {

            if (v.getId() == menuFavIv.getId()) {

                customerClickListener.favMenuItem(getAdapterPosition());

            } else {

                customerClickListener.showMenuItem(getAdapterPosition());
            }

        }
    }


    public static class EditableMenuItemVH extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ImageView menuItemImageIv, menuOptionsIv;
        private final TextView menuItemNameTv, menuItemPriceTv;
        private CircularProgressDrawable progressDrawable;

        public EditableMenuItemVH(@NonNull View itemView) {
            super(itemView);
            menuItemImageIv = itemView.findViewById(R.id.menuItemImageIv);
            menuItemNameTv = itemView.findViewById(R.id.menuItemNameTv);
            menuItemPriceTv = itemView.findViewById(R.id.menuItemPriceTv);
            menuOptionsIv = itemView.findViewById(R.id.menuOptionsIv);

            itemView.setOnClickListener(this);
            menuOptionsIv.setOnClickListener(this);
        }

        private void bind(MenuItem.MenuItemSummary menuItem) {


            if (progressDrawable == null) {
                progressDrawable = new CircularProgressDrawable(itemView.getContext());
                progressDrawable.setColorSchemeColors(orangeColor);
                progressDrawable.setStyle(CircularProgressDrawable.LARGE);
                progressDrawable.start();
            }

            if (!progressDrawable.isRunning()) {
                progressDrawable.start();
            }


            Picasso.get().load(menuItem.getImageUrls().get(0)).fit().centerCrop()
                    .placeholder(progressDrawable).into(menuItemImageIv);
            menuItemNameTv.setText(menuItem.getName());
            menuItemPriceTv.setText(menuItem.getPrice() + (menuItem.getCurrency() != null ? menuItem.getCurrency() : "ILS"));

        }

        @Override
        public void onClick(View v) {

            if (v.getId() == menuOptionsIv.getId()) {

                adminClickListener.showEditingMenu(menuOptionsIv, getAdapterPosition());

            } else {

                adminClickListener.showMenuItem(getAdapterPosition());
            }

        }
    }
}
