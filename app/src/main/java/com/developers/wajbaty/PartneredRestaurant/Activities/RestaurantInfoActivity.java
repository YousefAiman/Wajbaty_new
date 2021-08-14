package com.developers.wajbaty.PartneredRestaurant.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

import com.developers.wajbaty.Adapters.AdditionalOptionsAdapter;
import com.developers.wajbaty.Adapters.FillOptionsAdapter;
import com.developers.wajbaty.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RestaurantInfoActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener,
        View.OnClickListener {

    private static final String TAG = "RestaurantInfoActivity";

    //firebase
    private FirebaseFirestore firestore;

    //views
    private ImageView backIv;
    private Button infoInputNextBtn;
    private EditText nameEd, locationEd, descriptionEd;

    //category spinner
    private Spinner categorySpinner;
    private ArrayAdapter<String> spinnerAdapter;
    private Map<String, String> categories;
    private List<String> categoryNames;

    //service Options
    private HorizontalScrollView horizontalScrollView;
    private LinearLayout serviceOptionsLl;
    private List<TextView> serviceOptionsTvs;
    private View.OnClickListener serviceOptionsTvsListener;
    private ArrayList<String> selectedServiceOptions;

    //Additional Services
    private RecyclerView additionalServicesRv;
    private AdditionalOptionsAdapter additionalOptionsAdapter;
    private ArrayList<String> additionalServices;

    //Contact Info
    private RecyclerView contactInfoRv;
    private FillOptionsAdapter contactInfoAdapter;
    private List<String> contacts;

    //Social Media
    private RecyclerView socialMediaRv;
    private FillOptionsAdapter socialMediaAdapter;
    private List<String> socialMediaSites;

    private String language;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_info);

        initObjects();
        getViews();
        initListeners();
        attachAdapters();
        populateAddress();

        populateInfo();

        fetchOptions();


    }


    private void initObjects() {

        language = Locale.getDefault().getLanguage().equals("ar") ? "ar" : "en";
        firestore = FirebaseFirestore.getInstance();

        categories = new HashMap<>();
        categoryNames = new ArrayList<>();

        spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, categoryNames);

        selectedServiceOptions = new ArrayList<>();
        serviceOptionsTvs = new ArrayList<>();

        serviceOptionsTvsListener = new View.OnClickListener() {
            //            final Drawable checkBackground =
//                    ResourcesCompat.getDrawable(getResources(),R.drawable.option_checked_background,null);
            @Override
            public void onClick(View v) {

                final String text = ((TextView) v).getText().toString();
                if (selectedServiceOptions.contains(text)) {

                    //already clicked
                    v.setBackgroundResource(R.drawable.option_un_checked_background);
                    selectedServiceOptions.remove(text);

                } else {
                    v.setBackgroundResource(R.drawable.option_checked_background);
                    selectedServiceOptions.add(text);
                }

            }
        };

        additionalServices = new ArrayList<>();
        additionalServices.add(null);

        additionalOptionsAdapter = new AdditionalOptionsAdapter(additionalServices, "option");

        contacts = new ArrayList<>();
        contactInfoAdapter = new FillOptionsAdapter(contacts);

        socialMediaSites = new ArrayList<>();
        socialMediaAdapter = new FillOptionsAdapter(socialMediaSites);

    }

    private void getViews() {

        final NestedScrollView nestedScrollView = findViewById(R.id.nestedScrollView);
        nestedScrollView.setNestedScrollingEnabled(false);

        backIv = findViewById(R.id.backIv);
        infoInputNextBtn = findViewById(R.id.infoInputNextBtn);
        nameEd = findViewById(R.id.nameEd);
        locationEd = findViewById(R.id.locationEd);
        descriptionEd = findViewById(R.id.descriptionEd);
        categorySpinner = findViewById(R.id.categorySpinner);
        serviceOptionsLl = findViewById(R.id.serviceOptionsLl);
        additionalServicesRv = findViewById(R.id.additionalServicesRv);
        contactInfoRv = findViewById(R.id.contactInfoRv);
        socialMediaRv = findViewById(R.id.socialMediaRv);

    }


    private void initListeners() {

        backIv.setOnClickListener(this);
        infoInputNextBtn.setOnClickListener(this);
        categorySpinner.setOnItemSelectedListener(this);

    }

    private void attachAdapters() {

        categorySpinner.setAdapter(spinnerAdapter);
        additionalServicesRv.setAdapter(additionalOptionsAdapter);
        contactInfoRv.setAdapter(contactInfoAdapter);
        socialMediaRv.setAdapter(socialMediaAdapter);

    }

    private void fetchOptions() {


        final ArrayList<String> serviceOptions = new ArrayList<>();

        final List<Task<?>> tasks = new ArrayList<>();

        tasks.add(FirebaseFirestore.getInstance().collection("GeneralOptions")
                .document("Categories")
                .collection("Categories")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshots) {
                        if (snapshots != null && !snapshots.isEmpty()) {

                            final String name = "name_" + language;

                            for (DocumentSnapshot documentSnapshot : snapshots) {
                                categories.put(documentSnapshot.getId(), documentSnapshot.getString(name));
                            }
                        }
                    }
                }));


        tasks.add(firestore.collection("GeneralOptions")
                .document("ServiceOptions")
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        if (snapshot.exists()) {
                            serviceOptions.addAll((ArrayList<String>) snapshot.get("ServiceOptions"));
                        }
                    }
                }));

        tasks.add(firestore.collection("GeneralOptions")
                .document("ContactInformation")
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        if (snapshot.exists()) {
                            contacts.addAll((List<String>) snapshot.get("ContactInformation"));
                        }
                    }
                }));

        tasks.add(firestore.collection("GeneralOptions")
                .document("SocialMediaWebsites")
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        if (snapshot.exists()) {
                            socialMediaSites.addAll((List<String>) snapshot.get("SocialMediaWebsites"));
                        }
                    }
                }));

        Tasks.whenAllComplete(tasks).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
            @Override
            public void onComplete(@NonNull Task<List<Task<?>>> task) {

                if (!categories.isEmpty()) {
                    populateCategorySpinner();
                }

                if (!serviceOptions.isEmpty()) {

                    createServiceOptionsViews(serviceOptions);

                }

                if (!contacts.isEmpty()) {

                    contactInfoAdapter.notifyDataSetChanged();

                }
                if (!socialMediaSites.isEmpty()) {

                    socialMediaAdapter.notifyDataSetChanged();

                }

            }
        });

    }

    private void populateAddress() {

        final Intent intent = getIntent();

        if (intent != null) {

            if (intent.hasExtra("addressMap")) {
                final Map<String, String> addressMap =
                        (Map<String, String>) intent.getSerializableExtra("addressMap");
                if (addressMap != null && addressMap.containsKey("fullAddress")) {
                    locationEd.setText(addressMap.get("fullAddress"));
                }
            }
        }
    }

    private void populateInfo() {
        final Intent intent = getIntent();

        if (intent != null) {

            if (intent.hasExtra("infoBundle")) {

                final Bundle infoBundle = intent.getBundleExtra("infoBundle");

                if (infoBundle.containsKey("restaurantName")) {
                    nameEd.setText(infoBundle.getString("restaurantName"));
                }

            }
        }

    }


    private void createServiceOptionsViews(ArrayList<String> serviceOptions) {

        if (serviceOptions != null && !serviceOptions.isEmpty()) {

//            int loopCount = 1;
            final int whiteColor = getResources().getColor(R.color.white);
            final float db = getResources().getDisplayMetrics().density;

//            LinearLayout linearLayout = new LinearLayout(this);
//            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
//            linearLayout.setGravity(Gravity.END);

//            horizontalScrollView.ad
            for (int i = 0; i < serviceOptions.size(); i++) {

//
//                if(i / 4f > loopCount){
//
//                    Log.d(TAG,"adding a new linear layout");
//
//                    linearLayout = new LinearLayout(this);
//                    linearLayout.setOrientation(LinearLayout.HORIZONTAL);
//                    linearLayout.setGravity(Gravity.END);
//
//                    loopCount++;
//                }

                final TextView textView = new TextView(this);

                final LinearLayout.LayoutParams params =
                        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, (int) (12 * db), 0);

                textView.setBackgroundResource(R.drawable.option_un_checked_background);
                textView.setText(serviceOptions.get(i));
                textView.setTextColor(whiteColor);
                textView.setPadding((int) (30 * db), (int) (12 * db), (int) (30 * db), (int) (12 * db));
                textView.setOnClickListener(serviceOptionsTvsListener);
                textView.setLayoutParams(params);

                serviceOptionsTvs.add(textView);
                serviceOptionsLl.addView(textView);

            }
        }
    }

    private void createAdditionalServicesRecycler() {


    }

    private void createContactInfoRecycler() {


    }

    private void populateCategorySpinner() {


        for (String key : categories.keySet()) {
            categoryNames.add(categories.get(key));
        }

        spinnerAdapter.notifyDataSetChanged();


    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {


    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View v) {

        if (v.getId() == infoInputNextBtn.getId()) {

            final String name = nameEd.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this,
                        "You need to add the name of your restaurant!",
                        Toast.LENGTH_SHORT).show();
                return;
            }


            final String description = descriptionEd.getText().toString().trim();

            if (description.isEmpty()) {
                Toast.makeText(this,
                        "You need to add a description to your restaurant!",
                        Toast.LENGTH_SHORT).show();
                return;
            }


            if (selectedServiceOptions == null || selectedServiceOptions.isEmpty()) {
                Toast.makeText(this,
                        "You need to choose at least one service option for your restaurant",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            for (String selectedOption : selectedServiceOptions) {
                Log.d("ttt", "sevice option: " + selectedOption);
            }

//            boolean hasAtleastOneAdditionalService
            final ArrayList<String> selectedAdditionalServices = new ArrayList<>();

            for (int i = 0; i < additionalServices.size(); i++) {
                final EditText editText = additionalServicesRv.getChildAt(i).findViewById(R.id.optionEd);
                if (editText != null) {

                    final String additionalService = editText.getText().toString().trim();

                    if (!additionalService.isEmpty()) {
                        selectedAdditionalServices.add(additionalService);

                        Log.d("ttt", "additionalServices: " + additionalService);

                    }
                }
            }

            final Map<String, String> addedContactInfoMap = new HashMap<>();

            for (int i = 0; i < contacts.size(); i++) {
                final EditText editText = contactInfoRv.getChildAt(i).findViewById(R.id.optionEd);
                if (editText != null) {

                    final String contactInfo = editText.getText().toString().trim();

                    if (!contactInfo.isEmpty()) {

                        if (contacts.get(i) != null && !contacts.get(i).isEmpty()) {
                            addedContactInfoMap.put(contacts.get(i), contactInfo);
                            Log.d("ttt", contacts.get(i) + " : " + contactInfo);
                        }
                    }
                }
            }

            if (!addedContactInfoMap.containsKey("Phone Number")) {
                Toast.makeText(this,
                        "You need to at least add a phone number in the contact information section",
                        Toast.LENGTH_SHORT).show();

                return;
            }

            final Map<String, String> addedSocialMediaSitesMap = new HashMap<>();

            for (int i = 0; i < socialMediaSites.size(); i++) {
                final EditText editText = socialMediaRv.getChildAt(i).findViewById(R.id.optionEd);
                if (editText != null) {

                    final String socialMediaSite = editText.getText().toString().trim();

                    if (socialMediaSites.get(i) != null && !socialMediaSite.isEmpty()) {

                        addedSocialMediaSitesMap.put(socialMediaSites.get(i), socialMediaSite);
                        Log.d("ttt", socialMediaSites.get(i) + " : " + socialMediaSite);
                    }
                }
            }


            final Bundle infoBundle = new Bundle();
            infoBundle.putString("name", name);
            infoBundle.putString("description", description);

            final String category = categorySpinner.getSelectedItem().toString();

            String categoryKey = "";

            for (String key : categories.keySet()) {
                if (categories.get(key).equals(category)) {
                    categoryKey = key;
                    break;
                }
            }


            infoBundle.putString("category", categoryKey);

            infoBundle.putStringArrayList("selectedServiceOptions", selectedServiceOptions);

            if (selectedAdditionalServices != null && !selectedAdditionalServices.isEmpty()) {
                infoBundle.putStringArrayList("selectedAdditionalServices", selectedAdditionalServices);
            }

            if (addedContactInfoMap != null && !addedContactInfoMap.isEmpty()) {
                infoBundle.putSerializable("addedContactInfoMap", (Serializable) addedContactInfoMap);
            }

            if (addedSocialMediaSitesMap != null && !addedSocialMediaSitesMap.isEmpty()) {
                infoBundle.putSerializable("addedSocialMediaSitesMap", (Serializable) addedSocialMediaSitesMap);
            }

            final Intent intent = new Intent(this, RestaurantScheduleActivity.class);
            intent.putExtra("addressMap", getIntent().getSerializableExtra("addressMap"));
            intent.putExtra("imagesBundle", getIntent().getBundleExtra("imagesBundle"));
            intent.putExtra("infoBundle", infoBundle);
            startActivity(intent);

            finish();

        } else if (v.getId() == backIv.getId()) {

            finish();

        }
    }
}