package com.developers.wajbaty.DeliveryDriver.Activities;

import android.content.Intent;
import android.location.Location;
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

import com.developers.wajbaty.Adapters.CartInfoAdapter;
import com.developers.wajbaty.Fragments.ProgressDialogFragment;
import com.developers.wajbaty.Models.CartItem;
import com.developers.wajbaty.Models.CartItemRestaurantHeader;
import com.developers.wajbaty.Models.Delivery;
import com.developers.wajbaty.Models.DeliveryDriver;
import com.developers.wajbaty.R;
import com.developers.wajbaty.Utils.CloudMessagingNotificationsSender;
import com.developers.wajbaty.Utils.TimeFormatter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeliveryInfoActivity extends AppCompatActivity implements
        CartInfoAdapter.CartClickListener,
        View.OnClickListener {

    private Delivery delivery;

    //firebase
    private FirebaseFirestore firestore;

    //cartItems
    private ArrayList<CartItem> orderedCartItem;
    private CartInfoAdapter cartInfoAdapter;

    //views
    private Toolbar cartToolbar;
    private ImageView deliveryInfoUserIv;
    private TextView deliveryInfoUserNameTv, deliveryInfoAddressTv, deliveryInfoCoordinatesTv,
            deliveryInfoTotalDistanceTv, deliveryInfoOrderTimeTv, deliveryInfoTotalPriceTv,
            deliveryInfoRestaurantCountTv;
    private RecyclerView deliveryInfoCartItemsRv;
    private Button deliveryInfoStartDeliveryBtn;

    private boolean isForShow;

    private DocumentReference deliveryRef;

    private String currentUid;
    private ProgressDialogFragment progressDialogFragment;

    private Location currentLocation;

    private ListenerRegistration currentDeliveryListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_info);

        initializeObjects();

        getViews();

        final Intent intent = getIntent();

        if (intent == null) {
            finish();
            return;
        }

        if (intent.hasExtra("isForShow")) {
            isForShow = intent.getBooleanExtra("isForShow", false);
        }
        if (intent.hasExtra("currentLocation")) {
            currentLocation = (Location) intent.getParcelableExtra("currentLocation");
        }


        setUpListeners();

        if (intent.hasExtra("delivery")) {
            delivery = (Delivery) intent.getSerializableExtra("delivery");
            deliveryRef = firestore.collection("Deliveries").document(delivery.getID());

            if (!isForShow) {
                listenToDeliveryChanges();
            }

            populateViews();
        } else if (intent.hasExtra("deliveryID")) {

            firestore.collection("Deliveries").document(intent.getStringExtra("deliveryID"))
                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {

                        delivery = documentSnapshot.toObject(Delivery.class);
                        deliveryRef = firestore.collection("Deliveries").document(documentSnapshot.getId());

                        if (!isForShow) {
                            listenToDeliveryChanges();
                        }


                    }
                }
            }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    populateViews();
                }
            });

        } else {
            finish();
            return;
        }


    }


    private void initializeObjects() {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            finish();
            return;
        }

        firestore = FirebaseFirestore.getInstance();


        orderedCartItem = new ArrayList<>();
        cartInfoAdapter = new CartInfoAdapter(orderedCartItem, this, this);

        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    }


    private void getViews() {

        final NestedScrollView deliveryInfoNSV = findViewById(R.id.deliveryInfoNSV);
        deliveryInfoNSV.setNestedScrollingEnabled(false);

        cartToolbar = findViewById(R.id.cartToolbar);
        deliveryInfoUserIv = findViewById(R.id.deliveryInfoUserIv);
        deliveryInfoUserNameTv = findViewById(R.id.deliveryInfoUserNameTv);
        deliveryInfoAddressTv = findViewById(R.id.deliveryInfoAddressTv);
        deliveryInfoCoordinatesTv = findViewById(R.id.deliveryInfoCoordinatesTv);
        deliveryInfoTotalDistanceTv = findViewById(R.id.deliveryInfoTotalDistanceTv);
        deliveryInfoOrderTimeTv = findViewById(R.id.deliveryInfoOrderTimeTv);
        deliveryInfoTotalPriceTv = findViewById(R.id.deliveryInfoTotalPriceTv);
        deliveryInfoRestaurantCountTv = findViewById(R.id.deliveryInfoRestaurantCountTv);
        deliveryInfoCartItemsRv = findViewById(R.id.deliveryInfoCartItemsRv);
        deliveryInfoStartDeliveryBtn = findViewById(R.id.deliveryInfoStartDeliveryBtn);


        deliveryInfoCartItemsRv.setAdapter(cartInfoAdapter);
    }

    private void setUpListeners() {
        cartToolbar.setNavigationOnClickListener(v -> finish());

        if (!isForShow) {
            deliveryInfoStartDeliveryBtn.setOnClickListener(this);
        } else {
            deliveryInfoStartDeliveryBtn.setVisibility(View.GONE);
        }
    }


    private void populateViews() {

        firestore.collection("Users").document(delivery.getRequesterID())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if (documentSnapshot.exists()) {

                    final String username = documentSnapshot.getString("name");
                    cartToolbar.setTitle("Delivery for " + username);
                    deliveryInfoUserNameTv.setText(username);

                    final String imageURL = documentSnapshot.getString("imageURL");
                    Picasso.get().load(imageURL).fit().centerCrop().into(deliveryInfoUserIv);

                }
            }
        });

        deliveryInfoAddressTv.setText("Address: " + delivery.getAddress());

        deliveryInfoCoordinatesTv.setText("Coordinates: [" +
                BigDecimal.valueOf(delivery.getLat()).setScale(2, RoundingMode.DOWN) + "," +
                BigDecimal.valueOf(delivery.getLng()).setScale(2, RoundingMode.DOWN) + "]");

        deliveryInfoOrderTimeTv.setText(TimeFormatter.formatTime(delivery.getOrderTimeInMillis()));
        deliveryInfoTotalPriceTv.setText(delivery.getTotalCost() + delivery.getCurrency());
        deliveryInfoRestaurantCountTv.setText(delivery.getRestaurantCount() + " Restaurants");

        fetchCartItems();

    }

    private void fetchCartItems() {

        final ArrayList<CartItem> cartItems = new ArrayList<>();

        firestore.collection("Deliveries").document(delivery.getID())
                .collection("CartItems")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {

                cartItems.addAll(snapshots.toObjects(CartItem.class));

            }
        }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {


                final List<Task<DocumentSnapshot>> tasksList = new ArrayList<>();

                final Map<String, String> menuItemRestaurtantMap = new HashMap<>();

                final Map<String, Float> priceMap = delivery.getMenuItemPriceMap();

                for (CartItem cartItem : cartItems) {

                    cartItem.setPrice(priceMap.get(cartItem.getItemId()));

                    tasksList.add(firestore.collection("MenuItems").document(cartItem.getItemId())
                            .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {

                                    if (documentSnapshot != null) {

                                        if (documentSnapshot.contains("imageUrls")) {

                                            List<String> imageUrls =
                                                    (List<String>) documentSnapshot.get("imageUrls");

                                            if (imageUrls != null && !imageUrls.isEmpty()) {
                                                cartItem.setImageUrl(imageUrls.get(0));
                                            }

                                        }

                                        cartItem.setName(documentSnapshot.getString("name"));
                                        cartItem.setCurrency(documentSnapshot.getString("currency"));

                                        menuItemRestaurtantMap.put(documentSnapshot.getId(), documentSnapshot.getString("restaurantId"));

                                    }
                                }
                            }));
                }

                Tasks.whenAllComplete(tasksList).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<Task<?>>> task) {

                        if (!menuItemRestaurtantMap.isEmpty()) {

                            final List<Task<DocumentSnapshot>> restaurantTasks = new ArrayList<>();

                            deliveryRef.collection("RestaurantsOrdered")
                                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot snapshots) {

                                    if (!snapshots.isEmpty()) {

                                        final CollectionReference restaruantRef = firestore.collection("PartneredRestaurant");

                                        for (DocumentSnapshot snapshot : snapshots.getDocuments()) {

                                            restaurantTasks.add(restaruantRef.document(snapshot.getId())
                                                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onSuccess(DocumentSnapshot documentSnapshot) {

                                                            if (documentSnapshot.exists()) {

                                                                final long count = snapshot.getLong("itemCount");

                                                                orderedCartItem.add(
                                                                        new CartItemRestaurantHeader("From " + documentSnapshot.getString("name") +
                                                                                " - " + count + (count == 1 ? " item" : " items")));

                                                                for (String menuItemRestaurant : menuItemRestaurtantMap.keySet()) {

                                                                    if (menuItemRestaurtantMap.get(menuItemRestaurant).equals(snapshot.getId())) {

                                                                        for (CartItem cartItem : cartItems) {
                                                                            if (cartItem.getItemId().equals(menuItemRestaurant)) {
                                                                                orderedCartItem.add(cartItem);
                                                                            }
                                                                        }

                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }));

                                        }

                                        Tasks.whenAllComplete(restaurantTasks).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
                                            @Override
                                            public void onComplete(@NonNull Task<List<Task<?>>> task) {

                                                cartInfoAdapter.notifyDataSetChanged();

                                            }
                                        });


                                    }

                                }
                            });


                        }

                    }
                });


            }
        });


    }

    private void listenToDeliveryChanges() {


        deliveryRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            boolean isInitial = true;
            boolean driverWasAccepted = false;

            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                if (value != null) {

                    if (value.contains("status") && value.contains("driverID")) {

                        long status = value.getLong("status");

                        if (status != Delivery.STATUS_PENDING && value.getString("driverID") != null
                                && !value.getString("driverID").equals(currentUid)) {

                            Toast.makeText(DeliveryInfoActivity.this,
                                    "You can't start the delivery because " +
                                            "another Driver has already started the delivery!"
                                    , Toast.LENGTH_LONG).show();

                            disableStartDeliveryBtn();

                            return;
                        }

                    }

                    if (!isInitial) {

                        if (value.contains("proposingDriverMap")) {

                            HashMap<String, Object> proposingMap =
                                    (HashMap<String, Object>) value.get("proposingDriverMap");

                            if (proposingMap == null) {
                                return;
                            }

                            if (proposingMap.containsKey("driverID")) {

                                if (proposingMap.get("driverID") == null)
                                    return;

                                if (proposingMap.get("driverID").equals(currentUid)) {
//                                     if(!proposingMap.containsKey("status") || !(proposingMap.get("status") instanceof Boolean)){
//
//                                         hideProgressFragment();
//
//                                         Toast.makeText(DeliveryInfoActivity.this,
//                                                 "An Error occurred while confirming delivery request with user!" +
//                                                         "Please try again",
//                                                 Toast.LENGTH_LONG).show();
//
//                                         return;
//                                     }

                                    if (proposingMap.containsKey("hasDecided") &&
                                            proposingMap.get("hasDecided") != null &&
                                            (Boolean) proposingMap.get("hasDecided") &&
                                            !driverWasAccepted) {
                                        driverWasAccepted = true;

                                        if (proposingMap.containsKey("status") &&
                                                proposingMap.get("status") != null &&
                                                (Boolean) proposingMap.get("status")) {


                                            firestore.collection("Users").document(currentUid)
                                                    .update("currentDeliveryID", delivery.getID(),
                                                            "status", DeliveryDriver.STATUS_DELIVERING)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {

                                                            firestore.collection("Deliveries").document(delivery.getID())
                                                                    .update("status", Delivery.STATUS_ACCEPTED,
                                                                            "driverID", currentUid)
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {

                                                                            hideProgressFragment();

                                                                            Intent intent = new Intent(DeliveryInfoActivity.this, DriverDeliveryMapActivity.class)
                                                                                    .putExtra("delivery", delivery);

                                                                            if (currentLocation != null) {
                                                                                intent.putExtra("currentLocation", currentLocation);
                                                                            }

                                                                            startActivity(intent);

                                                                            finish();

                                                                        }
                                                                    }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {

                                                                    hideProgressFragment();

                                                                    deliveryInfoStartDeliveryBtn.setClickable(true);

                                                                    firestore.collection("Users").document(currentUid)
                                                                            .update("currentDeliveryID", null);

                                                                }
                                                            });

                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                    hideProgressFragment();
                                                    deliveryInfoStartDeliveryBtn.setClickable(true);

                                                }
                                            });

                                        } else {

                                            hideProgressFragment();

                                            Toast.makeText(DeliveryInfoActivity.this,
                                                    "User refused your delivery request", Toast.LENGTH_SHORT).show();

                                        }

                                    }

                                } else {

                                    hideProgressFragment();

                                    Toast.makeText(DeliveryInfoActivity.this,
                                            "You can't start the delivery because " +
                                                    "another Driver has already started the delivery!"
                                            , Toast.LENGTH_LONG).show();
                                }

                            } else {

                                hideProgressFragment();

                                Toast.makeText(DeliveryInfoActivity.this,
                                        "An Error occurred while user was trying to confirm delivery!", Toast.LENGTH_SHORT).show();

                            }
                        }
                    }


                    isInitial = false;
                }

            }
        });

    }

    @Override
    public void showMenuItem(int position) {


    }

    @Override
    public void onClick(View v) {

        if (v.getId() == deliveryInfoStartDeliveryBtn.getId()) {

            deliveryInfoStartDeliveryBtn.setClickable(false);

            progressDialogFragment = new ProgressDialogFragment();
            progressDialogFragment.setTitle("Waiting for customer confirmation");
            progressDialogFragment.setMessage("Please Wait!");
            progressDialogFragment.setCanBeDismissed(true);
            progressDialogFragment.setProgressDialogListener(new ProgressDialogFragment.ProgressDialogListener() {
                @Override
                public void onProgressDismissed() {
                    deliveryRef.update("proposingDriverMap", FieldValue.delete())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    deliveryInfoStartDeliveryBtn.setClickable(true);
                                }
                            });
                }
            });

            progressDialogFragment.show(getSupportFragmentManager(), "progressFragment");

            HashMap<String, Object> proposingMap = new HashMap<>();
            proposingMap.put("driverID", currentUid);
            proposingMap.put("status", null);
            proposingMap.put("hasDecided", false);

            deliveryRef.update("proposingDriverMap", proposingMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            firestore.collection("Users").document(currentUid)
                                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot snapshot) {

                                    if (snapshot.exists()) {

                                        CloudMessagingNotificationsSender.sendNotification(
                                                delivery.getRequesterID(),
                                                new CloudMessagingNotificationsSender.Data(
                                                        currentUid,
                                                        "Delivery Driver " + snapshot.getString("name"),
                                                        "Driver wants to deliver your order",
                                                        snapshot.contains("imageURL") && snapshot.getString("imageURL") != null ? snapshot.getString("imageURL") : null,
                                                        delivery.getID(),
                                                        CloudMessagingNotificationsSender.Data.TYPE_DRIVER_PROPOSAL)
                                        );

                                    }
                                }
                            });

//                            currentDeliveryListener = deliveryRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                                boolean isInitial = true;
//                                @Override
//                                public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
//
//                                    if(value!=null){
//
//                                        if(isInitial){
//                                            isInitial = false;
//                                            return;
//                                        }
//
//
//                                        if(value.contains("proposingDriverMap")){
//
//                                            HashMap<String,Boolean> proposingMap = (HashMap<String, Boolean>) value.get("proposingDriverMap");
//
//                                            if(proposingMap.containsKey(currentUid)){
//
//                                                if(proposingMap.get(currentUid)){
//
//                                                    firestore.collection("Users").document(currentUid)
//                                                            .update("currentDeliveryId",delivery.getID())
//                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                                @Override
//                                                                public void onSuccess(Void aVoid) {
//
//                                                                    firestore.collection("Deliveries").document(delivery.getID())
//                                                                            .update("status",Delivery.STATUS_ACCEPTED,
//                                                                                    "driverID", currentUid)
//                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                                                @Override
//                                                                                public void onSuccess(Void aVoid) {
//
//                                                                                    progressDialogFragment.dismiss();
//
//                                                                                    startActivity(new Intent(DeliveryInfoActivity.this,DriverDeliveryMapActivity.class)
//                                                                                            .putExtra("delivery",delivery));
//
//                                                                                }
//                                                                            }).addOnFailureListener(new OnFailureListener() {
//                                                                        @Override
//                                                                        public void onFailure(@NonNull Exception e) {
//
//                                                                            progressDialogFragment.dismiss();
//
//                                                                            deliveryInfoStartDeliveryBtn.setClickable(true);
//
//                                                                            firestore.collection("DeliveryDrivers").document(currentUid)
//                                                                                    .update("currentDeliveryId",null);
//
//                                                                        }
//                                                                    });
//
//                                                                }
//                                                            }).addOnFailureListener(new OnFailureListener() {
//                                                        @Override
//                                                        public void onFailure(@NonNull Exception e) {
//
//                                                            deliveryInfoStartDeliveryBtn.setClickable(true);
//
//                                                        }
//                                                    });
//
//                                                }else{
//
//                                                    Toast.makeText(DeliveryInfoActivity.this,
//                                                            "User refused your delivery request", Toast.LENGTH_SHORT).show();
//
//                                                    progressDialogFragment.dismiss();
//                                                }
//
//                                            }else{
//
//                                                Toast.makeText(DeliveryInfoActivity.this,
//                                                        "An Error occurred while user was trying to confirm delivery!", Toast.LENGTH_SHORT).show();
//
//                                                progressDialogFragment.dismiss();
//                                                if(currentDeliveryListener!=null){
//                                                    currentDeliveryListener.remove();
//                                                }
//
//                                            }
//                                        }
//
//                                    }
//
//                                }
//                            });

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(DeliveryInfoActivity.this,
                                    "Failed while trying to start delivery! Please Try " +
                                            "Again", Toast.LENGTH_SHORT).show();

                            deliveryInfoStartDeliveryBtn.setClickable(true);
                            Log.d("ttt", "failed to propose to delivery");
                        }
                    });


        }

    }

    private void disableStartDeliveryBtn() {
        deliveryInfoStartDeliveryBtn.setClickable(false);
        deliveryInfoStartDeliveryBtn.setBackgroundResource(R.drawable.filled_button_inactive_background);
    }

    private void hideProgressFragment() {

        if (progressDialogFragment != null && progressDialogFragment.isVisible()) {
            progressDialogFragment.dismiss();
        }

    }
}