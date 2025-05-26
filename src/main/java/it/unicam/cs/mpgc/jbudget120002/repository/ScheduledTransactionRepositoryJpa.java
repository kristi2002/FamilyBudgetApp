// src/main/java/it/unicam/cs/mpgc/jbudget120002/repository/ScheduledTransactionRepositoryJpa.java
package it.unicam.cs.mpgc.jbudget120002.repository;

import it.unicam.cs.mpgc.jbudget120002.model.ScheduledTransaction;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.List;

public class ScheduledTransactionRepositoryJpa
        extends JpaRepository<ScheduledTransaction, Long>
        implements ScheduledTransactionRepository {

    public ScheduledTransactionRepositoryJpa() {
        super(ScheduledTransaction.class);
    }

    @Override
    public List<ScheduledTransaction> findByStartDateBetween(LocalDate from, LocalDate to) {
        TypedQuery<ScheduledTransaction> q = em.createQuery(
                "FROM ScheduledTransaction s WHERE s.startDate BETWEEN :from AND :to",
                ScheduledTransaction.class);
        q.setParameter("from", from);
        q.setParameter("to", to);
        return q.getResultList();
    }
}
