package com.developers.wajbaty.PartneredRestaurant.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.developers.wajbaty.Adapters.AdditionalOptionsAdapter;
import com.developers.wajbaty.Adapters.ImageInputRecyclerAdapter;
import com.developers.wajbaty.Fragments.ProgressDialogFragment;
import com.developers.wajbaty.Models.MenuItem;
import com.developers.wajbaty.Models.MenuItemModel;
import com.developers.wajbaty.R;
import com.developers.wajbaty.Utils.GlobalVariables;
import com.developers.wajbaty.Utils.PermissionRequester;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class MenuItemModifierActivity extends AppCompatActivity implements Observer,
        ImageInputRecyclerAdapter.LocalImageListener,
        ImageInputRecyclerAdapter.CloudImageRemoveListener, View.OnClickListener {

    private static final int MAX_MENU_IMAGE_COUNT = 3, IMAGE_STORAGE_REQUEST = 1, PICK_IMAGE = 2;
//    public static final int MENU_ITEM_RESULT_CODE = 1;

    //views
    private Toolbar menuItemModifierTb;
    private RecyclerView menuItemImagesRv;
    private EditText menuItemNameEd, menuItemPriceEd;
    private Button menuItemConfirmBtn;

    //images
    private List<Object> menuItemImages;
    private ImageInputRecyclerAdapter imageAdapter;

    private MenuItem menuItem;
    private FirebaseStorage firebaseStorage;

    private ProgressDialogFragment progressDialogFragment;

    private int chosenImageNumber;

    //category spinner
    private Spinner menuCategorySpinner;
    private ArrayAdapter<String> categorySpinnerAdapter;
    private Map<String, String> categories;
    private List<String> categoryNames;

    //ingredients recycler
    private RecyclerView menuIngredientsRv;
    private AdditionalOptionsAdapter ingredientsAdapter;
    private ArrayList<String> ingredients;

    //currency spinner
    //private Spinner currencySpinner;
    private String currency;
    private TextView currencyTv;
    //private ArrayAdapter<String> currencySpinnerAdapter;
    //private ArrayList<String> currencies;

    //firebase
    private ProgressDialogFragment progressDialog;

    private MenuItemModel menuItemModel;

    private String language;

    public MenuItemModifierActivity() {

    }

    public MenuItemModifierActivity(MenuItem menuItem) {
        this.menuItem = menuItem;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_item_modifier);


        final Intent intent = getIntent();

        if (intent != null && intent.hasExtra("currency")) {
            currency = getIntent().getStringExtra("currency");
        }

        menuItemModel = new MenuItemModel();
        menuItemModel.addObserver(MenuItemModifierActivity.this);

        language = Locale.getDefault().getLanguage().equals("ar") ? "ar" : "en";

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            ProgressDialogFragment progressDialogFragment = new ProgressDialogFragment();
            progressDialogFragment.setMessage("Creating new user");
            progressDialogFragment.show(getSupportFragmentManager(), "progress");

            FirebaseAuth.getInstance().signInAnonymously().addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {

                    final FirebaseUser user = authResult.getUser();
                    final String userId = user.getUid();

                    final HashMap<String, Object> userTestMap = new HashMap<>();

                    userTestMap.put("ID", userId);
                    userTestMap.put("type", 1);

                    FirebaseFirestore.getInstance().collection("Users")
                            .document(userId)
                            .set(userTestMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    progressDialogFragment.dismiss();
                                    GlobalVariables.setCurrentRestaurantId("1");

                                }
                            });


                }
            });

        } else {
            GlobalVariables.setCurrentRestaurantId("1");
        }

        initObjects();
        getViews();
        attachAdapters();
//        fetchCategories();
        fetchCategoryOptions();
        setupMealImagesRv();
        setCurrency();
//        populateCurrencySpinner();

//        currencySpinner.setAdapter(currencySpinnerAdapter);
    }

    private void getViews() {

        menuItemModifierTb = findViewById(R.id.menuItemModifierTb);
        menuItemImagesRv = findViewById(R.id.menuItemImagesRv);
        menuIngredientsRv = findViewById(R.id.menuIngredientsRv);
        menuItemNameEd = findViewById(R.id.menuItemNameEd);
        menuItemPriceEd = findViewById(R.id.menuItemPriceEd);
        menuCategorySpinner = findViewById(R.id.menuCategorySpinner);
        menuItemConfirmBtn = findViewById(R.id.menuItemConfirmBtn);
        currencyTv = findViewById(R.id.currencyTv);
//        currencySpinner = findViewById(R.id.currencySpinner);

        menuItemModifierTb.setNavigationOnClickListener(v -> onBackPressed());
        menuItemConfirmBtn.setOnClickListener(this);

        menuItemImagesRv.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false) {
            @Override
            public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
                lp.height = getHeight();
                lp.width = getHeight();
                return true;
            }
        });

    }

    private void initObjects() {

        menuItemImages = new ArrayList<>();

        imageAdapter = new ImageInputRecyclerAdapter(this, menuItemImages,
                this, this,
                ImageInputRecyclerAdapter.TYPE_BANNER);

        categories = new HashMap<>();
        categoryNames = new ArrayList<>();

        categorySpinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, categoryNames);

        ingredients = new ArrayList<>();
        ingredients.add(null);
        ingredientsAdapter = new AdditionalOptionsAdapter(ingredients, "ingredient");

//        currencies = new ArrayList<>();
//        currencySpinnerAdapter = new ArrayAdapter<>(this,
//                android.R.layout.simple_spinner_dropdown_item,currencies);

//        currencySpinnerAdapter.setDropDownViewResource(R.layout.small_spinner_item_layout);
    }

    private void attachAdapters() {

        menuItemImagesRv.setAdapter(imageAdapter);
        menuCategorySpinner.setAdapter(categorySpinnerAdapter);
        menuIngredientsRv.setAdapter(ingredientsAdapter);


    }

    private void setupMealImagesRv() {

        if (menuItem != null && menuItem.getImageUrls() != null) {

            menuItemImages.addAll(menuItem.getImageUrls());

            if (menuItem.getImageUrls().size() < MAX_MENU_IMAGE_COUNT) {
                for (int i = menuItem.getImageUrls().size(); i < MAX_MENU_IMAGE_COUNT; i++) {
                    menuItemImages.add(null);
                }
            }

        } else {
            for (int i = 0; i < MAX_MENU_IMAGE_COUNT; i++) {
                menuItemImages.add(null);
            }
        }

        imageAdapter.notifyDataSetChanged();

    }


    private void fetchCategoryOptions() {

        FirebaseFirestore.getInstance().collection("GeneralOptions")
                .document("Categories")
                .collection("Categories")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {


                if (snapshots != null && !snapshots.isEmpty()) {

                    final String name = "name_" + language;

                    for (DocumentSnapshot documentSnapshot : snapshots) {
                        final String categoryName = documentSnapshot.getString(name);
                        categoryNames.add(categoryName);
                        categories.put(documentSnapshot.getId(), categoryName);
                    }
                }


            }
        }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (!categories.isEmpty()) {
                    categorySpinnerAdapter.notifyDataSetChanged();
                }
            }
        });

//                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//            @Override
//            public void onSuccess(DocumentSnapshot snapshot) {
//                if(snapshot.exists()){
//
//
//                }
//            }
//        }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if(!categories.isEmpty()){
//
//                    Log.d("ttt","categories size: "+categories.size());
//
//
//
//                    for(String key:categories.keySet()){
//                        Log.d("ttt","categories key: "+key);
//                        categoryNames.add(categories.get(key).get(language));
//
//                        Log.d("ttt","categories language: "+language + categories.get(key).get(language));
//
//                    }
//
//                    categorySpinnerAdapter.notifyDataSetChanged();
//
//                }
//            }
//        });
//

    }

    private void setCurrency() {
        Log.d("ttt", "currency: " + currency);
        currencyTv.setText(currency);
    }

    private void populateCurrencySpinner() {

//        currencies.addAll(CurrencyUtil.getCurrencies());
//        currencySpinner.setSelection(currencies.indexOf(CurrencyUtil.getDefaultCurrencyCode()));

    }

    @Override
    public void removeCloudImage(int index, int adapterType) {

        showProgressDialog();

        getFirebaseStorage().getReferenceFromUrl((String) menuItemImages.get(index)).delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {

                            menuItemImages.remove(index);
                            imageAdapter.notifyItemChanged(index);

                        } else {

                            Toast.makeText(MenuItemModifierActivity.this,
                                    "Deletion failed please try again!",
                                    Toast.LENGTH_SHORT).show();

                        }

                        progressDialogFragment.dismiss();
                    }
                });

    }

    @Override
    public void removeLocaleImage(int index, int adapterType) {

        menuItemImages.set(index, null);
        imageAdapter.notifyItemChanged(index);

    }

    @Override
    public void addLocalImage(int index, int adapterType) {

        chosenImageNumber = index;
        getImage();

    }


    void getImage() {

        if (PermissionRequester.needsToRequestStoragePermissions(IMAGE_STORAGE_REQUEST, this)) {
            startActivityIfNeeded(Intent.createChooser(
                    new Intent(Intent.ACTION_GET_CONTENT).setType("image/*"),
                    "Select Image"), PICK_IMAGE);
        }

    }

    public FirebaseStorage getFirebaseStorage() {
        if (firebaseStorage == null)
            firebaseStorage = FirebaseStorage.getInstance();
        return firebaseStorage;
    }

    private void showProgressDialog() {
        if (progressDialogFragment == null) {
            progressDialogFragment = new ProgressDialogFragment();
        }
        progressDialogFragment.show(getSupportFragmentManager(), "progress");
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == IMAGE_STORAGE_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getImage();
            }
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.getData() != null) {

            menuItemImages.set(chosenImageNumber, data.getData());
            imageAdapter.notifyItemChanged(chosenImageNumber);

        }

    }

    @Override
    public void onClick(View v) {

        if (v.getId() == menuItemConfirmBtn.getId()) {

            if (GlobalVariables.getCurrentRestaurantId() == null) {

                Toast.makeText(this,
                        "There was an error with verification! Please logout and login again",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            final String name = menuItemNameEd.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "Please add menu item name", Toast.LENGTH_SHORT).show();
                return;
            }

            final String category = menuCategorySpinner.getSelectedItem().toString();

            String categoryKey = "";

            for (String key : categories.keySet()) {
                if (categories.get(key).equals(category)) {
                    categoryKey = key;
                    break;
                }
            }


            if (categoryKey == null || categoryKey.isEmpty()) {
                Toast.makeText(this, "Please add menu item category", Toast.LENGTH_SHORT).show();
                return;
            }

            final String price = menuItemPriceEd.getText().toString().trim();

            if (price.isEmpty()) {
                Toast.makeText(this, "Please add menu item price", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean containsAnImage = false;

            if (!menuItemImages.isEmpty()) {
                for (int i = 0; i < menuItemImages.size(); i++) {
                    if (menuItemImages.get(i) != null) {
                        containsAnImage = true;
                        break;
                    }
                }
            }


            if (!containsAnImage) {
                Toast.makeText(this,
                        "Please add at least one image for your menu item", Toast.LENGTH_SHORT).show();
                return;
            }

            progressDialog = new ProgressDialogFragment();
            progressDialog.setTitle("Add the menu item to your menu!");
            progressDialog.setMessage("Please wait!");
            progressDialog.show(getSupportFragmentManager(), "progress");

            final List<Uri> uriImages = new ArrayList<>();

            for (Object image : menuItemImages) {
                if (image instanceof Uri) {
                    uriImages.add((Uri) image);
                }
            }


            final ArrayList<String> addedIngredients = new ArrayList<>();

            for (String ingrident : ingredients) {
                if (ingrident != null) {
                    addedIngredients.add(ingrident);
                }
            }


            menuItemModel.uploadMenuItem(name, Float.parseFloat(price),
                    currency, categoryKey, uriImages, addedIngredients.isEmpty() ? null : addedIngredients,
                    getIntent().getStringExtra("restaurantId"),
                    getIntent().hasExtra("restaurantRegion") ? getIntent().getStringExtra("restaurantRegion") : null);

        }

    }


    @Override
    public void update(Observable o, Object arg) {

        progressDialog.dismiss();

        if (arg instanceof MenuItem) {

            setResult(RESULT_OK, new Intent().putExtra("addedMenuItem", (MenuItem) arg));
            finish();

        } else if (arg instanceof String) {

            Toast.makeText(this,
                    "There was an error while trying to add the menu item!" +
                            "Please try again", Toast.LENGTH_SHORT).show();

            Log.d("menuItemUploading", (String) arg);

        }

    }


    @Override
    public void onBackPressed() {

        boolean containsAnImage = false;

        if (!menuItemImages.isEmpty()) {
            for (int i = 0; i < menuItemImages.size(); i++) {
                if (menuItemImages.get(i) != null) {
                    containsAnImage = true;
                    break;
                }
            }
        }

        boolean containsAnIngredient = false;

        if (!ingredients.isEmpty()) {
            for (int i = 0; i < ingredients.size(); i++) {
                if (ingredients.get(i) != null) {
                    containsAnIngredient = true;
                    break;
                }
            }
        }

        if (!menuItemNameEd.getText().toString().isEmpty() ||
                !menuItemPriceEd.getText().toString().isEmpty() ||
                containsAnImage || containsAnIngredient) {


            final AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Are you sure you want to exit!");
            alert.setMessage("Exiting before adding your menu item will discard it!");


            alert.setPositiveButton("Yes", (dialog, which) -> finish());

            alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            alert.show();

        } else {
            super.onBackPressed();
        }


    }
}