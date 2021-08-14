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

import com.developers.wajbaty.Models.RestaurantCategory;
import com.developers.wajbaty.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Locale;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.CategoryItemVH> {

    private static CategoryClickListener categoryClickListener;
    private static int orangeColor;
    private final ArrayList<RestaurantCategory> categories;
    private final String language;

    public CategoriesAdapter(ArrayList<RestaurantCategory> categories,
                             CategoryClickListener categoryClickListener,
                             Context context) {
        this.categories = categories;
        CategoriesAdapter.categoryClickListener = categoryClickListener;
        language = Locale.getDefault().getLanguage().equals("ar") ? "ar" : "en";
        orangeColor = ResourcesCompat.getColor(context.getResources(), R.color.orange, null);
    }

    @NonNull
    @Override
    public CategoryItemVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CategoryItemVH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryItemVH holder, int position) {
        holder.bind(categories.get(position));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }


    public interface CategoryClickListener {
        void onCategoryClicked(int position);
    }

    public class CategoryItemVH extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ImageView categoryImageIv;
        private final TextView categoryNameTv;
        private CircularProgressDrawable progressDrawable;

        public CategoryItemVH(@NonNull View itemView) {
            super(itemView);
            categoryImageIv = itemView.findViewById(R.id.categoryImageIv);
            categoryNameTv = itemView.findViewById(R.id.categoryNameTv);

            itemView.setOnClickListener(this);
        }

        private void bind(RestaurantCategory category) {

            if (category.getIconUrl() != null && !category.getIconUrl().isEmpty()) {

                if (progressDrawable == null) {
                    progressDrawable = new CircularProgressDrawable(itemView.getContext());
                    progressDrawable.setColorSchemeColors(orangeColor);
                    progressDrawable.setStyle(CircularProgressDrawable.LARGE);
                }
                if (!progressDrawable.isRunning()) {
                    progressDrawable.start();
                }
                Picasso.get().load(category.getIconUrl()).fit().centerInside()
                        .placeholder(progressDrawable).into(categoryImageIv);
            }
            categoryNameTv.setText(language.equals("ar") ? category.getName_ar() : category.getName_en());

        }

        @Override
        public void onClick(View v) {

            categoryClickListener.onCategoryClicked(getAdapterPosition());

        }
    }
}
