package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.model.Group;
import it.unicam.cs.mpgc.jbudget120002.model.User;

import java.util.List;

/**
 * Service interface for managing groups.
 * Defines business operations for group management.
 */
public interface GroupService {

    /**
     * Finds a group by its ID.
     *
     * @param id The ID of the group.
     * @return The group, or null if not found.
     */
    Group findById(Long id);

    /**
     * Finds all groups.
     *
     * @return A list of all groups.
     */
    List<Group> findAll();

    /**
     * Saves a group.
     *
     * @param group The group to save.
     */
    void save(Group group);

    /**
     * Deletes a group.
     *
     * @param group The group to delete.
     */
    void delete(Group group);

    /**
     * Finds a group by its name.
     *
     * @param name The name of the group.
     * @return The group, or null if not found.
     */
    Group findGroupByName(String name);

    /**
     * Adds a user to a group.
     *
     * @param groupId The ID of the group.
     * @param userId  The ID of the user.
     */
    void addUserToGroup(Long groupId, Long userId);

    /**
     * Removes a user from a group.
     *
     * @param groupId The ID of the group.
     * @param userId  The ID of the user.
     */
    void removeUserFromGroup(Long groupId, Long userId);

    /**
     * Gets all users in a group.
     *
     * @param groupId The ID of the group.
     * @return A list of users in the group.
     */
    List<User> getUsersInGroup(Long groupId);
} 