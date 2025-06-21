package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.model.Role;
import it.unicam.cs.mpgc.jbudget120002.model.User;

import java.util.List;
import java.util.Set;

/**
 * Service interface for managing users.
 * Defines business operations for user management.
 */
public interface UserService {

    /**
     * Finds a user by their ID.
     *
     * @param id The ID of the user.
     * @return The user, or null if not found.
     */
    User findById(Long id);

    /**
     * Finds all users.
     *
     * @return A list of all users.
     */
    List<User> findAll();

    /**
     * Saves a user.
     *
     * @param user The user to save.
     */
    void save(User user);

    /**
     * Saves a new user (alias for save method).
     *
     * @param user The user to save.
     */
    void saveUser(User user);

    /**
     * Deletes a user.
     *
     * @param user The user to delete.
     */
    void delete(User user);

    /**
     * Finds a user by their username.
     *
     * @param username The username of the user.
     * @return The user, or null if not found.
     */
    User findUserByName(String username);

    /**
     * Finds all users in a specific group.
     *
     * @param groupId The ID of the group.
     * @return A list of users in the group.
     */
    List<User> findUsersByGroup(Long groupId);

    /**
     * Assigns a role to a user.
     *
     * @param userId The ID of the user.
     * @param role   The role to assign.
     */
    void assignRoleToUser(Long userId, Role role);

    /**
     * Revokes a role from a user.
     *
     * @param userId The ID of the user.
     * @param role   The role to revoke.
     */
    void revokeRoleFromUser(Long userId, Role role);

    /**
     * Gets all roles for a user.
     *
     * @param userId The ID of the user.
     * @return A set of roles for the user.
     */
    Set<Role> getUserRoles(Long userId);
} 