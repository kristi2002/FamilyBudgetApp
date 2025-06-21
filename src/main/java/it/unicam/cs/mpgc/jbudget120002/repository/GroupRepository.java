package it.unicam.cs.mpgc.jbudget120002.repository;

import it.unicam.cs.mpgc.jbudget120002.model.Group;

import java.util.Optional;

/**
 * Repository interface for {@link Group} entities.
 * Provides data access methods for groups.
 */
public interface GroupRepository extends Repository<Group, Long> {

    /**
     * Finds a group by its name.
     *
     * @param name The name of the group to find.
     * @return An {@link Optional} containing the group if found, or empty otherwise.
     */
    Optional<Group> findByName(String name);
} 