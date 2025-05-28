package it.unicam.cs.mpgc.jbudget120002.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "budget_templates")
public class BudgetTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ElementCollection
    @CollectionTable(name = "budget_template_categories",
        joinColumns = @JoinColumn(name = "template_id"))
    @MapKeyColumn(name = "category_id")
    @Column(name = "amount", precision = 10, scale = 2)
    private Map<Long, BigDecimal> categoryAmounts = new HashMap<>();

    @Column
    private String description;

    // Default constructor for JPA
    protected BudgetTemplate() {}

    public BudgetTemplate(String name, Map<Long, BigDecimal> categoryAmounts) {
        this.name = name;
        this.categoryAmounts = new HashMap<>(categoryAmounts);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<Long, BigDecimal> getCategoryAmounts() {
        return new HashMap<>(categoryAmounts);
    }

    public void setCategoryAmounts(Map<Long, BigDecimal> categoryAmounts) {
        this.categoryAmounts = new HashMap<>(categoryAmounts);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addCategoryAmount(Long categoryId, BigDecimal amount) {
        categoryAmounts.put(categoryId, amount);
    }

    public void removeCategoryAmount(Long categoryId) {
        categoryAmounts.remove(categoryId);
    }

    public BigDecimal getCategoryAmount(Long categoryId) {
        return categoryAmounts.getOrDefault(categoryId, BigDecimal.ZERO);
    }
} 