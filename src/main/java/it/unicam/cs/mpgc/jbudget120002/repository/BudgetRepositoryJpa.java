package it.unicam.cs.mpgc.jbudget120002.repository;

import it.unicam.cs.mpgc.jbudget120002.model.Budget;
import it.unicam.cs.mpgc.jbudget120002.model.Group;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class BudgetRepositoryJpa extends JpaRepository<Budget, Long> implements BudgetRepository {

    public BudgetRepositoryJpa(EntityManager entityManager) {
        super(Budget.class, entityManager);
    }

    @Override
    public List<Budget> findByGroups(Set<Group> groups) {
        if (groups == null || groups.isEmpty()) {
            return Collections.emptyList();
        }
        TypedQuery<Budget> query = em.createQuery(
                "SELECT b FROM Budget b WHERE b.group IN :groups", Budget.class);
        query.setParameter("groups", groups);
        return query.getResultList();
    }

    @Override
    public List<Budget> findByDateRange(LocalDate start, LocalDate end) {
        TypedQuery<Budget> query = em.createQuery(
                "SELECT b FROM Budget b WHERE b.startDate <= :end AND b.endDate >= :start",
                Budget.class);
        query.setParameter("start", start);
        query.setParameter("end", end);
        return query.getResultList();
    }

    @Override
    public List<Budget> findByCategory(Long categoryId) {
        TypedQuery<Budget> query = em.createQuery(
                "SELECT DISTINCT b FROM Budget b JOIN b.tags t WHERE t.id = :categoryId",
                Budget.class);
        query.setParameter("categoryId", categoryId);
        return query.getResultList();
    }
} 