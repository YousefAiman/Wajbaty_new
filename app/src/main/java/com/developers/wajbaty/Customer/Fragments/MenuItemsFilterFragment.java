package com.developers.wajbaty.Customer.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.developers.wajbaty.Adapters.SelectableChecksAdapter;
import com.developers.wajbaty.Models.SelectableItem;
import com.developers.wajbaty.R;

import java.util.ArrayList;

public class MenuItemsFilterFragment extends DialogFragment implements View.OnClickListener,
        SelectableChecksAdapter.SelectListener {

    private static final String CATEGORY = "category",
            FILTER = "filter";

    private MenuItemsFilterListener menuItemsFilterListener;

    //views
    private Toolbar filterToolBar;
    private RecyclerView filterFiltersRv;
    private Button filterFilterBtn;

    //filter
    private String filter;
    private SelectableChecksAdapter selectableFilterAdapter;
    private ArrayList<SelectableItem> selectableFilterItems;


    public MenuItemsFilterFragment() {
    }

    public static MenuItemsFilterFragment newInstance(MenuItemsFilterListener menuItemsFilterListener, String category, String filter) {
        MenuItemsFilterFragment fragment = new MenuItemsFilterFragment();
        fragment.menuItemsFilterListener = menuItemsFilterListener;
        Bundle args = new Bundle();
        args.putString(CATEGORY, category);
        args.putString(FILTER, filter);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogTheme);

        if (getArguments() != null) {
            filter = getArguments().getString(FILTER);
        }


        selectableFilterItems = new ArrayList<>();
        selectableFilterAdapter = new SelectableChecksAdapter(selectableFilterItems, this, -1,
                "filter");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu_items_filter, container, false);
        filterToolBar = view.findViewById(R.id.filterToolBar);
        filterFiltersRv = view.findViewById(R.id.filterFiltersRv);
        filterFilterBtn = view.findViewById(R.id.filterFilterBtn);

        filterFiltersRv.setAdapter(selectableFilterAdapter);

        filterToolBar.setNavigationOnClickListener(v -> dismiss());
        filterFilterBtn.setOnClickListener(this);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fetchFilterSelectables();
    }

    private void fetchFilterSelectables() {

        selectableFilterItems.add(new SelectableItem("price", "Price"));
        selectableFilterItems.add(new SelectableItem("rating", "Rating"));
        selectableFilterItems.add(new SelectableItem("favoriteCount", "Favourites"));


        if (filter != null) {
            for (int i = 0; i < selectableFilterItems.size(); i++) {
                if (filter.equals(selectableFilterItems.get(i).getID())) {
                    selectableFilterAdapter.setSelectedItem(i);

                    break;
                }
            }
        }

        selectableFilterAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == filterFilterBtn.getId()) {

            menuItemsFilterListener.onFilterSelected(null, filter);
            dismiss();

        }

    }

    @Override
    public void itemSelected(int position, String type) {

        if (type.equals("filter")) {

            filter = selectableFilterItems.get(position).getID();

            final int previousSelected = selectableFilterAdapter.getSelectedItem();

            if (previousSelected == position) {

                selectableFilterAdapter.setSelectedItem(-1);
                selectableFilterAdapter.notifyItemChanged(previousSelected);
                filter = null;

            } else {

                filter = selectableFilterItems.get(position).getID();
                selectableFilterAdapter.setSelectedItem(position);
                selectableFilterAdapter.notifyItemChanged(position);

                if (previousSelected != -1) {
                    selectableFilterAdapter.notifyItemChanged(previousSelected);
                }

            }


        }


    }


    public interface MenuItemsFilterListener {
        void onFilterSelected(String category, String filterBy);
    }
}