package com.developers.wajbaty.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.developers.wajbaty.Models.User;
import com.developers.wajbaty.PartneredRestaurant.Activities.RestaurantLocationActivity;
import com.developers.wajbaty.R;
import com.developers.wajbaty.Utils.EmojiUtil;
import com.developers.wajbaty.Utils.GlobalVariables;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class VerifyAccountActivity extends AppCompatActivity {
    private static final String TAG = "VerifyAccountActivity";
    TextInputEditText codeEt;
    AppCompatButton verifyBtn, resendBtn;
    FirebaseAuth firebaseAuth;
    PhoneAuthProvider.ForceResendingToken forceResendingToken;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    String mVerificationId;
    FirebaseFirestore firebaseFirestore;
    User user;
    String defaultCode = "";
    String username, email, phone;
    int userType = 0;
    boolean fromSignin;

    private Map<String, Object> addressMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_account);

        intiViews();
        initItems();
        initClicks();

        username = getIntent().getStringExtra("username");
        email = getIntent().getStringExtra("email");
        phone = getIntent().getStringExtra("phoneNumber");
        userType = getIntent().getIntExtra("userType", 0);

/*        if (){

        }*/

        sendVerificationCode(phone);

    }

    private void initClicks() {
        verifyBtn.setOnClickListener(v -> {
            String code = codeEt.getText().toString().trim();
            if (code.isEmpty()) {
                codeEt.setError("Enter valid code");
                codeEt.requestFocus();
                return;
            }

            verifyCodeNumber(mVerificationId, code);
            /*if (phone.isEmpty()) {
                Toast.makeText(this, "Please Enter Phone Number..", Toast.LENGTH_SHORT).show();
            } else {
                register(phone);
                String code = codeEt.getText().toString().trim();
                if (code.isEmpty()){
                    Toast.makeText(this, "Please Enter Verification Code..", Toast.LENGTH_SHORT).show();
                } else {
                    verifyCodeNumber(mVerificationId, code);
                }
            }*/
        });

        resendBtn.setOnClickListener(v -> {
            sendVerificationCode(phone);
        });
    }

    private void initItems() {

        addressMap = (Map<String, Object>) getIntent().getSerializableExtra("addressMap");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                super.onCodeSent(verificationId, forceResendingToken);
                Log.d("ttt", verificationId);

                mVerificationId = verificationId;

                forceResendingToken = token;
                Toast.makeText(VerifyAccountActivity.this, "Verification Code Sent..", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
                String code = phoneAuthCredential.getSmsCode();
                if (code != null) {
                    codeEt.setText(code);
                    verifyCodeNumber(mVerificationId, code);
                }
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(VerifyAccountActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void sendVerificationCode(String mobile) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(mobile)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

    }

    private void verifyCodeNumber(String verificationId, String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void register(String phoneNumber) {
        PhoneAuthOptions authOptions = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumber)
                .setActivity(this)
                .setCallbacks(callbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(authOptions);
    }

    private void resendVerificationCode(String phone, PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthOptions authOptions = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phone)
                .setActivity(this)
                .setCallbacks(callbacks)
                .setForceResendingToken(token)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(authOptions);

    }

    void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(command -> {

                    if (getIntent().getBooleanExtra("Signin", false)) {
                        Toast.makeText(this, "Signin", Toast.LENGTH_SHORT).show();

                        firebaseFirestore.collection("Users")
                                .document(command.getUser().getUid())
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot userDoc) {

                                        userType = userDoc.getLong("type").intValue();

                                        if (userType == User.TYPE_ADMIN && (!userDoc.contains("myRestaurantID") ||
                                                userDoc.getString("myRestaurantID") == null)) {

                                            startActivity(new Intent(VerifyAccountActivity.this, RestaurantLocationActivity.class));

                                        } else {

                                            if (userType == User.TYPE_ADMIN) {
                                                GlobalVariables.setCurrentRestaurantId(userDoc.getString("myRestaurantID"));
                                            }

                                            startActivity(new Intent(VerifyAccountActivity.this, HomeActivity.class)
                                                    .putExtra("userType", userType)
                                                    .putExtra("addressMap", (Serializable) addressMap));
                                        }

                                        finish();
                                    }
                                });


                    } else {
//                        String phone = firebaseAuth.getCurrentUser().getPhoneNumber();
                        storeUser();
                    }

//                    startActivity(new Intent(this, AddImageProfileActivity.class));
                })
                .addOnFailureListener(command -> {
                    Toast.makeText(this, command.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void intiViews() {
        verifyBtn = findViewById(R.id.verify_code_btn);
        resendBtn = findViewById(R.id.resend_code_btn);
        codeEt = findViewById(R.id.et_code);
    }

    void storeUser() {
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {

                        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                        user = new User(currentUser.getUid(), username, email, phone,
                                null,
                                EmojiUtil.countryCodeToEmoji(defaultCode),
                                s,
                                userType);

                        firebaseFirestore.collection("Users")
                                .document(currentUser.getUid())
                                .set(user)
                                .addOnCompleteListener(command -> {
                                    if (userType != User.TYPE_ADMIN) {
                                        startActivity(new Intent(VerifyAccountActivity.this, AddImageProfileActivity.class)
                                                .putExtra("userType", userType)
                                                .putExtra("addressMap", (Serializable) addressMap));
                                    } else {

                                        startActivity(new Intent(VerifyAccountActivity.this, RestaurantLocationActivity.class));
                                    }
                                    Toast.makeText(VerifyAccountActivity.this, username, Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(command -> {
                                    Toast.makeText(VerifyAccountActivity.this, command.getMessage(), Toast.LENGTH_SHORT).show();
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(VerifyAccountActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}