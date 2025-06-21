package it.unicam.cs.mpgc.jbudget120002.repository;

import it.unicam.cs.mpgc.jbudget120002.model.Group;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.Optional;

/**
 * JPA implementation of the {@link GroupRepository}.
 */
public class GroupRepositoryJpa extends JpaRepository<Group, Long> implements GroupRepository {

    public GroupRepositoryJpa(EntityManager entityManager) {
        super(Group.class, entityManager);
    }

    @Override
    public Optional<Group> findByName(String name) {
        TypedQuery<Group> query = em.createQuery(
                "SELECT g FROM Group g WHERE g.name = :name", Group.class);
        query.setParameter("name", name);
        return query.getResultStream().findFirst();
    }
} 