package it.unicam.cs.mpgc.jbudget120002.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * Entity class representing a budget plan in the Family Budget App.
 * 
 * <p>This class manages budget allocations for specific categories and time periods,
 * tracking spending limits and actual expenditures. Budgets can be associated with
 * specific tags (categories) and groups, enabling organized financial planning.</p>
 * 
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>Define budget amounts and time periods</li>
 *   <li>Track budget categories through tags</li>
 *   <li>Monitor budget utilization</li>
 *   <li>Support budget templates and recurring budgets</li>
 *   <li>Enable budget alerts and notifications</li>
 *   <li>Group-based budget management</li>
 * </ul>
 * 
 * <p>Usage examples:</p>
 * <pre>{@code
 * // Create a monthly budget for groceries
 * Budget groceryBudget = new Budget("Monthly Groceries", new BigDecimal("500.00"), 
 *                                   LocalDate.now(), LocalDate.now().plusMonths(1));
 * 
 * // Add category tags
 * groceryBudget.addTag(foodTag);
 * 
 * // Check if budget is active
 * boolean isActive = groceryBudget.isActive(LocalDate.now());
 * 
 * // Calculate remaining amount
 * BigDecimal remaining = groceryBudget.getRemainingAmount(spentAmount);
 * }</pre>
 * 
 * @author FamilyBudgetApp Team
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "budgets")
public class Budget {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "budget_tags",
        joinColumns = @JoinColumn(name = "budget_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    // ==================== CONSTRUCTORS ====================

    /**
     * Default constructor for JPA.
     */
    protected Budget() {}

    /**
     * Creates a new budget with basic information.
     * 
     * @param name the budget name
     * @param amount the budget amount
     * @param startDate the budget start date
     * @param endDate the budget end date
     * @throws IllegalArgumentException if any required parameter is null or invalid
     */
    public Budget(String name, BigDecimal amount, LocalDate startDate, LocalDate endDate) {
        validateConstructorParams(name, amount, startDate, endDate);
        
        this.name = name;
        this.amount = amount;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // ==================== VALIDATION METHODS ====================

    /**
     * Validates constructor parameters.
     * 
     * @param name the name to validate
     * @param amount the amount to validate
     * @param startDate the start date to validate
     * @param endDate the end date to validate
     * @throws IllegalArgumentException if any parameter is invalid
     */
    private void validateConstructorParams(String name, BigDecimal amount, LocalDate startDate, LocalDate endDate) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Budget name cannot be null or empty");
        }
        if (amount == null) {
            throw new IllegalArgumentException("Budget amount cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Budget amount must be positive");
        }
        if (startDate == null) {
            throw new IllegalArgumentException("Budget start date cannot be null");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("Budget end date cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Budget start date cannot be after end date");
        }
    }

    // ==================== BUSINESS LOGIC METHODS ====================

    /**
     * Adds a tag to this budget.
     * 
     * @param tag the tag to add
     * @return true if the tag was added, false if already present
     */
    public boolean addTag(Tag tag) {
        if (tag == null) {
            return false;
        }
        return tags.add(tag);
    }

    /**
     * Removes a tag from this budget.
     * 
     * @param tag the tag to remove
     * @return true if the tag was removed, false if not present
     */
    public boolean removeTag(Tag tag) {
        if (tag == null) {
            return false;
        }
        return tags.remove(tag);
    }

    /**
     * Checks if this budget has a specific tag.
     * 
     * @param tag the tag to check
     * @return true if the budget has the tag, false otherwise
     */
    public boolean hasTag(Tag tag) {
        return tag != null && tags.contains(tag);
    }

    /**
     * Checks if this budget has any of the specified tags.
     * 
     * @param searchTags the tags to check
     * @return true if the budget has any of the tags, false otherwise
     */
    public boolean hasAnyTag(Set<Tag> searchTags) {
        if (searchTags == null || searchTags.isEmpty()) {
            return false;
        }
        return searchTags.stream().anyMatch(this::hasTag);
    }

    /**
     * Checks if this budget is active for the given date.
     * 
     * @param date the date to check
     * @return true if the budget is active on the given date, false otherwise
     */
    public boolean isActive(LocalDate date) {
        if (date == null) {
            return false;
        }
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * Checks if this budget is currently active.
     * 
     * @return true if the budget is currently active, false otherwise
     */
    public boolean isCurrentlyActive() {
        return isActive(LocalDate.now());
    }

    /**
     * Checks if this budget has expired.
     * 
     * @return true if the budget has expired, false otherwise
     */
    public boolean isExpired() {
        return LocalDate.now().isAfter(endDate);
    }

    /**
     * Checks if this budget is in the future.
     * 
     * @return true if the budget is in the future, false otherwise
     */
    public boolean isFuture() {
        return LocalDate.now().isBefore(startDate);
    }

    /**
     * Gets the duration of this budget in days.
     * 
     * @return the number of days between start and end date
     */
    public long getDurationInDays() {
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    /**
     * Gets the remaining amount based on spent amount.
     * 
     * @param spentAmount the amount already spent
     * @return the remaining budget amount
     */
    public BigDecimal getRemainingAmount(BigDecimal spentAmount) {
        if (spentAmount == null) {
            return amount;
        }
        return amount.subtract(spentAmount);
    }

    /**
     * Gets the utilization percentage based on spent amount.
     * 
     * @param spentAmount the amount already spent
     * @return the utilization percentage (0-100)
     */
    public double getUtilizationPercentage(BigDecimal spentAmount) {
        if (spentAmount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return spentAmount.divide(amount, 4, BigDecimal.ROUND_HALF_UP)
                         .multiply(BigDecimal.valueOf(100))
                         .doubleValue();
    }

    /**
     * Checks if the budget is over-utilized based on spent amount.
     * 
     * @param spentAmount the amount already spent
     * @return true if the budget is over-utilized, false otherwise
     */
    public boolean isOverUtilized(BigDecimal spentAmount) {
        return spentAmount != null && spentAmount.compareTo(amount) > 0;
    }

    /**
     * Gets the overage amount if the budget is exceeded.
     * 
     * @param spentAmount the amount already spent
     * @return the overage amount, or zero if not exceeded
     */
    public BigDecimal getOverageAmount(BigDecimal spentAmount) {
        if (spentAmount == null) {
            return BigDecimal.ZERO;
        }
        return spentAmount.compareTo(amount) > 0 ? spentAmount.subtract(amount) : BigDecimal.ZERO;
    }

    // ==================== GETTERS AND SETTERS ====================

    /**
     * Gets the budget's unique identifier.
     * 
     * @return the budget ID
     */
    public Long getId() { 
        return id; 
    }

    /**
     * Sets the budget's unique identifier.
     * 
     * @param id the budget ID
     */
    public void setId(Long id) { 
        this.id = id; 
    }

    /**
     * Gets the budget name.
     * 
     * @return the budget name
     */
    public String getName() { 
        return name; 
    }

    /**
     * Sets the budget name.
     * 
     * @param name the budget name
     */
    public void setName(String name) { 
        this.name = name; 
    }

    /**
     * Gets the budget amount.
     * 
     * @return the budget amount
     */
    public BigDecimal getAmount() { 
        return amount; 
    }

    /**
     * Sets the budget amount.
     * 
     * @param amount the budget amount
     */
    public void setAmount(BigDecimal amount) { 
        this.amount = amount; 
    }

    /**
     * Gets the budget start date.
     * 
     * @return the start date
     */
    public LocalDate getStartDate() { 
        return startDate; 
    }

    /**
     * Sets the budget start date.
     * 
     * @param startDate the start date
     */
    public void setStartDate(LocalDate startDate) { 
        this.startDate = startDate; 
    }

    /**
     * Gets the budget end date.
     * 
     * @return the end date
     */
    public LocalDate getEndDate() { 
        return endDate; 
    }

    /**
     * Sets the budget end date.
     * 
     * @param endDate the end date
     */
    public void setEndDate(LocalDate endDate) { 
        this.endDate = endDate; 
    }

    /**
     * Gets the budget tags.
     * 
     * @return the set of tags
     */
    public Set<Tag> getTags() { 
        return tags; 
    }

    /**
     * Sets the budget tags.
     * 
     * @param tags the set of tags
     */
    public void setTags(Set<Tag> tags) { 
        this.tags = tags != null ? tags : new HashSet<>(); 
    }

    /**
     * Gets the group associated with this budget.
     * 
     * @return the group, or null if not assigned
     */
    public Group getGroup() { 
        return group; 
    }

    /**
     * Sets the group associated with this budget.
     * 
     * @param group the group
     */
    public void setGroup(Group group) { 
        this.group = group; 
    }

    // ==================== EQUALS, HASHCODE, TOSTRING ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Budget)) return false;
        Budget budget = (Budget) o;
        return Objects.equals(id, budget.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Budget{id=%d, name='%s', amount=%s, startDate=%s, endDate=%s}", 
                           id, name, amount, startDate, endDate);
    }
} 