package com.developers.wajbaty.Utils;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

public class CurrencyUtil {

    public static void main(String[] args) {

        System.out.println(Currency.getInstance(Locale.getDefault()).getDisplayName());
        System.out.println(Currency.getInstance(Locale.getDefault()).getCurrencyCode());
        System.out.println(Currency.getInstance(Locale.getDefault()).getSymbol());

    }


    public static String getCurrencyCodeFromName(String code) {

        for (Currency currency : Currency.getAvailableCurrencies()) {
            if (currency.getCurrencyCode().equals(code)) {
                return currency.getCurrencyCode();
            }
        }
        return null;
    }

    public static String getDefaultCurrencyCode() {

        return Currency.getInstance(Locale.getDefault()).getCurrencyCode();

    }

    public static ArrayList<String> getCurrencies() {

        final List<Currency> supportedCurrencyCodes =
                new ArrayList<>(Currency.getAvailableCurrencies());

        final ArrayList<String> currencies = new ArrayList<>(supportedCurrencyCodes.size());

        for (Currency code : supportedCurrencyCodes) {
            currencies.add(code.getCurrencyCode());
        }

        return currencies;

    }


}
