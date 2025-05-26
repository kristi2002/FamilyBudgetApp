// src/main/java/it/unicam/cs/mpgc/jbudget120002/repository/DeadlineRepositoryJpa.java
package it.unicam.cs.mpgc.jbudget120002.repository;

import it.unicam.cs.mpgc.jbudget120002.model.Deadline;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;

public class DeadlineRepositoryJpa
        extends JpaRepository<Deadline, Long>
        implements DeadlineRepository {

    public DeadlineRepositoryJpa(EntityManager entityManager) {
        super(Deadline.class, entityManager);
    }

    @Override
    public List<Deadline> findByDueDateBefore(LocalDate date) {
        return em.createQuery(
                "FROM Deadline d WHERE d.dueDate < :d", Deadline.class)
                .setParameter("d", date)
                .getResultList();
    }
}
