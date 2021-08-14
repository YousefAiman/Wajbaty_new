package com.developers.wajbaty.Adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.developers.wajbaty.R;

import java.util.List;

public class AdditionalOptionsAdapter extends RecyclerView.Adapter<AdditionalOptionsAdapter.ServicesVh> {

    private final static int OPTION_LIMIT = 100;
    private final List<String> options;
    private final String hint;

    public AdditionalOptionsAdapter(List<String> options, String hint) {
        this.options = options;
        this.hint = hint;
    }

    @NonNull
    @Override
    public ServicesVh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ServicesVh(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_additional_options_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ServicesVh holder, int position) {
        holder.bind(position);
    }

    private void addPollItem() {

        options.add("");
        notifyItemInserted(options.size());

    }

    @Override
    public int getItemCount() {
        return options.size();
    }

    public class ServicesVh extends RecyclerView.ViewHolder implements TextWatcher {

        private final ImageView addOptionIv;
        private final EditText optionEd;

        public ServicesVh(@NonNull View itemView) {
            super(itemView);

            optionEd = itemView.findViewById(R.id.optionEd);
            addOptionIv = itemView.findViewById(R.id.addOptionIv);

            optionEd.addTextChangedListener(this);

        }

        private void bind(int position) {

            optionEd.setHint(hint + " " + position);


            if (position == getItemCount() - 1 && position != OPTION_LIMIT) {
                addOptionIv.setVisibility(View.VISIBLE);
                addOptionIv.setOnClickListener(v -> {
                    addOptionIv.setVisibility(View.INVISIBLE);
                    addPollItem();
                });
            } else {
                addOptionIv.setVisibility(View.INVISIBLE);
                addOptionIv.setOnClickListener(null);
            }

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            options.set(getAdapterPosition(), s.toString());

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }
}
