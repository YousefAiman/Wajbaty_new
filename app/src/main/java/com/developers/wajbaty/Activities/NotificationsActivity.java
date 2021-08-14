package com.developers.wajbaty.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.developers.wajbaty.Adapters.NotificationsAdapter;
import com.developers.wajbaty.Models.Notification;
import com.developers.wajbaty.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity implements NotificationsAdapter.NotificationListener {

    //constants
    private static final int NOTIFICATION_LIMIT = 10;

    //adapter
    private NotificationsAdapter adapter;
    private ArrayList<Notification> notifications;

    private List<ListenerRegistration> notificationsSnapshotListeners;
    private Query mainQuery, newNotificationsQuery;


    //views
    private Toolbar notificationsToolbar;
    private RecyclerView notificationsRv;
    private ProgressBar notificationsProgressBar;
    private TextView notificationsEmptyTv;

    //paging
    private DocumentSnapshot lastDocSnapshot;
    private boolean isFetchingNotifications;
    private ScrollListener currentScrollListener;

    //firetore
    private CollectionReference notificationRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        initializeObjects();

        getViews();

        setupNotificationListener(true);

        listenForNewNotifications();

    }

    private void initializeObjects() {

        notifications = new ArrayList<>();
        adapter = new NotificationsAdapter(notifications, this);

//        notificationsSnapshotListener = ;

        notificationsSnapshotListeners = new ArrayList<>();

        //firebase
        final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        notificationRef = firestore.collection("Users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("Notifications");


        mainQuery = notificationRef.whereEqualTo("seen", false)
                .whereLessThanOrEqualTo("timeCreatedInMillis", System.currentTimeMillis())
                .orderBy("timeCreatedInMillis", Query.Direction.DESCENDING)
                .limit(NOTIFICATION_LIMIT);

        newNotificationsQuery = notificationRef.whereGreaterThan("timeCreatedInMillis", System.currentTimeMillis());

    }

    private void getViews() {

        notificationsToolbar = findViewById(R.id.notificationsToolbar);
        notificationsRv = findViewById(R.id.notificationsRv);
        notificationsProgressBar = findViewById(R.id.notificationsProgressBar);
        notificationsEmptyTv = findViewById(R.id.notificationsEmptyTv);

        notificationsToolbar.setNavigationOnClickListener(v -> finish());
        notificationsRv.setAdapter(adapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback());
        itemTouchHelper.attachToRecyclerView(notificationsRv);


    }

    private void setupNotificationListener(boolean initialSnapshot) {

        Query query = mainQuery;

        if (!initialSnapshot && lastDocSnapshot != null) {
            query = query.startAfter(lastDocSnapshot);
        }

        notificationsSnapshotListeners.add(query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            boolean isInitial = true;

            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                if (isInitial) {

                    if (value != null) {

                        if (initialSnapshot) {

                            final List<DocumentSnapshot> documentSnapshots = value.getDocuments();

                            if (documentSnapshots.isEmpty()) {
                                isFetchingNotifications = false;
                                notificationsEmptyTv.setVisibility(View.VISIBLE);
                                notificationsProgressBar.setVisibility(View.GONE);
                                return;
                            }

                            for (DocumentSnapshot documentSnapshot : documentSnapshots) {
                                notifications.add(documentSnapshot.toObject(Notification.class));
                            }

                            if (!notifications.isEmpty()) {
                                adapter.notifyDataSetChanged();
                            } else {
                                notificationsEmptyTv.setVisibility(View.VISIBLE);
                            }

                            if (documentSnapshots.size() == NOTIFICATION_LIMIT && currentScrollListener == null) {
                                notificationsRv.addOnScrollListener(currentScrollListener = new ScrollListener());
                            }

                            lastDocSnapshot = documentSnapshots.get(documentSnapshots.size() - 1);

                        } else {

                            final int previousSize = notifications.size();
                            int count = 0;
                            for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
                                notifications.add(notifications.size(), documentSnapshot.toObject(Notification.class));
                                count++;
                            }

                            if (!notifications.isEmpty()) {
                                adapter.notifyItemRangeInserted(previousSize, count);
                            }

                            if (count < NOTIFICATION_LIMIT && currentScrollListener != null) {
                                notificationsRv.removeOnScrollListener(currentScrollListener);
                                currentScrollListener = null;
                            }

                        }
                        notificationsProgressBar.setVisibility(View.GONE);

                    } else {

                        notificationsProgressBar.setVisibility(View.GONE);
                        notificationsEmptyTv.setVisibility(View.VISIBLE);
                    }
                    isFetchingNotifications = false;

                    isInitial = false;
                } else {

                    if (value != null) {

                        for (DocumentChange dc : value.getDocumentChanges()) {

                            final DocumentSnapshot documentSnapshot = dc.getDocument();

                            switch (dc.getType()) {


                                case MODIFIED:

                                    if (documentSnapshot.contains("seen") &&
                                            (Boolean) dc.getDocument().get("seen")) {

                                        removeNotification(documentSnapshot.getId());

                                    }

                                    break;


                                case REMOVED:

                                    removeNotification(documentSnapshot.getId());

                                    break;

                            }

                        }


                    } else {

                        notificationsProgressBar.setVisibility(View.GONE);
                        notificationsEmptyTv.setVisibility(View.VISIBLE);

                    }

                }
            }
        }));

    }

    private void listenForNewNotifications() {

        notificationsSnapshotListeners.add(
                newNotificationsQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                        if (value != null) {

                            for (DocumentChange dc : value.getDocumentChanges()) {

                                final DocumentSnapshot documentSnapshot = dc.getDocument();

                                switch (dc.getType()) {

                                    case ADDED:

                                        notifications.add(0, documentSnapshot.toObject(Notification.class));
                                        adapter.notifyItemInserted(0);

                                        break;

                                    case MODIFIED:

                                        if (documentSnapshot.contains("seen") &&
                                                (Boolean) dc.getDocument().get("seen")) {

                                            removeNotification(documentSnapshot.getId());

                                        }

                                        break;


                                    case REMOVED:

                                        removeNotification(documentSnapshot.getId());

                                        break;

                                }


                            }

                        }

                    }
                })
        );

    }

    private void removeNotification(String id) {

        for (int i = 0; i < notifications.size(); i++) {
            if (notifications.get(i).getID().equals(id)) {

                notifications.remove(i);
                adapter.notifyItemRemoved(i);

                break;
            }
        }

    }
//    private void indicateEmpty


    @Override
    public void onNotificationClicked(int position) {

        if (position >= notifications.size())
            return;

        final Notification notification = notifications.get(position);

        switch (notification.getType()) {

            case Notification.TYPE_MESSAGE:

                startActivity(new Intent(this, MessagingActivity.class)
                        .putExtra("messagingUserId", notification.getSenderID())
                        .putExtra("intendedDeliveryID", notification.getDestinationID()));

                break;

        }

    }

    @Override
    public void onNotificationDismissed(int position) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (notificationsSnapshotListeners != null && !notificationsSnapshotListeners.isEmpty()) {
            for (ListenerRegistration listenerRegistration : notificationsSnapshotListeners) {
                listenerRegistration.remove();
            }
        }

    }

    private class ScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (!isFetchingNotifications && !recyclerView.canScrollVertically(1)) {

                setupNotificationListener(false);

            }
        }
    }

    private class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

        SwipeToDeleteCallback() {
            super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView,
                              @NonNull RecyclerView.ViewHolder viewHolder,
                              @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
//            if (GlobalVariables.isWifiIsOn()) {
//
            int position = viewHolder.getAdapterPosition();

            if (position < notifications.size()) {
                notificationRef.document(notifications.get(position).getID())
                        .update("seen", false);
            }

//            }
        }
    }
}