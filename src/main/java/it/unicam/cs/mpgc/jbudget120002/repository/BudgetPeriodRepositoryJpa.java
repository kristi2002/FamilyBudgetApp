// src/main/java/it/unicam/cs/mpgc/jbudget120002/repository/BudgetPeriodRepositoryJpa.java
package it.unicam.cs.mpgc.jbudget120002.repository;

import it.unicam.cs.mpgc.jbudget120002.model.BudgetPeriod;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.Optional;

public class BudgetPeriodRepositoryJpa
        extends JpaRepository<BudgetPeriod, Long>
        implements BudgetPeriodRepository {

    public BudgetPeriodRepositoryJpa() {
        super(BudgetPeriod.class);
    }

    @Override
    public Optional<BudgetPeriod> findByDate(LocalDate date) {
        TypedQuery<BudgetPeriod> q = em.createQuery(
                "FROM BudgetPeriod b WHERE b.startDate <= :d AND b.endDate >= :d",
                BudgetPeriod.class);
        q.setParameter("d", date);
        return q.getResultStream().findFirst();
    }
}
