package com.developers.wajbaty.Adapters;

import android.content.Context;
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

import java.util.ArrayList;

public class AdditionalOptionsAdapter extends RecyclerView.Adapter<AdditionalOptionsAdapter.ServicesVh> {

  private final static int OPTION_LIMIT = 100;
  private final ArrayList<String> options;
  private final String option;

//  public interface ScrollToBottomListener{
//    void scrollToBottom();
//  }

  public AdditionalOptionsAdapter(ArrayList<String> options,
                                  String option) {
    this.options = options;
    this.option = option;
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

    options.add(option);
    notifyItemInserted(options.size());

  }

  @Override
  public int getItemCount() {
    return options.size();
  }

  public class ServicesVh extends RecyclerView.ViewHolder implements TextWatcher{

    private final EditText optionEd;
    private final ImageView addOptionIv;

    public ServicesVh(@NonNull View itemView) {
      super(itemView);
      optionEd = itemView.findViewById(R.id.optionEd);
      addOptionIv = itemView.findViewById(R.id.addOptionIv);

      optionEd.addTextChangedListener(this);

    }

    private void bind(int position){

//      optionEd.setHint(options.get(position));



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

      options.set(getAdapterPosition(),s.toString());

    }

    @Override
    public void afterTextChanged(Editable s) {

    }
  }
}
