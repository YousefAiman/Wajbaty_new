package com.developers.wajbaty.Customer.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.developers.wajbaty.Activities.MenuItemActivity;
import com.developers.wajbaty.Adapters.MenuItemsAdapter;
import com.developers.wajbaty.Adapters.SelectableChecksAdapter;
import com.developers.wajbaty.Models.MenuItem;
import com.developers.wajbaty.Models.MenuItemModel;
import com.developers.wajbaty.Models.SelectableItem;
import com.developers.wajbaty.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;


public class MenuItemsFragment extends Fragment implements MenuItemsFilterFragment.MenuItemsFilterListener,
        MenuItemsAdapter.CustomerMenuItemClickListener,
        View.OnClickListener,
        SelectableChecksAdapter.SelectListener {

    private static final int MENU_ITEM_LIMIT = 10;
    private static final String REGION = "region";

    private String region;

    //menu items
    private MenuItemsAdapter adapter;
    private ArrayList<MenuItem.MenuItemSummary> menuItems;


    //firebase
    private Query mainQuery;
    private DocumentSnapshot lastDocSnapshot;
    private boolean isLoadingItems;
    private String category;
    private String filter;
    private ScrollListener scrollListener;
    private CollectionReference userFavRef;

    //views
    private RecyclerView menuItemsRv, menuItemsFilterRv;
    private ProgressBar menuItemsProgressBar;
    private TextView noMenuItemTv;
    private ExtendedFloatingActionButton filterFab;

    //liked
    private List<String> likedMenuItems;
    private String currentUid;
    private FirebaseFirestore firestore;

    //filter
    private SelectableChecksAdapter selectableAdapter;
    private ArrayList<SelectableItem> selectableItems;

    public MenuItemsFragment() {
    }


    public static MenuItemsFragment newInstance(String region) {
        MenuItemsFragment fragment = new MenuItemsFragment();
        Bundle args = new Bundle();
        args.putString(REGION, region);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            region = getArguments().getString(REGION);
        }

        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firestore = FirebaseFirestore.getInstance();

        filter = "rating";

        mainQuery = firestore.collection("MenuItems").limit(MENU_ITEM_LIMIT);

        if (region != null && !region.isEmpty()) {
            mainQuery = mainQuery.whereEqualTo("region", region);
        }

        Log.d("ttt", "region: " + region);


        likedMenuItems = new ArrayList<>();
        menuItems = new ArrayList<>();

        adapter = new MenuItemsAdapter(menuItems, this, likedMenuItems, requireContext());

        selectableItems = new ArrayList<>();
        selectableAdapter = new SelectableChecksAdapter(selectableItems, this, -1);

        userFavRef = firestore.collection("Users")
                .document(currentUid).collection("Favorites");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu_items, container, false);
        menuItemsRv = view.findViewById(R.id.menuItemsRv);
        menuItemsProgressBar = view.findViewById(R.id.menuItemsProgressBar);
        noMenuItemTv = view.findViewById(R.id.noMenuItemTv);
        filterFab = view.findViewById(R.id.filterFab);
        menuItemsFilterRv = view.findViewById(R.id.menuItemsFilterRv);

        menuItemsRv.setAdapter(adapter);
        menuItemsFilterRv.setAdapter(selectableAdapter);
        filterFab.setOnClickListener(this);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fetchFilters();
        getMenuItems(true);

    }

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


    private void getMenuItems(boolean isInitial) {

        if (!isInitial) {
            menuItemsProgressBar.setVisibility(View.VISIBLE);
        }

        isLoadingItems = true;
        Query currentQuery = mainQuery;

        if (category != null && !category.isEmpty()) {
            currentQuery = currentQuery.whereEqualTo("category", category);
        }


        if (filter != null && !filter.isEmpty()) {
            currentQuery = currentQuery.orderBy(filter, Query.Direction.DESCENDING);
        }

        if (lastDocSnapshot != null) {
            currentQuery = currentQuery.startAfter(lastDocSnapshot);
        }

        currentQuery.get().addOnSuccessListener(snapshots -> {

            if (!snapshots.isEmpty()) {

                lastDocSnapshot = snapshots.getDocuments().get(snapshots.size() - 1);

                final List<Task<QuerySnapshot>> tasks = new ArrayList<>();

                for (DocumentSnapshot snapshot : snapshots) {

                    Log.d("ttt", "looking if: " + snapshot.getId() + " is liked");

                    tasks.add(userFavRef.whereArrayContains("FavoriteMenuItems", snapshot.getId())
                            .limit(1).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot snapshots) {
                                    Log.d("ttt", "checking liked for: " + snapshot.getId() + " success");
                                    if (!snapshots.isEmpty()) {
                                        Log.d("ttt", "is liked");
                                        if (!likedMenuItems.contains(snapshot.getId())) {
                                            likedMenuItems.add(snapshot.getId());
                                        }
                                    } else {
                                        Log.d("ttt", "is not liked");
                                        likedMenuItems.remove(snapshot.getId());
                                    }
                                }
                            }));
                }

                Tasks.whenAllSuccess(tasks).addOnSuccessListener(new OnSuccessListener<List<Object>>() {
                    @Override
                    public void onSuccess(List<Object> objects) {
                        Log.d("ttt", "Tasks.whenAllSuccess");
                        if (isInitial) {
                            menuItems.addAll(snapshots.toObjects(MenuItem.MenuItemSummary.class));
                        } else {
                            menuItems.addAll(menuItems.size() - 1, snapshots.toObjects(MenuItem.MenuItemSummary.class));
                        }

                    }
                }).addOnCompleteListener(new OnCompleteListener<List<Object>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<Object>> task) {

                        if (isInitial) {

                            if (!menuItems.isEmpty()) {

                                Log.d("ttt", "!menuItems.isEmpty()");

                                adapter.notifyDataSetChanged();

                                if (menuItems.size() == MENU_ITEM_LIMIT && scrollListener == null) {
                                    menuItemsRv.addOnScrollListener(scrollListener = new ScrollListener());
                                }

                            } else {
                                checkNoItemsFound();
                                Log.d("ttt", "menuItems.isEmpty()");
                            }
                        } else {


                            if (!menuItems.isEmpty()) {
                                Log.d("ttt", "!menuItems.isEmpty()");
                                int size = task.getResult().size();

                                adapter.notifyItemRangeInserted(
                                        menuItems.size() - size, size);

                                if (task.getResult().size() < MENU_ITEM_LIMIT && scrollListener != null) {
                                    menuItemsRv.removeOnScrollListener(scrollListener);
                                    scrollListener = null;
                                }
                            } else {
                                Log.d("ttt", "menuItems.isEmpty()");
                            }
                        }


                        isLoadingItems = false;
                        menuItemsProgressBar.setVisibility(View.GONE);
                    }
                });

            } else if (isInitial) {

                checkNoItemsFound();
            }

        })
//                .addOnCompleteListener(task -> {
//
//            Log.d("ttt","isInitial: "+isInitial);
//
//
//        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        isLoadingItems = false;
                        menuItemsProgressBar.setVisibility(View.GONE);
                    }
                });

    }


    void checkNoItemsFound() {
        if (menuItems.isEmpty() && noMenuItemTv.getVisibility() == View.GONE) {
            noMenuItemTv.setVisibility(View.VISIBLE);
        } else if (!menuItems.isEmpty() && noMenuItemTv.getVisibility() == View.VISIBLE) {
            noMenuItemTv.setVisibility(View.GONE);
        }
    }

    @Override
    public void onFilterSelected(String category, String filterBy) {

        noMenuItemTv.setVisibility(View.GONE);

        this.category = category;
        this.filter = filterBy;

        menuItems.clear();
        adapter.notifyDataSetChanged();
        lastDocSnapshot = null;
        getMenuItems(true);

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

                        case MenuItemModel.FAVORING_SUCCESS:

                            likedMenuItems.add(menuItem.getID());
                            adapter.notifyItemChanged(position);

                            break;
                        case MenuItemModel.UN_FAVORING_SUCCESS:

                            likedMenuItems.remove(menuItem.getID());
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

                            Log.d("ttt", (String) resultMap.get(key));

                            break;

                        case MenuItemModel.FAVORING_FAILED:

                            Toast.makeText(requireContext(),
                                    "Failed while trying to add this menu item to your favorite!" +
                                            " Please Try again", Toast.LENGTH_LONG).show();

                            Log.d("ttt", (String) resultMap.get(key));

                            break;


                    }

                }

                model[0].deleteObserver(this);
                model[0] = null;
            }
        });

        model[0].favOrUnFavItem(
                menuItems.get(position).getRestaurantId(), currentUid
        );

    }

    @Override
    public void onClick(View v) {

        if (v.getId() == filterFab.getId()) {

            MenuItemsFilterFragment.newInstance(this, category, filter).show(
                    getChildFragmentManager(), "filterFragment");

        }

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
        getMenuItems(true);
    }

    private class ScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (!isLoadingItems && !recyclerView.canScrollVertically(1)) {

                Log.d("ttt", "is at bottom");

                getMenuItems(false);

            }
        }
    }

}