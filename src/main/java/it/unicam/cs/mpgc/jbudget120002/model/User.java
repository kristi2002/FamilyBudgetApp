package it.unicam.cs.mpgc.jbudget120002.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * Represents a user of the Family Budget App.
 * 
 * <p>This entity class manages user authentication, profile information, and relationships
 * with other entities in the system. Each user can belong to multiple groups, have various
 * roles, and own multiple transactions.</p>
 * 
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>User authentication and profile management</li>
 *   <li>Group membership management</li>
 *   <li>Role-based access control</li>
 *   <li>Transaction ownership</li>
 * </ul>
 * 
 * <p>Usage examples:</p>
 * <pre>{@code
 * // Create a new user
 * User user = new User("john_doe", "password123", "john@example.com", "John Doe");
 * 
 * // Add user to a group
 * user.getGroups().add(group);
 * 
 * // Assign roles
 * user.getRoles().add(Role.ADMIN);
 * }</pre>
 * 
 * @author FamilyBudgetApp Team
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 50)
    private String surname;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "user_groups",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "group_id")
    )
    private Set<Group> groups = new HashSet<>();

    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Transaction> transactions = new HashSet<>();

    /**
     * Default constructor for JPA.
     */
    public User() {
        // Default constructor for JPA
    }

    /**
     * Creates a new user with basic authentication and profile information.
     * 
     * @param username the unique username for authentication
     * @param password the user's password
     * @param email the user's email address
     * @param fullName the user's full name
     * @throws IllegalArgumentException if any required parameter is null or empty
     */
    public User(String username, String password, String email, String fullName) {
        validateConstructorParams(username, password, email, fullName);
        
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        
        // Parse name and surname from full name
        String[] nameParts = fullName.split(" ", 2);
        this.name = nameParts[0];
        this.surname = nameParts.length > 1 ? nameParts[1] : "";
    }

    /**
     * Creates a new user with just name and surname.
     * 
     * @param name the user's first name
     * @param surname the user's surname
     */
    public User(String name, String surname) {
        this.name = name != null ? name : "";
        this.surname = surname != null ? surname : "";
        this.fullName = this.name + " " + this.surname;
    }

    // ==================== VALIDATION METHODS ====================

    /**
     * Validates constructor parameters.
     * 
     * @param username the username to validate
     * @param password the password to validate
     * @param email the email to validate
     * @param fullName the full name to validate
     * @throws IllegalArgumentException if any parameter is invalid
     */
    private void validateConstructorParams(String username, String password, String email, String fullName) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be null or empty");
        }
    }

    // ==================== BUSINESS LOGIC METHODS ====================

    /**
     * Adds a user to a group.
     * 
     * @param group the group to add the user to
     * @return true if the group was added, false if already present
     */
    public boolean addToGroup(Group group) {
        if (group == null) {
            return false;
        }
        return groups.add(group);
    }

    /**
     * Removes a user from a group.
     * 
     * @param group the group to remove the user from
     * @return true if the group was removed, false if not present
     */
    public boolean removeFromGroup(Group group) {
        if (group == null) {
            return false;
        }
        return groups.remove(group);
    }

    /**
     * Checks if the user belongs to a specific group.
     * 
     * @param group the group to check
     * @return true if the user belongs to the group, false otherwise
     */
    public boolean belongsToGroup(Group group) {
        return group != null && groups.contains(group);
    }

    /**
     * Adds a role to the user.
     * 
     * @param role the role to add
     * @return true if the role was added, false if already present
     */
    public boolean addRole(Role role) {
        if (role == null) {
            return false;
        }
        return roles.add(role);
    }

    /**
     * Removes a role from the user.
     * 
     * @param role the role to remove
     * @return true if the role was removed, false if not present
     */
    public boolean removeRole(Role role) {
        if (role == null) {
            return false;
        }
        return roles.remove(role);
    }

    /**
     * Checks if the user has a specific role.
     * 
     * @param role the role to check
     * @return true if the user has the role, false otherwise
     */
    public boolean hasRole(Role role) {
        return role != null && roles.contains(role);
    }

    /**
     * Checks if the user has admin privileges.
     * 
     * @return true if the user has admin role, false otherwise
     */
    public boolean isAdmin() {
        return hasRole(Role.ADMIN);
    }

    // ==================== GETTERS AND SETTERS ====================

    /**
     * Gets the user's unique identifier.
     * 
     * @return the user ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the user's unique identifier.
     * 
     * @param id the user ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the user's username.
     * 
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the user's username.
     * 
     * @param username the username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the user's password.
     * 
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the user's password.
     * 
     * @param password the password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the user's email address.
     * 
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email address.
     * 
     * @param email the email address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the user's full name.
     * 
     * @return the full name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Sets the user's full name and updates name/surname accordingly.
     * 
     * @param fullName the full name
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
        if (fullName != null) {
            String[] nameParts = fullName.split(" ", 2);
            this.name = nameParts[0];
            this.surname = nameParts.length > 1 ? nameParts[1] : "";
        }
    }

    /**
     * Gets the user's first name.
     * 
     * @return the first name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the user's first name.
     * 
     * @param name the first name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the user's surname.
     * 
     * @return the surname
     */
    public String getSurname() {
        return surname;
    }

    /**
     * Sets the user's surname.
     * 
     * @param surname the surname
     */
    public void setSurname(String surname) {
        this.surname = surname;
    }

    /**
     * Gets the groups the user belongs to.
     * 
     * @return the set of groups
     */
    public Set<Group> getGroups() {
        return groups;
    }

    /**
     * Sets the groups the user belongs to.
     * 
     * @param groups the set of groups
     */
    public void setGroups(Set<Group> groups) {
        this.groups = groups != null ? groups : new HashSet<>();
    }

    /**
     * Gets the user's roles.
     * 
     * @return the set of roles
     */
    public Set<Role> getRoles() {
        return roles;
    }

    /**
     * Sets the user's roles.
     * 
     * @param roles the set of roles
     */
    public void setRoles(Set<Role> roles) {
        this.roles = roles != null ? roles : new HashSet<>();
    }

    /**
     * Gets the user's transactions.
     * 
     * @return the set of transactions
     */
    public Set<Transaction> getTransactions() {
        return transactions;
    }

    /**
     * Sets the user's transactions.
     * 
     * @param transactions the set of transactions
     */
    public void setTransactions(Set<Transaction> transactions) {
        this.transactions = transactions != null ? transactions : new HashSet<>();
    }

    // ==================== EQUALS, HASHCODE, TOSTRING ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && 
               Objects.equals(username, user.username) &&
               Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, email);
    }

    @Override
    public String toString() {
        return String.format("User{id=%d, username='%s', fullName='%s', email='%s'}", 
                           id, username, fullName, email);
    }
} 