package com.developers.wajbaty.PartneredRestaurant.Activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.developers.wajbaty.Adapters.ImageInputRecyclerAdapter;
import com.developers.wajbaty.Fragments.ProgressDialogFragment;
import com.developers.wajbaty.R;
import com.developers.wajbaty.Utils.PermissionRequester;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class RestaurantMediaFillingActivity extends AppCompatActivity implements
        ImageInputRecyclerAdapter.CloudImageRemoveListener, ImageInputRecyclerAdapter.LocalImageListener,
        View.OnClickListener {

    private static final int IMAGE_STORAGE_REQUEST = 1, PICK_IMAGE = 10,
            MAIN_IMAGE = 100, BANNER_IMAGE = 101, ALBUM_IMAGE = 103,
            MAX_BANNER_IMAGE_COUNT = 5, MAX_ALBUM_IMAGE_COUNT = 5;

    //views
    private ImageView backIv;
    private Button imageInputNextBtn;

    //mainImage
    private ImageView mainImageIv, mainImageCloseIv, mainAddIv;
    private Uri mainImageUri;
    private String previousMainImageURL;

    //banner recycler
    private RecyclerView bannerImagesRv;
    private List<Object> bannerImages;
    private ImageInputRecyclerAdapter bannerAdapter;


    //album recycler
    private RecyclerView restaurantMediaRv;
    private List<Object> albumImages;
    private ImageInputRecyclerAdapter albumAdapter;


    private FirebaseStorage firebaseStorage;

    private ProgressDialogFragment progressDialogFragment;
    private Bundle imagesBundle;
    private Bundle infoBundle;

    private int chosenImageNumber;
    private int chosenImageType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_media_filling);

        getViews();

        final Intent intent = getIntent();

        if (intent != null) {

            if (intent.hasExtra("imagesBundle")) {
                imagesBundle = getIntent().getBundleExtra("imagesBundle");
            }

            if (intent.hasExtra("restaurantName")) {
                infoBundle = new Bundle();
                infoBundle.putString("restaurantName", intent.getStringExtra("restaurantName"));
                if (intent.hasExtra("restaurantImageURL")) {
                    previousMainImageURL = intent.getStringExtra("restaurantImageURL");
                    infoBundle.putString("restaurantImageURL", previousMainImageURL);
                }
            }
        }


        getMainImage();
        setupBannerImagesRv();
        setupAlbumImagesRv();

    }


    private void getViews() {

        backIv = findViewById(R.id.backIv);
        mainImageIv = findViewById(R.id.mainImageIv);
        mainImageCloseIv = findViewById(R.id.mainImageCloseIv);
        mainAddIv = findViewById(R.id.mainAddIv);

        bannerImagesRv = findViewById(R.id.bannerImagesRv);
        restaurantMediaRv = findViewById(R.id.restaurantMediaRv);
        imageInputNextBtn = findViewById(R.id.imageInputNextBtn);

        bannerImagesRv.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false) {
            @Override
            public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
                lp.height = getHeight();
                lp.width = getHeight();
                return true;
            }
        });
        restaurantMediaRv.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false) {
            @Override
            public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
                lp.height = getHeight();
                lp.width = getHeight();
                return true;
            }
        });

        backIv.setOnClickListener(this);
        mainImageIv.setOnClickListener(this);
        mainImageCloseIv.setOnClickListener(this);
        imageInputNextBtn.setOnClickListener(this);

    }

    private void getMainImage() {

        if (previousMainImageURL != null) {

            mainAddIv.setVisibility(View.GONE);
            mainImageCloseIv.setVisibility(View.VISIBLE);

            mainImageIv.setOnClickListener(null);

            Picasso.get().load(previousMainImageURL).fit().centerCrop().into(mainImageIv);

        } else {
            if (imagesBundle != null && imagesBundle.containsKey("mainImage")) {
                Picasso.get().load(imagesBundle.getString("mainImage")).fit().centerCrop().into(mainImageIv);
            }
        }
    }

    private void setupBannerImagesRv() {

        bannerImages = new ArrayList<>();


        if (imagesBundle != null && imagesBundle.containsKey("bannerImages")) {

            final ArrayList<String> oldBannerImages = imagesBundle.getStringArrayList("bannerImages");

            bannerImages.addAll(oldBannerImages);

            if (oldBannerImages.size() < MAX_BANNER_IMAGE_COUNT) {
                for (int i = oldBannerImages.size(); i < MAX_BANNER_IMAGE_COUNT; i++) {
                    bannerImages.add(null);
                }
            }

        } else {

            for (int i = 0; i < MAX_BANNER_IMAGE_COUNT; i++) {
                bannerImages.add(null);
            }
        }

        bannerAdapter = new ImageInputRecyclerAdapter(this, bannerImages,
                this, this,
                ImageInputRecyclerAdapter.TYPE_BANNER);


        bannerImagesRv.setAdapter(bannerAdapter);

    }

    private void setupAlbumImagesRv() {

        albumImages = new ArrayList<>();

        if (imagesBundle != null && imagesBundle.containsKey("albumImages")) {

            final ArrayList<String> oldAlbumImages = imagesBundle.getStringArrayList("albumImages");

            albumImages.addAll(oldAlbumImages);

            if (oldAlbumImages.size() < MAX_ALBUM_IMAGE_COUNT) {
                for (int i = oldAlbumImages.size(); i < MAX_ALBUM_IMAGE_COUNT; i++) {
                    albumImages.add(null);
                }
            }

        } else {

            for (int i = 0; i < MAX_ALBUM_IMAGE_COUNT; i++) {
                albumImages.add(null);
            }
        }

        albumAdapter = new ImageInputRecyclerAdapter(this, albumImages,
                this, this, ImageInputRecyclerAdapter.TYPE_ALBUM);


        restaurantMediaRv.setAdapter(albumAdapter);
    }


    @Override
    public void removeCloudImage(int index, int adapterType) {

        showProgressDialog();

        switch (adapterType) {

            case ImageInputRecyclerAdapter.TYPE_BANNER:

                getFirebaseStorage().getReferenceFromUrl((String) bannerImages.get(index)).delete()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {

                                    bannerImages.remove(index);
                                    bannerAdapter.notifyItemChanged(index);

                                } else {

                                    Toast.makeText(RestaurantMediaFillingActivity.this,
                                            "Deletion failed please try again!",
                                            Toast.LENGTH_SHORT).show();

                                }

                                progressDialogFragment.dismiss();
                            }
                        });

                break;

            case ImageInputRecyclerAdapter.TYPE_ALBUM:

                getFirebaseStorage().getReferenceFromUrl((String) albumImages.get(index)).delete()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {

                                    albumImages.remove(index);
                                    albumAdapter.notifyItemChanged(index);

                                } else {

                                    Toast.makeText(RestaurantMediaFillingActivity.this,
                                            "Deletion failed please try again!",
                                            Toast.LENGTH_SHORT).show();

                                }

                                progressDialogFragment.dismiss();
                            }
                        });


                break;


        }

        Log.d("imageInput", "removeCloudImage");
    }


    @Override
    public void removeLocaleImage(int index, int adapterType) {

        switch (adapterType) {

            case ImageInputRecyclerAdapter.TYPE_BANNER:

                bannerImages.set(index, null);
                bannerAdapter.notifyItemChanged(index);

                break;

            case ImageInputRecyclerAdapter.TYPE_ALBUM:

                albumImages.set(index, null);
                albumAdapter.notifyItemChanged(index);

                break;


        }

        Log.d("imageInput", "remove Locale image");

    }

    @Override
    public void addLocalImage(int index, int adapterType) {

        Log.d("imageInput", "addLocalImage");

        chosenImageNumber = index;

        int type = 0;
        switch (adapterType) {

            case ImageInputRecyclerAdapter.TYPE_BANNER:

                type = BANNER_IMAGE;

                break;

            case ImageInputRecyclerAdapter.TYPE_ALBUM:

                type = ALBUM_IMAGE;

                break;
        }

        getImage(type);

    }


    void getImage(int type) {

        chosenImageType = type;

        if (PermissionRequester.needsToRequestStoragePermissions(IMAGE_STORAGE_REQUEST, this)) {
            startActivityIfNeeded(Intent.createChooser(
                    new Intent(Intent.ACTION_GET_CONTENT).setType("image/*"),
                    "Select Image"), type);
        }

    }


    public FirebaseStorage getFirebaseStorage() {
        if (firebaseStorage == null)
            firebaseStorage = FirebaseStorage.getInstance();
        return firebaseStorage;
    }

//    public boolean needsToRequestStoragePermissions(int code) {
//        final String[] permissions = {
//                Manifest.permission.READ_EXTERNAL_STORAGE};
//        if (ContextCompat.checkSelfPermission(getApplicationContext(), permissions[0]) != PackageManager.PERMISSION_GRANTED) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                requestPermissions(permissions, code);
//                return false;
//            }
//        }
//        return true;
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.getData() != null) {


            switch (requestCode) {

                case BANNER_IMAGE:


                    bannerImages.set(chosenImageNumber, data.getData());
                    bannerAdapter.notifyItemChanged(chosenImageNumber);


//
//                    for(int i=0;i<bannerImages.size();i++){
//
//                        if(bannerImages.get(i) == null){
//
//                            bannerImages.set(i,data.getData());
//                            bannerAdapter.notifyItemChanged(i);
//
//                            return;
//                        }
//                    }

                    break;

                case ALBUM_IMAGE:

                    albumImages.set(chosenImageNumber, data.getData());
                    albumAdapter.notifyItemChanged(chosenImageNumber);

//
//                    for(int i=0;i<albumImages.size();i++){
//                        if(albumImages.get(i) == null){
//
//                            albumImages.set(i,data.getData());
//                            albumAdapter.notifyItemChanged(i);
//
//                            return;
//                        }
//                    }

                    break;

                case MAIN_IMAGE:

                    mainImageIv.setOnClickListener(null);

                    mainAddIv.setVisibility(View.GONE);
                    mainImageCloseIv.setVisibility(View.VISIBLE);

                    mainImageUri = data.getData();
                    Picasso.get().load(mainImageUri).fit().centerCrop().into(mainImageIv);


                    break;


            }


        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == IMAGE_STORAGE_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getImage(chosenImageType);
            }
        }

    }


    private void showProgressDialog() {
        if (progressDialogFragment == null) {
            progressDialogFragment = new ProgressDialogFragment();
        }
        progressDialogFragment.show(getSupportFragmentManager(), "progress");
    }


    @Override
    public void onClick(View v) {

        if (v.getId() == backIv.getId()) {

            onBackPressed();

        } else if (v.getId() == mainImageIv.getId()) {

            getImage(MAIN_IMAGE);

        } else if (v.getId() == mainImageCloseIv.getId()) {


            mainAddIv.setVisibility(View.VISIBLE);
            mainImageCloseIv.setVisibility(View.GONE);

            mainImageIv.setOnClickListener(this);

            mainImageUri = null;
            mainImageIv.setImageDrawable(null);

        } else if (v.getId() == imageInputNextBtn.getId()) {

            if (mainImageUri == null && previousMainImageURL == null) {
                Toast.makeText(this,
                        R.string.atleast_main_image, Toast.LENGTH_SHORT).show();
                return;
            }

            final Bundle newImagesBundle = new Bundle();

            if (mainImageUri == null && previousMainImageURL != null) {
                newImagesBundle.putString("mainImageURL", previousMainImageURL);
            } else if (mainImageUri != null) {
                newImagesBundle.putParcelable("mainImage", mainImageUri);
            }


            final List<Uri> newBannerImages = new ArrayList<>();

            for (Object bannerImage : bannerImages) {
                if (bannerImage != null) {
                    if (bannerImage instanceof Uri) {
                        newBannerImages.add((Uri) bannerImage);
                    }
                }
            }

            if (!newBannerImages.isEmpty()) {
                newImagesBundle.putParcelableArrayList("bannerImages", (ArrayList<? extends Parcelable>) newBannerImages);
            }

            final List<Uri> newAlbumImages = new ArrayList<>();

            for (Object albumImage : albumImages) {
                if (albumImage != null) {
                    if (albumImage instanceof Uri) {
                        newAlbumImages.add((Uri) albumImage);
                    }
                }
            }

            if (!newAlbumImages.isEmpty()) {
                newImagesBundle.putParcelableArrayList("albumImages", (ArrayList<? extends Parcelable>) newAlbumImages);
            }

            final Intent intent = new Intent(this, RestaurantInfoActivity.class);
            intent.putExtra("imagesBundle", newImagesBundle);
            intent.putExtra("addressMap", getIntent().getSerializableExtra("addressMap"));

            if (infoBundle != null && !infoBundle.isEmpty()) {
                intent.putExtra("infoBundle", infoBundle);
            }
            startActivity(intent);

//            finish();

        }

    }
}