// src/main/java/it/unicam/cs/mpgc/jbudget120002/repository/ScheduledTransactionRepositoryJpa.java
package it.unicam.cs.mpgc.jbudget120002.repository;

import it.unicam.cs.mpgc.jbudget120002.model.ScheduledTransaction;
import it.unicam.cs.mpgc.jbudget120002.model.Tag;
import it.unicam.cs.mpgc.jbudget120002.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Repository
public class ScheduledTransactionRepositoryJpa
        extends JpaRepository<ScheduledTransaction, Long>
        implements ScheduledTransactionRepository {

    public ScheduledTransactionRepositoryJpa(EntityManager em) {
        super(ScheduledTransaction.class, em);
    }

    @Override
    public List<ScheduledTransaction> findByDateRange(LocalDate start, LocalDate end) {
        TypedQuery<ScheduledTransaction> query = em.createQuery(
                "SELECT s FROM ScheduledTransaction s WHERE " +
                        "s.startDate <= :end AND (s.endDate IS NULL OR s.endDate >= :start)",
                ScheduledTransaction.class);
        query.setParameter("start", start);
        query.setParameter("end", end);
        return query.getResultList();
    }

    @Override
    public List<ScheduledTransaction> findActive(LocalDate asOfDate) {
        TypedQuery<ScheduledTransaction> query = em.createQuery(
                "SELECT s FROM ScheduledTransaction s WHERE " +
                        "s.startDate <= :date AND (s.endDate IS NULL OR s.endDate >= :date) " +
                        "AND s.processingState = :state",
                ScheduledTransaction.class);
        query.setParameter("date", asOfDate);
        query.setParameter("state", ScheduledTransaction.ProcessingState.PENDING);
        return query.getResultList();
    }

    @Override
    public List<ScheduledTransaction> findByTag(Tag tag) {
        if (tag == null) return Collections.emptyList();
        TypedQuery<ScheduledTransaction> query = em.createQuery(
                "SELECT st FROM ScheduledTransaction st JOIN st.tags t WHERE t = :tag", ScheduledTransaction.class);
        query.setParameter("tag", tag);
        return query.getResultList();
    }

    @Override
    public List<ScheduledTransaction> findByTags(List<Tag> tags) {
        if (tags == null || tags.isEmpty()) return Collections.emptyList();
        TypedQuery<ScheduledTransaction> query = em.createQuery(
                "SELECT DISTINCT st FROM ScheduledTransaction st JOIN st.tags t WHERE t IN :tags", ScheduledTransaction.class);
        query.setParameter("tags", tags);
        return query.getResultList();
    }

    @Override
    public BigDecimal calculateSumForPeriod(LocalDate startDate, LocalDate endDate, boolean isIncome) {
        TypedQuery<BigDecimal> query = em.createQuery(
                "SELECT COALESCE(SUM(st.amount), 0) FROM ScheduledTransaction st " +
                        "WHERE st.isIncome = :isIncome " +
                        "AND st.startDate <= :endDate " +
                        "AND (st.endDate IS NULL OR st.endDate >= :startDate)", BigDecimal.class);
        query.setParameter("isIncome", isIncome);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        return query.getSingleResult();
    }

    @Override
    public List<ScheduledTransaction> findByUser(User user) {
        return em.createQuery("SELECT st FROM ScheduledTransaction st WHERE st.user = :user", ScheduledTransaction.class)
                .setParameter("user", user)
                .getResultList();
    }
}
