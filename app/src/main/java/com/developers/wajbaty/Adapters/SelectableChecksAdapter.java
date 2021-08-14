package com.developers.wajbaty.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.developers.wajbaty.Models.SelectableItem;
import com.developers.wajbaty.R;

import java.util.ArrayList;

public class SelectableChecksAdapter extends RecyclerView.Adapter<SelectableChecksAdapter.SelectableItemVH> {

    private static final int TYPE_UNSELECTED = 1, TYPE_SELECTED = 2;
    private final ArrayList<SelectableItem> selectableItems;
    private final SelectListener selectListener;
    private int selectedItem;
    private String selectableType;

    public SelectableChecksAdapter(ArrayList<SelectableItem> selectableItems,
                                   SelectListener selectListener,
                                   int selectedItem) {
        this.selectableItems = selectableItems;
        this.selectListener = selectListener;
        this.selectedItem = selectedItem;
    }

    public SelectableChecksAdapter(ArrayList<SelectableItem> selectableItems,
                                   SelectListener selectListener,
                                   int selectedItem,
                                   String selectableType) {
        this.selectableItems = selectableItems;
        this.selectListener = selectListener;
        this.selectedItem = selectedItem;
        this.selectableType = selectableType;
    }

    public int getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(int selectedItem) {
        this.selectedItem = selectedItem;
    }

    @NonNull
    @Override
    public SelectableItemVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new SelectableItemVH(LayoutInflater.from(parent.getContext())
                .inflate(viewType == TYPE_UNSELECTED ?
                                R.layout.item_unselected_check : R.layout.item_selected_check,
                        parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull SelectableItemVH holder, int position) {
        holder.bind(selectableItems.get(position));
    }

    @Override
    public int getItemCount() {
        return selectableItems.size();
    }

    @Override
    public int getItemViewType(int position) {

        if (position == selectedItem) {
            return TYPE_SELECTED;
        }

        return TYPE_UNSELECTED;
    }

    public interface SelectListener {
        void itemSelected(int position, String type);
    }

    public class SelectableItemVH extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView selectableTv;

        public SelectableItemVH(@NonNull View itemView) {
            super(itemView);
            selectableTv = itemView.findViewById(R.id.selectableTv);
            itemView.setOnClickListener(this);
        }

        private void bind(SelectableItem selectableItem) {

            selectableTv.setText(selectableItem.getName());

        }

        @Override
        public void onClick(View v) {

            selectListener.itemSelected(getAdapterPosition(), selectableType);
        }
    }
}
