package com.developers.wajbaty.Customer.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.developers.wajbaty.Fragments.ProgressDialogFragment;
import com.developers.wajbaty.Models.UserReview;
import com.developers.wajbaty.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class DeliveryDriverRatingFragment extends DialogFragment {

    private static final String DRIVER_ID = "driverID";

    private String driverID;
    private DeliveryDriverRatingListener deliveryDriverRatingListener;
    //views
    private ImageView driverRatingImageIv;
    private TextView driverRatingNameTv;
    private RatingBar driverRatingRatingBar;
    private EditText driverRatingNoteEd;
    private Button driverRatingSubmitBtn;
    //firebase
    private DocumentReference driverRef;
    public DeliveryDriverRatingFragment() {
    }

    public static DeliveryDriverRatingFragment newInstance(String driverID, DeliveryDriverRatingListener listener) {
        DeliveryDriverRatingFragment fragment = new DeliveryDriverRatingFragment();
        fragment.setDeliveryDriverRatingListener(listener);
        Bundle args = new Bundle();
        args.putString(DRIVER_ID, driverID);
        fragment.setArguments(args);
        return fragment;
    }

    public DeliveryDriverRatingListener getDeliveryDriverRatingListener() {
        return deliveryDriverRatingListener;
    }

    public void setDeliveryDriverRatingListener(DeliveryDriverRatingListener deliveryDriverRatingListener) {
        this.deliveryDriverRatingListener = deliveryDriverRatingListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            driverID = getArguments().getString(DRIVER_ID);
        }

        if (driverID != null) {
            driverRef = FirebaseFirestore.getInstance().collection("Users")
                    .document(driverID);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        requireDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_delivery_driver_rating, container, false);
        driverRatingImageIv = view.findViewById(R.id.driverRatingImageIv);
        driverRatingNameTv = view.findViewById(R.id.driverRatingNameTv);
        driverRatingRatingBar = view.findViewById(R.id.driverRatingRatingBar);
        driverRatingNoteEd = view.findViewById(R.id.driverRatingNoteEd);
        driverRatingSubmitBtn = view.findViewById(R.id.driverRatingSubmitBtn);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        driverRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot snapshot) {

                if (snapshot.exists()) {

                    if (snapshot.contains("imageURL")) {
                        final String imageURL = snapshot.getString("imageURL");
                        if (imageURL != null && !imageURL.isEmpty()) {

                            final CircularProgressDrawable progressDrawable = new CircularProgressDrawable(requireContext());
                            progressDrawable.setColorSchemeColors(ResourcesCompat.getColor(getResources(), R.color.orange, null));
                            progressDrawable.setStyle(CircularProgressDrawable.LARGE);
                            progressDrawable.start();

                            Picasso.get().load(imageURL).fit().centerCrop()
                                    .placeholder(progressDrawable).into(driverRatingImageIv);
                        }
                    }
                    driverRatingNameTv.setText(snapshot.getString("name"));
                }
            }
        });

        driverRatingSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (driverRatingRatingBar.getRating() != 0) {

                    ProgressDialogFragment progressDialog = new ProgressDialogFragment();
                    progressDialog.show(getChildFragmentManager(), "progressDialog");

                    float newRating = driverRatingRatingBar.getRating();
                    driverRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot snapshot) {

                            if (snapshot.exists()) {

                                double averageRating;

                                if (snapshot.contains("rating")) {
                                    averageRating = snapshot.getDouble("rating");
                                } else {
                                    averageRating = 0;
                                }

                                Log.d("rating", "averageRating: " + averageRating);
                                long totalRatings = 0;

                                if (snapshot.contains("totalRatings")) {
                                    totalRatings = snapshot.getLong("totalRatings");
                                }
                                Log.d("rating", "totalRatings: " + totalRatings);
                                double newAverageRating;

                                if (totalRatings != 0 && averageRating != 0) {
                                    newAverageRating = ((totalRatings * averageRating) + newRating) / (totalRatings + 1);
                                } else {
                                    newAverageRating = newRating;
                                }

                                Log.d("rating", "newAverageRating: " + newAverageRating);

                                long finalTotalRatings = totalRatings;
                                driverRef.update("totalRatings", totalRatings + 1,
                                        "rating", newAverageRating)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                final String note =
                                                        driverRatingNoteEd.getText().toString().trim();

                                                if (!note.isEmpty()) {

                                                    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                                                    if (user == null)
                                                        return;

                                                    final String currentUID = user.getUid();

                                                    final UserReview userReview = new UserReview(
                                                            currentUID,
                                                            (int) newRating,
                                                            System.currentTimeMillis(),
                                                            note);

                                                    driverRef.collection("Reviews")
                                                            .document(currentUID).set(userReview)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    progressDialog.dismiss();
                                                                    deliveryDriverRatingListener.onRatingSubmitted();
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {

                                                            driverRef.update("totalRatings", finalTotalRatings,
                                                                    "rating", averageRating);

                                                            Toast.makeText(requireContext(),
                                                                    "Rating driver failed!" +
                                                                            "Please try again", Toast.LENGTH_SHORT).show();
                                                            progressDialog.dismiss();

                                                        }
                                                    });

                                                } else {
                                                    progressDialog.dismiss();
                                                    deliveryDriverRatingListener.onRatingSubmitted();
                                                }

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        Toast.makeText(requireContext(),
                                                "Rating driver failed!" +
                                                        "Please try again", Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();

                                    }
                                });

                            }

                        }
                    });


                } else {

                    Toast.makeText(requireContext(),
                            "Please add choose the driver's rating before submitting!",
                            Toast.LENGTH_SHORT).show();

                }

            }
        });

    }

    public interface DeliveryDriverRatingListener {
        void onRatingSubmitted();
    }
}