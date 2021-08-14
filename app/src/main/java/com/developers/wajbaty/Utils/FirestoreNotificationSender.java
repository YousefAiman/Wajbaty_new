package com.developers.wajbaty.Utils;

import com.developers.wajbaty.Models.Notification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirestoreNotificationSender {

    private static final CollectionReference usersRef =
            FirebaseFirestore.getInstance().collection("Users");

    public static void sendNotification(String destinationID, String receiverID, int type,
                                        String content) {

        final String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        final CollectionReference notificationsRef = usersRef.document(receiverID).collection("Notifications");

        final String notificationPath = currentUserId + "_" + type + "_" + destinationID;

        notificationsRef.document(notificationPath)
                .get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {

                Notification notification =
                        new Notification(notificationPath,
                                currentUserId,
                                destinationID,
                                content,
                                type,
                                System.currentTimeMillis(),
                                false);

                documentSnapshot.getReference().set(notification);
            } else {

                if (type == Notification.TYPE_MESSAGE) {

                    documentSnapshot.getReference().update(
                            "content", content
                            , "timeCreatedInMillis", System.currentTimeMillis());

                }
//                else {
//                    Log.d("ttt", "deleting notification firestore");
//                    documentSnapshot.getReference().delete();
//                }

            }
        });
    }

    public static void deleteNotification(String destinationID, String receiverID, int type) {

        final CollectionReference notificationsRef = usersRef.document(receiverID).collection("Notifications");
        final String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        final String notificationPath = currentUserId + "_" + type + "_" + destinationID;

        notificationsRef.document(notificationPath).delete();
    }
}
