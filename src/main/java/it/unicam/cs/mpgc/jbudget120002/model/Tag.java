package it.unicam.cs.mpgc.jbudget120002.model;

import jakarta.persistence.*;
import java.util.*;
import java.util.Objects;

/**
 * Entity class representing a categorization tag in the Family Budget App.
 * 
 * <p>This class implements a hierarchical tagging system for categorizing
 * transactions, budgets, and other financial data. Tags can be organized in
 * a tree structure with parent-child relationships, enabling flexible and
 * organized financial categorization.</p>
 * 
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>Define category names and hierarchies</li>
 *   <li>Support parent-child relationships between categories</li>
 *   <li>Enable flexible categorization of financial data</li>
 *   <li>Maintain category metadata and relationships</li>
 *   <li>Support category-based reporting and analysis</li>
 *   <li>Prevent circular references in hierarchies</li>
 * </ul>
 * 
 * <p>Usage examples:</p>
 * <pre>{@code
 * // Create a root tag
 * Tag food = new Tag("Food");
 * 
 * // Create child tags
 * Tag groceries = new Tag("Groceries");
 * groceries.setParent(food);
 * 
 * // Check hierarchy
 * boolean isChild = groceries.isDescendant(food); // true
 * int level = groceries.getLevel(); // 1
 * 
 * // Get all descendants
 * Set<Tag> allFoodTags = food.getAllDescendants();
 * }</pre>
 * 
 * @author FamilyBudgetApp Team
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "tags")
public class Tag {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Tag parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Tag> children = new HashSet<>();

    @ManyToMany(mappedBy = "tags")
    private Set<Transaction> transactions = new HashSet<>();

    @Column(name = "full_path", nullable = true, length = 500)
    private String fullPath;

    // ==================== CONSTRUCTORS ====================

    /**
     * Default constructor for JPA.
     */
    public Tag() {
        // Default constructor for JPA
    }

    /**
     * Creates a new tag with the specified name.
     * 
     * @param name the tag name
     * @throws IllegalArgumentException if the name is null or empty
     */
    public Tag(String name) {
        validateName(name);
        this.name = name.trim();
        updateFullPath();
    }

    // ==================== VALIDATION METHODS ====================

    /**
     * Validates the tag name.
     * 
     * @param name the name to validate
     * @throws IllegalArgumentException if the name is invalid
     */
    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tag name cannot be null or empty");
        }
    }

    // ==================== JPA LIFECYCLE METHODS ====================

    /**
     * Updates the full path before persisting or updating the entity.
     */
    @PrePersist
    @PreUpdate
    private void ensureFullPath() {
        updateFullPath();
    }

    // ==================== BUSINESS LOGIC METHODS ====================

    /**
     * Updates the full path of this tag and all its descendants.
     */
    private void updateFullPath() {
        StringBuilder path = new StringBuilder();
        if (parent != null) {
            path.append(parent.getFullPath()).append("/");
        }
        path.append(name != null ? name : "");
        this.fullPath = path.toString();
        
        // Update children's paths
        if (children != null) {
            for (Tag child : children) {
                child.updateFullPath();
            }
        }
    }

    /**
     * Checks if this tag is a root tag (has no parent).
     * 
     * @return true if this tag is a root, false otherwise
     */
    public boolean isRoot() {
        return parent == null;
    }

    /**
     * Checks if this tag is a leaf tag (has no children).
     * 
     * @return true if this tag is a leaf, false otherwise
     */
    public boolean isLeaf() {
        return children.isEmpty();
    }

    /**
     * Gets the level of this tag in the hierarchy.
     * Root tags have level 0, their children have level 1, etc.
     * 
     * @return the level of this tag
     */
    public int getLevel() {
        int level = 0;
        Tag current = this;
        while (current.getParent() != null) {
            level++;
            current = current.getParent();
        }
        return level;
    }

    /**
     * Gets all ancestors of this tag in order from immediate parent to root.
     * 
     * @return a list of ancestors, empty if this is a root tag
     */
    public List<Tag> getAncestors() {
        List<Tag> ancestors = new ArrayList<>();
        Tag current = parent;
        while (current != null) {
            ancestors.add(current);
            current = current.getParent();
        }
        return ancestors;
    }

    /**
     * Checks if the specified tag is an ancestor of this tag.
     * 
     * @param potentialAncestor the tag to check
     * @return true if the specified tag is an ancestor, false otherwise
     */
    public boolean isAncestor(Tag potentialAncestor) {
        return getAncestors().contains(potentialAncestor);
    }

    /**
     * Checks if the specified tag is a descendant of this tag.
     * 
     * @param potentialDescendant the tag to check
     * @return true if the specified tag is a descendant, false otherwise
     */
    public boolean isDescendant(Tag potentialDescendant) {
        return potentialDescendant != null && potentialDescendant.isAncestor(this);
    }

    /**
     * Gets all descendants of this tag (children, grandchildren, etc.).
     * 
     * @return a set of all descendants
     */
    public Set<Tag> getAllDescendants() {
        Set<Tag> descendants = new HashSet<>();
        for (Tag child : children) {
            descendants.add(child);
            descendants.addAll(child.getAllDescendants());
        }
        return descendants;
    }

    /**
     * Gets all transactions associated with this tag and its descendants.
     * 
     * @return a set of all transactions
     */
    public Set<Transaction> getAllTransactions() {
        Set<Transaction> allTransactions = new HashSet<>(transactions);
        for (Tag child : children) {
            allTransactions.addAll(child.getAllTransactions());
        }
        return allTransactions;
    }

    /**
     * Adds a child tag to this tag.
     * 
     * @param child the child tag to add
     * @return true if the child was added, false if already present
     */
    public boolean addChild(Tag child) {
        if (child == null) {
            return false;
        }
        child.setParent(this);
        return children.add(child);
    }

    /**
     * Removes a child tag from this tag.
     * 
     * @param child the child tag to remove
     * @return true if the child was removed, false if not present
     */
    public boolean removeChild(Tag child) {
        if (child == null) {
            return false;
        }
        if (children.remove(child)) {
            child.setParent(null);
            return true;
        }
        return false;
    }

    /**
     * Checks if this tag has a specific child.
     * 
     * @param child the child to check
     * @return true if the tag has the child, false otherwise
     */
    public boolean hasChild(Tag child) {
        return child != null && children.contains(child);
    }

    // ==================== GETTERS AND SETTERS ====================

    /**
     * Gets the tag's unique identifier.
     * 
     * @return the tag ID
     */
    public Long getId() { 
        return id; 
    }

    /**
     * Sets the tag's unique identifier.
     * 
     * @param id the tag ID
     */
    public void setId(Long id) { 
        this.id = id; 
    }

    /**
     * Gets the tag name.
     * 
     * @return the tag name
     */
    public String getName() { 
        return name; 
    }

    /**
     * Sets the tag name and updates the full path.
     * 
     * @param name the tag name
     * @throws IllegalArgumentException if the name is null or empty
     */
    public void setName(String name) {
        validateName(name);
        this.name = name.trim();
        updateFullPath();
    }

    /**
     * Gets the parent tag.
     * 
     * @return the parent tag, or null if this is a root tag
     */
    public Tag getParent() { 
        return parent; 
    }

    /**
     * Sets the parent tag and updates the hierarchy accordingly.
     * 
     * @param parent the parent tag
     * @throws IllegalArgumentException if setting the parent would create a cycle
     */
    public void setParent(Tag parent) {
        if (this.equals(parent)) {
            throw new IllegalArgumentException("A tag cannot be its own parent");
        }
        if (parent != null && isAncestor(parent)) {
            throw new IllegalArgumentException("Creating a cycle in tag hierarchy is not allowed");
        }
        
        // Remove from current parent's children
        if (this.parent != null) {
            this.parent.children.remove(this);
        }
        
        // Set new parent
        this.parent = parent;
        
        // Add to new parent's children
        if (parent != null) {
            parent.children.add(this);
        }
        
        updateFullPath();
    }

    /**
     * Gets the child tags.
     * 
     * @return an unmodifiable set of child tags
     */
    public Set<Tag> getChildren() { 
        return Collections.unmodifiableSet(children);
    }

    /**
     * Gets the transactions associated with this tag.
     * 
     * @return an unmodifiable set of transactions
     */
    public Set<Transaction> getTransactions() { 
        return Collections.unmodifiableSet(transactions);
    }

    /**
     * Gets the full path of this tag in the hierarchy.
     * 
     * @return the full path (e.g., "Food/Groceries/Fresh Produce")
     */
    public String getFullPath() {
        return fullPath;
    }

    // ==================== EQUALS, HASHCODE, TOSTRING ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tag)) return false;
        Tag tag = (Tag) o;
        return Objects.equals(id, tag.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return fullPath != null ? fullPath : name;
    }
}
