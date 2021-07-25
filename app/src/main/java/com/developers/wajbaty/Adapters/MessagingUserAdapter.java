package com.developers.wajbaty.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.developers.wajbaty.Models.MessageMap;
import com.developers.wajbaty.Models.UserMessage;
import com.developers.wajbaty.R;
import com.developers.wajbaty.Utils.TimeFormatter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class MessagingUserAdapter extends RecyclerView.Adapter<MessagingUserAdapter.ViewHolder> {

    private final List<UserMessage> chattingUsers;
    private static MessagingUserListener messagingUserListener;
    private final HashMap<String,String> userImageURLsMap,userUserNamesMap;
    private final CollectionReference userRef;

    public MessagingUserAdapter(List<UserMessage> chattingUsers, MessagingUserListener messagingUserListener) {
        this.chattingUsers = chattingUsers;
        MessagingUserAdapter.messagingUserListener = messagingUserListener;
        userImageURLsMap = new HashMap<>();
        userUserNamesMap = new HashMap<>();
        userRef = FirebaseFirestore.getInstance().collection("Users");
    }

    public interface MessagingUserListener{
        void onMessagingUserClicked(int position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_message, parent,
                false));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        final UserMessage userMessage = chattingUsers.get(position);

    }

    @Override
    public int getItemCount() {
        return chattingUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final ImageView chattingUserImageView;
        private final TextView chattingUserNameTv;
        private final TextView chattingPromotionIdTv;
        private final TextView chattingMessageTimeStampTv;
        private final TextView chattinglatestMessageTv;
        private final TextView unreadMessagesCountTv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            chattingUserImageView = itemView.findViewById(R.id.ChatterImageView);
            chattingUserNameTv = itemView.findViewById(R.id.ChatterUserNameTv);
            chattingPromotionIdTv = itemView.findViewById(R.id.ChattingPromotionIdTv);
            chattingMessageTimeStampTv = itemView.findViewById(R.id.LastChattingMessageTimeTv);
            chattinglatestMessageTv = itemView.findViewById(R.id.LatestMessageTv);
            unreadMessagesCountTv = itemView.findViewById(R.id.unreadMessagesCountTv);

            itemView.setOnClickListener(this);
        }

        void bind(UserMessage userMessage){

            String senderID = userMessage.getChattingLatestMessageMap().getSender();

            if(userUserNamesMap.containsKey(senderID)){

                Picasso.get().load(userImageURLsMap.get(senderID)).fit()
                        .centerCrop().into(chattingUserImageView);

                chattingUserNameTv.setText(userUserNamesMap.get(senderID));

            }else{
                getUserInfo(senderID,chattingUserImageView,chattingUserNameTv);
            }


            final long messageUnread = userMessage.getMessagesCount() - userMessage.getLastMessageRead();
            if (messageUnread == 0) {
                unreadMessagesCountTv.setVisibility(View.INVISIBLE);
                chattinglatestMessageTv.setTextColor(ContextCompat.getColor(itemView.getContext(),
                        R.color.grey));
            } else {

                unreadMessagesCountTv.setVisibility(View.VISIBLE);
                unreadMessagesCountTv.setText(messageUnread + "");
                chattinglatestMessageTv.setTextColor(ContextCompat.getColor(itemView.getContext(),
                        R.color.light_black));

            }

            if (!userMessage.getChattingLatestMessageMap().getDeleted()) {
                chattinglatestMessageTv.setText
                        (userMessage.getChattingLatestMessageMap().getContent());
            } else {
                chattinglatestMessageTv.setText("لقد تم حذف هذه الرسالة");
            }


            chattingMessageTimeStampTv.setText(
                    TimeFormatter.formatTime(userMessage.getChattingLatestMessageMap().getTime()));


            chattingPromotionIdTv.setText(userMessage.getChattingDestinationId() + "");

        }

        @Override
        public void onClick(View v) {
//            if (WifiUtil.checkWifiConnection(context)) {

            messagingUserListener.onMessagingUserClicked(getAdapterPosition());

            }
//        }
    }

    private void getUserInfo(String userID,ImageView userIv,TextView usernameTv){

        userRef.document(userID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if(documentSnapshot.exists()){

                    final String imageURL = documentSnapshot.getString("imageURL"),
                            username = documentSnapshot.getString("imageURL");

                    if(imageURL!=null){
                        Picasso.get().load(imageURL).fit().centerCrop().into(userIv);
                    }

                    usernameTv.setText(username);

                    userImageURLsMap.put(userID,imageURL);
                    userUserNamesMap.put(userID,username);

                }

            }
        });

    }

}
