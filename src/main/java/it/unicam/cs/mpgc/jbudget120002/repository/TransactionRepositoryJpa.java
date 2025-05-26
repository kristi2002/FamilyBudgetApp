package it.unicam.cs.mpgc.jbudget120002.repository;

import it.unicam.cs.mpgc.jbudget120002.model.Transaction;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.List;

public class TransactionRepositoryJpa extends JpaRepository<Transaction, Long> implements TransactionRepository {

    public TransactionRepositoryJpa(EntityManager entityManager) {
        super(Transaction.class, entityManager);
    }

    @Override
    public List<Transaction> findByDateBetween(LocalDate from, LocalDate to) {
        return em.createQuery(
                "FROM Transaction t WHERE t.date BETWEEN :from AND :to ORDER BY t.date",
                Transaction.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
    }

    @Override
    public List<Transaction> findByTagName(String tagName) {
        return em.createQuery(
                "SELECT t FROM Transaction t JOIN t.tags tag WHERE tag.name = :name",
                Transaction.class)
                .setParameter("name", tagName)
                .getResultList();
    }
}
