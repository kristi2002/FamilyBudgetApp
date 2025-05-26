// src/main/java/it/unicam/cs/mpgc/jbudget120002/repository/TagRepository.java
package it.unicam.cs.mpgc.jbudget120002.repository;

import it.unicam.cs.mpgc.jbudget120002.model.Tag;
import java.util.Optional;

public interface TagRepository extends Repository<Tag, Long> {
    Optional<Tag> findByName(String name);
}
