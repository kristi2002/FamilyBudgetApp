package it.unicam.cs.mpgc.jbudget120002.model;

import jakarta.persistence.*;

@Entity
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String currency;
    private String locale;
    private String theme;

    public UserSettings() {}

    public UserSettings(String currency, String locale, String theme) {
        this.currency = currency;
        this.locale   = locale;
        this.theme    = theme;
    }

    public Long getId() { return id; }
    public String getCurrency() { return currency; }
    public String getLocale()   { return locale; }
    public String getTheme()    { return theme; }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
    public void setLocale(String locale) {
        this.locale = locale;
    }
    public void setTheme(String theme) {
        this.theme = theme;
    }
}
