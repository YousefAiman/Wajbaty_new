package com.developers.wajbaty.Customer.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.developers.wajbaty.Adapters.FavoriteRestaurantsAdapter;
import com.developers.wajbaty.Models.PartneredRestaurant;
import com.developers.wajbaty.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

public class FavoriteMenuItemsFragment extends Fragment implements FavoriteRestaurantsAdapter.FavoriteRestaurantsListener {

    private static final int FAVORITE_ITEM_LIMIT = 10;
    private static final String TAG = "FavoriteMenuItemsFragment";

    //views
    private RecyclerView favoritesRv;
    private TextView noFavItemTv;

    //adapter
    private FavoriteRestaurantsAdapter favoriteRestaurantsAdapter;
    private ArrayList<PartneredRestaurant.PartneredRestaurantSummary> restaurantSummaries;

    //firebase
    private ArrayList<String> favRestaurantsIds;
    private FirebaseFirestore firestore;
    private String currentUid;
    private Query mainQuery;
    private boolean isLoadingItems;
    private ScrollListener scrollListener;

    public FavoriteMenuItemsFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firestore = FirebaseFirestore.getInstance();

        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        restaurantSummaries = new ArrayList<>();

        favoriteRestaurantsAdapter = new FavoriteRestaurantsAdapter(restaurantSummaries, this, requireContext());

        mainQuery = firestore.collection("PartneredRestaurant");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);
        favoritesRv = view.findViewById(R.id.favoritesRv);
        noFavItemTv = view.findViewById(R.id.noFavItemTv);

        favoritesRv.setAdapter(favoriteRestaurantsAdapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getFavRestaurantsIds();

    }

    private void getFavRestaurantsIds() {

        firestore.collection("Users")
                .document(currentUid)
                .collection("Favorites")
                .document("FavoriteRestaurants")
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot snapshot) {
                favRestaurantsIds = (ArrayList<String>) snapshot.get("FavoriteRestaurants");
            }
        }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (favRestaurantsIds != null && !favRestaurantsIds.isEmpty()) {
                    getFavoriteRestaurants(true);
                } else {

                    favoritesRv.setVisibility(View.INVISIBLE);
                    noFavItemTv.setVisibility(View.VISIBLE);
                }

            }
        });

    }

    private void getFavoriteRestaurants(boolean isInitial) {

//        showProgressBar();

        isLoadingItems = true;

        Query currentQuery = mainQuery;

        if (restaurantSummaries.isEmpty()) {

            if (favRestaurantsIds.size() > 10) {
                currentQuery = mainQuery.whereIn("ID", favRestaurantsIds.subList(0, 10));
            } else {
                currentQuery = mainQuery.whereIn("ID", favRestaurantsIds);
            }

        } else if (favRestaurantsIds.size() >= restaurantSummaries.size() + 10) {

            currentQuery = mainQuery.whereIn("ID", favRestaurantsIds.subList(
                    restaurantSummaries.size(), restaurantSummaries.size() + 10));

        } else if (favRestaurantsIds.size() > restaurantSummaries.size()) {

            currentQuery = mainQuery.whereIn("ID", favRestaurantsIds.subList(
                    restaurantSummaries.size(), favRestaurantsIds.size()));
        }


        currentQuery.get().addOnSuccessListener(snapshots -> {

            if (!snapshots.isEmpty()) {

                if (favoritesRv.getVisibility() == View.INVISIBLE) {
                    favoritesRv.setVisibility(View.VISIBLE);
                }


                if (isInitial) {
                    favoritesRv.setVisibility(View.VISIBLE);
                    restaurantSummaries.addAll(snapshots.toObjects(PartneredRestaurant.PartneredRestaurantSummary.class));
                } else {
                    restaurantSummaries.addAll(restaurantSummaries.size() - 1,
                            snapshots.toObjects(PartneredRestaurant.PartneredRestaurantSummary.class));
                }
            } else if (restaurantSummaries.isEmpty() && favoritesRv.getVisibility() == View.VISIBLE) {

                favoritesRv.setVisibility(View.INVISIBLE);

            }

        }).addOnCompleteListener(task -> {

            if (task.isSuccessful() && task.getResult() != null) {

                if (isInitial) {

                    if (!restaurantSummaries.isEmpty()) {
                        favoritesRv.setVisibility(View.VISIBLE);
                        favoriteRestaurantsAdapter.notifyDataSetChanged();

                        if (restaurantSummaries.size() == FAVORITE_ITEM_LIMIT && scrollListener == null) {
                            favoritesRv.addOnScrollListener(scrollListener = new ScrollListener());
                        }

                    }
                } else {

                    if (!task.getResult().isEmpty()) {

                        int size = task.getResult().size();

                        favoriteRestaurantsAdapter.notifyItemRangeInserted(
                                restaurantSummaries.size() - size, size);

                        if (task.getResult().size() < FAVORITE_ITEM_LIMIT && scrollListener != null) {
                            favoritesRv.removeOnScrollListener(scrollListener);
                            scrollListener = null;
                        }
                    }
                }
            }

            if (restaurantSummaries.isEmpty() && noFavItemTv.getVisibility() == View.GONE) {
                noFavItemTv.setVisibility(View.VISIBLE);
            } else if (!restaurantSummaries.isEmpty() && noFavItemTv.getVisibility() == View.VISIBLE) {
                noFavItemTv.setVisibility(View.GONE);
            }

            isLoadingItems = false;
//            hideProgressbar();

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
//                hideProgressbar();
            }
        });

    }


    @Override
    public void removeFromFav(int position) {


    }

    @Override
    public void showRestaurant(int position) {

    }


    private class ScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (!isLoadingItems &&
                    !recyclerView.canScrollVertically(1) &&
                    newState == RecyclerView.SCROLL_STATE_IDLE) {

                Log.d("ttt", "is at bottom");
                getFavoriteRestaurants(false);
//                getMenuItemsForCategory(false);

            }
        }
    }


}