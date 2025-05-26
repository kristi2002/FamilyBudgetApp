package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.model.UserSettings;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Optional;

public class UserSettingsServiceImpl implements UserSettingsService {
    private final EntityManager entityManager;

    public UserSettingsServiceImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public UserSettings create(UserSettings s) {
        entityManager.getTransaction().begin();
        try {
            entityManager.persist(s);
            entityManager.getTransaction().commit();
            return s;
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        }
    }

    @Override
    public Optional<UserSettings> findFirst() {
        TypedQuery<UserSettings> q = entityManager.createQuery(
                "FROM UserSettings u", UserSettings.class);
        return q.getResultStream().findFirst();
    }

    @Override
    public UserSettings update(UserSettings s) {
        entityManager.getTransaction().begin();
        try {
            entityManager.merge(s);
            entityManager.getTransaction().commit();
            return s;
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        }
    }

    @Override
    public void delete(Long id) {
        entityManager.getTransaction().begin();
        try {
            findFirst()
                .filter(us -> us.getId().equals(id))
                .ifPresent(entityManager::remove);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        }
    }
}
