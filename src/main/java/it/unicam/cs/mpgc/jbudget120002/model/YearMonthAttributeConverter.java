package it.unicam.cs.mpgc.jbudget120002.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.YearMonth;

/**
 * JPA AttributeConverter to persist YearMonth as a String (format: "yyyy-MM").
 */
@Converter(autoApply = true)
public class YearMonthAttributeConverter implements AttributeConverter<YearMonth, String> {

    private static final String SEPARATOR = "-";

    @Override
    public String convertToDatabaseColumn(YearMonth attribute) {
        return (attribute != null)
                ? attribute.getYear() + SEPARATOR + String.format("%02d", attribute.getMonthValue())
                : null;
    }

    @Override
    public YearMonth convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        String[] parts = dbData.split(SEPARATOR);
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        return YearMonth.of(year, month);
    }
}
