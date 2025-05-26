// src/main/java/it/unicam/cs/mpgc/jbudget120002/repository/UserSettingsRepository.java
package it.unicam.cs.mpgc.jbudget120002.repository;

import it.unicam.cs.mpgc.jbudget120002.model.UserSettings;
import java.util.Optional;

public interface UserSettingsRepository
        extends Repository<UserSettings, Long> {
    Optional<UserSettings> findFirst();  // e.g. if you only have one settings row
}
