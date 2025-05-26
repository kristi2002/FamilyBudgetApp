// src/main/java/it/unicam/cs/mpgc/jbudget120002/repository/UserSettingsRepositoryJpa.java
package it.unicam.cs.mpgc.jbudget120002.repository;

import it.unicam.cs.mpgc.jbudget120002.model.UserSettings;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Optional;

public class UserSettingsRepositoryJpa
        extends JpaRepository<UserSettings, Long>
        implements UserSettingsRepository {

    public UserSettingsRepositoryJpa(EntityManager entityManager) {
        super(UserSettings.class, entityManager);
    }

    @Override
    public Optional<UserSettings> findFirst() {
        TypedQuery<UserSettings> q = em.createQuery(
                "FROM UserSettings u", UserSettings.class);
        return q.getResultStream().findFirst();
    }
}
