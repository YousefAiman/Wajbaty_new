package com.developers.wajbaty.Adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import androidx.appcompat.widget.SearchView;

import com.developers.wajbaty.R;

public class SearchViewSuggestionAdapter extends CursorAdapter  {


    private final int itemLayout;
    private final SearchView searchView;
    private final PlaceClickListener placeClickListener;

    public interface PlaceClickListener{
        void choosePlace(String restaurantName);
    }

    public SearchViewSuggestionAdapter(Context context, Cursor c, boolean autoRequery,
                                       int itemLayout,SearchView searchView,
                                       PlaceClickListener placeClickListener) {
        super(context, c, autoRequery);
        this.itemLayout = itemLayout;
        this.searchView = searchView;
        this.placeClickListener = placeClickListener;

    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(itemLayout,parent,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        final String restaurantName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
        final String restaurantAddress = cursor.getString(cursor.getColumnIndexOrThrow("address"));

        ((TextView) view.findViewById(R.id.placeNameTv)).setText(restaurantName);
        ((TextView) view.findViewById(R.id.placeAddressTv)).setText(restaurantAddress);


        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                placeClickListener.choosePlace(restaurantName);
            }
        });

    }
}
