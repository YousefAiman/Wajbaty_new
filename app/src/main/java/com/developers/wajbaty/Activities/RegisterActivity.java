package com.developers.wajbaty.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.developers.wajbaty.Models.User;
import com.developers.wajbaty.R;
import com.developers.wajbaty.Utils.EmojiUtil;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    TextInputEditText usernameEd, emailEd, passwordEd, conf_passwordEd;
    EditText phoneNumberEd;
    Spinner phoneSpinner;
    AppCompatButton registerBtn;
    TextView signinTv;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    PhoneAuthProvider.ForceResendingToken forceResendingToken;
    PhoneNumberUtil phoneNumberUtil;
    String phone;
    CollectionReference collectionReference;
    RadioGroup radioGroup;
    RadioButton selectedRadioButton;
    RadioButton customerRadioButton, restaurantRadioButton, deliveryRadioButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        initItems();
        initClicks();

        createCountryCodeSpinner();
    }

    private void initClicks() {
        registerBtn.setOnClickListener(v -> {
            String username = usernameEd.getText().toString().trim();
            String email = emailEd.getText().toString().trim();
            String password = passwordEd.getText().toString().trim();
            String confPassword = conf_passwordEd.getText().toString().trim();

            String phone = phoneNumberEd.getText().toString().trim();
            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confPassword.isEmpty() || phone.isEmpty()) {

//                Toast.makeText(this, "Please fill all fields..", Toast.LENGTH_SHORT).show();
                usernameEd.setError("Field can't be empty");
                emailEd.setError("Field can't be empty");


            } else if (!validateEmail(email)) {
//                Toast.makeText(this, "Invalid Email", Toast.LENGTH_SHORT).show();

            } else if (!validatePassword(password)) {
//                Toast.makeText(this, "Invalid Password", Toast.LENGTH_SHORT).show();
//                passwordEd.setError("Invalid Password");
//                Toast.makeText(this, "Password must contain mix of upper and lower case letters as well as digits and one special character(4-20)", Toast.LENGTH_SHORT).show();

            } else if (!confPassword.equals(password)) {
                Toast.makeText(RegisterActivity.this, "password doesn't compatible", Toast.LENGTH_SHORT).show();
            } else {

                if (!checkPhoneNumber(phone, phoneSpinner.getSelectedItem().toString().split("\\+")[1])) {

//            progressDialog.dismiss();

//                    Toast.makeText(this, "Please verify the phone number" + "Invalid phone number!", Toast.LENGTH_SHORT).show();
                    Toast.makeText(this, "رقم الهاتف غير صالح! " + "الرجاء التأكد من الرقم", Toast.LENGTH_LONG).show();

                    return;
                }

                String fullMobile = phoneSpinner.getSelectedItem().toString() + phone;
                fullMobile = fullMobile.substring(fullMobile.indexOf("+"));
                Log.d("ttt", fullMobile);


/*                int id = radioGroup.getCheckedRadioButtonId();
                
                if (id == -1) {
                    Toast.makeText(this,"Please select Type", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this,"1", Toast.LENGTH_SHORT).show();

                }*/

                int id = radioGroup.getCheckedRadioButtonId();

                int userType;
                if(id == customerRadioButton.getId()){
                    userType = User.TYPE_CUSTOMER;
                }else if(id == restaurantRadioButton.getId()){
                    userType = User.TYPE_ADMIN;
                }else  if(id == deliveryRadioButton.getId()){
                    userType = User.TYPE_DELIVERY;
                }

//                selectType();

                Intent intent = new Intent(this, VerifyAccountActivity.class);
                intent.putExtra("phoneNumber", fullMobile);
                intent.putExtra("phoneNumber", fullMobile);
                startActivity(intent);

            }

//            String fullMobile = phoneSpinner.getSelectedItem().toString() + phone;
//            fullMobile = fullMobile.substring(fullMobile.indexOf("+"));
//            Log.d("ttt", fullMobile);

//            int id = radioGroup.getCheckedRadioButtonId();
//            selectedRadioButton = (RadioButton)findViewById(id);
//            Toast.makeText(this, selectedRadioButton.getText(), Toast.LENGTH_SHORT).show();


//            onRadioButtonClicked(v);

//            selectType();
//
//            Intent intent = new Intent(this, VerifyAccountActivity.class);
//            intent.putExtra("phoneNumber", fullMobile);
//            startActivity(intent);

        });


    }

    private void initItems() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        phoneNumberUtil = PhoneNumberUtil.getInstance();
    }

    private void initViews() {
        usernameEd = findViewById(R.id.et_username);
        emailEd = findViewById(R.id.et_email);
        passwordEd = findViewById(R.id.et_password);
        conf_passwordEd = findViewById(R.id.et_conf_password);
        phoneNumberEd = findViewById(R.id.et_phoneNumber);
        phoneSpinner = findViewById(R.id.phoneSpinner);
        registerBtn = findViewById(R.id.register_btn);
        signinTv = findViewById(R.id.signin_tv);
        radioGroup = findViewById(R.id.radioGroup);
        customerRadioButton = findViewById(R.id.customerRadioButton);
        restaurantRadioButton = findViewById(R.id.restaurantRadioButton);
        deliveryRadioButton = findViewById(R.id.deliveryRadioButton);
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = selectedRadioButton.isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.customerRadioButton:
                if (checked)
                    Toast.makeText(this, "زبون", Toast.LENGTH_SHORT).show();
                    break;
            case R.id.restaurantRadioButton:
                if (checked)
                    Toast.makeText(this, "مطعم", Toast.LENGTH_SHORT).show();
                    break;
            case R.id.deliveryRadioButton:
                if (checked)
                    Toast.makeText(this, "ديليفري", Toast.LENGTH_SHORT).show();
                    break;
        }
    }

    private void register(String phoneNumber, String password) {


        PhoneAuthOptions authOptions = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumber)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {

                    }

                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(s, forceResendingToken);

                    }
                })
                .build();
        PhoneAuthProvider.verifyPhoneNumber(authOptions);


    }


    boolean validateUsername() {
        String username = usernameEd.getText().toString().trim();
        if (username.isEmpty()) {
            usernameEd.setError("Field can;t be empty");
            return false;
        } else if (usernameEd.length() > 15) {
            usernameEd.setError("Username too long");
            return false;
        } else {
            usernameEd.setError(null);
            return true;
        }
    }

    public boolean validateEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    public boolean validatePassword(String password) {
        Pattern pattern;
        Matcher matcher;
        final String PASSWORD_PATTERN = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%]).{6,20})";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

//        if (password.isEmpty()){
//
//        }

        return matcher.matches();


    }

    void createCountryCodeSpinner() {
        new Thread(() -> {

            List<String> supportedCountryCodes = new ArrayList<>(phoneNumberUtil.getSupportedRegions());

            String defaultCode = "";

            defaultCode = defaultCode.toUpperCase();

            final String defaultSpinnerChoice = EmojiUtil.countryCodeToEmoji(defaultCode)
                    + " +" + phoneNumberUtil.getCountryCodeForRegion(defaultCode);


            final List<String> spinnerArray = new ArrayList<>(supportedCountryCodes.size());

            for (String code : supportedCountryCodes) {

                spinnerArray.add(EmojiUtil.countryCodeToEmoji(code)
                        + " +" + phoneNumberUtil.getCountryCodeForRegion(code));
            }

            supportedCountryCodes = null;

            Collections.sort(spinnerArray, new Comparator<String>() {
                @Override
                public int compare(String s, String t1) {
                    return extractCode(s) - extractCode(t1);
                }

                int extractCode(String s) {
                    return Integer.parseInt(s.split("\\+")[1]);
                }
            });


            Log.d("ttt", "list size: " + spinnerArray.size());
            if (this != null) {

                final ArrayAdapter<String> ad
                        = new ArrayAdapter<>(
                        this,
                        R.layout.spinner_item_layout,
                        spinnerArray);

                ad.setDropDownViewResource(R.layout.spinner_item_layout);

                phoneSpinner.post(() -> {
                    phoneSpinner.setAdapter(ad);

                    phoneSpinner.setSelection(spinnerArray.indexOf(defaultSpinnerChoice));
                });
            }
        }).start();
    }

    boolean checkPhoneNumber(String number, String code) {

        final Phonenumber.PhoneNumber newNum = new Phonenumber.PhoneNumber();

        newNum.setCountryCode(Integer.parseInt(code)).setNationalNumber(Long.parseLong(number));

        return phoneNumberUtil.isValidNumber(newNum);
    }

    void selectType (){
        if (customerRadioButton.isChecked()) {
//            startActivity(new Intent(this, CustomerHomeActivity.class));
//            Toast.makeText(this, "زبون", Toast.LENGTH_SHORT).show();
        } else if (restaurantRadioButton.isChecked()) {

//            Toast.makeText(this, "مطعم", Toast.LENGTH_SHORT).show();
        } else if (deliveryRadioButton.isChecked()) {
//            Toast.makeText(this, "ديليفري", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Please select Type", Toast.LENGTH_SHORT).show();
        }
    }


}
