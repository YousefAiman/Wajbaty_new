package com.developers.wajbaty.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.developers.wajbaty.R;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

public class FillOptionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static int OPTION_LIMIT = 100, TYPE_FILLED = 1, TYPE_FIELD = 2;
    private final List<String> options;

    public FillOptionsAdapter(List<String> options) {
        this.options = options;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == TYPE_FIELD) {
            return new FieldOptionVh(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_additional_options_layout, parent, false));
        }

        return new FilledOptionVh(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_fill_option, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof FieldOptionVh) {

            ((FieldOptionVh) holder).bind();

        } else {

            ((FilledOptionVh) holder).bind(options.get(position));

        }

    }

    @Override
    public int getItemViewType(int position) {

        if (position == options.size() - 1) {
            return TYPE_FIELD;
        }

        return TYPE_FILLED;

    }


    private void addPollItem(String filedName, int position) {

        options.set(position, filedName);
        notifyItemChanged(options.size() - 1);

        if (options.size() != OPTION_LIMIT) {
            options.add("");
            notifyItemInserted(options.size());
        }

    }

    @Override
    public int getItemCount() {
        return options.size();
    }

    public class FieldOptionVh extends RecyclerView.ViewHolder {

        private final EditText optionEd;
        private final ImageView addOptionIv;

        public FieldOptionVh(@NonNull View itemView) {
            super(itemView);
            optionEd = itemView.findViewById(R.id.optionEd);
            addOptionIv = itemView.findViewById(R.id.addOptionIv);
        }

        private void bind() {

//      optionEd.setHint(options.get(position));

//      if (position == getItemCount() - 1 && position != OPTION_LIMIT) {

            addOptionIv.setVisibility(View.VISIBLE);
            addOptionIv.setOnClickListener(v -> {

                final String text = optionEd.getText().toString();
                if (!text.isEmpty()) {
                    addOptionIv.setVisibility(View.INVISIBLE);
                    addPollItem(text, getAdapterPosition());
                }

            });

//      } else {
//        addOptionIv.setVisibility(View.INVISIBLE);
//        addOptionIv.setOnClickListener(null);
//      }

        }

    }

    public class FilledOptionVh extends RecyclerView.ViewHolder {

        private final EditText optionEd;
        private final TextInputLayout optionInputLayout;

        public FilledOptionVh(@NonNull View itemView) {
            super(itemView);
            optionEd = itemView.findViewById(R.id.optionEd);
            optionInputLayout = itemView.findViewById(R.id.optionInputLayout);
        }

        private void bind(String hint) {

            if (hint.toLowerCase().contains("number")) {
                optionEd.setInputType(EditorInfo.TYPE_CLASS_PHONE);
            } else if (hint.toLowerCase().contains("email")) {
                optionEd.setInputType(EditorInfo.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS);
            }

            optionInputLayout.setHint(hint);
        }

    }

}
