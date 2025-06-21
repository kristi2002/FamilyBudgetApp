package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.model.UserSettings;
import it.unicam.cs.mpgc.jbudget120002.repository.UserSettingsRepository;
import jakarta.persistence.EntityManager;

import java.util.Optional;

public class UserSettingsServiceImpl extends BaseService implements UserSettingsService {
    private final UserSettingsRepository repository;

    public UserSettingsServiceImpl(EntityManager entityManager, UserSettingsRepository repository) {
        super(entityManager);
        this.repository = repository;
    }

    @Override
    public UserSettings create(UserSettings s) {
        executeInTransaction(() -> repository.save(s));
        return s;
    }

    @Override
    public Optional<UserSettings> findFirst() {
        return repository.findFirst();
    }

    @Override
    public UserSettings update(UserSettings s) {
        executeInTransaction(() -> repository.save(s));
        return s;
    }

    @Override
    public void delete(Long id) {
        executeInTransaction(() -> repository.deleteById(id));
    }
}
