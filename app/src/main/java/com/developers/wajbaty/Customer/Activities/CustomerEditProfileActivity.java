package com.developers.wajbaty.Customer.Activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;

import com.developers.wajbaty.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

public class CustomerEditProfileActivity extends AppCompatActivity {
    AppCompatEditText usernameEd, phoneEd, emailEd;
    AppCompatButton saveBtn;
    FirebaseFirestore firebaseFirestore;
    DocumentReference documentReference;
    String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    Toolbar editProfileToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_edit_profile);

        initViews();

        editProfileToolbar.setNavigationOnClickListener(v -> finish());

        firebaseFirestore = FirebaseFirestore.getInstance();

        documentReference = firebaseFirestore.collection("Users").document(currentUid);

        saveBtn.setOnClickListener(v -> {
            updateProfile();
        });
    }

    private void initViews() {
        usernameEd = findViewById(R.id.et_username_upd);
        phoneEd = findViewById(R.id.et_phone_upd);
        emailEd = findViewById(R.id.et_email_upd);
        saveBtn = findViewById(R.id.save_profile_btn);
        editProfileToolbar = findViewById(R.id.editProfileToolbar);
    }

    @Override
    protected void onStart() {
        super.onStart();

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.getResult().exists()) {
                    String username = task.getResult().getString("name");
                    String phone = task.getResult().getString("phoneNumber");
                    String email = task.getResult().getString("email");

                    usernameEd.setText(username);
                    phoneEd.setText(phone);
                    emailEd.setText(email);
                } else {
                    Toast.makeText(CustomerEditProfileActivity.this, "No Profile", Toast.LENGTH_SHORT).show();
                }

            }
        })
                .addOnFailureListener(command -> {
                    Toast.makeText(this, command.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateProfile() {
        String username = usernameEd.getText().toString();
        String phone = phoneEd.getText().toString();
        String email = emailEd.getText().toString();

        DocumentReference documentReference = firebaseFirestore.collection("Users").document(currentUid);
        firebaseFirestore.runTransaction(new Transaction.Function<Object>() {
            @Nullable
            @Override
            public Object apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                transaction.update(documentReference, "name", username);
                transaction.update(documentReference, "phoneNumber", phone);
                transaction.update(documentReference, "email", email);

                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Object>() {
            @Override
            public void onSuccess(Object o) {
                Toast.makeText(CustomerEditProfileActivity.this, "updated", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CustomerEditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}