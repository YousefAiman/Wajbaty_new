package com.developers.wajbaty.PartneredRestaurant.Fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.developers.wajbaty.Fragments.ProgressDialogFragment;
import com.developers.wajbaty.Models.MenuItem;
import com.developers.wajbaty.Models.offer.DiscountOffer;
import com.developers.wajbaty.Models.offer.Offer;
import com.developers.wajbaty.R;
import com.developers.wajbaty.Utils.TimeFormatter;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class DiscountDialogFragment extends DialogFragment implements View.OnClickListener {

    private static final String MENU_ITEM = "menuItem";

    private MenuItem.MenuItemSummary menuItem;

    //views
    private TextView discountDialogMealTv;
    private EditText discountDialogPriceTv;
    private TextView discountDialogTimeTv;
    private Button discountDialogConfirmBtn;

    //discount time
    private long untilTimeInMillis;

    private DiscountDialogInterface discountDialogInterface;
    private ProgressDialogFragment progressDialog;

    public DiscountDialogFragment() {
    }

    public static DiscountDialogFragment newInstance(MenuItem.MenuItemSummary menuItem) {
        DiscountDialogFragment fragment = new DiscountDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(MENU_ITEM, menuItem);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            menuItem = (MenuItem.MenuItemSummary) getArguments().getSerializable(MENU_ITEM);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        requireDialog().getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (getParentFragment() != null && getParentFragment() instanceof DiscountDialogInterface) {
            discountDialogInterface = (DiscountDialogInterface) getParentFragment();
        }

    }

//    @NonNull
//    @Override
//    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
//        requireDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.MATCH_PARENT);
//        return super.onCreateDialog(savedInstanceState);
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_discount_dialog, container, false);
        discountDialogMealTv = view.findViewById(R.id.discountDialogMealTv);
        discountDialogPriceTv = view.findViewById(R.id.discountDialogPriceTv);
        discountDialogTimeTv = view.findViewById(R.id.discountDialogTimeTv);
        discountDialogConfirmBtn = view.findViewById(R.id.discountDialogConfirmBtn);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        discountDialogMealTv.setText("Discount on " + menuItem.getName() + " - " +
                menuItem.getPrice() + menuItem.getCurrency());

        discountDialogTimeTv.setOnClickListener(this);
        discountDialogConfirmBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        if (v.getId() == discountDialogConfirmBtn.getId()) {

            addDiscountOffer();

        } else if (v.getId() == discountDialogTimeTv.getId()) {

            pickDiscountEndTime();

        }

    }

    private void pickDiscountEndTime() {


        final Calendar currentDate = Calendar.getInstance(Locale.getDefault());

        if (untilTimeInMillis != 0) {
            currentDate.setTimeInMillis(untilTimeInMillis);
        }

        final int currentYear = currentDate.get(Calendar.YEAR),
                currentMonth = currentDate.get(Calendar.MONTH),
                currentDay = currentDate.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog StartTime = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                Calendar newDate = Calendar.getInstance(Locale.getDefault());
                newDate.set(year, monthOfYear, dayOfMonth);

                if (currentYear >= year && currentMonth >= monthOfYear && currentDay >= dayOfMonth) {

                    TimePickerDialog mTimePicker = new TimePickerDialog(requireContext(), (timePicker, selectedHour, selectedMinute) -> {

                        final Calendar calendar = Calendar.getInstance(Locale.getDefault());
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        calendar.set(Calendar.HOUR, selectedHour);
                        calendar.set(Calendar.MINUTE, selectedMinute);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);

                        if (calendar.getTimeInMillis() > currentDate.getTimeInMillis()) {

                            untilTimeInMillis = calendar.getTimeInMillis();

                            String format =
                                    TimeFormatter.formatWithPattern(calendar.getTimeInMillis(),
                                            TimeFormatter.MONTH_DAY_HOUR_MINUTE);


                            discountDialogTimeTv.setText(new SimpleDateFormat(format, Locale.getDefault())
                                    .format(calendar.getTime()));

                        } else {

                            Toast.makeText(requireContext(),
                                    "The discount end time can't be at the specified time",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), true);

                    mTimePicker.setTitle("Select Time");
                    mTimePicker.show();

                } else {

                    Toast.makeText(requireContext(),
                            "The discount end time can't be at the specified time",
                            Toast.LENGTH_SHORT).show();

                }


            }

        }, currentYear, currentMonth, currentDay);
        StartTime.show();


    }

    private void addDiscountOffer() {

        final String newPriceString = discountDialogPriceTv.getText().toString().trim();

        if (newPriceString.isEmpty()) {
            Toast.makeText(requireContext(),
                    "Please add the new discounted price!", Toast.LENGTH_SHORT).show();
            return;
        }

        final float newPrice = Float.parseFloat(newPriceString);

        if (newPrice >= menuItem.getPrice()) {
            Toast.makeText(requireContext(),
                    "Discount price must be lower than the original menu item price!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (untilTimeInMillis == 0) {
            Toast.makeText(requireContext(),
                    "Please add the discount end date", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog = new ProgressDialogFragment();
        progressDialog.show(getChildFragmentManager(), "progress");

        if (menuItem.isDiscounted()) {

            final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(requireContext());
            alertBuilder.setTitle("Are you sure you want to add this discount?");
            alertBuilder.setMessage("doing this discard the previous discount!");
            alertBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    discountMenuItem(newPrice);
                }
            });

        } else {

            discountMenuItem(newPrice);

        }

    }

    private void discountMenuItem(float newPrice) {

        final String offerId = UUID.randomUUID().toString();


        final long startTime = System.currentTimeMillis();

        final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        Map<String, Object> discountMap = new HashMap<>();
        discountMap.put("discountedPrice", newPrice);
        discountMap.put("startedAt", startTime);
        discountMap.put("endsAt", untilTimeInMillis);

        firestore.collection("MenuItems").document(menuItem.getID())
                .update("isDiscounted", true,
                        "discountMap", discountMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        firestore.collection("PartneredRestaurant")
                                .document(menuItem.getRestaurantId()).get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot snapshot) {

                                        if (snapshot.exists()) {

                                            final GeoPoint geoPoint = new GeoPoint(snapshot.getDouble("lat"),
                                                    snapshot.getDouble("lng"));

                                            final DiscountOffer discountOffer = new DiscountOffer(
                                                    offerId,
                                                    menuItem.getRestaurantId(),
                                                    menuItem.getID(),
                                                    Offer.MENU_ITEM_DISCOUNT,
                                                    menuItem.getImageUrls().get(0),
                                                    menuItem.getName(),
                                                    startTime,
                                                    untilTimeInMillis,
                                                    geoPoint,
                                                    menuItem.getPrice(),
                                                    newPrice,
                                                    GeoFireUtils.getGeoHashForLocation(
                                                            new GeoLocation(geoPoint.getLatitude(),
                                                                    geoPoint.getLongitude())));


                                            snapshot.getReference()
                                                    .collection("Offers")
                                                    .document(offerId).set(discountOffer)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {

                                                            progressDialog.dismiss();

                                                            dismiss();

                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                }
                                            });


                                        }

                                    }
                                });

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(requireContext(),
                        "Adding discount failed! please try again",
                        Toast.LENGTH_SHORT).show();
            }
        });

    }

    public interface DiscountDialogInterface {

        void onMenuItemDiscounted(int position, Map<String, Object> discountMap);
    }
}