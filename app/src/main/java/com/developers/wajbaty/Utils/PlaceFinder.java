
package com.developers.wajbaty.Utils;

import com.developers.wajbaty.Models.PlaceResponse;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by cristianjgomez on 9/4/15.
 */
public class PlaceFinder {
    //region Constants
    public static final String API_URL = "https://maps.googleapis.com";
    //endregion

    //region Constructors
    private PlaceFinder() {

    }
    //endregion

    //region Interfaces

    /**
     * Defines
     */
    public interface GooglePlaces {
        @GET("/maps/api/place/nearbysearch/json")
        Call<PlaceResponse> getPlaceByLocationRequest(
                @Query("location") String location,
                @Query("radius") int radius,
                @Query("key") String key,
                @Query("language") String language
        );
    }
    //endregion

    //region Getters and setters
    public static GooglePlaces getService() {
        Retrofit retrofit =  new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(API_URL)
                .build();
        return retrofit.create(GooglePlaces.class);
    }
    //endregion
}