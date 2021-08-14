package com.developers.wajbaty;

import android.content.Context;
import android.util.Log;

import com.developers.wajbaty.Models.PartneredRestaurant;
import com.developers.wajbaty.Utils.GeocoderUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ExampleUnitTest {


    @Test
    public void phoneNumberIsValid() {

            final Phonenumber.PhoneNumber newNum = new Phonenumber.PhoneNumber();
            newNum.setCountryCode(+972).setNationalNumber(592436189);
           assertTrue(PhoneNumberUtil.getInstance().isValidNumber(newNum));

    }

  @Test
    public void phoneNumberIsInValid() {

            final Phonenumber.PhoneNumber newNum = new Phonenumber.PhoneNumber();
            newNum.setCountryCode(+973).setNationalNumber(592436189);
           assertTrue(PhoneNumberUtil.getInstance().isValidNumber(newNum));

    }


    @Test
    public void searchForRestaurantByName() {

        //adding 5 restaurants to the list for searching
        final ArrayList<PartneredRestaurant.NearbyPartneredRestaurant>
                nearbyRestaurants = new ArrayList<>();

        final String[] restaurantNames = {"Thailende", "Taboon",
                "Light House", "Al Dar", "Mazaj"};

        for (int i = 0; i < 5; i++) {

            PartneredRestaurant.NearbyPartneredRestaurant nearbyPartneredRestaurant =
                    new PartneredRestaurant.NearbyPartneredRestaurant(
                            String.valueOf(i),
                            restaurantNames[i],
                            null,
                            null,
                            0,
                            0
                    );
            nearbyRestaurants.add(nearbyPartneredRestaurant);
        }

        String searchQuery = "burger";

        searchQuery = searchQuery.toLowerCase();

        final List<String> results = new ArrayList<>();

        for (PartneredRestaurant.NearbyPartneredRestaurant restaurant: nearbyRestaurants) {

            if (restaurant.getName().equalsIgnoreCase(searchQuery) ||
                    restaurant.getName().toLowerCase().contains(searchQuery) ||
                    searchQuery.contains(restaurant.getName().toLowerCase())) {

                results.add(restaurant.getName());

            }
        }


        System.out.println("Restaurants found: "+results.size());
        System.out.println("Which are:");
        for(String result:results){
            System.out.println(result);

        }
        assertEquals(results.size(),1);

    }




}