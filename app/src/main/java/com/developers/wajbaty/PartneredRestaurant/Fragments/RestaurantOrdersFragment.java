package com.developers.wajbaty.PartneredRestaurant.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.developers.wajbaty.Adapters.RestaurantOrdersAdapter;
import com.developers.wajbaty.Models.RestaurantOrder;
import com.developers.wajbaty.PartneredRestaurant.Activities.RestaurantOrderActivity;
import com.developers.wajbaty.R;
import com.developers.wajbaty.Utils.GlobalVariables;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;


public class RestaurantOrdersFragment extends Fragment implements RestaurantOrdersAdapter.RestaurantsOrdersListener {

    private static final int ORDER_PAGE_LIMIT = 10;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    //views
    private RecyclerView restaurantOrdersRv;
    private ProgressBar restaurantOrdersProgressBar;
    private TextView restaurantOrdersEmptyTv;

    //adapter
    private RestaurantOrdersAdapter adapter;
    private ArrayList<RestaurantOrder> restaurantOrders;

    private Query mainQuery;
    private DocumentSnapshot lastDocSnapshot;
    private boolean isLoadingOrders;
    private ScrollListener scrollListener;

    public RestaurantOrdersFragment() {
    }

    public static RestaurantOrdersFragment newInstance(String param1, String param2) {
        RestaurantOrdersFragment fragment = new RestaurantOrdersFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        restaurantOrders = new ArrayList<>();
        adapter = new RestaurantOrdersAdapter(restaurantOrders, this);

        mainQuery = FirebaseFirestore.getInstance().collection("PartneredRestaurant")
                .document(GlobalVariables.getCurrentRestaurantId())
                .collection("MealsOrders")
                .whereEqualTo("status", RestaurantOrder.TYPE_PENDING)
                .orderBy("orderTimeInMillis", Query.Direction.DESCENDING)
                .limit(ORDER_PAGE_LIMIT);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant_orders, container, false);

        restaurantOrdersRv = view.findViewById(R.id.restaurantOrdersRv);
        restaurantOrdersProgressBar = view.findViewById(R.id.restaurantOrdersProgressBar);
        restaurantOrdersEmptyTv = view.findViewById(R.id.restaurantOrdersEmptyTv);

        restaurantOrdersRv.setAdapter(adapter);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d("ttt", "GlobalVariables.getCurrentRestaurantId(): " + GlobalVariables.getCurrentRestaurantId());

        getRestaurantOrders(true);

    }


    private void getRestaurantOrders(boolean isInitial) {

        showProgressBar();

        isLoadingOrders = true;
        Query currentQuery = mainQuery;
        if (lastDocSnapshot != null) {
            currentQuery = currentQuery.startAfter(lastDocSnapshot);
        }

        currentQuery.get().addOnSuccessListener(snapshots -> {

            Log.d("ttt", "onsucess");
            if (!snapshots.isEmpty()) {

                Log.d("ttt", "!snapshots.isEmpty()");
                lastDocSnapshot = snapshots.getDocuments().get(snapshots.size() - 1);

                if (isInitial) {
                    restaurantOrders.addAll(snapshots.toObjects(RestaurantOrder.class));
                } else {
                    restaurantOrders.addAll(restaurantOrders.size() - 1, snapshots.toObjects(RestaurantOrder.class));
                }
            } else {
                Log.d("ttt", "snapshots.isEmpty()");
            }
        }).addOnCompleteListener(task -> {

            if (task.isSuccessful() && task.getResult() != null) {

                if (isInitial) {

                    if (!restaurantOrders.isEmpty()) {
                        Log.d("ttt", "isInitial !restaurantOrders.isEmpty()");
                        adapter.notifyDataSetChanged();

                        if (restaurantOrders.size() == ORDER_PAGE_LIMIT && scrollListener == null) {
                            restaurantOrdersRv.addOnScrollListener(scrollListener = new ScrollListener());
                        }

                    } else {
                        Log.d("ttt", "isInitial restaurantOrders.isEmpty()");
                    }
                } else {

                    if (!task.getResult().isEmpty()) {
                        Log.d("ttt", "not initial !restaurantOrders.isEmpty()");
                        int size = task.getResult().size();

                        adapter.notifyItemRangeInserted(
                                restaurantOrders.size() - size, size);

                        if (task.getResult().size() < ORDER_PAGE_LIMIT && scrollListener != null) {
                            restaurantOrdersRv.removeOnScrollListener(scrollListener);
                            scrollListener = null;
                        }
                    } else {
                        Log.d("ttt", "not initial restaurantOrders.isEmpty()");
                    }
                }
            }

            if (restaurantOrders.isEmpty() && restaurantOrdersEmptyTv.getVisibility() == View.GONE) {
                restaurantOrdersEmptyTv.setVisibility(View.VISIBLE);
            } else if (!restaurantOrders.isEmpty() && restaurantOrdersEmptyTv.getVisibility() == View.VISIBLE) {
                restaurantOrdersEmptyTv.setVisibility(View.GONE);
            }

            isLoadingOrders = false;
            hideProgressbar();

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                hideProgressbar();
            }
        });


    }


    @Override
    public void onRestaurantOrderClicked(int position) {

        startActivity(new Intent(requireContext(), RestaurantOrderActivity.class)
                .putExtra("restaurantOrder", restaurantOrders.get(position)));

    }

    private void hideProgressbar() {
        if (restaurantOrdersProgressBar.getVisibility() == View.VISIBLE) {
            restaurantOrdersProgressBar.setVisibility(View.GONE);
        }
    }

    private void showProgressBar() {

        if (restaurantOrdersProgressBar.getVisibility() == View.GONE) {
            restaurantOrdersProgressBar.setVisibility(View.VISIBLE);
        }

    }

    private class ScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (!isLoadingOrders &&
                    !recyclerView.canScrollVertically(1) &&
                    newState == RecyclerView.SCROLL_STATE_IDLE) {

                Log.d("ttt", "is at bottom");

                getRestaurantOrders(false);

            }
        }
    }


}