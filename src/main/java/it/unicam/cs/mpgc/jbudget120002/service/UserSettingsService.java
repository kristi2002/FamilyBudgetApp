package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.model.UserSettings;
import java.util.Optional;

public interface UserSettingsService {
    UserSettings create(UserSettings s);
    Optional<UserSettings> findFirst();
    UserSettings update(UserSettings s);
    void delete(Long id);
}
