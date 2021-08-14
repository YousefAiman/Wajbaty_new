package com.developers.wajbaty.Customer.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.developers.wajbaty.Activities.MenuItemActivity;
import com.developers.wajbaty.Adapters.CartAdapter;
import com.developers.wajbaty.Models.CartItem;
import com.developers.wajbaty.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CartActivity extends AppCompatActivity implements CartAdapter.CartClickListener,
        View.OnClickListener {

    private static final int CART_ITEM_LIMIT = 10;
    private static final String TAG = "CartActivity";

    //cart
    private CartAdapter cartAdapter;
    private ArrayList<CartItem> cartItems;
    private long totalCost;


    //firebase
    private FirebaseFirestore firestore;
    private Query mainQuery;
    private String currentUid;
    private DocumentSnapshot lastDocSnap;
    private boolean isLoadingItems;
    private ScrollListener scrollListener;
    private CollectionReference cartRef;
    private List<ListenerRegistration> removeListeners;

    //views
    private Toolbar cartToolbar;
    private RecyclerView cartRv;
    private TextView cartTotalPriceTv, noCartItemTv;
    private Button cartOrderDeliveryBtn;
    private ProgressBar cartProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        initializeObjects();

        getViews();

        fetchCartInfo();

        getCartItems(true);

    }

    private void initializeObjects() {

        firestore = FirebaseFirestore.getInstance();

        cartItems = new ArrayList<>();
        cartAdapter = new CartAdapter(cartItems, this, this);

        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mainQuery = firestore.collection("Users")
                .document(currentUid).collection("Cart")
                .orderBy("timeAdded", Query.Direction.DESCENDING).limit(CART_ITEM_LIMIT);

        cartRef = firestore.collection("Users")
                .document(currentUid).collection("Cart");

    }

    private void getViews() {

        cartToolbar = findViewById(R.id.cartToolbar);
        cartRv = findViewById(R.id.cartRv);
        cartProgressBar = findViewById(R.id.cartProgressBar);
        noCartItemTv = findViewById(R.id.noCartItemTv);
        cartTotalPriceTv = findViewById(R.id.cartTotalPriceTv);
        cartOrderDeliveryBtn = findViewById(R.id.cartOrderDeliveryBtn);

        cartToolbar.setNavigationOnClickListener(v -> finish());
        cartOrderDeliveryBtn.setOnClickListener(this);
        cartRv.setAdapter(cartAdapter);

    }

    private void fetchCartInfo() {

        firestore.collection("Users")
                .document(currentUid).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {

                        if (snapshot.exists() && snapshot.contains("CartTotal")) {
                            totalCost = snapshot.getLong("CartTotal");
                        } else {
                            totalCost = 0;
                        }

                        cartTotalPriceTv.setText("Total Price: " + totalCost + "ILS");

                        if (snapshot.contains("currentDelivery") && snapshot.get("currentDelivery") != null
                                && ((Map<String, Object>) snapshot.get("currentDelivery")).containsKey("status")) {
                            cartOrderDeliveryBtn.setClickable(false);
                            cartOrderDeliveryBtn.setBackgroundResource(R.drawable.filled_button_inactive_background);
                        }

//                if(snapshot.contains("currentDeliveryID") && snapshot.get("currentDeliveryID")!=null){
//                    cartOrderDeliveryBtn.setClickable(false);
//                    cartOrderDeliveryBtn.setBackgroundResource(R.drawable.filled_button_inactive_background);
//                }else{
//                    firestore.collection("Deliveries").
//                            whereEqualTo("requesterID",currentUid).limit(1)
//                            .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                        @Override
//                        public void onSuccess(QuerySnapshot snapshots) {
//                            if(!snapshots.isEmpty()){
//                                cartOrderDeliveryBtn.setClickable(false);
//                                cartOrderDeliveryBtn.setBackgroundResource(R.drawable.filled_button_inactive_background);
//                            }
//                        }
//                    });
//                }

                    }
                });

    }


    private void getCartItems(boolean isInitial) {

        showProgressBar();

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

                for (DocumentSnapshot snap : snapshots) {
                    Log.d(TAG, "cart item id: " + snap.getId());
                    addedCartItemsIds.add(snap.getId());
                }

                addedCartItems.addAll(snapshots.toObjects(CartItem.class));

                lastDocSnap = snapshots.getDocuments().get(snapshots.size() - 1);

//                if (isInitial) {
//                    cartRv.setVisibility(View.VISIBLE);
//                    cartItems.addAll(snapshots.toObjects(CartItem.class));
//                } else {
//                    cartItems.addAll(cartItems.size() - 1, snapshots.toObjects(CartItem.class));
//                }
            } else if (cartItems.isEmpty() && cartRv.getVisibility() == View.VISIBLE) {

                Log.d(TAG, "empty cart items");

                cartRv.setVisibility(View.INVISIBLE);

            }

        }).addOnCompleteListener(task -> {

            if (task.isSuccessful() && task.getResult() != null && !addedCartItemsIds.isEmpty()) {

                firestore.collection("MenuItems").whereIn("id", addedCartItemsIds)
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
                                    cartRv.setVisibility(View.VISIBLE);
                                    cartAdapter.notifyDataSetChanged();

                                    if (cartItems.size() == CART_ITEM_LIMIT && scrollListener == null) {
                                        cartRv.addOnScrollListener(scrollListener = new ScrollListener());
                                    }

                                }


                            } else {

                                if (!task.getResult().isEmpty()) {

                                    cartItems.addAll(cartItems.size() - 1, addedCartItems);

                                    int size = task.getResult().size();

                                    cartAdapter.notifyItemRangeInserted(
                                            cartItems.size() - size, size);

                                    if (task.getResult().size() < CART_ITEM_LIMIT && scrollListener != null) {
                                        cartRv.removeOnScrollListener(scrollListener);
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


                        if (cartItems.isEmpty() && noCartItemTv.getVisibility() == View.GONE) {
                            noCartItemTv.setVisibility(View.VISIBLE);
                        } else if (!cartItems.isEmpty() && noCartItemTv.getVisibility() == View.VISIBLE) {
                            noCartItemTv.setVisibility(View.GONE);
                        }

                        isLoadingItems = false;
                        hideProgressbar();


                    }
                });

            } else {

                cartOrderDeliveryBtn.setClickable(false);
                cartOrderDeliveryBtn.setBackgroundResource(R.drawable.filled_button_inactive_background);
                Log.d(TAG, "added cart items is empty");
                cartRv.setVisibility(View.INVISIBLE);
                noCartItemTv.setVisibility(View.VISIBLE);
                isLoadingItems = false;
                hideProgressbar();

            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Log.d(TAG, "added cart items failed: " + e.getMessage());
                hideProgressbar();
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
                                                        cartRv.post(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                cartAdapter.notifyItemRemoved(finalI);
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


    private void hideProgressbar() {
        if (cartProgressBar.getVisibility() == View.VISIBLE) {
            cartProgressBar.setVisibility(View.GONE);
        }
    }

    private void showProgressBar() {

        if (cartProgressBar.getVisibility() == View.GONE) {
            cartProgressBar.setVisibility(View.VISIBLE);
        }

    }


    @Override
    public void removeCartItem(int position) {

        showProgressBar();

        final CartItem cartItem = cartItems.get(position);

        cartRef.document(cartItem.getItemId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if (documentSnapshot.exists()) {

                    final float price = documentSnapshot.getLong("count") * cartItem.getPrice();

                    documentSnapshot.getReference().delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            firestore.collection("Users")
                                    .document(currentUid)
                                    .update("CartTotal", FieldValue.increment(-price))
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            final float currentPrice = cartItem.getCount() * cartItem.getPrice();

                                            totalCost -= currentPrice;
                                            cartTotalPriceTv.setText("Total price: " + totalCost + "ILS");

                                            if (position < cartItems.size()) {
                                                cartItems.remove(position);
                                                cartAdapter.notifyItemRemoved(position);
                                            }

                                            hideProgressbar();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    Toast.makeText(CartActivity.this,
                                            "An error occurred while trying to remove from cart!" +
                                                    "Please try again", Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "failed to remove item from cart: " + e.getMessage());

                                    cartRef.document(cartItem.getItemId()).set(cartItem);
                                    hideProgressbar();
                                }
                            });


                        }
                    });

                } else {

                    totalCost -= cartItems.get(position).getPrice();
                    cartTotalPriceTv.setText("Total price: " + totalCost + "ILS");

                    if (position < cartItems.size()) {
                        cartItems.remove(position);
                        cartAdapter.notifyItemRemoved(position);
                    }

                    hideProgressbar();

                }

            }
        });


    }

    @Override
    public void showCartItemInfo(int position) {

        final Intent menuItemIntent = new Intent(this, MenuItemActivity.class);
        menuItemIntent.putExtra("MenuItemID", cartItems.get(position).getItemId());
        startActivity(menuItemIntent);

    }

    @Override
    public void increasedCartItemCount(int position) {

        totalCost += cartItems.get(position).getPrice();
        cartTotalPriceTv.setText("Total price: " + totalCost + "ILS");

    }

    @Override
    public void decreasedCartItemCount(int position) {

        totalCost -= cartItems.get(position).getPrice();
        cartTotalPriceTv.setText("Total price: " + totalCost + "ILS");

    }

    @Override
    public void onClick(View v) {

        if (v.getId() == cartOrderDeliveryBtn.getId()) {

            Intent intent = new Intent(this, DeliveryLocationMapActivity.class)
                    .putExtra("addressMap", getIntent().getSerializableExtra("addressMap"));

            Query query = firestore.collection("Users")
                    .document(currentUid).collection("Cart")
                    .orderBy("timeAdded", Query.Direction.DESCENDING);

            if (lastDocSnap != null) {
                query.startAfter(lastDocSnap);
            }

            query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot snapshots) {

                    if (snapshots != null && !snapshots.isEmpty()) {

                        if (cartItems.isEmpty()) {
                            cartItems.addAll(snapshots.toObjects(CartItem.class));
                        } else {
                            cartItems.addAll(cartItems.size() - 1, snapshots.toObjects(CartItem.class));
                        }

                    }
                }
            }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    intent.putExtra("cartItems", cartItems);
                    Log.d("ttt", "cart items task completed");
                    startActivity(intent);
                    finish();
                }
            });


        }
    }

    @Override
    protected void onDestroy() {

        if (scrollListener != null && cartRv != null) {
            cartRv.removeOnScrollListener(scrollListener);
        }

        if (removeListeners != null) {
            for (ListenerRegistration listenerRegistration : removeListeners) {
                listenerRegistration.remove();
            }
        }


        final List<Task<?>> tasks = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            tasks.add(cartRef.document(cartItem.getItemId()).update("count", cartItem.getCount()));
        }

        Tasks.whenAllSuccess(tasks).addOnSuccessListener(new OnSuccessListener<List<Object>>() {
            @Override
            public void onSuccess(List<Object> objects) {

                firestore.collection("Users").document(currentUid).update("CartTotal",
                        totalCost);

            }
        });

        super.onDestroy();
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