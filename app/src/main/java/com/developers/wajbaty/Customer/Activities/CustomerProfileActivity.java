package com.developers.wajbaty.Customer.Activities;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import com.developers.wajbaty.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class CustomerProfileActivity extends AppCompatActivity {
    TextView usernameTv, phoneTv, emailTv;
    AppCompatButton editBtn;
    FirebaseFirestore firebaseFirestore;
    DocumentReference documentReference;
    String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    Toolbar profileToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_profile);

        initViews();

        profileToolbar.setNavigationOnClickListener(v -> finish());

        firebaseFirestore = FirebaseFirestore.getInstance();
        documentReference = firebaseFirestore.collection("Users").document(currentUid);

        editBtn.setOnClickListener(v -> {
        });
    }

    private void initViews() {
        usernameTv = findViewById(R.id.usernameTv);
        phoneTv = findViewById(R.id.phoneTv);
        emailTv = findViewById(R.id.emailTv);
        editBtn = findViewById(R.id.edit_profile_btn);
        profileToolbar = findViewById(R.id.profileToolbar);
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

                    usernameTv.setText(username);
                    phoneTv.setText(phone);
                    emailTv.setText(email);

                } else {
                    Toast.makeText(CustomerProfileActivity.this, "No Profile", Toast.LENGTH_SHORT).show();
                }

            }
        })
                .addOnFailureListener(command -> {
                    Toast.makeText(this, command.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}