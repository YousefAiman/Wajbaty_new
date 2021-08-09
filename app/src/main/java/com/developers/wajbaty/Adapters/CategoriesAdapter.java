package com.developers.wajbaty.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.developers.wajbaty.Models.CartItem;
import com.developers.wajbaty.Models.RestaurantCategory;
import com.developers.wajbaty.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Locale;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.CategoryItemVH> {

  private final ArrayList<RestaurantCategory> categories;
  private static CategoryClickListener categoryClickListener;
  private final String language;


  public interface CategoryClickListener{
    void onCategoryClicked(int position);
  }

  public CategoriesAdapter(ArrayList<RestaurantCategory> categories,
                           CategoryClickListener categoryClickListener) {
    this.categories = categories;
    CategoriesAdapter.categoryClickListener = categoryClickListener;
    language = Locale.getDefault().getLanguage().equals("ar")?"ar":"en";
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

  public class CategoryItemVH extends RecyclerView.ViewHolder implements View.OnClickListener {

    private final ImageView categoryImageIv;
    private final TextView categoryNameTv;

    public CategoryItemVH(@NonNull View itemView) {
      super(itemView);
      categoryImageIv = itemView.findViewById(R.id.categoryImageIv);
      categoryNameTv = itemView.findViewById(R.id.categoryNameTv);

      itemView.setOnClickListener(this);
    }

    private void bind(RestaurantCategory category){

      if(category.getIconUrl()!=null && !category.getIconUrl().isEmpty()){
        Picasso.get().load(category.getIconUrl()).fit().centerInside().into(categoryImageIv);
      }
      categoryNameTv.setText(language.equals("ar")?category.getName_ar():category.getName_en());

    }

    @Override
    public void onClick(View v) {

      categoryClickListener.onCategoryClicked(getAdapterPosition());

    }
  }
}
