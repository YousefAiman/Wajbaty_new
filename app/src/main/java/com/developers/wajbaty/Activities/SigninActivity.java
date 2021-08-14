package com.developers.wajbaty.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.developers.wajbaty.R;
import com.developers.wajbaty.Utils.EmojiUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SigninActivity extends AppCompatActivity {

    TextView registerTv;
    //TextInputEditText phoneEd, passwordEd;
    AppCompatButton signinBtn;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    EditText phoneEd;
    Spinner phoneSpinner;
    PhoneNumberUtil phoneNumberUtil;
    String defaultCode = "";
    String fullMobile;
    boolean fromSignin;

    private Map<String, Object> addressMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        initViews();
        iniItems();

        createCountryCodeSpinner();


        signinBtn.setOnClickListener(v -> {

            String phone = phoneEd.getText().toString().trim();

            if (!checkPhoneNumber(phone, phoneSpinner.getSelectedItem().toString().split("\\+")[1])) {

//            progressDialog.dismiss();

//                    Toast.makeText(this, "Please verify the phone number" + "Invalid phone number!", Toast.LENGTH_SHORT).show();
                Toast.makeText(this, "رقم الهاتف غير صالح! " + "الرجاء التأكد من الرقم", Toast.LENGTH_LONG).show();

                return;
            }

            fullMobile = phoneSpinner.getSelectedItem().toString() + phone;
            fullMobile = fullMobile.substring(fullMobile.indexOf("+"));
            Log.d("ttt", fullMobile);

            firebaseFirestore.collection("Users").whereEqualTo("phoneNumber", fullMobile)
                    .limit(1)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (!task.getResult().isEmpty()) {
                            Intent intent = new Intent(this, VerifyAccountActivity.class);
                            intent.putExtra("phoneNumber", fullMobile);
                            intent.putExtra("Signin", true);
                            intent.putExtra("addressMap", (Serializable) addressMap);
                            startActivity(intent);
                        } else {
                            Toast.makeText(SigninActivity.this, "Phone Number does't registered", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(command -> {
                        Toast.makeText(this, command.getMessage(), Toast.LENGTH_SHORT).show();
                    });

            FirebaseUser user = firebaseAuth.getCurrentUser();
//            if (user.getUid() != null) {
/*                if (user.getPhoneNumber().equals(phoneEd.getText())) {
                    startActivity(new Intent(this, MainActivity.class));
                } else {
                    Toast.makeText(this, "Wrong Phone Number!", Toast.LENGTH_SHORT).show();
                }*/
/*            } else {
                Toast.makeText(this, "Phone Number does't registered", Toast.LENGTH_SHORT).show();
            }*/
        });

        registerTv.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });


    }

    private void iniItems() {

        addressMap = (Map<String, Object>) getIntent().getSerializableExtra("addressMap");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        phoneNumberUtil = PhoneNumberUtil.getInstance();

    }

    private void initViews() {
//        phoneEd = findViewById(R.id.et_phoneNumber_sign);
//        passwordEd = findViewById(R.id.et_password_sign);

        phoneEd = findViewById(R.id.et_phoneNumber_sign);
        phoneSpinner = findViewById(R.id.phoneSpinner);
        signinBtn = findViewById(R.id.signin_btn);
        registerTv = findViewById(R.id.register_tv);
    }

    void createCountryCodeSpinner() {

        new Thread(() -> {

            List<String> supportedCountryCodes = new ArrayList<>(phoneNumberUtil.getSupportedRegions());


            String defaultCode;


            if (addressMap != null && addressMap.containsKey("countryCode")) {
                defaultCode = (String) addressMap.get("countryCode");
            } else {
                defaultCode = Locale.getDefault().getCountry();
            }

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
        }).start();
    }

    boolean checkPhoneNumber(String number, String code) {

        final Phonenumber.PhoneNumber newNum = new Phonenumber.PhoneNumber();

        newNum.setCountryCode(Integer.parseInt(code)).setNationalNumber(Long.parseLong(number));

        return phoneNumberUtil.isValidNumber(newNum);
    }

}