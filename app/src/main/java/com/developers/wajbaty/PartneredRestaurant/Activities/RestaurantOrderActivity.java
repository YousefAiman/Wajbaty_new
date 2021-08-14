package com.developers.wajbaty.PartneredRestaurant.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

import com.developers.wajbaty.Activities.MenuItemActivity;
import com.developers.wajbaty.Adapters.CartInfoAdapter;
import com.developers.wajbaty.Models.CartItem;
import com.developers.wajbaty.Models.RestaurantOrder;
import com.developers.wajbaty.R;
import com.developers.wajbaty.Utils.GlobalVariables;
import com.developers.wajbaty.Utils.TimeFormatter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RestaurantOrderActivity extends AppCompatActivity implements CartInfoAdapter.CartClickListener,
        View.OnClickListener {

    private static final int CART_ITEM_LIMIT = 10;
    private static final String TAG = "RestaurantOrder";
    private RestaurantOrder restaurantOrder;

    //firebase
    private FirebaseFirestore firestore;
    private Query mainQuery;
    private DocumentSnapshot lastDocSnap;
    private boolean isLoadingItems;
    private ScrollListener scrollListener;
    private CollectionReference menuItemRef;
    private List<ListenerRegistration> removeListeners;

    //adapter
    private CartInfoAdapter adapter;
    private ArrayList<CartItem> cartItems;

    //views
    private ImageView restaurantOrderDriverUserIv;
    private TextView restaurantOrderDriverNameTv, restaurantOrderTimeTv, restaurantOrderItemCountTv,
            restaurantOrderTotalCostTv, restaurantOrderStatusTv;
    private RecyclerView restaurantOrderCartItemsRv;
    private Button restaurantOrderFinishedBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_order);

        initializeObjects();

        getViews();

        populateViews();

        getCartItems(true);

    }

    private void initializeObjects() {

        final Intent intent = getIntent();
        if (intent != null && intent.hasExtra("restaurantOrder")) {
            restaurantOrder = (RestaurantOrder) intent.getSerializableExtra("restaurantOrder");
        }

        firestore = FirebaseFirestore.getInstance();

        menuItemRef = firestore.collection("MenuItems");

        mainQuery = firestore.collection("PartneredRestaurant")
                .document(GlobalVariables.getCurrentRestaurantId())
                .collection("MealsOrders")
                .document(restaurantOrder.getID())
                .collection("Cart")
                .orderBy("timeAdded", Query.Direction.DESCENDING)
                .limit(CART_ITEM_LIMIT);

        cartItems = new ArrayList<>();
        adapter = new CartInfoAdapter(cartItems, this, this);
    }

    private void getViews() {

        final NestedScrollView restaurantOrderNSV = findViewById(R.id.restaurantOrderNSV);
        restaurantOrderNSV.setNestedScrollingEnabled(false);

        final Toolbar restaurantOrderToolbar = findViewById(R.id.restaurantOrderToolbar);
        restaurantOrderToolbar.setNavigationOnClickListener(v -> finish());

        restaurantOrderDriverUserIv = findViewById(R.id.restaurantOrderDriverUserIv);
        restaurantOrderDriverNameTv = findViewById(R.id.restaurantOrderDriverNameTv);
        restaurantOrderTimeTv = findViewById(R.id.restaurantOrderTimeTv);
        restaurantOrderItemCountTv = findViewById(R.id.restaurantOrderItemCountTv);
        restaurantOrderTotalCostTv = findViewById(R.id.restaurantOrderTotalCostTv);
        restaurantOrderCartItemsRv = findViewById(R.id.restaurantOrderCartItemsRv);
        restaurantOrderStatusTv = findViewById(R.id.restaurantOrderStatusTv);
        restaurantOrderFinishedBtn = findViewById(R.id.restaurantOrderFinishedBtn);

        restaurantOrderCartItemsRv.setAdapter(adapter);
    }


    private void populateViews() {

        if (restaurantOrder.getStatus() == RestaurantOrder.TYPE_DONE) {
            restaurantOrderFinishedBtn.setVisibility(View.GONE);
        } else if (restaurantOrder.getStatus() == RestaurantOrder.TYPE_PENDING) {
            restaurantOrderFinishedBtn.setOnClickListener(this);
        }

        if (restaurantOrder.getDriverID() != null) {

            firestore.collection("Users").document(restaurantOrder.getDriverID())
                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot snapshot) {
                    if (snapshot.exists()) {
                        if (snapshot.contains("imageURL")) {
                            Picasso.get().load(snapshot.getString("imageURL"))
                                    .fit().centerCrop().into(restaurantOrderDriverUserIv);
                        }
                        if (snapshot.contains("name")) {
                            restaurantOrderDriverNameTv.setText(snapshot.getString("name"));
                        }
                    }
                }
            });

        } else {
            restaurantOrderDriverNameTv.setText("No driver yet");
        }


        restaurantOrderTimeTv.setText(TimeFormatter.formatTime(restaurantOrder.getOrderTimeInMillis()));
        restaurantOrderItemCountTv.setText("Total Items: " + restaurantOrder.getItemCount());
        restaurantOrderTotalCostTv.setText(restaurantOrder.getTotalCost() + " " + restaurantOrder.getCurrency());


        String status = "";

        switch (restaurantOrder.getStatus()) {
            case RestaurantOrder.TYPE_PENDING:
                status = "Pending";
                break;
            case RestaurantOrder.TYPE_DONE:
                status = "Done";
                break;
            case RestaurantOrder.TYPE_CANCELLED:
                status = "Cancelled";
                break;
        }
        restaurantOrderStatusTv.setText(status);

        getCartItems(true);

    }


    private void getCartItems(boolean isInitial) {

//        showProgressBar();

        isLoadingItems = true;
        Query currentQuery = mainQuery;
        if (lastDocSnap != null) {
            currentQuery = currentQuery.startAfter(lastDocSnap);
        }

        final List<CartItem> addedCartItems = new ArrayList<>();
        final List<String> addedCartItemsIds = new ArrayList<>();

        currentQuery.get().addOnSuccessListener(snapshots -> {

            if (!snapshots.isEmpty()) {

                Log.d(TAG, "gotten cart items size: " + snapshots.size());

                lastDocSnap = snapshots.getDocuments().get(snapshots.size() - 1);

                for (DocumentSnapshot snap : snapshots) {
                    Log.d(TAG, "cart item id: " + snap.getId());
                    addedCartItemsIds.add(snap.getId());
                }

                addedCartItems.addAll(snapshots.toObjects(CartItem.class));

            }

        }).addOnCompleteListener(task -> {

            Log.d(TAG, "getting cart item complete");
            if (task.isSuccessful() && task.getResult() != null && !addedCartItemsIds.isEmpty()) {
                Log.d(TAG, "addedCartItemsIds.isEmpty()");
                menuItemRef.whereIn("id", addedCartItemsIds)
                        .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshots) {

                        Log.d(TAG, "gotten menu items: " + snapshots.size());

                        final List<DocumentSnapshot> documentSnapshots = snapshots.getDocuments();

                        for (int i = 0; i < snapshots.size(); i++) {
                            final DocumentSnapshot snapshot = documentSnapshots.get(i);

                            if (snapshot.contains("imageUrls")) {

                                List<String> imageUrls =
                                        (List<String>) snapshot.get("imageUrls");

                                if (imageUrls != null && !imageUrls.isEmpty()) {
                                    addedCartItems.get(i).setImageUrl(imageUrls.get(0));
                                }

                            }

                            addedCartItems.get(i).setName(snapshot.getString("name"));
                            addedCartItems.get(i).setCurrency(snapshot.getString("currency"));
                            if (snapshot.contains("discounted") && snapshot.getBoolean("discounted")
                                    && snapshot.contains("discountMap")) {

                                final Map<String, Object> discountMap = (Map<String, Object>) snapshot.get("discountMap");

                                if (discountMap != null && discountMap.containsKey("endsAt") && ((long) discountMap.get("endsAt")) > System.currentTimeMillis()) {
                                    addedCartItems.get(i).setPrice(((Double) discountMap.get("discountedPrice")).floatValue());
                                } else {

                                    snapshot.getReference().update("discounted", false,
                                            "discountMap", FieldValue.delete());

                                    addedCartItems.get(i).setPrice(((Double) Objects.requireNonNull(snapshot.get("price"))).floatValue());

                                }

                            } else {
                                addedCartItems.get(i).setPrice(((Double) Objects.requireNonNull(snapshot.get("price"))).floatValue());

                            }


                        }

                    }
                }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        //                    cartRv.setVisibility(View.VISIBLE);
//                    cartItems.addAll(snapshots.toObjects(CartItem.class));
//                } else {
//                    cartItems.addAll(cartItems.size() - 1, snapshots.toObjects(CartItem.class));
//                }


                        Log.d(TAG, "gotten menu items complete: " + task.getResult().size());


                        if (task.isSuccessful() && !task.getResult().isEmpty()) {

                            Log.d(TAG, "gotten menu items not empty: " + task.getResult().size());

                            if (isInitial) {

                                cartItems.addAll(addedCartItems);

                                if (!cartItems.isEmpty()) {
                                    adapter.notifyDataSetChanged();
                                    if (cartItems.size() == CART_ITEM_LIMIT && scrollListener == null) {
                                        restaurantOrderCartItemsRv.addOnScrollListener(scrollListener = new ScrollListener());
                                    }

                                }


                            } else {

                                if (!task.getResult().isEmpty()) {

                                    cartItems.addAll(cartItems.size() - 1, addedCartItems);

                                    int size = task.getResult().size();

                                    adapter.notifyItemRangeInserted(
                                            cartItems.size() - size, size);

                                    if (task.getResult().size() < CART_ITEM_LIMIT && scrollListener != null) {
                                        restaurantOrderCartItemsRv.removeOnScrollListener(scrollListener);
                                        scrollListener = null;
                                    }
                                }
                            }

                            if (!addedCartItems.isEmpty()) {

                                final List<String> itemIds = new ArrayList<>();
                                for (CartItem cartItem : addedCartItems) {
                                    itemIds.add(cartItem.getItemId());
                                }

                                addCartRemoveListener(itemIds);
                            }

                        }

                        isLoadingItems = false;

                    }
                });

            } else {

                isLoadingItems = false;
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Log.d(TAG, "added cart items failed: " + e.getMessage());
                isLoadingItems = false;
            }
        });


    }


    private void addCartRemoveListener(List<String> itemIds) {

        if (removeListeners == null)
            removeListeners = new ArrayList<>();

        removeListeners.add(
                firestore.collection("MenuItems")
                        .whereIn("id", itemIds)
                        .whereEqualTo("isBeingRemoved", true)
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                                if (value == null)
                                    return;

                                for (DocumentChange dc : value.getDocumentChanges()) {

                                    final String documentId = dc.getDocument().getId();

                                    if (dc.getType() == DocumentChange.Type.ADDED) {

//                               final AtomicInteger position = new AtomicInteger(-1);
                                        final Thread thread = new Thread(new Runnable() {
                                            @Override
                                            public void run() {

                                                for (int i = 0; i < cartItems.size(); i++) {

                                                    if (cartItems.get(i).getItemId().equals(documentId)) {
//                                                position.set(i);
                                                        cartItems.remove(i);
                                                        final int finalI = i;
                                                        restaurantOrderCartItemsRv.post(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                adapter.notifyItemRemoved(finalI);
                                                            }
                                                        });

                                                        break;
                                                    }

                                                }


                                            }
                                        });

                                        thread.start();
//
//                                try {
//
//                                    thread.join();
//
//                                    cartItems.get()
//
//                                } catch (InterruptedException e) { e.printStackTrace(); }
//
                                    }

                                }

                            }
                        }));

    }

    @Override
    public void showMenuItem(int position) {

        final Intent menuItemIntent = new Intent(this, MenuItemActivity.class);
        menuItemIntent.putExtra("MenuItemID", cartItems.get(position).getItemId());
        startActivity(menuItemIntent);

    }

    @Override
    public void onClick(View v) {

        if (v.getId() == restaurantOrderFinishedBtn.getId()) {

            firestore.collection("PartneredRestaurant")
                    .document(GlobalVariables.getCurrentRestaurantId())
                    .collection("MealsOrders")
                    .document(restaurantOrder.getID()).update("status", RestaurantOrder.TYPE_DONE)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            Toast.makeText(RestaurantOrderActivity.this,
                                    "Order Confirmed Done", Toast.LENGTH_SHORT).show();

                            restaurantOrderFinishedBtn.setVisibility(View.GONE);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(RestaurantOrderActivity.this,
                            "Failed to confirm order is done! Please Try again"
                            , Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (scrollListener != null && restaurantOrderCartItemsRv != null) {
            restaurantOrderCartItemsRv.removeOnScrollListener(scrollListener);
        }


        if (removeListeners != null) {
            for (ListenerRegistration listenerRegistration : removeListeners) {
                listenerRegistration.remove();
            }
        }

    }

    private class ScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (!isLoadingItems &&
                    !recyclerView.canScrollVertically(1) &&
                    newState == RecyclerView.SCROLL_STATE_IDLE) {

                getCartItems(false);

            }
        }
    }
}