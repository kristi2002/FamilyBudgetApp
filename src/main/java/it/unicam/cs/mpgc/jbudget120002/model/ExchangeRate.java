package it.unicam.cs.mpgc.jbudget120002.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "exchange_rates")
public class ExchangeRate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "from_currency", nullable = false, length = 3)
    private String fromCurrency;

    @Column(name = "to_currency", nullable = false, length = 3)
    private String toCurrency;

    @Column(nullable = false, precision = 10, scale = 6)
    private BigDecimal rate;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    // Default constructor for JPA
    protected ExchangeRate() {}

    public ExchangeRate(String fromCurrency, String toCurrency, BigDecimal rate) {
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.rate = rate;
        this.lastUpdated = LocalDateTime.now();
    }

    // Getters
    public Long getId() { return id; }
    public String getFromCurrency() { return fromCurrency; }
    public String getToCurrency() { return toCurrency; }
    public BigDecimal getRate() { return rate; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }

    // Setters
    public void setRate(BigDecimal rate) {
        this.rate = rate;
        this.lastUpdated = LocalDateTime.now();
    }
} 