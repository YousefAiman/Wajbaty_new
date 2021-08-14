package com.developers.wajbaty.Activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.developers.wajbaty.Adapters.ImagePagerAdapter;
import com.developers.wajbaty.Models.MenuItemModel;
import com.developers.wajbaty.PartneredRestaurant.Fragments.FirebaseReviewsFragment;
import com.developers.wajbaty.R;
import com.developers.wajbaty.Utils.GlobalVariables;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class MenuItemActivity extends AppCompatActivity implements
        Observer,
        View.OnClickListener,
        Toolbar.OnMenuItemClickListener {

    private static final String TAG = "MenuItemActivity";
    private com.developers.wajbaty.Models.MenuItem menuItem;
    private MenuItemModel menuItemModel;

    //views
    private Toolbar menuItemToolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private TextView priceToolbarTv, menuItemCategoryTv, menuIngredientsTitleTv, menuIngredientsTv;
    private FrameLayout menuItemRatingsFrameLayout;
    private ViewPager menuItemImagesPager;
    private LinearLayout menuItemDotsLinear;

    //images pager
    private ImagePagerAdapter imagePagerAdapter;

    //firebase
    private DocumentReference menuItemRef;
    private FirebaseFirestore firestore;
    private String currentUid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_item);

        getViews();

        firestore = FirebaseFirestore.getInstance();

        final Intent intent = getIntent();

        if (intent != null) {

//            if(intent.hasExtra("MenuItem")){
//                menuItem = (com.developers.wajbaty.Models.MenuItem)
//                        intent.getSerializableExtra("MenuItem");
//
//                initializeObjects();
//
//                populateViews();
//
//            }else
            if (intent.hasExtra("MenuItemID")) {

                firestore.collection("MenuItems").document(intent.getStringExtra("MenuItemID"))
                        .get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        menuItem = documentSnapshot.toObject(com.developers.wajbaty.Models.MenuItem.class);
                    }
                }).addOnCompleteListener(task -> {

                    if (menuItem != null) {
                        initializeObjects();
                        populateViews();
                    } else {
                        finish();
                    }

                });

            } else {
                finish();
            }

        } else {
            finish();
        }


    }

    private void getViews() {

        menuItemToolbar = findViewById(R.id.menuItemToolbar);
        collapsingToolbarLayout = findViewById(R.id.menuItemCollabsingToolbar);
        menuItemImagesPager = findViewById(R.id.menuItemImagesPager);
        priceToolbarTv = findViewById(R.id.priceToolbarTv);
        menuItemCategoryTv = findViewById(R.id.menuItemCategoryTv);
        menuIngredientsTitleTv = findViewById(R.id.menuIngredientsTitleTv);
        menuIngredientsTv = findViewById(R.id.menuIngredientsTv);
        menuItemRatingsFrameLayout = findViewById(R.id.menuItemRatingsFrameLayout);
        menuItemDotsLinear = findViewById(R.id.menuItemDotsLinear);


        if (GlobalVariables.getCurrentRestaurantId() == null ||
                !GlobalVariables.getCurrentRestaurantId().equals(menuItem.getRestaurantId())) {
            menuItemToolbar.inflateMenu(R.menu.menu_item_customer_menu);
        }

        menuItemToolbar.setOnMenuItemClickListener(this);
        menuItemToolbar.setNavigationOnClickListener(v -> finish());

    }


    private void initializeObjects() {

        menuItemModel = new MenuItemModel(new com.developers.wajbaty.Models.MenuItem.MenuItemSummary(menuItem));
        menuItemModel.addObserver(this);

        imagePagerAdapter = new ImagePagerAdapter(menuItem.getImageUrls(), R.layout.menu_item_image_page, 1f, this);

        menuItemRef = firestore.collection("MenuItems").document(menuItem.getID());

        menuItemModel.checkAlreadyFavItem(getCurrentUid());

    }


    private void populateViews() {

        menuItemImagesPager.setAdapter(imagePagerAdapter);

        setUpPager();

        if (menuItem.getIngredients() == null || menuItem.getIngredients().isEmpty()) {
            menuIngredientsTitleTv.setVisibility(View.GONE);
            menuIngredientsTv.setVisibility(View.GONE);
        }

        menuItemModel.checkInUserCart(getCurrentUid());

        collapsingToolbarLayout.setTitle(menuItem.getName());
        priceToolbarTv.setText(menuItem.getPrice() + (menuItem.getCurrency() != null ? menuItem.getCurrency() : "ILS"));
        menuItemCategoryTv.setText(menuItem.getCategory());


        final List<String> ingredients = menuItem.getIngredients();

        if (ingredients != null && !ingredients.isEmpty()) {

            String ingredientText = "";

            for (String ingredient : ingredients) {
                ingredientText = ingredientText.concat(ingredient + "\n");
            }

            menuIngredientsTv.setText(ingredientText);

        }


        FragmentManager manager = getSupportFragmentManager();
        if (manager != null) {

            manager.beginTransaction().add(menuItemRatingsFrameLayout.getId(),
                    new FirebaseReviewsFragment(menuItemRef, menuItemRef.collection("Reviews"),
                            menuItem.getReviewSummary())).commit();

        }
    }


    @Override
    public void onClick(View v) {

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {


        if (item.getItemId() == R.id.add_to_cart_action) {


            if (!menuItemModel.hasInCart) {
                menuItemModel.addToOrRemoveFromCart(getCurrentUid());
            } else {

                Toast.makeText(this,
                        "Item Was already added to the cart!", Toast.LENGTH_SHORT).show();

            }


        } else if (item.getItemId() == R.id.favorite_item_action) {

            menuItemModel.favOrUnFavItem(menuItem.getRestaurantId(), getCurrentUid());

        }

        return false;
    }

    private String getCurrentUid() {

        if (currentUid == null)
            currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        return currentUid;
    }

    @Override
    public void update(Observable o, Object arg) {

        if (arg instanceof Integer) {

            switch ((int) arg) {

                case MenuItemModel.HAS_IN_CART:
                case MenuItemModel.ADD_TO_CART_SUCCESS:

                    changeMenuIcon(R.id.add_to_cart_action, R.drawable.cart_filled_icon);

                    break;

                case MenuItemModel.NOT_IN_CART:
                case MenuItemModel.REMOVE_FROM_CART_SUCCESS:

                    changeMenuIcon(R.id.add_to_cart_action, R.drawable.cart_outlined_icon);

                    break;

                case MenuItemModel.CHECK_FAVORED_SUCCESS:

                    if (menuItemModel.isFavored) {
                        changeMenuIcon(R.id.favorite_item_action, R.drawable.heart_filled_icon);
                    }

                    break;
                case MenuItemModel.FAVORING_SUCCESS:

                    changeMenuIcon(R.id.favorite_item_action, R.drawable.heart_filled_icon);

                    break;
                case MenuItemModel.UN_FAVORING_SUCCESS:

                    changeMenuIcon(R.id.favorite_item_action, R.drawable.heart_outlined_icon);

                    break;

            }


        } else if (arg instanceof Map) {

            final Map<Integer, Object> resultMap = (Map<Integer, Object>) arg;

            final int key = resultMap.keySet().iterator().next();

            switch (key) {

                case MenuItemModel.CHECK_FAVORED_FAILED:

                    Log.d(TAG, (String) resultMap.get(key));

                    break;

                case MenuItemModel.UN_FAVORING_FAILED:

                    Toast.makeText(this,
                            "Failed while trying to remove this menu item from your favorite!" +
                                    " Please Try again", Toast.LENGTH_LONG).show();

                    Log.d(TAG, (String) resultMap.get(key));

                    break;

                case MenuItemModel.FAVORING_FAILED:

                    Toast.makeText(this,
                            "Failed while trying to add this menu item to your favorite!" +
                                    " Please Try again", Toast.LENGTH_LONG).show();

                    Log.d(TAG, (String) resultMap.get(key));

                    break;

                case MenuItemModel.REMOVE_FROM_CART_FAILED:

                    Toast.makeText(this,
                            "Failed while trying to remove this menu item to your cart!" +
                                    " Please Try again", Toast.LENGTH_LONG).show();

                    Log.d(TAG, (String) resultMap.get(key));

                    break;

                case MenuItemModel.ADD_TO_CART_FAILED:

                    Toast.makeText(this,
                            "Failed while trying to add this menu item to your cart!" +
                                    " Please Try again", Toast.LENGTH_LONG).show();

                    Log.d(TAG, (String) resultMap.get(key));

                    break;


            }

        }

    }


    private void changeMenuIcon(int itemId, int icon) {
        menuItemToolbar.getMenu().findItem(itemId).setIcon(icon);
    }

    private void setUpPager() {

        if (imagePagerAdapter.getCount() > 1) {


            final Drawable nonactive_dot = ContextCompat.getDrawable(this, R.drawable.pager_indicator_inactive),
                    full_dot = ContextCompat.getDrawable(this, R.drawable.pager_indicator_active);

            final float density = getResources().getDisplayMetrics().density;

            final ImageView[] dots = new ImageView[imagePagerAdapter.getCount()];

            final LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);

            for (int i = 0; i < imagePagerAdapter.getCount(); i++) {

                dots[i] = new ImageView(this);

                if (i == 0) {
                    dots[0].setImageDrawable(ContextCompat.getDrawable(this,
                            R.drawable.pager_indicator_active));
                } else {
                    dots[i].setImageDrawable(ContextCompat.getDrawable(this,
                            R.drawable.pager_indicator_inactive));
                }


                params.setMargins((int) (4 * density), 0, (int) (4 * density), 0);

                menuItemDotsLinear.addView(dots[i], params);

            }

            menuItemImagesPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                private int previousDot = 0;

                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {

                    dots[previousDot].setImageDrawable(nonactive_dot);

                    previousDot = position;

                    dots[position].setImageDrawable(full_dot);

                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });
        }

    }

}