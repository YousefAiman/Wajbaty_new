package com.developers.wajbaty.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.developers.wajbaty.Models.Delivery;
import com.developers.wajbaty.R;
import com.developers.wajbaty.Utils.TimeFormatter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class DriverDeliveriesAdapter extends RecyclerView.Adapter<DriverDeliveriesAdapter.DeliveryRequestVH> {

    private static final int TYPE_REQUESTED_DELIVERY = 1,TYPE_CURRENT_DELIVERY = 2;
    private String currentSummaryID;

    private final ArrayList<Delivery> deliveries;
    private final CollectionReference customerRef;
    private final HashMap<String,String> userImageURLsMap,userUserNamesMap;
    private static DriverDeliveriesListener driverDeliveriesListener;


    public interface DriverDeliveriesListener{

        void onDeliveryClicked(int position);

    }

    public DriverDeliveriesAdapter(ArrayList<Delivery> deliveries,DriverDeliveriesListener driverDeliveriesListener) {

        this.deliveries = deliveries;
        DriverDeliveriesAdapter.driverDeliveriesListener = driverDeliveriesListener;

        customerRef = FirebaseFirestore.getInstance().collection("Users");
        userImageURLsMap = new HashMap<>();
        userUserNamesMap = new HashMap<>();
    }

    public void setCurrentSummaryID(String currentSummaryID) {
        this.currentSummaryID = currentSummaryID;
    }

    @NonNull
    @Override
    public DriverDeliveriesAdapter.DeliveryRequestVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

//        if(viewType == TYPE_CURRENT_DELIVERY){
//            return new CurrentDeliveryVH(LayoutInflater.from(parent.getContext())
//                    .inflate(R.layout.item_delivery_request,parent,false));
//        }
            return new DeliveryRequestVH(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_delivery_request,parent,false));


    }

    @Override
    public void onBindViewHolder(@NonNull DriverDeliveriesAdapter.DeliveryRequestVH holder, int position) {

        holder.bind(deliveries.get(position));

//        if(holder.getItemViewType() == TYPE_CURRENT_DELIVERY){
//            ((CurrentDeliveryVH)holder).bind();
//        }else{
//            ((DeliveryRequestVH)holder).bind();
//
//        }

    }

    @Override
    public int getItemCount() {
        return deliveries.size();
    }

//    @Override
//    public int getItemViewType(int position) {
//
//        if(currentSummaryID !=null && currentSummaryID.equals(deliveries.get(position).getID())){
//        return TYPE_CURRENT_DELIVERY;
//        }
//
//        return TYPE_REQUESTED_DELIVERY;
//    }

    public class DeliveryRequestVH extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ImageView deliveryRequestUserImageIv;
        private final TextView deliveryRequestUserNameTv,
                deliveryRequestRequestedTimeTv,
                deliveryRequestAddressTv;


        public DeliveryRequestVH(@NonNull View itemView) {
            super(itemView);
            deliveryRequestUserImageIv = itemView.findViewById(R.id.deliveryRequestUserImageIv);
            deliveryRequestUserNameTv = itemView.findViewById(R.id.deliveryRequestUserNameTv);
            deliveryRequestRequestedTimeTv = itemView.findViewById(R.id.deliveryRequestRequestedTimeTv);
            deliveryRequestAddressTv = itemView.findViewById(R.id.deliveryRequestAddressTv);

            itemView.setOnClickListener(this);
        }

        private void bind(Delivery delivery){

            if(userUserNamesMap.containsKey(delivery.getRequesterID())){

                Picasso.get().load(userImageURLsMap.get(delivery.getRequesterID())).fit()
                        .centerCrop().into(deliveryRequestUserImageIv);

                deliveryRequestUserNameTv.setText(userUserNamesMap.get(delivery.getRequesterID()));

            }else{
                getUserInfo(delivery.getRequesterID(),deliveryRequestUserImageIv,deliveryRequestUserNameTv);
            }

            deliveryRequestRequestedTimeTv.setText(TimeFormatter.formatTime(delivery.getOrderTimeInMillis()));

            deliveryRequestAddressTv.setText(delivery.getAddress());


        }

        @Override
        public void onClick(View v) {

            driverDeliveriesListener.onDeliveryClicked(getAdapterPosition());

        }
    }

//    private class CurrentDeliveryVH extends RecyclerView.ViewHolder{
//
//
//        private final ImageView deliveryRequestUserImageIv;
//        private final TextView deliveryRequestUserNameTv,
//                deliveryRequestRequestedTimeTv,
//                deliveryRequestAddressTv;
//
//
//        public CurrentDeliveryVH(@NonNull View itemView) {
//            super(itemView);
//            deliveryRequestUserImageIv = itemView.findViewById(R.id.deliveryRequestUserImageIv);
//            deliveryRequestUserNameTv = itemView.findViewById(R.id.deliveryRequestUserNameTv);
//            deliveryRequestRequestedTimeTv = itemView.findViewById(R.id.deliveryRequestRequestedTimeTv);
//            deliveryRequestAddressTv = itemView.findViewById(R.id.deliveryRequestAddressTv);
//
//        }
//
//        private void bind(Delivery delivery){
//
//            if(userUserNamesMap.containsKey(delivery.getRequesterID())){
//
//                Picasso.get().load(userImageURLsMap.get(delivery.getRequesterID())).fit()
//                        .centerCrop().into(deliveryRequestUserImageIv);
//
//                deliveryRequestUserNameTv.setText(userUserNamesMap.get(delivery.getRequesterID()));
//
//            }else{
//                getUserInfo(delivery.getRequesterID(),deliveryRequestUserImageIv,deliveryRequestUserNameTv);
//            }
//
//            deliveryRequestRequestedTimeTv.setText(TimeFormatter.formatTime(delivery.getOrderTimeInMillis()));
//
//            deliveryRequestAddressTv.setText(delivery.getAddress());
//
//
//
//        }
//
//    }

    private void getUserInfo(String userID,ImageView userIv,TextView usernameTv){

        customerRef.document(userID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if(documentSnapshot.exists()){

                    final String imageURL = documentSnapshot.getString("imageURL"),
                    username = documentSnapshot.getString("name");

                    Picasso.get().load(imageURL).fit().centerCrop().into(userIv);
                    usernameTv.setText(username);

                    userImageURLsMap.put(userID,imageURL);
                    userUserNamesMap.put(userID,username);

                }

            }
        });

    }

}
