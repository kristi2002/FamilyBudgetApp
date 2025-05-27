package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.model.ExchangeRate;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

public class ExchangeRateServiceImpl extends BaseService implements ExchangeRateService {
    
    public ExchangeRateServiceImpl(EntityManager entityManager) {
        super(entityManager);
    }

    @Override
    public ExchangeRate createExchangeRate(String fromCurrency, String toCurrency, BigDecimal rate) {
        beginTransaction();
        try {
            ExchangeRate exchangeRate = new ExchangeRate(fromCurrency, toCurrency, rate);
            entityManager.persist(exchangeRate);
            commitTransaction();
            return exchangeRate;
        } catch (Exception e) {
            rollbackTransaction();
            throw e;
        }
    }

    @Override
    public Optional<ExchangeRate> findExchangeRate(String fromCurrency, String toCurrency) {
        TypedQuery<ExchangeRate> query = entityManager.createQuery(
            "SELECT e FROM ExchangeRate e WHERE e.fromCurrency = :from AND e.toCurrency = :to",
            ExchangeRate.class);
        query.setParameter("from", fromCurrency);
        query.setParameter("to", toCurrency);
        
        try {
            return Optional.of(query.getSingleResult());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public void updateExchangeRate(String fromCurrency, String toCurrency, BigDecimal rate) {
        beginTransaction();
        try {
            findExchangeRate(fromCurrency, toCurrency).ifPresent(exchangeRate -> {
                exchangeRate.setRate(rate);
                entityManager.merge(exchangeRate);
            });
            commitTransaction();
        } catch (Exception e) {
            rollbackTransaction();
            throw e;
        }
    }

    @Override
    public BigDecimal convertAmount(BigDecimal amount, String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return amount;
        }

        Optional<ExchangeRate> exchangeRate = findExchangeRate(fromCurrency, toCurrency);
        if (exchangeRate.isPresent()) {
            return amount.multiply(exchangeRate.get().getRate())
                .setScale(2, RoundingMode.HALF_UP);
        }

        // Try reverse conversion
        exchangeRate = findExchangeRate(toCurrency, fromCurrency);
        if (exchangeRate.isPresent()) {
            return amount.divide(exchangeRate.get().getRate(), 2, RoundingMode.HALF_UP);
        }

        throw new IllegalStateException("No exchange rate found for " + fromCurrency + " to " + toCurrency);
    }

    @Override
    public void refreshExchangeRates() {
        // TODO: Implement API call to fetch latest exchange rates
        // This would typically call an external API like Open Exchange Rates
        // For now, we'll just update the timestamp
        beginTransaction();
        try {
            TypedQuery<ExchangeRate> query = entityManager.createQuery(
                "SELECT e FROM ExchangeRate e", ExchangeRate.class);
            query.getResultList().forEach(exchangeRate -> {
                exchangeRate.setRate(exchangeRate.getRate()); // This will update the timestamp
                entityManager.merge(exchangeRate);
            });
            commitTransaction();
        } catch (Exception e) {
            rollbackTransaction();
            throw e;
        }
    }
} 