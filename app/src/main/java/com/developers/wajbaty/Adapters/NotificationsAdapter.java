package com.developers.wajbaty.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.developers.wajbaty.Models.Notification;
import com.developers.wajbaty.R;
import com.developers.wajbaty.Utils.TimeFormatter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationsVH> {

    private static NotificationListener notificationListener;
    private final ArrayList<Notification> notifications;
    private final CollectionReference customerRef;
    private final HashMap<String, String> userImageURLsMap, userUserNamesMap;

    public NotificationsAdapter(ArrayList<Notification> notifications, NotificationListener notificationListener) {
        this.notifications = notifications;
        NotificationsAdapter.notificationListener = notificationListener;


        customerRef = FirebaseFirestore.getInstance().collection("Users");
        userImageURLsMap = new HashMap<>();
        userUserNamesMap = new HashMap<>();

    }

    @NonNull
    @Override
    public NotificationsVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NotificationsVH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationsVH holder, int position) {

        holder.bind(notifications.get(position));

    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    private void getUserInfo(String userID, ImageView userIv, TextView usernameTv) {

        customerRef.document(userID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if (documentSnapshot.exists()) {

                    final String imageURL = documentSnapshot.getString("imageURL"),
                            username = documentSnapshot.getString("name");

                    Picasso.get().load(imageURL).fit().centerCrop().into(userIv);
                    usernameTv.setText(username);

                    userImageURLsMap.put(userID, imageURL);
                    userUserNamesMap.put(userID, username);

                }

            }
        });

    }

    public interface NotificationListener {
        void onNotificationClicked(int position);

        void onNotificationDismissed(int position);
    }

    public class NotificationsVH extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ImageView notificationUserImageIv;
        private final TextView notificationUserNameTv, notificationContentTv, notificationTimeTv;

        public NotificationsVH(@NonNull View itemView) {
            super(itemView);
            notificationUserImageIv = itemView.findViewById(R.id.notificationUserImageIv);
            notificationUserNameTv = itemView.findViewById(R.id.notificationUserNameTv);
            notificationContentTv = itemView.findViewById(R.id.notificationContentTv);
            notificationTimeTv = itemView.findViewById(R.id.notificationTimeTv);

            itemView.setOnClickListener(this);
        }

        private void bind(Notification notification) {

            final String senderID = notification.getSenderID();

            if (userUserNamesMap.containsKey(senderID)) {

                Picasso.get().load(userImageURLsMap.get(senderID)).fit()
                        .centerCrop().into(notificationUserImageIv);

                notificationUserNameTv.setText(userUserNamesMap.get(senderID));

            } else {
                getUserInfo(senderID, notificationUserImageIv, notificationUserNameTv);
            }

            String content = "";
            switch (notification.getType()) {

                case Notification.TYPE_MESSAGE:
                    content = "New Message: " + notification.getContent();
                    break;
            }

            notificationContentTv.setText(content);

            notificationTimeTv.setText(TimeFormatter.formatTime(notification.getTimeCreatedInMillis()));
        }

        @Override
        public void onClick(View v) {

            notificationListener.onNotificationClicked(getAdapterPosition());

        }
    }

}
