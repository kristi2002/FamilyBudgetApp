package it.unicam.cs.mpgc.jbudget120002.repository;

import it.unicam.cs.mpgc.jbudget120002.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

/**
 * JPA implementation of the {@link UserRepository}.
 */
public class UserRepositoryJpa extends JpaRepository<User, Long> implements UserRepository {

    public UserRepositoryJpa(EntityManager entityManager) {
        super(User.class, entityManager);
    }

    @Override
    public Optional<User> findByName(String name) {
        TypedQuery<User> query = em.createQuery(
                "SELECT u FROM User u WHERE u.name = :name", User.class);
        query.setParameter("name", name);
        return query.getResultStream().findFirst();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        TypedQuery<User> query = em.createQuery(
                "SELECT u FROM User u WHERE u.username = :username", User.class);
        query.setParameter("username", username);
        return query.getResultStream().findFirst();
    }

    @Override
    public List<User> findByGroupId(Long groupId) {
        TypedQuery<User> query = em.createQuery(
                "SELECT u FROM User u JOIN u.groups g WHERE g.id = :groupId", User.class);
        query.setParameter("groupId", groupId);
        return query.getResultList();
    }
} 