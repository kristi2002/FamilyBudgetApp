package it.unicam.cs.mpgc.jbudget120002.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class providing currency formatting, parsing, and conversion methods
 * for the Family Budget App. This class centralizes currency-related operations
 * to ensure consistency and accuracy across the application.
 *
 * Responsibilities:
 * - Format and parse currency amounts
 * - Provide currency symbol and code lookups
 * - Support multi-currency conversions and calculations
 * - Handle locale-specific currency formatting
 *
 * Usage:
 * Used throughout the application for consistent currency handling in
 * models, services, controllers, and UI components.
 */
public class CurrencyUtils {
    private static final Map<String, Currency> SUPPORTED_CURRENCIES = new HashMap<>();
    private static final String DEFAULT_CURRENCY = "EUR";
    private static final List<String> COMMON_CURRENCIES = Arrays.asList(
        "EUR", "USD", "GBP", "JPY", "CHF", "CAD", "AUD", "CNY", "INR", "BRL"
    );
    
    static {
        // Initialize common currencies
        COMMON_CURRENCIES.forEach(code -> {
            try {
                SUPPORTED_CURRENCIES.put(code, Currency.getInstance(code));
            } catch (IllegalArgumentException e) {
                // Log error but continue
                System.err.println("Failed to initialize currency: " + code);
            }
        });
    }

    public static String formatAmount(BigDecimal amount) {
        return formatAmount(amount, DEFAULT_CURRENCY);
    }

    public static String formatAmount(BigDecimal amount, String currencyCode) {
        return formatAmount(amount, currencyCode, Locale.getDefault());
    }

    public static String formatAmount(BigDecimal amount, String currencyCode, Locale locale) {
        if (amount == null) {
            return "0.00";
        }

        Currency currency = SUPPORTED_CURRENCIES.getOrDefault(
            currencyCode, SUPPORTED_CURRENCIES.get(DEFAULT_CURRENCY));
        
        NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
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

    public static List<String> getCommonCurrencies() {
        return COMMON_CURRENCIES;
    }

    public static boolean isValidCurrency(String currencyCode) {
        return SUPPORTED_CURRENCIES.containsKey(currencyCode);
    }

    public static String getDefaultCurrency() {
        return DEFAULT_CURRENCY;
    }

    public static int getFractionDigits(String currencyCode) {
        Currency currency = SUPPORTED_CURRENCIES.getOrDefault(
            currencyCode, SUPPORTED_CURRENCIES.get(DEFAULT_CURRENCY));
        return currency.getDefaultFractionDigits();
    }

    public static String getDisplayName(String currencyCode) {
        Currency currency = SUPPORTED_CURRENCIES.getOrDefault(
            currencyCode, SUPPORTED_CURRENCIES.get(DEFAULT_CURRENCY));
        return currency.getDisplayName();
    }

    public static String getDisplayName(String currencyCode, Locale locale) {
        Currency currency = SUPPORTED_CURRENCIES.getOrDefault(
            currencyCode, SUPPORTED_CURRENCIES.get(DEFAULT_CURRENCY));
        return currency.getDisplayName(locale);
    }
} 