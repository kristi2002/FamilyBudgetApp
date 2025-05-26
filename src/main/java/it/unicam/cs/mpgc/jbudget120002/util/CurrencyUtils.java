package it.unicam.cs.mpgc.jbudget120002.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

public class CurrencyUtils {
    private static final Map<String, Currency> SUPPORTED_CURRENCIES = new HashMap<>();
    private static final String DEFAULT_CURRENCY = "EUR";
    
    static {
        SUPPORTED_CURRENCIES.put("EUR", Currency.getInstance("EUR"));
        SUPPORTED_CURRENCIES.put("USD", Currency.getInstance("USD"));
        SUPPORTED_CURRENCIES.put("GBP", Currency.getInstance("GBP"));
        SUPPORTED_CURRENCIES.put("JPY", Currency.getInstance("JPY"));
        SUPPORTED_CURRENCIES.put("CHF", Currency.getInstance("CHF"));
    }

    public static String formatAmount(BigDecimal amount) {
        return formatAmount(amount, DEFAULT_CURRENCY);
    }

    public static String formatAmount(BigDecimal amount, String currencyCode) {
        if (amount == null) {
            return "0.00";
        }

        Currency currency = SUPPORTED_CURRENCIES.getOrDefault(
            currencyCode, SUPPORTED_CURRENCIES.get(DEFAULT_CURRENCY));
        
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.getDefault());
        formatter.setCurrency(currency);
        
        return formatter.format(amount);
    }

    public static String getSymbol(String currencyCode) {
        Currency currency = SUPPORTED_CURRENCIES.getOrDefault(
            currencyCode, SUPPORTED_CURRENCIES.get(DEFAULT_CURRENCY));
        return currency.getSymbol();
    }

    public static String[] getSupportedCurrencies() {
        return SUPPORTED_CURRENCIES.keySet().toArray(new String[0]);
    }

    public static boolean isValidCurrency(String currencyCode) {
        return SUPPORTED_CURRENCIES.containsKey(currencyCode);
    }

    public static String getDefaultCurrency() {
        return DEFAULT_CURRENCY;
    }
} 