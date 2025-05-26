package it.unicam.cs.mpgc.jbudget120002.repository;

import it.unicam.cs.mpgc.jbudget120002.model.Transaction;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.util.List;

public class TransactionRepositoryJpa extends JpaRepository<Transaction, Long>
        implements TransactionRepository {

    public TransactionRepositoryJpa() {
        super(Transaction.class);
    }

    @Override
    public List<Transaction> findByDateBetween(LocalDate from, LocalDate to) {
        TypedQuery<Transaction> q = em.createQuery(
                "FROM Transaction t WHERE t.date BETWEEN :from AND :to", Transaction.class);
        q.setParameter("from", from);
        q.setParameter("to", to);
        return q.getResultList();
    }

    @Override
    public List<Transaction> findByTagName(String tagName) {
        TypedQuery<Transaction> q = em.createQuery(
                "SELECT t FROM Transaction t JOIN t.tags tag WHERE tag.name = :name", Transaction.class);
        q.setParameter("name", tagName);
        return q.getResultList();
    }
}
