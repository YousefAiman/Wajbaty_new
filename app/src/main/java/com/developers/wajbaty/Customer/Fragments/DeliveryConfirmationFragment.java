package com.developers.wajbaty.Customer.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.developers.wajbaty.R;


public class DeliveryConfirmationFragment extends DialogFragment implements View.OnClickListener {

    private static final String DRIVER_NAME = "driverName";
    private static DeliveryConfirmationListener deliveryConfirmationListener;
    private String driverName;
    //views
    private TextView confirmDeliveryTitleTv;
    private Button confirmDeliveryBtn, denyDeliveryConfirmationBtn;

    public DeliveryConfirmationFragment() {
        // Required empty public constructor
    }

    public static DeliveryConfirmationFragment newInstance(DeliveryConfirmationListener deliveryConfirmationListener, String driverName) {
        DeliveryConfirmationFragment fragment = new DeliveryConfirmationFragment();
        DeliveryConfirmationFragment.deliveryConfirmationListener = deliveryConfirmationListener;
        Bundle args = new Bundle();
        args.putString(DRIVER_NAME, driverName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            driverName = getArguments().getString(DRIVER_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_delivery_confirmation, container, false);

        confirmDeliveryTitleTv = view.findViewById(R.id.confirmDeliveryTitleTv);
        confirmDeliveryBtn = view.findViewById(R.id.confirmDeliveryBtn);
        denyDeliveryConfirmationBtn = view.findViewById(R.id.denyDeliveryConfirmationBtn);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        confirmDeliveryTitleTv.setText(confirmDeliveryTitleTv.getText().toString().concat(driverName));
        confirmDeliveryBtn.setOnClickListener(this);
        denyDeliveryConfirmationBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        if (v.getId() == confirmDeliveryBtn.getId()) {

            dismiss();
            deliveryConfirmationListener.onDeliveryConfirmed();

        } else if (v.getId() == denyDeliveryConfirmationBtn.getId()) {

            dismiss();
            deliveryConfirmationListener.onDeliveryDenied();

        }

    }

    public interface DeliveryConfirmationListener {
        void onDeliveryConfirmed();

        void onDeliveryDenied();
    }
}