// src/main/java/it/unicam/cs/mpgc/jbudget120002/repository/TagRepositoryJpa.java
package it.unicam.cs.mpgc.jbudget120002.repository;

import it.unicam.cs.mpgc.jbudget120002.model.Tag;
import jakarta.persistence.TypedQuery;
import java.util.Optional;

public class TagRepositoryJpa extends JpaRepository<Tag, Long>
        implements TagRepository {

    public TagRepositoryJpa() {
        super(Tag.class);
    }

    @Override
    public Optional<Tag> findByName(String name) {
        TypedQuery<Tag> q = em.createQuery(
                "FROM Tag t WHERE t.name = :name", Tag.class);
        q.setParameter("name", name);
        return q.getResultStream().findFirst();
    }
}
