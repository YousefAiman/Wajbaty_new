package com.developers.wajbaty.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.developers.wajbaty.Activities.MessagingActivity;
import com.developers.wajbaty.Adapters.MessagingUserAdapter;
import com.developers.wajbaty.Models.MessageMap;
import com.developers.wajbaty.Models.UserMessage;
import com.developers.wajbaty.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
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
import java.util.HashMap;
import java.util.List;

public class MessagesFragment extends Fragment implements MessagingUserAdapter.MessagingUserListener {

    private final static int PAGINATION = 8;
    private String currentUserUid;

    //views
    private RecyclerView chatsRv;
    private TextView noMessagesTv;
    private ProgressBar messagesProgressBar;
    private LinearLayoutManager llm;

    //adapter
    private MessagingUserAdapter adapter;
    private List<UserMessage> userMessages;

    //firebase
    private CollectionReference userRef;
    private Query mainQuery;
    private List<ListenerRegistration> snapshotListeners;

    //user messages
    private boolean isLoadingItems;
    private ScrollListener scrollListener;
    private DocumentSnapshot lastDocSnap;

    public MessagesFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userRef = FirebaseFirestore.getInstance().collection("Users");
        currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userMessages = new ArrayList<>();

        adapter = new MessagingUserAdapter(userMessages, this, requireContext());

        llm = new LinearLayoutManager(getContext(),
                RecyclerView.VERTICAL, false) {
            @Override
            public void onItemsRemoved(@NonNull RecyclerView recyclerView,
                                       int positionStart, int itemCount) {
                super.onItemsRemoved(recyclerView, positionStart, itemCount);

                Log.d("savedMessages", "on item removed: " + getItemCount());

                if (getItemCount() == 0 && noMessagesTv.getVisibility() == View.GONE) {
                    noMessagesTv.setVisibility(View.VISIBLE);
                    chatsRv.setVisibility(View.INVISIBLE);
                }

            }

            @Override
            public void onItemsAdded(@NonNull RecyclerView recyclerView,
                                     int positionStart, int itemCount) {
                super.onItemsAdded(recyclerView, positionStart, itemCount);

                if (noMessagesTv.getVisibility() == View.VISIBLE) {
                    noMessagesTv.setVisibility(View.GONE);
                    chatsRv.setVisibility(View.VISIBLE);
                }

            }
        };

        snapshotListeners = new ArrayList<>();

        mainQuery = FirebaseFirestore.getInstance().collection("PrivateMessages")
                .whereArrayContains("users", currentUserUid)
//                .whereEqualTo("isDeletedFor:" + currentUserUid,false)
                .orderBy("lastMessageTimeInMillis", Query.Direction.DESCENDING)
                .whereLessThanOrEqualTo("lastMessageTimeInMillis", System.currentTimeMillis())
                .limit(PAGINATION);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);

        chatsRv = view.findViewById(R.id.chattingUserRv);
        noMessagesTv = view.findViewById(R.id.noMessagesTv);
        messagesProgressBar = view.findViewById(R.id.messagesProgressBar);

        final AdView adView = view.findViewById(R.id.adView);
        adView.loadAd(new AdRequest.Builder().build());
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                adView.setVisibility(View.VISIBLE);
            }
        });

        chatsRv.setLayoutManager(llm);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chatsRv.setAdapter(adapter);

        getRealTimeMessagingUsers(true);

        listenForNewMessages();

    }


    void getRealTimeMessagingUsers(boolean isInitial) {

        isLoadingItems = true;

        messagesProgressBar.setVisibility(View.VISIBLE);

        Query currentQuery = mainQuery;

        if (lastDocSnap != null) {
            currentQuery = currentQuery.startAfter(lastDocSnap);
        }

        snapshotListeners.add(currentQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            boolean initialSnapshots = true;

            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                if (value != null) {

                    if (initialSnapshots) {

                        final List<DocumentSnapshot> documentSnapshots = value.getDocuments();

                        if (!documentSnapshots.isEmpty()) {
                            lastDocSnap = documentSnapshots.get(documentSnapshots.size() - 1);

                            for (DocumentSnapshot documentSnapshot : documentSnapshots) {

                                MessageMap messageMap = new MessageMap((HashMap<String, Object>)
                                        documentSnapshot.get("lastMessage"));

                                List<String> users = (List<String>) documentSnapshot.get("users");

                                String uid;
                                if (users.get(0).equals(currentUserUid)) {
                                    uid = users.get(1);
                                } else {
                                    uid = users.get(0);
                                }

                                userMessages.add(new UserMessage(
                                        documentSnapshot.getString("destinationID"),
                                        messageMap,
                                        uid,
                                        documentSnapshot.getLong(currentUserUid + ":LastSeenMessage"),
                                        documentSnapshot.getLong("messagesCount")));
                            }

                            if (!userMessages.isEmpty()) {

                                if (isInitial) {

                                    adapter.notifyDataSetChanged();

                                    if (userMessages.size() == PAGINATION && scrollListener == null) {
                                        chatsRv.addOnScrollListener(scrollListener = new ScrollListener());
                                    }

                                } else {

                                    int size = value.getDocuments().size();

                                    adapter.notifyItemRangeInserted(
                                            userMessages.size() - size, size);

                                    if (size < PAGINATION && scrollListener != null) {
                                        chatsRv.removeOnScrollListener(scrollListener);
                                        scrollListener = null;
                                    }

                                }

                                if (noMessagesTv.getVisibility() == View.VISIBLE) {
                                    noMessagesTv.setVisibility(View.INVISIBLE);
                                    chatsRv.setVisibility(View.VISIBLE);
                                }

                            } else {

                                if (isInitial) {

                                    noMessagesTv.setVisibility(View.VISIBLE);
                                    chatsRv.setVisibility(View.INVISIBLE);

                                }

                            }

                        } else {

                            noMessagesTv.setVisibility(View.VISIBLE);
                            chatsRv.setVisibility(View.INVISIBLE);

                        }
                        messagesProgressBar.setVisibility(View.GONE);
                        initialSnapshots = false;
                    } else {


                        for (DocumentChange dc : value.getDocumentChanges()) {

                            switch (dc.getType()) {

                                case MODIFIED:


                                    UserMessageModified(dc.getDocument());

                                    break;


                                case REMOVED:

                                    findAndRemoveUserMessage(dc.getDocument());

                                    break;
                            }

                        }

                    }

                } else {
                    messagesProgressBar.setVisibility(View.GONE);
                }

            }
        }));

    }

    private void listenForNewMessages() {

        Query newMessagesQuery =
                FirebaseFirestore.getInstance().collection("PrivateMessages")
                        .whereArrayContains("users", currentUserUid)
//                .whereEqualTo("isDeletedFor:" + currentUserUid,false)
                        .orderBy("lastMessageTimeInMillis")
                        .whereGreaterThan("lastMessageTimeInMillis", System.currentTimeMillis());

        snapshotListeners.add(
                newMessagesQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException error) {

                        if (value != null) {

                            for (DocumentChange dc : value.getDocumentChanges()) {

                                DocumentSnapshot snapshot = dc.getDocument();

                                switch (dc.getType()) {

                                    case ADDED:

                                        Log.d("ttt", "type added message");
                                        List<String> users = (List<String>) snapshot.get("users");

                                        String uid;

                                        if (users.get(0).equals(currentUserUid)) {
                                            uid = users.get(1);
                                        } else {
                                            uid = users.get(0);
                                        }

//                                        boolean currentUidIsFirst = currentUserUid.toUpperCase()
//                                                .compareTo(uid.toUpperCase()) < 0;
//
//                                        7J6eWOO6ggVROicvqbZNikahj9Q2-lve5PnFFkgWS68ck00wIbi0vbGi2-502f3763-2c0f-4989-998e-f423968024f8
//
//
//                                        String id;
//                                        if(currentUidIsFirst){
//                                            id = currentUserUid +"-"+ uid +"-"+ snapshot.get("destinationID");
//                                        }else{
//                                            id = uid +"-"+ currentUserUid +"-"+ snapshot.get("destinationID");
//                                        }
//

                                        String destionatinId = snapshot.getString("destinationID");

                                        for (UserMessage userMessage : userMessages) {
                                            if (userMessage.getChattingDestinationId().equals(destionatinId)) {
                                                return;
                                            }
                                        }


                                        MessageMap messageMap = new MessageMap((HashMap<String, Object>)
                                                snapshot.get("lastMessage"));


                                        userMessages.add(0, new UserMessage(
                                                snapshot.getString("destinationID"),
                                                messageMap,
                                                uid,
                                                snapshot.getLong(currentUserUid + ":LastSeenMessage"),
                                                snapshot.getLong("messagesCount")));

                                        adapter.notifyItemInserted(0);

                                        if (noMessagesTv.getVisibility() == View.VISIBLE) {
                                            noMessagesTv.setVisibility(View.GONE);
                                            chatsRv.setVisibility(View.VISIBLE);
                                        }

                                        break;

                                    case MODIFIED:

                                        UserMessageModified(snapshot);

                                        break;

                                    case REMOVED:

                                        findAndRemoveUserMessage(dc.getDocument());

                                        break;
                                }

                            }

                        }

                    }
                }));

    }

//    long getTime(DataSnapshot snapshot) {
//
//        final DataSnapshot databaseReference = snapshot.child("messages")
//                .child(String.valueOf(snapshot.child("messages").getChildrenCount() - 1))
//                .child("time");
//
//        return databaseReference.exists() ? databaseReference.getValue(Long.class) : 0;
//    }
//
//
//    void addSavedMessageFromDataSnapshot(DataSnapshot child, boolean isInitial) {
//
//        final UserMessage userMessage = new UserMessage();
//        userMessage.setChattingDestinationId(child.child("intendedpromoid").getValue(Long.class));
//        userMessage.setLastMessageRead(
//                child.child(currentUserUid + ":LastSeenMessage").getValue(Long.class));
//
//
//        userMessage.setMessagingUserId(child.child(messagingUserId).getValue(String.class));
//
//        final DataSnapshot messageChild =
//                child.child("messages").child(String.valueOf(
//                        child.child("messages").getChildrenCount() - 1));
//
//        userMessage.setChattingLatestMessageMap(messageChild.getValue(MessageMap.class));
//
////    userMessage.setChattingLatestMessageMap(new MessageMap(
////            messageChild.child("content").getValue(String.class),
////            messageChild.child("deleted").getValue(Boolean.class),
////            messageChild.child("sender").getValue(Integer.class),
////            messageChild.child("time").getValue(Long.class)
////    ));
////
//
//
//        userMessage.setMessagesCount(Integer.parseInt(messageChild.getKey()) + 1);
//
//        Log.d("savedMessages", "message count: " + userMessage.getMessagesCount());
//
//        addMessagesAdditionAndUpdateListener(child, userMessage, messageChild,
//                userMessage.getMessagesCount());
//
//        userRef.whereEqualTo("userId", userMessage.getMessagingUserId())
//                .get().addOnSuccessListener(snaps -> {
//            final DocumentSnapshot userSnap = snaps.getDocuments().get(0);
//            userMessage.setChattingUsername(userSnap.getString("name"));
//            userMessage.setChattingUserImage(userSnap.getString("imageurl"));
//
//
//            if (isInitial) {
//
//                userMessages.add(userMessage);
//                adapter.notifyItemInserted(userMessages.size());
////        if(userMessages.size() > 1){
////
////          Log.d("savedMessages","sorting");
////          Collections.sort(userMessages, (userMessage1, userMessage2)
////                  -> Long.compare(
////                  userMessage2.getChattingLatestMessageMap().getTime()
////                  , userMessage1.getChattingLatestMessageMap().getTime()));
////
////          adapter.notifyDataSetChanged();
////        }else{
////          Log.d("savedMessages","added to first");
////          adapter.notifyItemInserted(0);
////        }
//
//                if (snapshots != null && !snapshots.isEmpty()) {
//                    snapshots.remove(child);
//                }
//            } else {
//                userMessages.add(0, userMessage);
//                adapter.notifyItemInserted(0);
//            }
//
//            addLastSeenListener(child, userMessage);
//
//            addDeletionListener(child, userMessage);
//
//        });
//
//    }
//
//
//    void addMessagesAdditionAndUpdateListener(DataSnapshot child,
//                                              UserMessage userMessage,
//                                              DataSnapshot messageChild,
//                                              long startAt) {
//
//        final DatabaseReference databaseReference = child.child("messages").getRef();
//
//        ChildEventListener childEventListener;
//
//        Log.d("savedMessages", "listenting from: " + startAt);
//
//        databaseReference.orderByKey().startAt(String.valueOf(startAt))
//                .addChildEventListener(childEventListener = new ChildEventListener() {
//                    @Override
//                    public void onChildAdded(@NonNull DataSnapshot snapshot,
//                                             @Nullable String previousChildName) {
//
//                        Log.d("savedMessages", "message added");
//
//                        if ((Long.parseLong(Objects.requireNonNull(snapshot.getKey())) + 1)
//                                > userMessage.getMessagesCount()) {
//
//                            Log.d("savedMessages", "message added after last");
//
//
//                            userMessage.setChattingLatestMessageMap(snapshot.getValue(MessageMap.class));
////                  userMessage.setChattingLatestMessageMap(new MessageMap(
////                          snapshot.child("content").getValue(String.class),
////                          snapshot.child("deleted").getValue(Boolean.class),
////                          snapshot.child("sender").getValue(Integer.class),
////                          snapshot.child("time").getValue(Long.class)
////                  ));
//
//
//                            userMessage.setMessagesCount(userMessage.getMessagesCount() + 1);
//
//                            final int index = userMessages.indexOf(userMessage);
//
//                            adapter.notifyItemChanged(index);
//
//                            if (index > 0) {
//                                Collections.swap(userMessages, index, 0);
//                                adapter.notifyItemMoved(index, 0);
////                    adapter.notifyItemMoved(0,index);
//                            }
//
//                        }
//
////                databaseReference.removeEventListener(this);
////
////                addMessagesAdditionAndUpdateListener(child,userMessage,messageChild
////                        ,userMessage.getMessagesCount());
//
//                        Log.d("savedMessages", "listenting from: " +
//                                (Integer.parseInt(snapshot.getKey()) + 1));
//
//                    }
//
//                    @Override
//                    public void onChildChanged(@NonNull DataSnapshot snapshot,
//                                               @Nullable String previousChildName) {
//
//                        Log.d("savedMessages", "message changed: " + snapshot.getKey());
//                        if (snapshot.exists() &&
//                                (Long.parseLong(Objects.requireNonNull(snapshot.getKey())) + 1)
//                                        == userMessage.getMessagesCount()) {
//
//
//                            final MessageMap messageMap = snapshot.getValue(MessageMap.class);
//
////                  MessageMap messageMap= new MessageMap(
////                          snapshot.child("content").getValue(String.class),
////                          snapshot.child("deleted").getValue(Boolean.class),
////                          snapshot.child("sender").getValue(Integer.class),
////                          snapshot.child("time").getValue(Long.class)
////                  );
//
//
//                            if (messageMap == null)
//                                return;
//
//
////                  if(userMessage.getChattingLatestMessageMap().getContent()
////                          .equals(messageMap.getContent())
////                          && messageMap.getTime() == messageMap.getTime()){
//
//                            Log.d("savedMessages", "onChildChanged: " +
//                                    userMessage.getChattingLatestMessageMap().getContent());
//
//
//                            userMessage.getChattingLatestMessageMap().setDeleted(true);
//                            adapter.notifyItemChanged(userMessages.indexOf(userMessage));
//
////                  }
//                        }
//
//                    }
//
//                    @Override
//                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {
//
//                    }
//
//                    @Override
//                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });
//
//
//        childEventListeners.put(databaseReference, childEventListener);
//
//    }
//
//    void addDeletionListener(DataSnapshot child, UserMessage userMessage) {
//
//        final DatabaseReference databaseReference =
//                child.child("isDeletedFor:" +
////                    (type == 0?"sender":"receiver")
//                                currentUserUid
//                ).getRef();
//
//        ValueEventListener valueEventListener;
//        databaseReference.addValueEventListener(valueEventListener = new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.getValue(Boolean.class)) {
//
//                    final DatabaseReference lastSeenRef =
//                            child.child(currentUserUid + ":LastSeenMessage").getRef();
//
//                    if (valueEventListeners.containsKey(lastSeenRef)) {
//                        lastSeenRef.removeEventListener(valueEventListeners.get(lastSeenRef));
//                    } else {
//                        Log.d("savedMessages", "this last seen listener doesn't exist");
//                    }
//
//                    final DatabaseReference messagesRef = child.child("messages").getRef();
//                    if (childEventListeners.containsKey(messagesRef)) {
//                        messagesRef.removeEventListener(childEventListeners.get(messagesRef));
//                    } else {
//                        Log.d("savedMessages", "this child messages listener doesn't exist");
//                    }
//
//
//                    valueEventListeners.remove(databaseReference);
//                    databaseReference.removeEventListener(this);
//
//                    final int index = userMessages.indexOf(userMessage);
//                    userMessages.remove(index);
//                    adapter.notifyItemRemoved(index);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//        valueEventListeners.put(databaseReference, valueEventListener);
//
//    }
//
//    void addLastSeenListener(DataSnapshot child, UserMessage userMessage) {
//
//        final DatabaseReference lastSeenRef =
//                child.child(currentUserUid + ":LastSeenMessage").getRef();
//
//        ValueEventListener valueEventListener;
//        lastSeenRef
////            .orderByValue().startAt(userMessage.getLastMessageRead()+1)
//                .addValueEventListener(valueEventListener = new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        Log.d("savedMessages", "last seen changed to: " +
//                                snapshot.getValue(Long.class));
//                        if (snapshot.exists()) {
//                            userMessage.setLastMessageRead(snapshot.getValue(Long.class));
//                            adapter.notifyItemChanged(userMessages.indexOf(userMessage));
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//                    }
//                });
//
//        valueEventListeners.put(lastSeenRef, valueEventListener);
//
//    }
//
//
//    void getNextPage() {
//
//        if (snapshots.size() > PAGINATION) {
//
//            for (int i = 0; i < PAGINATION; i++) {
//                addSavedMessageFromDataSnapshot(snapshots.get(i), true);
//            }
//
//            if (moreMessagesTv.getVisibility() == View.GONE) {
//                moreMessagesTv.setVisibility(View.VISIBLE);
//                moreMessagesTv.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        Log.d("savedMessages", "snapshots: " + snapshots.size());
//                        getNextPage();
//                    }
//                });
//            }
//
//        } else {
//
//            moreMessagesTv.setVisibility(View.GONE);
//            moreMessagesTv.setOnClickListener(null);
//
//            for (DataSnapshot child : snapshots) {
//                addSavedMessageFromDataSnapshot(child, true);
//            }
//        }
//
//    }

    @Override
    public void onMessagingUserClicked(int position) {

        UserMessage userMessage = userMessages.get(position);

        startActivity(
                new Intent(requireContext(), MessagingActivity.class)
                        .putExtra("messagingUserId", userMessage.getMessagingUserId())
                        .putExtra("intendedDeliveryID", userMessage.getChattingDestinationId()));

    }

    private void findAndRemoveUserMessage(DocumentSnapshot documentSnapshot) {

        for (int i = 0; i < userMessages.size(); i++) {

            UserMessage userMessage = userMessages.get(i);

            if (userMessage.getChattingDestinationId().equals(documentSnapshot.getId())) {

                userMessages.remove(i);
                adapter.notifyItemRemoved(i);

                break;
            }

        }

    }

    private void UserMessageModified(DocumentSnapshot changedSnapshot) {

        for (int i = 0; i < userMessages.size(); i++) {

            UserMessage userMessage = userMessages.get(i);

            if (userMessage.getChattingDestinationId().equals(changedSnapshot.getString("destinationID"))) {

                MessageMap oldMessageMap = userMessage.getChattingLatestMessageMap();

                MessageMap newMessageMap = new MessageMap((HashMap<String, Object>) changedSnapshot.get("lastMessage"));

                if (newMessageMap.getDeleted()) {

                    oldMessageMap.setDeleted(true);
                    adapter.notifyItemChanged(i);

                } else {

                    long lastSeen = changedSnapshot.getLong(currentUserUid + ":LastSeenMessage");

                    if (changedSnapshot.getLong("lastMessageTimeInMillis") >
                            oldMessageMap.getTime()) {

                        userMessage.setChattingLatestMessageMap(newMessageMap);
                        userMessage.setLastMessageRead(lastSeen);
                        userMessage.setMessagesCount(userMessage.getMessagesCount() + 1);

                        adapter.notifyItemChanged(i);
                    } else {

                        if (lastSeen > userMessage.getLastMessageRead()) {
                            userMessage.setLastMessageRead(lastSeen);
                            adapter.notifyItemChanged(i);
                        }

                    }
                }

                break;
            }
        }


    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (!snapshotListeners.isEmpty()) {
            for (ListenerRegistration listenerRegistration : snapshotListeners) {
                listenerRegistration.remove();
            }
        }
    }

    private class ScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (!isLoadingItems &&
                    !recyclerView.canScrollVertically(1) &&
                    newState == RecyclerView.SCROLL_STATE_IDLE) {

                Log.d("ttt", "is at bottom");
                getRealTimeMessagingUsers(false);

            }
        }
    }

}
