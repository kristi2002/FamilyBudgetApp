package it.unicam.cs.mpgc.jbudget120002.repository;

import it.unicam.cs.mpgc.jbudget120002.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link User} entities.
 * Provides data access methods for users.
 */
public interface UserRepository extends Repository<User, Long> {

    /**
     * Finds a user by their name.
     *
     * @param name The name of the user to find.
     * @return An {@link Optional} containing the user if found, or empty otherwise.
     */
    Optional<User> findByName(String name);

    /**
     * Finds a user by their username.
     *
     * @param username The username of the user to find.
     * @return An {@link Optional} containing the user if found, or empty otherwise.
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds all users belonging to a specific group.
     *
     * @param groupId The ID of the group.
     * @return A list of users in the specified group.
     */
    List<User> findByGroupId(Long groupId);
} 