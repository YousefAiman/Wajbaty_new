package com.developers.wajbaty.Customer.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.developers.wajbaty.Activities.MenuItemActivity;
import com.developers.wajbaty.Adapters.CategoriesAdapter;
import com.developers.wajbaty.Adapters.DiscountOffersPagerAdapter;
import com.developers.wajbaty.Adapters.RestaurantsPagerAdapter;
import com.developers.wajbaty.Customer.Activities.CategoryActivity;
import com.developers.wajbaty.Customer.Activities.CustomerDeliveryMapActivity;
import com.developers.wajbaty.DeliveryDriver.Activities.DeliveryInfoActivity;
import com.developers.wajbaty.Models.Delivery;
import com.developers.wajbaty.Models.DeliveryModel;
import com.developers.wajbaty.Models.PartneredRestaurant;
import com.developers.wajbaty.Models.RestaurantCategory;
import com.developers.wajbaty.Models.offer.DiscountOffer;
import com.developers.wajbaty.Models.offer.Offer;
import com.developers.wajbaty.PartneredRestaurant.Activities.RestaurantActivity;
import com.developers.wajbaty.R;
import com.developers.wajbaty.Utils.TimeFormatter;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment implements
        DiscountOffersPagerAdapter.OfferClickListener,
        RestaurantsPagerAdapter.RestaurantClickListener,
        CategoriesAdapter.CategoryClickListener {

    private static final int OFFER_LIMIT = 10, RESTAURANT_LIMIT = 10, CATEGORY_LIMIT = 10;

    private static final String ADDRESS_MAP = "addressMap";

    private Map<String, Object> addressMap;

    //offers
    private ViewPager homeOffersViewPager;
    private DiscountOffersPagerAdapter discountPagerAdapter;
    private ArrayList<DiscountOffer> discountOffers;
    private LinearLayout homeOffersDotLl;
    private List<Task<QuerySnapshot>> offerTasks;

    //restaurants
    private ViewPager homeRestaurantsViewPager;
    private TextView restaurantsTv;
    private RestaurantsPagerAdapter restaurantsPagerAdapter;
    private ArrayList<PartneredRestaurant.PartneredRestaurantSummary> restaurantSummaries;
    private Query restaurantQuery;

    //categories
    private RecyclerView homeCategoriesRv;
    private TextView categoryTv;
    private CategoriesAdapter categoriesAdapter;
    private ArrayList<RestaurantCategory> categories;
    private Query categoryQuery;
    private boolean isLoadingCategories;
    private String language;
    private DocumentSnapshot lastCatgoryDocSnap;
    private HorizontalScrollListener horizontalScrollListener;

    //current Delivery
    private View customerCurrentDeliveryLayout;
    private View customerPendingDeliveryLayout;
    private String currentDeliveryID;


    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(Map<String, Object> addressMap) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putSerializable(ADDRESS_MAP, (Serializable) addressMap);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            addressMap = (Map<String, Object>) getArguments().getSerializable(ADDRESS_MAP);
        }


        discountOffers = new ArrayList<>();
        discountPagerAdapter = new DiscountOffersPagerAdapter(discountOffers,
                R.layout.item_discount_offer, this, requireContext());

        restaurantSummaries = new ArrayList<>();
        restaurantsPagerAdapter = new RestaurantsPagerAdapter(restaurantSummaries, this);

        categories = new ArrayList<>();
        categoriesAdapter = new CategoriesAdapter(categories, this, requireContext());

        language = Locale.getDefault().getLanguage().equals("ar") ? "ar" : "en";

        final FirebaseFirestore firestore = FirebaseFirestore.getInstance();


        LatLng latLng = (LatLng) addressMap.get("latLng");

        Log.d("ttt", "gotten latLng in home fragment: " + latLng.latitude + "-" + latLng.longitude);

        final GeoLocation center = new GeoLocation(latLng.latitude, latLng.longitude);

        final List<GeoQueryBounds> geoQueryBounds =
                GeoFireUtils.getGeoHashQueryBounds(center, 10 * 1000);

        final Query offersQuery =
                firestore.collectionGroup("Offers")
                        .whereEqualTo("type", Offer.MENU_ITEM_DISCOUNT)
                        .orderBy("geohash").limit(OFFER_LIMIT);

        offerTasks = new ArrayList<>();

        for (GeoQueryBounds b : geoQueryBounds) {
            Query query = offersQuery.startAt(b.startHash).endAt(b.endHash);
            offerTasks.add(query.get());
        }

        Log.d("ttt", "countryCode: " + addressMap.get("countryCode"));
        Log.d("ttt", "city: " + addressMap.get("city"));

        restaurantQuery = firestore.collection("PartneredRestaurant")
                .whereEqualTo("countryCode", addressMap.get("countryCode"))
//                .whereEqualTo("city", addressMap.get("city"))
                .orderBy("averageRating", Query.Direction.DESCENDING).limit(RESTAURANT_LIMIT);


        categoryQuery = firestore.collection("GeneralOptions")
                .document("Categories")
                .collection("Categories")
                .limit(CATEGORY_LIMIT);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_home, container, false);

        customerCurrentDeliveryLayout = view.findViewById(R.id.customerCurrentDeliveryLayout);
        customerPendingDeliveryLayout = view.findViewById(R.id.customerPendingDeliveryLayout);
        homeOffersViewPager = view.findViewById(R.id.homeOffersViewPager);
        homeOffersDotLl = view.findViewById(R.id.homeOffersDotLl);
        homeRestaurantsViewPager = view.findViewById(R.id.homeRestaurantsViewPager);
        restaurantsTv = view.findViewById(R.id.restaurantsTv);
        homeCategoriesRv = view.findViewById(R.id.homeCategoriesRv);
        categoryTv = view.findViewById(R.id.categoryTv);

        final AdView adView = view.findViewById(R.id.adView);
        adView.loadAd(new AdRequest.Builder().build());
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                adView.setVisibility(View.VISIBLE);
            }
        });

        homeRestaurantsViewPager.setPageMargin((int) (20 * getResources().getDisplayMetrics().density));

        homeOffersViewPager.setAdapter(discountPagerAdapter);
        homeRestaurantsViewPager.setAdapter(restaurantsPagerAdapter);
        homeCategoriesRv.setAdapter(categoriesAdapter);

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        getOffers();

        getBestRatedRestaurants();

        getCategories(true);

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        getCurrentDelivery(context);
    }

    private void getCurrentDelivery(Context context) {

        final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("Users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    String currentDeliveryID;

                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                        if (value != null) {

                            if (currentDeliveryID == null) {

                                if (value.contains("currentDelivery")) {

                                    final Map<String, Object> currentDeliveryMap = (Map<String, Object>) value.get("currentDelivery");

                                    if (currentDeliveryMap == null)
                                        return;

                                    currentDeliveryID = (String) currentDeliveryMap.get("currentDeliveryID");

                                    if (currentDeliveryID != null && !currentDeliveryID.isEmpty()) {

//                                        int status = ((Long) currentDeliveryMap.get("status")).intValue();

                                        firestore.collection("Deliveries").document(currentDeliveryID)
                                                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                if (documentSnapshot.exists()) {
                                                    showCurrentDelivery(documentSnapshot, context);
                                                }
                                            }
                                        });

//                                        if(status == Delivery.STATUS_PENDING){
//
//
//                                        }else {
//                                            firestore.collection("Deliveries").document(currentDeliveryID)
//                                                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                                                @Override
//                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
//                                                    if(documentSnapshot.exists()){
//                                                        showCurrentDelivery(documentSnapshot);
//                                                    }
//                                                }
//                                            });
//                                        }


                                    }

                                }
                            } else {

                                if (value.contains("currentDelivery")) {

                                    final Map<String, Object> currentDeliveryMap = (Map<String, Object>) value.get("currentDelivery");

                                    if (currentDeliveryMap == null)
                                        return;

                                    String newDeliveryID = (String) currentDeliveryMap.get("currentDeliveryID");

                                    if (newDeliveryID != null && !newDeliveryID.isEmpty() && !newDeliveryID.equals(currentDeliveryID)) {

                                        currentDeliveryID = newDeliveryID;

                                        firestore.collection("Deliveries").document(currentDeliveryID)
                                                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                if (documentSnapshot.exists()) {
                                                    showCurrentDelivery(documentSnapshot, context);
                                                }
                                            }
                                        });

                                    } else {
                                        currentDeliveryID = null;
                                        customerCurrentDeliveryLayout.setVisibility(View.GONE);
                                        customerPendingDeliveryLayout.setVisibility(View.GONE);
                                    }

                                } else {
                                    currentDeliveryID = null;
                                    customerCurrentDeliveryLayout.setVisibility(View.GONE);
                                    customerPendingDeliveryLayout.setVisibility(View.GONE);

                                }

                            }

                        } else {
                            currentDeliveryID = null;
                            customerCurrentDeliveryLayout.setVisibility(View.GONE);
                            customerPendingDeliveryLayout.setVisibility(View.GONE);
                        }

                    }
                });


    }


    private void showCurrentDelivery(DocumentSnapshot snapshot, Context context) {

        Delivery delivery = snapshot.toObject(Delivery.class);

        if (delivery == null)
            return;

        if (delivery.getStatus() == Delivery.STATUS_PENDING) {

            new DeliveryModel(delivery, context).listenForDriverDeliveryAcceptance();

            Log.d("ttt", "showing pending delivery");

            customerPendingDeliveryLayout.setVisibility(View.VISIBLE);

            final TextView
                    customerDeliveryAddressTv = customerPendingDeliveryLayout.findViewById(R.id.customerDeliveryAddressTv),
                    customerDeliveryOrderTimeTv = customerPendingDeliveryLayout.findViewById(R.id.customerDeliveryOrderTimeTv),
                    customerDeliveryTotalPriceTv = customerPendingDeliveryLayout.findViewById(R.id.customerDeliveryTotalPriceTv),
                    customerRestaurantCountTv = customerPendingDeliveryLayout.findViewById(R.id.customerRestaurantCountTv);

            final Button customerShowItemsTv = customerPendingDeliveryLayout.findViewById(R.id.customerShowItemsTv), customerDeliveryCancelBtn = customerPendingDeliveryLayout.findViewById(R.id.customerDeliveryCancelBtn);

            customerDeliveryAddressTv.setText("Delivery Address: " + delivery.getAddress());
            customerDeliveryOrderTimeTv.setText("Order Time: " + TimeFormatter.formatTime(delivery.getOrderTimeInMillis()));
            customerDeliveryTotalPriceTv.setText("Total cost: " + delivery.getTotalCost() + delivery.getCurrency());

            customerRestaurantCountTv.setText("N# of Restaurants: " + delivery.getRestaurantCount() + " Restaurants");

            customerShowItemsTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(requireContext(), DeliveryInfoActivity.class)
                            .putExtra("isForShow", true)
                            .putExtra("delivery", delivery));
                }
            });

            customerDeliveryCancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    customerPendingDeliveryLayout.setVisibility(View.GONE);
                    currentDeliveryID = null;
                    new DeliveryModel(delivery, requireContext()).deleteDelivery(0, null);
                }
            });

//            customerCurrentDeliveryLayout.setVisibility(View.VISIBLE);
//            View view = getLayoutInflater().inflate(R.layout.customer_pending_delivery_layout,null);
//            customerCurrentDeliveryLayout.addView(view);
//            customerCurrentDeliveryLayout.setLayoutResource(R.layout.customer_pending_delivery_layout);
//            View inflated = customerCurrentDeliveryLayout.inflate();

        } else {

            if (delivery.getDriverID() == null || delivery.getDriverID().isEmpty()) {
                return;
            }
            customerCurrentDeliveryLayout.setVisibility(View.VISIBLE);

            final ImageView customerDeliveryDriverImageIv = customerCurrentDeliveryLayout.findViewById(R.id.customerDeliveryDriverImageIv);
            final TextView
                    customerDeliveryDriverNameTv = customerCurrentDeliveryLayout.findViewById(R.id.customerDeliveryDriverNameTv),
                    customerDeliveryAddressTv = customerCurrentDeliveryLayout.findViewById(R.id.customerDeliveryAddressTv),
                    customerDeliveryOrderTimeTv = customerCurrentDeliveryLayout.findViewById(R.id.customerDeliveryOrderTimeTv),
                    customerDeliveryTotalPriceTv = customerCurrentDeliveryLayout.findViewById(R.id.customerDeliveryTotalPriceTv),
                    customerRestaurantCountTv = customerCurrentDeliveryLayout.findViewById(R.id.customerRestaurantCountTv);

            final Button customerShowItemsTv = customerCurrentDeliveryLayout.findViewById(R.id.customerShowItemsTv), customerShowMapTv = customerCurrentDeliveryLayout.findViewById(R.id.customerShowMapTv);

            FirebaseFirestore.getInstance().collection("Users")
                    .document(delivery.getDriverID()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {

                    if (documentSnapshot.exists()) {

                        Picasso.get().load(documentSnapshot.getString("imageURL")).fit().centerCrop().into(customerDeliveryDriverImageIv);
                        customerDeliveryDriverNameTv.setText(documentSnapshot.getString("name"));

                    }
                }
            });

            customerDeliveryAddressTv.setText("Delivery Address: " + delivery.getAddress());
            customerDeliveryOrderTimeTv.setText("Order Time: " + TimeFormatter.formatTime(delivery.getOrderTimeInMillis()));
            customerDeliveryTotalPriceTv.setText("Total cost: " + delivery.getTotalCost() + delivery.getCurrency());

            customerRestaurantCountTv.setText("N# of Restaurants: " + delivery.getRestaurantCount() + " Restaurants");

            customerShowItemsTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    startActivity(new Intent(requireContext(), DeliveryInfoActivity.class)
                            .putExtra("isForShow", true)
                            .putExtra("delivery", delivery));

                }
            });

            customerShowMapTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    startActivity(new Intent(requireContext(), CustomerDeliveryMapActivity.class)
                            .putExtra("delivery", delivery));

                }
            });
        }

    }


    private void getOffers() {

        Tasks.whenAllComplete(offerTasks).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
            @Override
            public void onComplete(@NonNull Task<List<Task<?>>> task) {

                for (Task<QuerySnapshot> offerTask : offerTasks) {

                    if (offerTask.isSuccessful()) {
                        Log.d("ttt", "offer task succesfull");
                    }

                    if (offerTask.isSuccessful() && offerTask.getResult() != null && !offerTask.getResult().isEmpty()) {
                        discountOffers.addAll(offerTask.getResult().toObjects(DiscountOffer.class));
                    } else {
                        Log.d("ttt", "offer task empty");
                    }
                }

                if (!discountOffers.isEmpty()) {
                    discountPagerAdapter.notifyDataSetChanged();
                } else {
                    homeOffersViewPager.setVisibility(View.GONE);
                    homeOffersDotLl.setVisibility(View.GONE);
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("ttt", "failed to get offers: " + e.getMessage());
            }
        });

    }

    private void getBestRatedRestaurants() {

        restaurantQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {

                if (snapshots != null && !snapshots.isEmpty()) {

                    Log.d("ttt", "got restaurants: " + restaurantSummaries.size());

                    restaurantSummaries.addAll(snapshots.toObjects(PartneredRestaurant.PartneredRestaurantSummary.class));
                }

            }
        }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (!restaurantSummaries.isEmpty()) {
                    restaurantsPagerAdapter.notifyDataSetChanged();
                } else {

                    restaurantsTv.setVisibility(View.GONE);
                    homeRestaurantsViewPager.setVisibility(View.GONE);

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("ttt", "failed to get restaurants: " + e.getMessage());
            }
        });

    }

    private void getCategories(boolean isInitial) {

        isLoadingCategories = true;

        Query currentQuery = categoryQuery;

        if (lastCatgoryDocSnap != null) {
            currentQuery = currentQuery.startAfter(lastCatgoryDocSnap);
        }

        currentQuery.get().addOnSuccessListener(snapshots -> {

            if (!snapshots.isEmpty()) {

                lastCatgoryDocSnap = snapshots.getDocuments().get(snapshots.getDocuments().size() - 1);

                if (isInitial) {
                    categories.addAll(snapshots.toObjects(RestaurantCategory.class));
                } else {
                    categories.addAll(categories.size() - 1, snapshots.toObjects(RestaurantCategory.class));
                }

                for (DocumentSnapshot snapshot : snapshots) {
                    Log.d("ttt", "gotten category snapshot: " + snapshot.getId());
                }

            } else if (categories.isEmpty() && homeCategoriesRv.getVisibility() == View.VISIBLE) {
                homeCategoriesRv.setVisibility(View.GONE);
                categoryTv.setVisibility(View.GONE);
            }

        }).addOnCompleteListener(task -> {

            if (task.isSuccessful() && task.getResult() != null) {

                if (isInitial) {

                    if (!categories.isEmpty()) {

                        categoriesAdapter.notifyDataSetChanged();

                        if (categories.size() == CATEGORY_LIMIT && horizontalScrollListener == null) {
                            homeCategoriesRv.addOnScrollListener(horizontalScrollListener = new HorizontalScrollListener());
                        }

                        if (homeCategoriesRv.getVisibility() == View.GONE) {
                            homeCategoriesRv.setVisibility(View.VISIBLE);
                            categoryTv.setVisibility(View.VISIBLE);
                        }

                    }
                } else {

                    if (!task.getResult().isEmpty()) {

                        final int size = task.getResult().size();

                        categoriesAdapter.notifyItemRangeInserted(
                                categories.size() - size, size);

                        if (task.getResult().size() < CATEGORY_LIMIT && horizontalScrollListener != null) {
                            homeCategoriesRv.removeOnScrollListener(horizontalScrollListener);
                            horizontalScrollListener = null;
                        }

                    } else {
                        homeCategoriesRv.removeOnScrollListener(horizontalScrollListener);
                        horizontalScrollListener = null;

                    }
                }
            }

            if (categories.isEmpty() && homeCategoriesRv.getVisibility() == View.VISIBLE) {
                homeCategoriesRv.setVisibility(View.GONE);
                categoryTv.setVisibility(View.GONE);
            } else if (!categories.isEmpty() && homeCategoriesRv.getVisibility() == View.GONE) {
                homeCategoriesRv.setVisibility(View.GONE);
                categoryTv.setVisibility(View.GONE);
            }

            isLoadingCategories = false;

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Log.d("ttt", "failed to fetch categories: " + e.getMessage());

            }
        });
    }


    @Override
    public void onOfferClicked(int position) {

        if (discountOffers.get(position).getType() == Offer.MENU_ITEM_DISCOUNT) {

            startActivity(new Intent(requireContext(), MenuItemActivity.class)
                    .putExtra("MenuItemID", discountOffers.get(position).getDestinationId()));

        }

    }

    @Override
    public void onRestaurantClicked(int position) {

        startActivity(new Intent(requireContext(), RestaurantActivity.class)
                .putExtra("ID", restaurantSummaries.get(position).getID())
                .putExtra("currency", (String) addressMap.get("currency")));

    }

    @Override
    public void onCategoryClicked(int position) {

        startActivity(new Intent(requireContext(), CategoryActivity.class)
                .putExtra("category", categories.get(position).getID())
                .putExtra("addressMap", (Serializable) addressMap));

    }


    private class HorizontalScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (!isLoadingCategories && !recyclerView.canScrollHorizontally(1) &&
                    newState == RecyclerView.SCROLL_STATE_IDLE) {

                Log.d("ttt", "is at end");

                getCategories(false);

            }
        }
    }


}