package it.unicam.cs.mpgc.jbudget120002.model;

import jakarta.persistence.*;
import java.util.*;

/**
 * Entity class representing a categorization tag in the Family Budget App.
 * This class implements a hierarchical tagging system for categorizing
 * transactions, budgets, and other financial data.
 *
 * Responsibilities:
 * - Define category names and hierarchies
 * - Support parent-child relationships between categories
 * - Enable flexible categorization of financial data
 * - Maintain category metadata and relationships
 * - Support category-based reporting and analysis
 *
 * Usage:
 * Used throughout the application to categorize transactions and budgets,
 * enabling organized financial management and detailed reporting.
 */
@Entity
@Table(name = "tags")
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Tag parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Tag> children = new HashSet<>();

    @ManyToMany(mappedBy = "tags")
    private Set<Transaction> transactions = new HashSet<>();

    @Column(name = "full_path", nullable = true)
    private String fullPath;

    @PrePersist
    @PreUpdate
    private void ensureFullPath() {
        updateFullPath();
    }

    public Tag() {
        // Default constructor for JPA
    }

    public Tag(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tag name cannot be empty");
        }
        this.name = name.trim();
        updateFullPath();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tag name cannot be empty");
        }
        this.name = name.trim();
        updateFullPath();
    }

    public Tag getParent() { return parent; }
    public void setParent(Tag parent) {
        if (this.equals(parent)) {
            throw new IllegalArgumentException("A tag cannot be its own parent");
        }
        if (parent != null && isAncestor(parent)) {
            throw new IllegalArgumentException("Creating a cycle in tag hierarchy is not allowed");
        }
        
        if (this.parent != null) {
            this.parent.children.remove(this);
        }
        this.parent = parent;
        if (parent != null) {
            parent.children.add(this);
        }
        updateFullPath();
    }

    public Set<Tag> getChildren() { 
        return Collections.unmodifiableSet(children);
    }

    public Set<Transaction> getTransactions() { 
        return Collections.unmodifiableSet(transactions);
    }

    public String getFullPath() {
        return fullPath;
    }

    private void updateFullPath() {
        StringBuilder path = new StringBuilder();
        if (parent != null) {
            path.append(parent.getFullPath()).append("/");
        }
        path.append(name != null ? name : "");
        this.fullPath = path.toString();
        
        // Update children's paths
        if (children != null) {  // Check for null during initialization
            for (Tag child : children) {
                child.updateFullPath();
            }
        }
    }

    public boolean isRoot() {
        return parent == null;
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public int getLevel() {
        int level = 0;
        Tag current = this;
        while (current.getParent() != null) {
            level++;
            current = current.getParent();
        }
        return level;
    }

    public List<Tag> getAncestors() {
        List<Tag> ancestors = new ArrayList<>();
        Tag current = parent;
        while (current != null) {
            ancestors.add(current);
            current = current.getParent();
        }
        return ancestors;
    }

    public boolean isAncestor(Tag potentialAncestor) {
        return getAncestors().contains(potentialAncestor);
    }

    public boolean isDescendant(Tag potentialDescendant) {
        return potentialDescendant != null && potentialDescendant.isAncestor(this);
    }

    public Set<Tag> getAllDescendants() {
        Set<Tag> descendants = new HashSet<>();
        for (Tag child : children) {
            descendants.add(child);
            descendants.addAll(child.getAllDescendants());
        }
        return descendants;
    }

    public Set<Transaction> getAllTransactions() {
        Set<Transaction> allTransactions = new HashSet<>(transactions);
        for (Tag child : children) {
            allTransactions.addAll(child.getAllTransactions());
        }
        return allTransactions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tag)) return false;
        Tag tag = (Tag) o;
        return id != null && id.equals(tag.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return fullPath;
    }
}
