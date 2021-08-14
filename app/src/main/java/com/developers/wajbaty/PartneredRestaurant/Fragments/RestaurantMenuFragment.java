package com.developers.wajbaty.PartneredRestaurant.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.developers.wajbaty.Activities.MenuItemActivity;
import com.developers.wajbaty.Adapters.MenuItemsAdapter;
import com.developers.wajbaty.Adapters.SelectableChecksAdapter;
import com.developers.wajbaty.Models.MenuItem;
import com.developers.wajbaty.Models.MenuItemModel;
import com.developers.wajbaty.Models.PartneredRestaurant;
import com.developers.wajbaty.Models.SelectableItem;
import com.developers.wajbaty.Models.offer.Offer;
import com.developers.wajbaty.PartneredRestaurant.Activities.MenuItemModifierActivity;
import com.developers.wajbaty.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class RestaurantMenuFragment extends Fragment implements View.OnClickListener,
        MenuItemsAdapter.CustomerMenuItemClickListener,
        MenuItemsAdapter.AdminMenuItemClickListener,
        SelectableChecksAdapter.SelectListener,
        DiscountDialogFragment.DiscountDialogInterface {

    private static final String TAG = "RestaurantMenuFragment";

    private static final int MENU_ITEM_LIMIT = 8;
    private static final String RESTAURANT = "restaurant", LIKED_LIST = "likedList",
            CURRENCY = "currency";
    boolean canEditMenu;
    //views
    private RecyclerView filterRv, restaurantMenuRv;
    private ExtendedFloatingActionButton addToMenuFb;
    private ProgressBar menuProgressBar;
    private TextView noMenuItemTv;
    private PartneredRestaurant restaurant;
    private List<String> likedMenuItems;
    //filter
    private SelectableChecksAdapter selectableAdapter;
    private ArrayList<SelectableItem> selectableItems;

    //menu items
    private MenuItemsAdapter adapter;
    private ArrayList<MenuItem.MenuItemSummary> menuItems;

    private Query mainQuery;
    private DocumentSnapshot lastDocSnapshot;
    private boolean isLoadingItems;
    private String category;
    private ScrollListener scrollListener;

    private FirebaseFirestore firestore;
    private ActivityResultLauncher<Intent> intentLauncher;
    private String currentUid;

    private MenuClickListener menuClickListener;
    private String currency;

    public RestaurantMenuFragment() {
    }

    //    public RestaurantMenuFragment(PartneredRestaurant restaurant){
//        this.restaurant = restaurant;
//    }
//    public RestaurantMenuFragment(PartneredRestaurant restaurant,List<String> likedMenuItems) {
//        this.restaurant = restaurant;
//        this.likedMenuItems = likedMenuItems;
//    }

    public static RestaurantMenuFragment newInstance(PartneredRestaurant restaurant, ArrayList<String> likedMenuItems, String currency) {
        RestaurantMenuFragment fragment = new RestaurantMenuFragment();
        Bundle args = new Bundle();
        args.putSerializable(RESTAURANT, restaurant);
        if (likedMenuItems != null) {
            args.putStringArrayList(LIKED_LIST, likedMenuItems);
        }
        args.putString(CURRENCY, currency);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            restaurant = (PartneredRestaurant) getArguments().getSerializable(RESTAURANT);

            if (getArguments().containsKey(LIKED_LIST)) {
                likedMenuItems = getArguments().getStringArrayList(LIKED_LIST);
            } else {
                likedMenuItems = new ArrayList<>();
            }

            currency = getArguments().getString(CURRENCY);

            Log.d("ttt", "currency in menu: " + currency);
        }

        firestore = FirebaseFirestore.getInstance();

        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        canEditMenu = restaurant.getOwnerUid().equals(currentUid)
                || (restaurant.getAdmins() != null && restaurant.getAdmins().contains(currentUid));

        menuItems = new ArrayList<>();

        if (canEditMenu) {
            adapter = new MenuItemsAdapter(menuItems, this, likedMenuItems, true);
        } else {
            adapter = new MenuItemsAdapter(menuItems, this, likedMenuItems, requireContext());
        }

        selectableItems = new ArrayList<>();
        selectableAdapter = new SelectableChecksAdapter(selectableItems, this, -1);

//        mainQuery = firestore.collection("PartneredRestaurant")
//                .document(restaurant.getID())
//                .collection("MenuItems")
//                .document("MenuItems")
//                .get

        mainQuery = firestore.collection("MenuItems")
                .whereEqualTo("restaurantId", restaurant.getID())
                .orderBy("rating", Query.Direction.DESCENDING)
                .limit(MENU_ITEM_LIMIT);

        intentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {

                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null
                            && result.getData().hasExtra("addedMenuItem")) {

                        final MenuItem menuItem = (MenuItem) result.getData().getSerializableExtra("addedMenuItem");
                        if (menuItem.getCategory().equals(category)) {

                            final MenuItem.MenuItemSummary summary = new MenuItem.MenuItemSummary(
                                    menuItem
                            );

                            menuItems.add(0, summary);
                            adapter.notifyItemInserted(0);

                            if (restaurantMenuRv.getVisibility() == View.INVISIBLE) {
                                restaurantMenuRv.setVisibility(View.VISIBLE);
                                noMenuItemTv.setVisibility(View.GONE);
                            }


                        }

                    }

                });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_restaurant_menu, container,
                false);

        filterRv = view.findViewById(R.id.filterRv);
        restaurantMenuRv = view.findViewById(R.id.restaurantMenuRv);
        menuProgressBar = view.findViewById(R.id.menuProgressBar);
        noMenuItemTv = view.findViewById(R.id.noMenuItemTv);
        addToMenuFb = view.findViewById(R.id.addToMenuFb);


        if (canEditMenu) {
            addToMenuFb.setIconResource(R.drawable.add_icon_white);
            addToMenuFb.setText("Add To Menu");
            addToMenuFb.setOnClickListener(this);
        } else {
            addToMenuFb.setVisibility(View.GONE);
//            addToMenuFb.setIconResource(R.drawable.scooter_marker_icon);
//            addToMenuFb.setText("Order From Restaurant");
        }

        restaurantMenuRv.setAdapter(adapter);
        filterRv.setAdapter(selectableAdapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fetchFilters();
        getMenuItemsForCategory(true);

    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.addToMenuFb) {

            Log.d("ttt", "clicked add to menu");
//            requireActivity().startActivityIfNeeded();

            if (canEditMenu) {


                Intent intent = new Intent(requireContext(), MenuItemModifierActivity.class)
                        .putExtra("restaurantId", restaurant.getID())
                        .putExtra("currency", currency);


                String region = null;

                if (restaurant.getAddress() != null) {

                    if (restaurant.getAddress().containsKey("adminArea")) {
                        region = restaurant.getAddress().get("adminArea");
                    } else if (restaurant.getAddress().containsKey("region")) {
                        region = restaurant.getAddress().get("region");
                    } else if (restaurant.getAddress().containsKey("county")) {
                        region = restaurant.getAddress().get("county");
                    } else if (restaurant.getAddress().containsKey("city")) {
                        region = restaurant.getAddress().get("city");
                    }

                }

                if (region != null) {
                    intent.putExtra("restaurantRegion", region);
                }

                intentLauncher.launch(intent);

            } else {


            }
        }

    }

//    private void getMenuItems(){
//
//        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
//
//        firestore.collection("PartneredRestaurant")
//                .document(restaurant.getID())
//                .collection("MenuItems")
//                .document("MenuItems")
//                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//            @Override
//            public void onSuccess(DocumentSnapshot snapshot) {
//
//                List<DocumentReference> references = (List<DocumentReference>) snapshot.get("MenuItems");
//
//                for(DocumentReference ref:references){
//
//                    tasks.add(
//                    ref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                        @Override
//                        public void onSuccess(DocumentSnapshot snapshot) {
//                            menuItems.add(snapshot.toObject(MenuItem.class));
//                        }
//                    })
//                    );
//                }
//
//
//            }
//        }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//
//                Tasks.whenAllSuccess(tasks)
//                        .addOnSuccessListener(new OnSuccessListener<List<Object>>() {
//                            @Override
//                            public void onSuccess(List<Object> objects) {
//
//                                adapter.notifyDataSetChanged();
//
//                            }
//                        });
//
//            }
//        });
//
//    }
//    private void getLikedMenuItems(){
//
//
//
//    }

    private void fetchFilters() {

        final String language = Locale.getDefault().getLanguage().equals("ar") ? "ar" : "en";


        firestore.collection("GeneralOptions").document("Categories")
                .collection("Categories")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {

                final String name = "name_" + language;

                if (snapshots != null && !snapshots.isEmpty()) {

                    for (DocumentSnapshot documentSnapshot : snapshots) {
                        selectableItems.add(new SelectableItem(documentSnapshot.getId(), documentSnapshot.getString(name)));
                    }

                }
            }
        }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                selectableAdapter.notifyDataSetChanged();
            }
        });

    }

    private void getMenuItemsForCategory(boolean isInitial) {

        showProgressBar();

        isLoadingItems = true;
        Query currentQuery = mainQuery;
        if (lastDocSnapshot != null) {
            currentQuery = currentQuery.startAfter(lastDocSnapshot);
        }
        if (category != null && !category.isEmpty()) {
            currentQuery = currentQuery.whereEqualTo("category", category);
        }

        currentQuery.get().addOnSuccessListener(snapshots -> {

            if (!snapshots.isEmpty()) {

                if (restaurantMenuRv.getVisibility() == View.INVISIBLE) {
                    restaurantMenuRv.setVisibility(View.VISIBLE);
                }

                lastDocSnapshot = snapshots.getDocuments().get(snapshots.size() - 1);

                if (isInitial) {
                    restaurantMenuRv.setVisibility(View.VISIBLE);
                    menuItems.addAll(snapshots.toObjects(MenuItem.MenuItemSummary.class));
                } else {
                    menuItems.addAll(menuItems.size() - 1, snapshots.toObjects(MenuItem.MenuItemSummary.class));
                }
            } else if (menuItems.isEmpty() && restaurantMenuRv.getVisibility() == View.VISIBLE) {

                restaurantMenuRv.setVisibility(View.INVISIBLE);
                noMenuItemTv.setVisibility(View.VISIBLE);

            }

        }).addOnCompleteListener(task -> {

            if (task.isSuccessful() && task.getResult() != null) {

                if (isInitial) {

                    if (!menuItems.isEmpty()) {
                        restaurantMenuRv.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();

                        if (menuItems.size() == MENU_ITEM_LIMIT && scrollListener == null) {
                            restaurantMenuRv.addOnScrollListener(scrollListener = new ScrollListener());
                        }

                    }
                } else {

                    if (!task.getResult().isEmpty()) {

                        int size = task.getResult().size();

                        adapter.notifyItemRangeInserted(
                                menuItems.size() - size, size);

                        if (task.getResult().size() < MENU_ITEM_LIMIT && scrollListener != null) {
                            restaurantMenuRv.removeOnScrollListener(scrollListener);
                            scrollListener = null;
                        }
                    }
                }
            }

            if (menuItems.isEmpty() && noMenuItemTv.getVisibility() == View.GONE) {
                noMenuItemTv.setVisibility(View.VISIBLE);
            } else if (!menuItems.isEmpty() && noMenuItemTv.getVisibility() == View.VISIBLE) {
                noMenuItemTv.setVisibility(View.GONE);
            }

            isLoadingItems = false;
            hideProgressbar();

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                hideProgressbar();
            }
        });


    }

    @Override
    public void showEditingMenu(ImageView menuIv, int position) {

        final PopupMenu menuItemMenu = new PopupMenu(requireContext(), menuIv);

            menuItemMenu.getMenuInflater().inflate(
                    menuItems.get(position).isDiscounted()?R.menu.menu_item_admin_discounted_menu
                    :R.menu.menu_item_admin_menu, menuItemMenu.getMenu());


        if (menuClickListener == null) {
            menuClickListener = new MenuClickListener(position);
        } else {
            menuClickListener.setPosition(position);
        }

        menuItemMenu.setOnMenuItemClickListener(menuClickListener);

        menuItemMenu.show();

    }


    @Override
    public void onMenuItemDiscounted(int position, Map<String, Object> discountMap) {

        final MenuItem.MenuItemSummary menuItem = menuItems.get(position);
        menuItem.setDiscounted(true);
        menuItem.setDiscountMap(discountMap);

    }

    private void removeItemDiscount(int position) {

        final String menuItemId = menuItems.get(position).getID();

        final DocumentReference menuItemRef =
                firestore.collection("PartneredRestaurant").document(menuItemId);

        menuItemRef.update("isDiscounted", false,
                "discountMap", FieldValue.delete())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        firestore.collection("Offers")
                                .whereEqualTo("destinationId", menuItemId)
                                .whereEqualTo("type", Offer.MENU_ITEM_DISCOUNT)
                                .limit(1).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot snapshots) {

                                if (snapshots != null && snapshots.isEmpty()) {

                                    snapshots.getDocuments().get(0).getReference().delete()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    Toast.makeText(requireContext(),
                                                            "Discount was removed successfully",
                                                            Toast.LENGTH_SHORT).show();

                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                            Toast.makeText(requireContext(),
                                                    "Failed while trying to remove discount",
                                                    Toast.LENGTH_SHORT).show();

                                            menuItemRef.update("isDiscounted", false,
                                                    "discountMap", menuItems.get(position).getDiscountMap());

                                        }
                                    });

                                }

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {


                                Toast.makeText(requireContext(),
                                        "Failed while trying to remove discount",
                                        Toast.LENGTH_SHORT).show();


                                menuItemRef.update("isDiscounted", false,
                                        "discountMap", menuItems.get(position).getDiscountMap());

                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {


                Toast.makeText(requireContext(),
                        "Failed while trying to remove discount",
                        Toast.LENGTH_SHORT).show();


                menuItemRef.update("isDiscounted", false,
                        "discountMap", menuItems.get(position).getDiscountMap());

            }
        });


    }

    @Override
    public void showMenuItem(int position) {

        final Intent menuItemIntent = new Intent(requireContext(), MenuItemActivity.class);
        menuItemIntent.putExtra("MenuItemID", menuItems.get(position).getID());
        startActivity(menuItemIntent);

    }

    @Override
    public void favMenuItem(int position) {

        final MenuItem.MenuItemSummary menuItem = menuItems.get(position);
        final MenuItemModel[] model = {new MenuItemModel(menuItem)};
        model[0].setFavored(likedMenuItems.contains(menuItem.getID()));
        model[0].addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {

                if (arg instanceof Integer) {

                    switch ((int) arg) {

                        case MenuItemModel.UN_FAVORING_SUCCESS:

                            getLikedMenuItems().remove(menuItem.getID());
                            adapter.notifyItemChanged(position);

                            break;
                        case MenuItemModel.FAVORING_SUCCESS:
                            getLikedMenuItems().add(menuItem.getID());
                            adapter.notifyItemChanged(position);

                            break;
                    }


                } else if (arg instanceof Map) {

                    final Map<Integer, Object> resultMap = (Map<Integer, Object>) arg;

                    final int key = resultMap.keySet().iterator().next();

                    switch (key) {
                        case MenuItemModel.UN_FAVORING_FAILED:

                            Toast.makeText(requireContext(),
                                    "Failed while trying to remove this menu item from your favorite!" +
                                            " Please Try again", Toast.LENGTH_LONG).show();

                            Log.d(TAG, (String) resultMap.get(key));

                            break;

                        case MenuItemModel.FAVORING_FAILED:

                            Toast.makeText(requireContext(),
                                    "Failed while trying to add this menu item to your favorite!" +
                                            " Please Try again", Toast.LENGTH_LONG).show();

                            Log.d(TAG, (String) resultMap.get(key));

                            break;


                    }


                }

                model[0].deleteObserver(this);
                model[0] = null;
            }
        });

        model[0].favOrUnFavItem(menuItems.get(position).getRestaurantId(), currentUid);

    }

    private List<String> getLikedMenuItems() {
        if (likedMenuItems == null)
            likedMenuItems = new ArrayList<>();

        return likedMenuItems;
    }


    @Override
    public void itemSelected(int position, String type) {

        final int previousSelected = selectableAdapter.getSelectedItem();


        if (previousSelected == position) {

            selectableAdapter.setSelectedItem(-1);
            selectableAdapter.notifyItemChanged(previousSelected);
            category = null;

        } else {

            category = selectableItems.get(position).getID();
            selectableAdapter.setSelectedItem(position);
            selectableAdapter.notifyItemChanged(position);

            if (previousSelected != -1) {
                selectableAdapter.notifyItemChanged(previousSelected);
            }

        }

        menuItems.clear();
        adapter.notifyDataSetChanged();
        lastDocSnapshot = null;
        getMenuItemsForCategory(true);

    }

    private void hideProgressbar() {
        if (menuProgressBar.getVisibility() == View.VISIBLE) {
            menuProgressBar.setVisibility(View.GONE);
        }
    }

    private void showProgressBar() {

        if (menuProgressBar.getVisibility() == View.GONE) {
            menuProgressBar.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onDestroy() {

        if (scrollListener != null && restaurantMenuRv != null) {
            restaurantMenuRv.removeOnScrollListener(scrollListener);
        }
        super.onDestroy();
    }

    private class ScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (!isLoadingItems &&
                    !recyclerView.canScrollVertically(1) &&
                    newState == RecyclerView.SCROLL_STATE_IDLE) {

                Log.d("ttt", "is at bottom");

                getMenuItemsForCategory(false);

            }
        }
    }

    private class MenuClickListener implements PopupMenu.OnMenuItemClickListener {

        private int position;

        MenuClickListener(int position) {
            this.position = position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        @Override
        public boolean onMenuItemClick(android.view.MenuItem item) {

            Log.d("ttt", "onMenuItemClick: " + position);

            final int itemId = item.getItemId();

            if (itemId == R.id.edit_item_action) {


            } else if (itemId == R.id.remove_item_action) {

                showProgressBar();

                final MenuItem.MenuItemSummary menuItem = menuItems.get(position);

//                if(model == null || !model.getMenuItem().getID().equals(menuItem.getID())){
                MenuItemModel model = new MenuItemModel(menuItem);
//                }

                model.addObserver((o, arg) -> {

                    if (arg instanceof Map) {

                        final Map<Integer, Object> resultMap = (Map<Integer, Object>) arg;
                        final Integer resultKey = resultMap.keySet().iterator().next();

                        switch (resultKey) {

                            case MenuItemModel.REMOVE_SUCCESS:

                                final int index = menuItems.indexOf(menuItem);
                                menuItems.remove(index);
                                adapter.notifyItemRemoved(index);

                                break;

                            case MenuItemModel.REMOVE_FAILED:

                                Log.d(TAG, "failed while trying to remove menu item " +
                                        menuItem.getID() + " for: " + resultMap.get(resultKey).toString());

                                Toast.makeText(requireContext(),
                                        "Failed while trying to delete this menu item!" +
                                                "Please try again", Toast.LENGTH_SHORT).show();

                                break;

                        }

                        hideProgressbar();
                    }
                });

                model.deleteMenuItem();
                return true;

            } else if (itemId == R.id.discount_item_action) {

                DiscountDialogFragment.newInstance(menuItems.get(position))
                        .show(getChildFragmentManager(), "discountDialogFragment");


                return true;
            } else if (itemId == R.id.remove_discount_item_action) {

                removeItemDiscount(position);

                return true;
            }

            return false;

        }
    }

}