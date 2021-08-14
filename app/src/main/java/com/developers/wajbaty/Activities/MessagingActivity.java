package com.developers.wajbaty.Activities;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.developers.wajbaty.Adapters.MessageTextMapAdapter;
import com.developers.wajbaty.Models.MessageMap;
import com.developers.wajbaty.Models.Notification;
import com.developers.wajbaty.R;
import com.developers.wajbaty.Utils.BadgeUtil;
import com.developers.wajbaty.Utils.CloudMessagingNotificationsSender;
import com.developers.wajbaty.Utils.FirestoreNotificationSender;
import com.developers.wajbaty.Utils.GlobalVariables;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.collect.Iterables;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessagingActivity extends AppCompatActivity
        implements MessageTextMapAdapter.DeleteMessageListener, View.OnLayoutChangeListener {

    private final static int DOCUMENT_MESSAGE_LIMIT = 15;

    private String currentUserId;

    private CollectionReference userRef;

    //views
    private EditText messageEd;
    private RecyclerView messageRv;
    private TextView messagingUserNameTv;
    private ImageView messagingUserIv;
    private ImageButton sendMessageBtn;
    private ProgressBar messagesProgressBar;


    private LinearLayoutManager llm;


    //user
    private String messagingUserId, currentUserName;
    private String intendedDeliveryID;
    private DocumentReference currentUserRef;
    private SharedPreferences sharedPreferences;
    private ProgressDialog progressDialog;
    private String currentUsername, currentImageURL;
    private CloudMessagingNotificationsSender.Data data;
    private NotificationManager notificationManager;

    //messages
    private MessageTextMapAdapter messageTextMapAdapter;
    private ArrayList<MessageMap> messageMaps;
    private OnScrollListener currentScrollListener;
    private DocumentReference currentMessagingUserRef;
    private DocumentReference messagingFirestoreRef;

    private DatabaseReference messagingChildRef;
    private String firstKey, lastKey;

    private Map<DatabaseReference, ChildEventListener> childEventListeners;
    private Map<DatabaseReference, ValueEventListener> valueEventListeners;

    private int lastListYScroll;
    private boolean isFetchingMoreMessages = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        initializeObjects();

        getViews();

        addListeners();

        populateViews();

        readMessagesNew();
    }


    private void initializeObjects() {

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        currentUserId = "7J6eWOO6ggVROicvqbZNikahj9Q2";

        userRef = FirebaseFirestore.getInstance().collection("Users");

        sharedPreferences = getSharedPreferences("Wajbaty", Context.MODE_PRIVATE);

        final Intent intent = getIntent();

        if (intent.hasExtra("messagingBundle")) {

            final Bundle messagingBundle = intent.getBundleExtra("messagingBundle");

            messagingUserId = messagingBundle.getString("messagingUserId");
            intendedDeliveryID = messagingBundle.getString("destinationUID");

            if (Build.VERSION.SDK_INT < 26) {
                BadgeUtil.decrementBadgeNum(this);
            }

        } else {

            messagingUserId = intent.getStringExtra("messagingUserId");
            intendedDeliveryID = intent.getStringExtra("intendedDeliveryID");

        }

//        messagingUserId = "56kj04FZNwYwj533gLqBDKDr36T2";
//        intendedDeliveryID = "1e33d5b8-3f0d-40c6-8f9c-127d7b8e8562";


        sharedPreferences.edit()
//          .putBoolean("messagingscreen", true)
                .putString("currentMessagingUserId", messagingUserId)
                .putString("currentMessagingDeliveryID", intendedDeliveryID).apply();

        if (GlobalVariables.getMessagesNotificationMap() != null) {

            final String notificationIdentifier = messagingUserId + CloudMessagingNotificationsSender.Data.TYPE_MESSAGE + intendedDeliveryID;

            if (GlobalVariables.getMessagesNotificationMap().containsKey(notificationIdentifier)) {
                Log.d("ttt", "removing: " + notificationIdentifier);

                if (notificationManager == null)
                    notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                notificationManager.cancel(GlobalVariables.getMessagesNotificationMap()
                        .get(notificationIdentifier));

                GlobalVariables.getMessagesNotificationMap().remove(notificationIdentifier);
            }
        }

        llm = new LinearLayoutManager(this,
                RecyclerView.VERTICAL, false) {
            @Override
            public void onItemsAdded(@NonNull RecyclerView recyclerView, int positionStart, int itemCount) {

                Log.d("ttt", "positionStart: " + positionStart);

                if (itemCount == 1) {
                    messageRv.post(() -> scrollToPosition(messageMaps.size() - 1));
                }

            }
        };

//        llm.setStackFromEnd(true);
        messageMaps = new ArrayList<>();

        messageTextMapAdapter = new MessageTextMapAdapter(
                messageMaps,
                MessagingActivity.this,
                MessagingActivity.this);

    }

    private void getViews() {


        final Toolbar messagesToolBar = findViewById(R.id.messagesToolBar);
        messagesToolBar.setNavigationOnClickListener(v -> finish());
        messagesToolBar.setOnMenuItemClickListener(this::onOptionsItemSelected);

        sendMessageBtn = findViewById(R.id.sendMessageBtn);
        sendMessageBtn.setClickable(false);
        messagesProgressBar = findViewById(R.id.messagesProgressBar);
        messageRv = findViewById(R.id.messagesRv);
        messagingUserNameTv = findViewById(R.id.messagingUserNameTv);
        messagingUserIv = findViewById(R.id.messagingUserIv);
        messageEd = findViewById(R.id.messageEd);


        messageRv.setLayoutManager(llm);
        messageRv.addOnLayoutChangeListener(this);
        messageRv.setAdapter(messageTextMapAdapter);
    }


    private void populateViews() {

        userRef.document(messagingUserId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                currentMessagingUserRef = documentSnapshot.getReference();
                messagingUserNameTv.setText(documentSnapshot.getString("name"));

                Picasso.get().load(documentSnapshot.getString("imageURL")).fit().into(messagingUserIv);

            }
        });

        currentUserRef = userRef.document(currentUserId);
        currentUserRef.update("ActivelyMessaging", messagingUserId);

        currentUserRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            //            boolean isInitial = true;
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                if (value != null) {

                    currentUserName = value.getString("name");
                    currentImageURL = value.getString("imageURL");

//                    if(isInitial){
//                        isInitial = false;
//                    }else{
//
//
//                    }
                }
            }
        });
    }

    private void addListeners() {

//        messagingUserNameTv.setOnClickListener(v -> showProfile());
//        messagingUserIv.setOnClickListener(v -> showProfile());


    }


    void readMessagesNew() {

        valueEventListeners = new HashMap<>();

        boolean currentUidIsFirst = currentUserId.toUpperCase()
                .compareTo(messagingUserId.toUpperCase()) < 0;
        String id;
        if (currentUidIsFirst) {
            id = currentUserId + "-" + messagingUserId + "-" + intendedDeliveryID;
        } else {
            id = messagingUserId + "-" + currentUserId + "-" + intendedDeliveryID;
        }

        Log.d("ttt", "messaing doc id: " + id);

        messagingChildRef =
                FirebaseDatabase.getInstance().getReference().child("PrivateMessages").child(id);

        messagingFirestoreRef = FirebaseFirestore.getInstance().collection("PrivateMessages")
                .document(messagingChildRef.getKey());

        sendMessageBtn.setClickable(true);

        messagingChildRef.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    addUserDeleteEventListener();
                    getInitialMessages();

                } else {
                    messagesProgressBar.setVisibility(View.GONE);
                    sendMessageBtn.setOnClickListener(new FirstMessageClickListener());
                }

            }
        });
//        ValueEventListener valueEventListener;
//        messagingChildRef.addListenerForSingleValueEvent(valueEventListener = new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                if(snapshot.exists()){
//
//
//                }else{
//
//
//
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.d("realTimeActivity", "single event listener: " + error.getMessage());
//
//            }
//        });
//
//        valueEventListeners.put(messagingChildRef, valueEventListener);

    }


    void getMoreTopMessages() {

        isFetchingMoreMessages = true;
        messagesProgressBar.setVisibility(View.VISIBLE);

        messagingChildRef
                .child("Messages")
                .orderByKey()
                .limitToLast(DOCUMENT_MESSAGE_LIMIT)
                .endAt(String.valueOf(Integer.parseInt(firstKey) - 1))
                .get().addOnSuccessListener(snapshot -> {
            final List<MessageMap> newMessages = new ArrayList<>();

            for (DataSnapshot child : snapshot.getChildren()) {
                newMessages.add(child.getValue(MessageMap.class));
            }

            messageMaps.addAll(0, newMessages);
            firstKey = String.valueOf(Integer.parseInt(lastKey) - messageMaps.size());
            Log.d("realTimeActivity", "from previous first: " +
                    (Integer.parseInt(firstKey) - 1));


        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                Log.d("realTimeActivity", "to new first: " +
                        Integer.parseInt(firstKey));


                Log.d("realTimeActivity", "messageMaps size: " + messageMaps.size());
                messageTextMapAdapter.notifyItemRangeInserted(0,
                        (int) task.getResult().getChildrenCount());


                messagesProgressBar.setVisibility(View.INVISIBLE);

                if (task.getResult().getChildrenCount() < DOCUMENT_MESSAGE_LIMIT) {
                    Log.d("realTimeActivity", "snapshot.getChildrenCount(): "
                            + task.getResult().getChildrenCount());
                    messageRv.removeOnScrollListener(currentScrollListener);
                }
                isFetchingMoreMessages = false;
            }
        });

    }


    void sendCloudNotification(String message) {
        Log.d("ttt", "sending cloud notificaiton");
        if (data == null) {
            data = new CloudMessagingNotificationsSender.Data(
                    currentUserId,
                    "New message from: " + currentUserName,
                    message,
                    currentImageURL,
                    intendedDeliveryID,
                    CloudMessagingNotificationsSender.Data.TYPE_MESSAGE
            );

        } else {
            data.setBody(message);
        }

        CloudMessagingNotificationsSender.sendNotification(messagingUserId, data);

    }


    @Override
    public void onLayoutChange(View view, int i, int i1, int i2, int bottom, int i4,
                               int i5, int i6, int oldBottom) {
        Log.d("ttt", "onLayoutChange: " + "bottom: " + bottom + " | oldBottom: " + oldBottom);
        if (oldBottom != 0) {

            Log.d("ttt", oldBottom + " - " + bottom);
            if (oldBottom > bottom) {
                messageRv.post(new Runnable() {
                    @Override
                    public void run() {
                        messageRv.scrollToPosition(messageMaps.size() - 1);
                    }
                });
            }

        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        sharedPreferences.edit()
                .remove("isPaused")
                .remove("currentMessagingUserId")
                .remove("currentMessagingDeliveryID").apply();

        if (childEventListeners != null && !childEventListeners.isEmpty()) {
            for (DatabaseReference reference : childEventListeners.keySet()) {
                reference.removeEventListener(childEventListeners.get(reference));
            }
        }
        if (valueEventListeners != null && !valueEventListeners.isEmpty()) {
            for (DatabaseReference reference : valueEventListeners.keySet()) {
                reference.removeEventListener(valueEventListeners.get(reference));
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (currentUserRef != null)
            currentUserRef.update("ActivelyMessaging", null);


        if (messagingChildRef != null) {
            Log.d("ttt", "messagingChildRef != null");
            if (currentUserRef != null) {
                Log.d("ttt", "currentUserRef!=null");

                final String notificationPath = messagingUserId + "_" +
                        CloudMessagingNotificationsSender.Data.TYPE_MESSAGE + "_" + intendedDeliveryID;

                Log.d("ttt", "notificationPath: " + notificationPath);

                currentUserRef.collection("Notifications")
                        .document(notificationPath).update("seen", true);

            }


            Log.d("saveMessages", "update last seen: " +
                    lastKey);

            Log.d("savedMessages", "lastKey: " + lastKey);

            if (lastKey != null && !lastKey.isEmpty()) {

                messagingFirestoreRef.update(currentUserId + ":LastSeenMessage", Integer.parseInt(lastKey) + 1);

            }

        }

        sharedPreferences.edit().putBoolean("isPaused", true).apply();

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (messagingUserId != null) {
            sharedPreferences.edit()
                    .putString("currentMessagingUserId", messagingUserId)
                    .putString("currentMessagingDeliveryID", intendedDeliveryID).apply();
        }
    }

    void getInitialMessages() {

        messagingChildRef
                .child("Messages")
                .orderByKey()
                .limitToLast(DOCUMENT_MESSAGE_LIMIT)
                .get().addOnSuccessListener(snapshot -> {

            Log.d("realTimeActivity", "children count: " + snapshot.getChildrenCount());

            for (DataSnapshot child : snapshot.getChildren()) {
                messageMaps.add(child.getValue(MessageMap.class));
            }

            firstKey = Iterables.get(snapshot.getChildren(), 0).getKey();

            lastKey = Iterables.getLast(snapshot.getChildren()).getKey();


            Log.d("realTimeActivity", "from first: " + firstKey + " to last: " + lastKey);

        }).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                messageTextMapAdapter.notifyDataSetChanged();

                messageRv.post(() -> messageRv.scrollToPosition(messageMaps.size() - 1));


                messagesProgressBar.setVisibility(View.INVISIBLE);

                messagingFirestoreRef.update(currentUserId + ":LastSeenMessage", Integer.parseInt(lastKey) + 1);

                Log.d("realTimeActivity", "size from initial: " + messageMaps.size());

                messageRv.addOnScrollListener(currentScrollListener = new OnScrollListener());

                if (Integer.parseInt(lastKey) + 1 > DOCUMENT_MESSAGE_LIMIT) {
                    Log.d("realTimeActivity", "snapshot.getChildrenCount(): " +
                            Integer.parseInt(lastKey) + 1);
//                    isFetchingMoreMessages = true;
                }

                isFetchingMoreMessages = false;
                sendMessageBtn.setOnClickListener(new MessageSenderClickListener());
                addListenerForNewMessages();
                addDeleteFieldListener();
            }
        });

    }

    void addDeleteFieldListener() {

        ValueEventListener valueEventListener;
        messagingChildRef
                .child("lastDeleted")
                .addValueEventListener(valueEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d("realTimeActivity", "onDataChange");
                        if (snapshot.exists()) {

                            MessageMap messageMap = snapshot.getValue(MessageMap.class);
                            for (int i = 0; i < messageMaps.size(); i++) {
                                if (messageMaps.get(i).getContent().equals(messageMap.getContent())
                                        && messageMaps.get(i).getTime() == messageMap.getTime()) {
                                    if (!messageMaps.get(i).getDeleted()) {
                                        messageMaps.get(i).setDeleted(true);
                                        messageTextMapAdapter.notifyItemChanged(i);
                                    }
                                    break;
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        valueEventListeners.put(messagingChildRef.child("lastDeleted"), valueEventListener);


    }

    void addListenerForNewMessages() {

        childEventListeners = new HashMap<>();

        ChildEventListener childEventListener;

        final Query query =
                messagingChildRef
                        .child("Messages")
                        .orderByKey()
                        .startAt(String.valueOf(Integer.parseInt(lastKey) + 1));

        query.addChildEventListener(childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot,
                                     @Nullable String previousChildName) {

                lastKey = snapshot.getKey();
                messageMaps.add(snapshot.getValue(MessageMap.class));
                messageTextMapAdapter.notifyItemInserted(messageMaps.size());
                scrollToBottom();

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot,
                                       @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot,
                                     @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        childEventListeners.put(query.getRef(), childEventListener);
    }

    @Override
    public void deleteMessage(MessageMap messageMap, DialogInterface dialog) {

        final String id = getMessageDataSnapshotId(messageMap);

        messagingChildRef.child("Messages").child(id).child("deleted").setValue(true).
                addOnSuccessListener(v -> messagingChildRef.child("lastDeleted").setValue(messageMap)
                        .addOnSuccessListener(vo -> dialog.dismiss()).addOnFailureListener(e ->
                                dialog.dismiss())).addOnFailureListener(e -> {
            dialog.dismiss();
            Toast.makeText(MessagingActivity.this,
                    "لقد فشل حذف الرسالة", Toast.LENGTH_SHORT).show();

            Log.d("ttt", "failed: " + e.getMessage());
        });
    }

    String getMessageDataSnapshotId(MessageMap messageMap) {
        return String.valueOf(Integer.parseInt(firstKey) + messageMaps.indexOf(messageMap));
    }

    void checkUserActivityAndSendNotifications(String message) {

        if (currentMessagingUserRef == null) {
            Log.d("ttt", "currentMessagingUserRef==null");
            return;
        }

        currentMessagingUserRef
                .get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.contains("ActivelyMessaging")) {
                final String messaging = documentSnapshot.getString("ActivelyMessaging");
                if (messaging == null || !messaging.equals(intendedDeliveryID + "-" + currentUserId)) {
                    Log.d("ttt", "sendBothNotifs");
                    sendBothNotifs(message);
                }
            } else {
                Log.d("ttt", "sendBothNotifs");
                sendBothNotifs(message);
            }
        }).addOnFailureListener(e -> Log.d("ttt", "currentMessagingUserRef e: " + e.getMessage()));


    }

    void sendBothNotifs(String message) {

        FirestoreNotificationSender.sendNotification(intendedDeliveryID, messagingUserId,
                Notification.TYPE_MESSAGE, message);

        sendCloudNotification(message);
    }

    void addUserDeleteEventListener() {

        messagingFirestoreRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                if (value != null) {

                    if (value.contains("isDeletedFor:" + messagingUserId) && value.getBoolean("isDeletedFor:" + messagingUserId)) {

                        Log.d("realTimeActivity", "messaging user deleted this messages");

                        Toast.makeText(MessagingActivity.this,
                                "لا يمكنك المراسلة على هذه المحادثة!",
                                Toast.LENGTH_SHORT).show();

                        messageEd.setVisibility(View.GONE);
                        sendMessageBtn.setVisibility(View.GONE);

                        messageTextMapAdapter.disableLongClick();

                    }
                }

            }
        });


    }

    private void scrollToBottom() {
        messageRv.post(() -> messageRv.scrollToPosition(messageMaps.size() - 1));
    }

    class FirstMessageClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {

            final String message = messageEd.getText().toString().trim();
            if (!message.equals("")) {
//                if (WifiUtil.checkWifiConnection(view.getContext())) {
                messageEd.setText("");
                sendMessageBtn.setClickable(false);

                final Map<String, Object> firestoreMessagingMap = new HashMap<>();
                final List<String> users = new ArrayList<>();
                users.add(currentUserId);
                users.add(messagingUserId);

                firestoreMessagingMap.put("users", users);

                final MessageMap messageMap =
                        new MessageMap(message, System.currentTimeMillis(), currentUserId);

                firestoreMessagingMap.put("lastMessage", messageMap);
                firestoreMessagingMap.put("lastMessageTimeInMillis", messageMap.getTime());
                firestoreMessagingMap.put("isDeletedFor:" + currentUserId, false);
                firestoreMessagingMap.put("isDeletedFor:" + messagingUserId, false);
                firestoreMessagingMap.put("intendedDeliveryID", intendedDeliveryID);
                firestoreMessagingMap.put("messagesCount", 1);
                firestoreMessagingMap.put("destinationID", intendedDeliveryID);
                firestoreMessagingMap.put(currentUserId + ":LastSeenMessage", 0);
                firestoreMessagingMap.put(messagingUserId + ":LastSeenMessage", 0);


                final HashMap<String, MessageMap> messages = new HashMap<>();
                messages.put("0", messageMap);

                messagingFirestoreRef
                        .set(firestoreMessagingMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {

                                    messagingChildRef.child("Messages").setValue(messages).addOnSuccessListener(v -> {

                                        messageMaps.add(messageMap);
                                        messageTextMapAdapter.notifyDataSetChanged();
                                        firstKey = "0";
                                        lastKey = "0";

                                        addUserDeleteEventListener();
                                        addListenerForNewMessages();
                                        addDeleteFieldListener();
                                        checkUserActivityAndSendNotifications(messageMap.getContent());


                                        sendMessageBtn.setOnClickListener(new MessageSenderClickListener());
                                        sendMessageBtn.setClickable(true);


                                    })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(MessagingActivity.this, "failed",
                                                        Toast.LENGTH_SHORT).show();
                                                sendMessageBtn.setClickable(true);
                                            });

                                }

                            }
                        });

//                }
            } else {
                Toast.makeText(view.getContext(),
                        "لا يمكنك ارسال رسالة فارغة! ", Toast.LENGTH_SHORT).show();
            }


        }
    }

    class MessageSenderClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {

            final String message = messageEd.getText().toString().trim();
            if (!message.equals("")) {
//                if (WifiUtil.checkWifiConnection(view.getContext())) {
                messageEd.setText("");
                sendMessageBtn.setClickable(false);

                MessageMap messageMap = new MessageMap(
                        message,
                        System.currentTimeMillis(),
                        currentUserId
                );

                messagingChildRef.child("Messages")
                        .child(String.valueOf(Integer.parseInt(lastKey) + 1))
                        .setValue(messageMap).addOnSuccessListener(aVoid -> {

                    messagingFirestoreRef.update("lastMessage", messageMap,
                            "messagesCount", FieldValue.increment(1),
                            "lastMessageTimeInMillis", messageMap.getTime())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    checkUserActivityAndSendNotifications(messageMap.getContent());
                                    sendMessageBtn.setClickable(true);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            messagingChildRef.child("Messages")
                                    .child(String.valueOf(Integer.parseInt(lastKey) + 1))
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(MessagingActivity.this,
                                                    "failed", Toast.LENGTH_SHORT).show();
                                            sendMessageBtn.setClickable(true);

                                        }
                                    });
                        }
                    });

                }).addOnFailureListener(e -> {

                    Toast.makeText(MessagingActivity.this,
                            "failed", Toast.LENGTH_SHORT).show();
                    sendMessageBtn.setClickable(true);

                });

//                }
            } else {
                Toast.makeText(view.getContext(),
                        "لا يمكنك ارسال رسالة فارغة! ", Toast.LENGTH_SHORT).show();
            }


        }
    }

    class OnScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            if (!isFetchingMoreMessages && !messageRv.canScrollVertically(-1)) {

                Log.d("realTimeActivity", "geeting more messages");
                getMoreTopMessages();
            }
        }

//        @Override
//        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
//            super.onScrollStateChanged(recyclerView, newState);
//
//            Log.d("ttt","onScrollStateChanged");
//            if (!isFetchingMoreMessages &&
//                    !recyclerView.canScrollVertically(-1)) {
//
//                Log.d("ttt", "is at bottom");
//
//                    messagesProgressBar.setVisibility(View.VISIBLE);
//                    Log.d("realTimeActivity", "geeting more messages");
//                    getMoreTopMessages();
//            }
//        }
    }

}