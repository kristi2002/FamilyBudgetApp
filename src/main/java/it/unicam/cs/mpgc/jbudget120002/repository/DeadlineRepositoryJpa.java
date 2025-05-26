// src/main/java/it/unicam/cs/mpgc/jbudget120002/repository/DeadlineRepositoryJpa.java
package it.unicam.cs.mpgc.jbudget120002.repository;

import it.unicam.cs.mpgc.jbudget120002.model.Deadline;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.List;

public class DeadlineRepositoryJpa
        extends JpaRepository<Deadline, Long>
        implements DeadlineRepository {

    public DeadlineRepositoryJpa() {
        super(Deadline.class);
    }

    @Override
    public List<Deadline> findByDueDateBefore(LocalDate date) {
        TypedQuery<Deadline> q = em.createQuery(
                "FROM Deadline d WHERE d.dueDate < :d", Deadline.class);
        q.setParameter("d", date);
        return q.getResultList();
    }
}
