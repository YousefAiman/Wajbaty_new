package com.developers.wajbaty.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.developers.wajbaty.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;

public class VerifyAccountActivity extends AppCompatActivity {
    private static final String TAG = "VerifyAccountActivity";
    TextInputEditText codeEt;
    AppCompatButton verifyBtn, resendBtn;
    FirebaseAuth firebaseAuth;
    PhoneAuthProvider.ForceResendingToken forceResendingToken;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    String mVerificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_account);

        intiViews();
        initItems();
        initClicks();

        String phone = getIntent().getStringExtra("phoneNumber");
        sendVerificationCode(phone);

    }

    private void initClicks() {
        verifyBtn.setOnClickListener(v -> {
            String code = codeEt.getText().toString().trim();
            if (code.isEmpty()){
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

//            verifyCodeNumber(mVerificationId, code);

        });
    }

    private void initItems() {
        firebaseAuth = FirebaseAuth.getInstance();
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
                if (code != null){
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
                    String phone = firebaseAuth.getCurrentUser().getPhoneNumber();



                    Toast.makeText(this, "Logged in as " + phone, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, AddImageProfileActivity.class));
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
}