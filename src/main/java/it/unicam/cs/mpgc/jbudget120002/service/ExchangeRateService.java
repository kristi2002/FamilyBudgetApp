package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.model.ExchangeRate;
import java.math.BigDecimal;
import java.util.Optional;

public interface ExchangeRateService {
    ExchangeRate createExchangeRate(String fromCurrency, String toCurrency, BigDecimal rate);
    Optional<ExchangeRate> findExchangeRate(String fromCurrency, String toCurrency);
    void updateExchangeRate(String fromCurrency, String toCurrency, BigDecimal rate);
    BigDecimal convertAmount(BigDecimal amount, String fromCurrency, String toCurrency);
    void refreshExchangeRates();
} 