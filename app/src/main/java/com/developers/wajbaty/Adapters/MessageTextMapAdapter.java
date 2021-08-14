package com.developers.wajbaty.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.developers.wajbaty.Models.MessageMap;
import com.developers.wajbaty.R;
import com.google.firebase.auth.FirebaseAuth;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MessageTextMapAdapter extends RecyclerView.Adapter<MessageTextMapAdapter.ViewHolder> {

    private static final Date date = new Date();
    private static final int
            MSG_TYPE_LEFT = 0,
            MSG_TYPE_RIGHT = 1;
    private final DateFormat
            hourMinuteFormat = new SimpleDateFormat("h:mm a", Locale.getDefault()),
            withoutYearFormat = new SimpleDateFormat("h:mm a MMM dd", Locale.getDefault()),
            formatter = new SimpleDateFormat("h:mm a yyyy MMM dd", Locale.getDefault()),
            todayYearFormat = new SimpleDateFormat("yyyy", Locale.getDefault()),
            todayYearMonthDayFormat = new SimpleDateFormat("yyyy MMM dd", Locale.getDefault());
    private final ArrayList<MessageMap> messages;
    private final Context context;
    private final DeleteMessageListener deleteMessageListener;
    private final String currentUid;
    private boolean longCLickEnabled = true;

    public MessageTextMapAdapter(ArrayList<MessageMap> messages,
                                 Context context,
                                 DeleteMessageListener deleteMessageListener) {
        this.messages = messages;
        this.context = context;
        this.deleteMessageListener = deleteMessageListener;
        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public void disableLongClick() {
        longCLickEnabled = false;
    }

    @NonNull
    @Override
    public MessageTextMapAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            return new ViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_sent_chat, parent, false));
        } else {
            return new ViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_received_chat, parent, false));
        }
    }

    @Override
    public long getItemId(int position) {
        return messages.get(position).hashCode();
    }

    @Override
    public void onBindViewHolder(@NonNull MessageTextMapAdapter.ViewHolder holder, int position) {
        holder.addMessage(messages.get(position));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {

        if (messages.get(position).getSender().equals(currentUid)) {
            return MSG_TYPE_RIGHT;
        }

        return MSG_TYPE_LEFT;
    }

    void showMessageDeleteDialog(MessageMap messageMap) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle("Do you want to delete this message?");
        alert.setPositiveButton("Delete", (dialog, which) -> {
            deleteMessageListener.deleteMessage(messageMap, dialog);
        });
        alert.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        alert.create().show();
    }

    private String getTimeFormatted(long time) {

        if (time < 1000000000000L) {
            time *= 1000;
        }
        if (todayYearMonthDayFormat.format(date)
                .equals(todayYearMonthDayFormat.format(time))) {
            return hourMinuteFormat.format(time);

        } else if (todayYearFormat.format(date).equals(todayYearFormat.format(time))) {
            return withoutYearFormat.format(time);
        } else {
            return formatter.format(time);
        }
    }

    public interface DeleteMessageListener {
        void deleteMessage(MessageMap messageMap, DialogInterface dialog);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener,
            View.OnClickListener {

        private final TextView messageTv, messageTimeTv;
        private boolean timeIsVisible;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            messageTv = itemView.findViewById(R.id.messageTv);
            messageTimeTv = itemView.findViewById(R.id.messageTimeTv);

            itemView.setOnLongClickListener(this);
            itemView.setOnClickListener(this);
        }

        void addMessage(MessageMap message) {

            if (message == null)
                return;

            if (!message.getDeleted()) {
                messageTv.setText(message.getContent());
            } else {
                messageTv.setText("تم حذف هذه الرسالة");
            }


            if (message.getDeleted()) {
                itemView.setOnLongClickListener(null);
            }

            if (timeIsVisible) {
                messageTimeTv.setText(getTimeFormatted(messages.get(getAdapterPosition()).getTime()));
                messageTimeTv.setVisibility(View.VISIBLE);
            } else {
                messageTimeTv.setVisibility(View.GONE);
            }

        }

        @Override
        public boolean onLongClick(View view) {

            final MessageMap message = messages.get(getAdapterPosition());

            if (message.getSender().equals(currentUid)) {
                showMessageDeleteDialog(message);
            }

            return longCLickEnabled;
        }

        @Override
        public void onClick(View view) {


            if (messageTimeTv.getVisibility() == View.GONE) {
                messageTimeTv.setText(getTimeFormatted(messages.get(getAdapterPosition()).getTime()));
                messageTimeTv.setVisibility(View.VISIBLE);
                timeIsVisible = true;
            } else {
                messageTimeTv.setVisibility(View.GONE);
                timeIsVisible = false;
            }

        }

    }

}
