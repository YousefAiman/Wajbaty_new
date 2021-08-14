package com.developers.wajbaty.Customer.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.developers.wajbaty.Adapters.CategoryRestaurantsAdapter;
import com.developers.wajbaty.Models.PartneredRestaurant;
import com.developers.wajbaty.Models.PartneredRestaurantModel;
import com.developers.wajbaty.PartneredRestaurant.Activities.RestaurantActivity;
import com.developers.wajbaty.R;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class CategoryActivity extends AppCompatActivity implements CategoryRestaurantsAdapter.CategoryRestaurantsListener {


    private static final int RESTAURANT_LIMIT = 10;
    private static final double radius = 10 * 1000;


    private String category;
    private Map<String, Object> addressMap;

    //views
    private Toolbar categoryRestaurantsToolbar;
    private TextView categoryRestaurantsResultsTv;
    private RecyclerView categoryRestaurantsRv;
    private ProgressBar categoryRestaurantsProgressBar;

    //firebase
    private FirebaseFirestore firestore;

    //summaries
    private CategoryRestaurantsAdapter adapter;
    private ArrayList<PartneredRestaurant.PartneredRestaurantSummary> restaurantSummaries;
    private Query restaurantQuery;
    private DocumentSnapshot lastDocSnapshot;
    private ScrollListener scrollListener;
    private boolean isLoadingItems;

    //geolocation
    private List<GeoQueryBounds> geoQueryBounds;

    //liked restaurants
    private List<String> likedRestaurants;
    private String currentUid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        initializeObjects();

        getViews();

        fetchCategoryResultSize();

        fetchFavoriteRestaurants();


    }

    private void initializeObjects() {

        final Intent intent = getIntent();
        if (intent != null && intent.hasExtra("category") && intent.hasExtra("addressMap")) {
            category = intent.getStringExtra("category");
            addressMap = (Map<String, Object>) getIntent().getSerializableExtra("addressMap");
        }

        likedRestaurants = new ArrayList<>();

        restaurantSummaries = new ArrayList<>();

        adapter = new CategoryRestaurantsAdapter(restaurantSummaries,
                this, likedRestaurants, this);


        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        firestore = FirebaseFirestore.getInstance();

        restaurantQuery = firestore.collection("PartneredRestaurant")
                .whereEqualTo("category", category)
                .whereEqualTo("countryCode", addressMap.get("countryCode"))
                .orderBy("geohash").limit(RESTAURANT_LIMIT);

        final LatLng latLng = (LatLng) addressMap.get("latLng");

        if (latLng != null) {

            GeoLocation center = new GeoLocation(latLng.latitude, latLng.longitude);
            geoQueryBounds = GeoFireUtils.getGeoHashQueryBounds(center, radius);


        }

    }


    private void getViews() {

        categoryRestaurantsToolbar = findViewById(R.id.categoryRestaurantsToolbar);
        categoryRestaurantsResultsTv = findViewById(R.id.categoryRestaurantsResultsTv);
        categoryRestaurantsRv = findViewById(R.id.categoryRestaurantsRv);
        categoryRestaurantsProgressBar = findViewById(R.id.categoryRestaurantsProgressBar);

        categoryRestaurantsToolbar.setNavigationOnClickListener(v -> finish());

        categoryRestaurantsRv.setAdapter(adapter);
    }


    private void fetchCategoryResultSize() {

        firestore.collection("GeneralOptions").document("Categories")
                .collection("Categories").document(category)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if (documentSnapshot.exists()) {
                    final long totalCount = documentSnapshot.getLong("count");
                    categoryRestaurantsResultsTv.setText(totalCount + (totalCount > 1 || totalCount == 0
                            ? " Restaurants" : " Restaurant"));
                }

            }
        });

    }

    private void fetchFavoriteRestaurants() {

        firestore.collection("Users").document(currentUid).collection("Favorites")
                .document("FavoriteRestaurants").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if (documentSnapshot.exists() && documentSnapshot.contains("FavoriteRestaurants")) {
                    likedRestaurants.addAll((List<String>) documentSnapshot.get("FavoriteRestaurants"));
                }

            }
        }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                getCategoryRestaurants(true);

            }
        });

    }

    private void getCategoryRestaurants(boolean isInitial) {

//        showProgressDialog();
        categoryRestaurantsProgressBar.setVisibility(View.VISIBLE);

        isLoadingItems = true;

        final List<Task<QuerySnapshot>> tasks = new ArrayList<>();

        for (GeoQueryBounds b : geoQueryBounds) {
            Query query = restaurantQuery.startAt(b.startHash).endAt(b.endHash);

            if (lastDocSnapshot != null) {
                query = query.startAfter(lastDocSnapshot);
            }

            tasks.add(query.get());
        }


        Tasks.whenAllComplete(tasks).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
            final int previousSize = restaurantSummaries.size();

            @Override
            public void onComplete(@NonNull Task<List<Task<?>>> task) {

                for (Task<QuerySnapshot> driverTask : tasks) {

                    if (driverTask.isSuccessful() && driverTask.getResult() != null && !driverTask.getResult().isEmpty()) {

                        if (isInitial) {
                            restaurantSummaries.addAll(driverTask.getResult()
                                    .toObjects(PartneredRestaurant.PartneredRestaurantSummary.class));
                        } else {
                            restaurantSummaries.addAll(restaurantSummaries.size(), driverTask.getResult()
                                    .toObjects(PartneredRestaurant.PartneredRestaurantSummary.class));
                        }

                    }
                }

                final QuerySnapshot finalSnapshot = tasks.get(tasks.size() - 1).getResult();

                if (finalSnapshot != null && !finalSnapshot.isEmpty()) {
                    if (!finalSnapshot.getDocuments().isEmpty()) {
                        final DocumentSnapshot lastDoc = finalSnapshot.getDocuments().get(finalSnapshot.size() - 1);
                        if (lastDoc != null) {
                            lastDocSnapshot = lastDoc;
                        }
                    }
                }

                Log.d("ttt", "category restaurantSummaries: " + restaurantSummaries.size());

                if (isInitial) {

                    if (!restaurantSummaries.isEmpty()) {

                        categoryRestaurantsRv.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();

                        if (restaurantSummaries.size() == RESTAURANT_LIMIT && scrollListener == null) {
                            categoryRestaurantsRv.addOnScrollListener(scrollListener = new ScrollListener());
                        }

                    }

                } else {

                    if (!restaurantSummaries.isEmpty()) {

                        adapter.notifyItemRangeInserted(
                                restaurantSummaries.size() - previousSize, previousSize);

                        if (task.getResult().size() < RESTAURANT_LIMIT && scrollListener != null) {
                            categoryRestaurantsRv.removeOnScrollListener(scrollListener);
                            scrollListener = null;
                        }
                    }

                }

                categoryRestaurantsProgressBar.setVisibility(View.GONE);
//                dismissProgressDialog();                isLoadingItems = false;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                isLoadingItems = false;

                categoryRestaurantsProgressBar.setVisibility(View.GONE);

//                dismissProgressDialog();

                Log.d("ttt", "failed to get geo task: " + e.getMessage());
            }
        });

    }


    @Override
    public void addOrRemoveFromFav(int position) {


        categoryRestaurantsProgressBar.setVisibility(View.GONE);

        String id = restaurantSummaries.get(position).getID();

        boolean isLiked = likedRestaurants.contains(id);

        PartneredRestaurantModel partneredRestaurantModel = new PartneredRestaurantModel();
        partneredRestaurantModel.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {

                if (arg instanceof HashMap) {

                    final HashMap<Integer, Object> resultMap = (HashMap<Integer, Object>) arg;

                    final int key = resultMap.keySet().iterator().next();
                    final Object result = resultMap.get(key);

                    if (key == PartneredRestaurantModel.TYPE_FAVORITE) {

                        categoryRestaurantsProgressBar.setVisibility(View.GONE);

                        if (result instanceof Boolean && (boolean) result) {

                            if (isLiked) {

                                likedRestaurants.remove(id);

                            } else {
                                likedRestaurants.add(id);
                            }

                            adapter.notifyItemChanged(position);

                        } else if (result instanceof String) {

                            Toast.makeText(CategoryActivity.this,
                                    "Adding to favorite failed! Please try again",
                                    Toast.LENGTH_LONG).show();

                            Log.d("categoryAcitivty", "failed to fav restautant: " + result);
                        }

                    }

                }

            }
        });

        partneredRestaurantModel.favRestaurant(isLiked);
    }

    @Override
    public void showRestaurant(int position) {

        startActivity(new Intent(this, RestaurantActivity.class)
                .putExtra("ID", restaurantSummaries.get(position).getID())
                .putExtra("currency", (String) addressMap.get("currency")));

    }

    private class ScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (!isLoadingItems &&
                    !recyclerView.canScrollVertically(1) &&
                    newState == RecyclerView.SCROLL_STATE_IDLE) {

                Log.d("ttt", "is at bottom");

                getCategoryRestaurants(false);

            }
        }
    }


}