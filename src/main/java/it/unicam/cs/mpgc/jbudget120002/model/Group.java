package it.unicam.cs.mpgc.jbudget120002.model;

import jakarta.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;
import java.util.ArrayList;

/**
 * Entity class representing a group in the Family Budget App.
 * 
 * <p>This class manages user groups and their hierarchical relationships. Groups can be
 * organized in a tree structure with parent-child relationships, enabling flexible
 * organization of users for collaborative budget management.</p>
 * 
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>Organize users into collaborative groups</li>
 *   <li>Support hierarchical group structures</li>
 *   <li>Manage group membership</li>
 *   <li>Track group creation and metadata</li>
 *   <li>Enable group-based budget sharing</li>
 *   <li>Prevent circular references in hierarchies</li>
 * </ul>
 * 
 * <p>Usage examples:</p>
 * <pre>{@code
 * // Create a family group
 * Group family = new Group("Smith Family");
 * family.setDescription("Main family group for budget management");
 * 
 * // Add users to the group
 * family.addUser(john);
 * family.addUser(jane);
 * 
 * // Create subgroups
 * Group children = new Group("Children");
 * children.setParent(family);
 * 
 * // Check hierarchy
 * boolean isChild = children.isDescendant(family); // true
 * }</pre>
 * 
 * @author FamilyBudgetApp Team
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "groups")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_date", nullable = false, updatable = false, 
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date creationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Group parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<Group> children = new HashSet<>();

    @ManyToMany(mappedBy = "groups")
    private Set<User> users = new HashSet<>();

    // ==================== CONSTRUCTORS ====================

    /**
     * Default constructor for JPA.
     */
    public Group() {
    }

    /**
     * Creates a new group with the specified name.
     * 
     * @param name the group name
     * @throws IllegalArgumentException if the name is null or empty
     */
    public Group(String name) {
        validateName(name);
        this.name = name;
    }

    // ==================== VALIDATION METHODS ====================

    /**
     * Validates the group name.
     * 
     * @param name the name to validate
     * @throws IllegalArgumentException if the name is invalid
     */
    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Group name cannot be null or empty");
        }
    }

    // ==================== JPA LIFECYCLE METHODS ====================

    /**
     * Sets the creation date before persisting the entity.
     */
    @PrePersist
    protected void onCreate() {
        creationDate = new Date();
    }

    // ==================== BUSINESS LOGIC METHODS ====================

    /**
     * Adds a user to this group.
     * 
     * @param user the user to add
     * @return true if the user was added, false if already present
     */
    public boolean addUser(User user) {
        if (user == null) {
            return false;
        }
        if (users.add(user)) {
            user.getGroups().add(this);
            return true;
        }
        return false;
    }

    /**
     * Removes a user from this group.
     * 
     * @param user the user to remove
     * @return true if the user was removed, false if not present
     */
    public boolean removeUser(User user) {
        if (user == null) {
            return false;
        }
        if (users.remove(user)) {
            user.getGroups().remove(this);
            return true;
        }
        return false;
    }

    /**
     * Checks if this group contains a specific user.
     * 
     * @param user the user to check
     * @return true if the group contains the user, false otherwise
     */
    public boolean containsUser(User user) {
        return user != null && users.contains(user);
    }

    /**
     * Gets the number of users in this group.
     * 
     * @return the number of users
     */
    public int getUserCount() {
        return users.size();
    }

    /**
     * Checks if this group is a root group (has no parent).
     * 
     * @return true if this group is a root, false otherwise
     */
    public boolean isRoot() {
        return parent == null;
    }

    /**
     * Checks if this group is a leaf group (has no children).
     * 
     * @return true if this group is a leaf, false otherwise
     */
    public boolean isLeaf() {
        return children.isEmpty();
    }

    /**
     * Gets the level of this group in the hierarchy.
     * Root groups have level 0, their children have level 1, etc.
     * 
     * @return the level of this group
     */
    public int getLevel() {
        int level = 0;
        Group current = this;
        while (current.getParent() != null) {
            level++;
            current = current.getParent();
        }
        return level;
    }

    /**
     * Gets all ancestors of this group in order from immediate parent to root.
     * 
     * @return a list of ancestors, empty if this is a root group
     */
    public java.util.List<Group> getAncestors() {
        java.util.List<Group> ancestors = new java.util.ArrayList<>();
        Group current = parent;
        while (current != null) {
            ancestors.add(current);
            current = current.getParent();
        }
        return ancestors;
    }

    /**
     * Checks if the specified group is an ancestor of this group.
     * 
     * @param potentialAncestor the group to check
     * @return true if the specified group is an ancestor, false otherwise
     */
    public boolean isAncestor(Group potentialAncestor) {
        return getAncestors().contains(potentialAncestor);
    }

    /**
     * Checks if the specified group is a descendant of this group.
     * 
     * @param potentialDescendant the group to check
     * @return true if the specified group is a descendant, false otherwise
     */
    public boolean isDescendant(Group potentialDescendant) {
        return potentialDescendant != null && potentialDescendant.isAncestor(this);
    }

    /**
     * Gets all descendants of this group (children, grandchildren, etc.).
     * 
     * @return a set of all descendants
     */
    public Set<Group> getAllDescendants() {
        Set<Group> descendants = new HashSet<>();
        for (Group child : children) {
            descendants.add(child);
            descendants.addAll(child.getAllDescendants());
        }
        return descendants;
    }

    /**
     * Gets all users in this group and its descendants.
     * 
     * @return a set of all users
     */
    public Set<User> getAllUsers() {
        Set<User> allUsers = new HashSet<>(users);
        for (Group child : children) {
            allUsers.addAll(child.getAllUsers());
        }
        return allUsers;
    }

    /**
     * Adds a child group to this group.
     * 
     * @param child the child group to add
     * @return true if the child was added, false if already present
     */
    public boolean addChild(Group child) {
        if (child == null) {
            return false;
        }
        child.setParent(this);
        return children.add(child);
    }

    /**
     * Removes a child group from this group.
     * 
     * @param child the child group to remove
     * @return true if the child was removed, false if not present
     */
    public boolean removeChild(Group child) {
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
     * Checks if this group has a specific child.
     * 
     * @param child the child to check
     * @return true if the group has the child, false otherwise
     */
    public boolean hasChild(Group child) {
        return child != null && children.contains(child);
    }

    // ==================== GETTERS AND SETTERS ====================

    /**
     * Gets the group's unique identifier.
     * 
     * @return the group ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the group's unique identifier.
     * 
     * @param id the group ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the group name.
     * 
     * @return the group name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the group name.
     * 
     * @param name the group name
     */
    public void setName(String name) {
        validateName(name);
        this.name = name;
    }

    /**
     * Gets the group description.
     * 
     * @return the group description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the group description.
     * 
     * @param description the group description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the group creation date.
     * 
     * @return the creation date
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * Sets the group creation date.
     * 
     * @param creationDate the creation date
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Gets the parent group.
     * 
     * @return the parent group, or null if this is a root group
     */
    public Group getParent() {
        return parent;
    }

    /**
     * Sets the parent group and updates the hierarchy accordingly.
     * 
     * @param parent the parent group
     * @throws IllegalArgumentException if setting the parent would create a cycle
     */
    public void setParent(Group parent) {
        if (this.equals(parent)) {
            throw new IllegalArgumentException("A group cannot be its own parent");
        }
        if (parent != null && isAncestor(parent)) {
            throw new IllegalArgumentException("Creating a cycle in group hierarchy is not allowed");
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
    }

    /**
     * Gets the child groups.
     * 
     * @return the set of child groups
     */
    public Set<Group> getChildren() {
        return children;
    }

    /**
     * Sets the child groups.
     * 
     * @param children the set of child groups
     */
    public void setChildren(Set<Group> children) {
        this.children = children != null ? children : new HashSet<>();
    }

    /**
     * Gets the users in this group.
     * 
     * @return the set of users
     */
    public Set<User> getUsers() {
        return users;
    }

    /**
     * Sets the users in this group.
     * 
     * @param users the set of users
     */
    public void setUsers(Set<User> users) {
        this.users = users != null ? users : new HashSet<>();
    }

    // ==================== EQUALS, HASHCODE, TOSTRING ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Group)) return false;
        Group group = (Group) o;
        return Objects.equals(id, group.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Group{id=%d, name='%s', description='%s', userCount=%d}", 
                           id, name, description, getUserCount());
    }
} 