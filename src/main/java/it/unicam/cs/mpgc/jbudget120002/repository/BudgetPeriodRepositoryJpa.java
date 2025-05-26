// src/main/java/it/unicam/cs/mpgc/jbudget120002/repository/BudgetPeriodRepositoryJpa.java
package it.unicam.cs.mpgc.jbudget120002.repository;

import it.unicam.cs.mpgc.jbudget120002.model.BudgetPeriod;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.Optional;

public class BudgetPeriodRepositoryJpa 
        extends JpaRepository<BudgetPeriod, Long>
        implements BudgetPeriodRepository {

    public BudgetPeriodRepositoryJpa(EntityManager entityManager) {
        super(BudgetPeriod.class, entityManager);
    }

    @Override
    public Optional<BudgetPeriod> findByDate(LocalDate date) {
        return em.createQuery(
                "FROM BudgetPeriod b WHERE b.startDate <= :d AND b.endDate >= :d",
                BudgetPeriod.class)
                .setParameter("d", date)
                .getResultStream()
                .findFirst();
    }
}
