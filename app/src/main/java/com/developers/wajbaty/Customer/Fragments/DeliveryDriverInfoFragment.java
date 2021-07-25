package com.developers.wajbaty.Customer.Fragments;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.developers.wajbaty.Models.DeliveryDriver;
import com.developers.wajbaty.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;


public class DeliveryDriverInfoFragment extends DialogFragment implements View.OnClickListener {

    private static final String DELIVERY_DRIVER_ID = "deliveryDriverID";

    private String deliveryDriverID;
    private DeliveryListener deliveryListener;

    //views
    private ImageView deliveryDriverImageIv;
    private TextView deliveryDriverNameTv;
    private RatingBar deliveryDriverRatingBar;
    private Button deliveryDriverStartDeliveryBtn;
    private Button deliveryDriverCancelDeliveryBtn;


    public interface DeliveryListener{
        void startDelivery();
        void cancelDelivery();
    }

    public void setDeliveryListener(DeliveryListener deliveryListener) {
        this.deliveryListener = deliveryListener;
    }

    public DeliveryDriverInfoFragment() {
        // Required empty public constructor
    }


    public static DeliveryDriverInfoFragment newInstance(String deliveryDriverID,DeliveryDriverInfoFragment.DeliveryListener deliveryListener) {
        DeliveryDriverInfoFragment fragment = new DeliveryDriverInfoFragment();
        Bundle args = new Bundle();
        args.putSerializable(DELIVERY_DRIVER_ID, deliveryDriverID);
        fragment.setDeliveryListener(deliveryListener);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            deliveryDriverID =  getArguments().getString(DELIVERY_DRIVER_ID);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        requireDialog().getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_delivery_driver_info, container, false);

        deliveryDriverImageIv = view.findViewById(R.id.deliveryDriverImageIv);
        deliveryDriverNameTv = view.findViewById(R.id.deliveryDriverNameTv);
        deliveryDriverRatingBar = view.findViewById(R.id.deliveryDriverRatingBar);
        deliveryDriverStartDeliveryBtn = view.findViewById(R.id.deliveryDriverStartDeliveryBtn);
        deliveryDriverCancelDeliveryBtn = view.findViewById(R.id.deliveryDriverCancelDeliveryBtn);

        deliveryDriverStartDeliveryBtn.setOnClickListener(this);
        deliveryDriverCancelDeliveryBtn.setOnClickListener(this);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseFirestore.getInstance().collection("DeliveryDrivers")
                .document(deliveryDriverID)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if(documentSnapshot.exists()){
                    Picasso.get().load(documentSnapshot.getString("imageURL")).fit().centerCrop().into(deliveryDriverImageIv);
                    deliveryDriverNameTv.setText(documentSnapshot.getString("name"));
                    deliveryDriverRatingBar.setRating(documentSnapshot.getDouble("rating").floatValue());
                }

            }
        });

    }

    @Override
    public void onClick(View v) {

        if(v.getId() == deliveryDriverStartDeliveryBtn.getId()){

            dismiss();
            deliveryListener.startDelivery();

        }else if(v.getId() == deliveryDriverCancelDeliveryBtn.getId()){

            dismiss();
            deliveryListener.cancelDelivery();

        }

    }
}