package com.developers.wajbaty.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.developers.wajbaty.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class AddImageProfileActivity extends AppCompatActivity {

    ImageView add_Im, icon_Im;
    String cameraImageFilePath = "";

    int REQUEST_IMAGE_CAPTURE = 1;
    int IMAGE_PERMISSION = 2;
    int PICK_CAMERA = 3;
    int PICK_IMAGE = 4;

    FirebaseFirestore firebaseFirestore;
    StorageReference storageReference;
    Uri imageUri;
    Uri uri;

    private Map<String, Object> addressMap;
    private int userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_image_profile);

        addressMap = (Map<String, Object>) getIntent().getSerializableExtra("addressMap");
        userType = getIntent().getIntExtra("userType", 0);

        add_Im = findViewById(R.id.add_Im);
        icon_Im = findViewById(R.id.icon_Im);


        CardView cardView = findViewById(R.id.Iv_card);
        cardView.setOnClickListener(v -> {
//            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//            startActivityIfNeeded(intent, REQUEST_IMAGE_CAPTURE);

            final CharSequence[] options = {"take_photo", "choose_from_gallery", "cancel"};

            AlertDialog.Builder builder = new AlertDialog.Builder(AddImageProfileActivity.this);
            builder.setTitle("add_photo");
            builder.setItems(options, (dialog, which) -> {
                if (options[which].equals("take_photo")) {
                    getCamera();
                } else if (options[which].equals("choose_from_gallery")) {

                    openGallery();
                } else if (options[which].equals("cancel")) {
                    dialog.dismiss();
                }
            }).show();

        });

        AppCompatButton nextBtn = findViewById(R.id.next_Im_btn);
        nextBtn.setOnClickListener(v -> {

            firebaseFirestore = FirebaseFirestore.getInstance();
            storageReference = FirebaseStorage.getInstance().getReference();

//            startActivity(new Intent(this, HomeActivity.class));

            uploadImage();

        });

        TextView skip = findViewById(R.id.skip_Tv);
        skip.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class)
                    .putExtra("userType", userType)
                    .putExtra("addressMap", (Serializable) addressMap));
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_CAMERA && resultCode == RESULT_OK) {
            Log.d("ttt", "getting camera result");

            if (cameraImageFilePath != null && !cameraImageFilePath.isEmpty()) {
                uri = Uri.fromFile(new File(cameraImageFilePath));

                if (uri == null) {
                    Toast.makeText(AddImageProfileActivity.this,
                            "فشل !", Toast.LENGTH_SHORT).show();
                    return;
                }

                Picasso.get().load(uri).fit().centerCrop().into(add_Im);

            } else {
                Toast.makeText(AddImageProfileActivity.this,
                        "فشل !", Toast.LENGTH_SHORT).show();
            }


        } else if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            uri = data.getData();
            if (uri != null) {
                Picasso.get().load(uri).fit().centerCrop().into(add_Im);
                icon_Im.setVisibility(View.INVISIBLE);
            } else {
                Toast.makeText(this, "no image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    void openGallery() {
        if (ActivityCompat.checkSelfPermission(AddImageProfileActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, IMAGE_PERMISSION);
            }
            return;
        }
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        intent.putExtra();
        startActivityIfNeeded(intent, PICK_IMAGE);

    }

    void getCamera() {

        if (ActivityCompat.checkSelfPermission(AddImageProfileActivity.this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_IMAGE_CAPTURE);
            }
            return;
        }

//        if (WifiUtil.checkWifiConnection(this)) {


        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(
                        this,
                        "com.developers.wajbaty.provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityIfNeeded(takePictureIntent, PICK_CAMERA);
                icon_Im.setVisibility(View.INVISIBLE);

            }
        }
//        }
    }


    private File createImageFile() throws IOException {
        final String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
                .format(new Date());

        final String imageFileName = "JPEG" + timeStamp + "_";

        final File image = File.createTempFile(
                imageFileName,
                ".jpg",
                getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        );

        // Save a file: path for use with ACTION_VIEW intents
        cameraImageFilePath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCamera();
            } else {
                Toast.makeText(this, "هذا التطبيق يحتاج للوصول إلى الكاميرا!",
                        Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == IMAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "هذا التطبيق يحتاج للوصول إلى الاستوديو!",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void uploadImage() {
        if (uri != null) {
//                dialog.setTitle("Uploading...");
//                dialog.show();
            final StorageReference storageRef2 = storageReference.child("images/" + UUID.randomUUID().toString());

            storageRef2.putFile(uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                                dialog.dismiss();
                            Toast.makeText(AddImageProfileActivity.this, "Upload Successfully", Toast.LENGTH_SHORT).show();

                                    /*Upload upload = new Upload(photoNameEd.getText().toString().trim(), taskSnapshot.getMetadata().getReference().getDownloadUrl().toString());
                                    String id = databaseRef.push().getKey();
                                    databaseRef.child(id).setValue(upload);*/

                            storageRef2.getDownloadUrl()
                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {

                                            final Map<String, Object> map = new HashMap<>();
                                            long currentTime = System.currentTimeMillis() / 1000;
                                            map.put("imageURL", uri.toString());

                                            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

                                            firebaseFirestore.collection("Users")
                                                    .document(firebaseAuth.getCurrentUser().getUid())
                                                    .update("imageURL", uri.toString())
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            startActivity(new Intent(AddImageProfileActivity.this, HomeActivity.class)
                                                                    .putExtra("userType", userType)
                                                                    .putExtra("addressMap", (Serializable) addressMap));
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(AddImageProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });

                                        }
                                    });


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AddImageProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
//                                dialog.setMessage("Uploaded " + (int) progress + " %");
                        }
                    });


        } else {
            Toast.makeText(this, "Choose the photo you want to upload", Toast.LENGTH_SHORT).show();
        }
//        } else {
//            Toast.makeText(this, "not image", Toast.LENGTH_SHORT).show();
//        }
    }
}