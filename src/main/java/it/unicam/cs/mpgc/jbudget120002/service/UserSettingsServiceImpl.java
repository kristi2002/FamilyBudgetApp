package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.model.UserSettings;
import it.unicam.cs.mpgc.jbudget120002.repository.UserSettingsRepository;
import java.util.Optional;

public class UserSettingsServiceImpl implements UserSettingsService {

    private final UserSettingsRepository repo;

    public UserSettingsServiceImpl(UserSettingsRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserSettings create(UserSettings s) {
        repo.save(s);
        return s;
    }

    @Override
    public Optional<UserSettings> findFirst() {
        return repo.findFirst();
    }

    @Override
    public UserSettings update(UserSettings s) {
        repo.save(s);
        return s;
    }

    @Override
    public void delete(Long id) {
        repo.findFirst()
                .filter(us -> us.getId().equals(id))
                .ifPresent(repo::delete);
    }
}
