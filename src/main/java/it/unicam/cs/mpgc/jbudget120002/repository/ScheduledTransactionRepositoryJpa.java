// src/main/java/it/unicam/cs/mpgc/jbudget120002/repository/ScheduledTransactionRepositoryJpa.java
package it.unicam.cs.mpgc.jbudget120002.repository;

import it.unicam.cs.mpgc.jbudget120002.model.ScheduledTransaction;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;

public class ScheduledTransactionRepositoryJpa
        extends JpaRepository<ScheduledTransaction, Long>
        implements ScheduledTransactionRepository {

    public ScheduledTransactionRepositoryJpa(EntityManager entityManager) {
        super(ScheduledTransaction.class, entityManager);
    }

    @Override
    public List<ScheduledTransaction> findByStartDateBetween(LocalDate from, LocalDate to) {
        return em.createQuery(
                "FROM ScheduledTransaction s WHERE s.startDate BETWEEN :from AND :to",
                ScheduledTransaction.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
    }
}
