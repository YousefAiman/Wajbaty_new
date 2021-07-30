package com.developers.wajbaty.Customer.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.developers.wajbaty.Adapters.SelectableChecksAdapter;
import com.developers.wajbaty.Models.SelectableItem;
import com.developers.wajbaty.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Locale;

public class MenuItemsFilterFragment extends DialogFragment implements View.OnClickListener,
        SelectableChecksAdapter.SelectListener{

    private static final String CATEGORY = "category",
                            FILTER = "filter";

    private MenuItemsFilterListener menuItemsFilterListener;

    //views
    private Toolbar filterToolBar;
    private RecyclerView filterCategoriesRv,filterFiltersRv;
    private Button filterFilterBtn;

    //categories
    private String category;
    private SelectableChecksAdapter selectableCategoriesAdapter;
    private ArrayList<SelectableItem> selectableCategoryItems;

    //filter
    private String filter;
    private SelectableChecksAdapter selectableFilterAdapter;
    private ArrayList<SelectableItem> selectableFilterItems;


    public interface MenuItemsFilterListener{
        void onFilterSelected(String category,String filterBy);
    }

    public MenuItemsFilterFragment() {
    }

    public static MenuItemsFilterFragment newInstance(MenuItemsFilterListener menuItemsFilterListener,String category,String filter) {
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
            category = getArguments().getString(CATEGORY);
            filter = getArguments().getString(FILTER);
        }


        selectableCategoryItems = new ArrayList<>();
        selectableCategoriesAdapter = new SelectableChecksAdapter(selectableCategoryItems,this,-1,
                "category");


        selectableFilterItems = new ArrayList<>();
        selectableFilterAdapter = new SelectableChecksAdapter(selectableFilterItems,this,-1,
                "filter");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_menu_items_filter, container, false);
        filterToolBar = view.findViewById(R.id.filterToolBar);
        filterCategoriesRv = view.findViewById(R.id.filterCategoriesRv);
        filterFiltersRv = view.findViewById(R.id.filterFiltersRv);
        filterFilterBtn = view.findViewById(R.id.filterFilterBtn);

        filterCategoriesRv.setAdapter(selectableCategoriesAdapter);
        filterFiltersRv.setAdapter(selectableFilterAdapter);

        filterToolBar.setNavigationOnClickListener(v-> dismiss());
        filterFilterBtn.setOnClickListener(this);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fetchFilters();


        fetchFilterSelectables();
    }

    private void fetchFilters(){

        final String language = Locale.getDefault().getLanguage().equals("ar")?"ar":"en";

        FirebaseFirestore.getInstance().collection("GeneralOptions").document("Categories")
                .collection("Categories")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            boolean categoryFound;
            @Override
            public void onSuccess(QuerySnapshot snapshots) {

                final String name = "name_"+language;

                if(snapshots!=null && !snapshots.isEmpty()){

                    for(DocumentSnapshot documentSnapshot:snapshots){

                        selectableCategoryItems.add(new SelectableItem(documentSnapshot.getId(),documentSnapshot.getString(name)));

                        if(!categoryFound && category!=null){
                            if (category.equals(documentSnapshot.getId())) {
                                categoryFound = true;
                                selectableCategoriesAdapter.setSelectedItem(selectableCategoryItems.size()-1);
                            }
                        }

                    }

                }
            }
        }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                selectableCategoriesAdapter.notifyDataSetChanged();
            }
        });

    }

    private void fetchFilterSelectables(){

        selectableFilterItems.add(new SelectableItem("price","Price"));
        selectableFilterItems.add(new SelectableItem("rating","Rating"));
        selectableFilterItems.add(new SelectableItem("favoriteCount","Favourites"));


        if(filter!=null){
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

        if(v.getId() == filterFilterBtn.getId()){

            menuItemsFilterListener.onFilterSelected(category,filter);
            dismiss();

        }

    }



    @Override
    public void itemSelected(int position,String type) {

        if(type.equals("category")){
              final int previousSelected = selectableCategoriesAdapter.getSelectedItem();

                if(previousSelected == position){

                    selectableCategoriesAdapter.setSelectedItem(-1);
                    selectableCategoriesAdapter.notifyItemChanged(previousSelected);
                    category = null;

                }else{

                    category = selectableCategoryItems.get(position).getID();
                    selectableCategoriesAdapter.setSelectedItem(position);
                    selectableCategoriesAdapter.notifyItemChanged(position);

                    if(previousSelected != -1){
                        selectableCategoriesAdapter.notifyItemChanged(previousSelected);
                    }

                }

        }else if(type.equals("filter")){

            filter = selectableFilterItems.get(position).getID();

            final int previousSelected = selectableFilterAdapter.getSelectedItem();

            if(previousSelected == position){

                selectableFilterAdapter.setSelectedItem(-1);
                selectableFilterAdapter.notifyItemChanged(previousSelected);
                filter = null;

            }else{

                filter = selectableFilterItems.get(position).getID();
                selectableFilterAdapter.setSelectedItem(position);
                selectableFilterAdapter.notifyItemChanged(position);

                if(previousSelected != -1){
                    selectableFilterAdapter.notifyItemChanged(previousSelected);
                }

            }


        }



    }
}