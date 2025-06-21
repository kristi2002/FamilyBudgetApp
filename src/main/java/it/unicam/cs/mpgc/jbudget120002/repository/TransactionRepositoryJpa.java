package it.unicam.cs.mpgc.jbudget120002.repository;

import it.unicam.cs.mpgc.jbudget120002.model.Group;
import it.unicam.cs.mpgc.jbudget120002.model.ScheduledTransaction;
import it.unicam.cs.mpgc.jbudget120002.model.Tag;
import it.unicam.cs.mpgc.jbudget120002.model.Transaction;
import it.unicam.cs.mpgc.jbudget120002.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    public List<Transaction> findByDateBetweenForUser(User user, LocalDate from, LocalDate to) {
        Set<Group> userGroups = user.getGroups();
        if (userGroups.isEmpty()) {
            TypedQuery<Transaction> query = em.createQuery(
                "SELECT t FROM Transaction t WHERE t.user = :user AND t.date BETWEEN :from AND :to ORDER BY t.date", 
                Transaction.class);
            query.setParameter("user", user);
            query.setParameter("from", from);
            query.setParameter("to", to);
            return query.getResultList();
        }

        TypedQuery<Transaction> query = em.createQuery(
            "SELECT t FROM Transaction t WHERE (t.user = :user OR EXISTS " +
            "(SELECT g FROM t.user.groups g WHERE g IN :groups)) AND t.date BETWEEN :from AND :to ORDER BY t.date", 
            Transaction.class);
        query.setParameter("user", user);
        query.setParameter("groups", userGroups);
        query.setParameter("from", from);
        query.setParameter("to", to);
        return query.getResultList();
    }

    @Override
    public List<Transaction> findByTagName(String tagName) {
        return em.createQuery(
                "SELECT t FROM Transaction t JOIN t.tags tag WHERE tag.name = :name",
                Transaction.class)
                .setParameter("name", tagName)
                .getResultList();
    }

    @Override
    public List<Transaction> findByTagId(Long tagId) {
        TypedQuery<Transaction> query = em.createQuery(
            "SELECT DISTINCT t FROM Transaction t JOIN t.tags tag WHERE tag.id = :tagId",
            Transaction.class);
        query.setParameter("tagId", tagId);
        return query.getResultList();
    }

    @Override
    public List<Transaction> findByTagIdAndDateRange(Long tagId, LocalDate startDate, LocalDate endDate) {
        TypedQuery<Transaction> query = em.createQuery(
            "SELECT DISTINCT t FROM Transaction t JOIN t.tags tag " +
            "WHERE tag.id = :tagId AND t.date BETWEEN :start AND :end",
            Transaction.class);
        query.setParameter("tagId", tagId);
        query.setParameter("start", startDate);
        query.setParameter("end", endDate);
        return query.getResultList();
    }

    @Override
    public List<Transaction> findByScheduledTransaction(Long scheduledTransactionId) {
        TypedQuery<Transaction> query = em.createQuery(
            "SELECT t FROM Transaction t WHERE t.scheduledTransaction.id = :scheduledId",
            Transaction.class);
        query.setParameter("scheduledId", scheduledTransactionId);
        return query.getResultList();
    }

    @Override
    public List<Transaction> findByLoanPlan(Long loanPlanId) {
        TypedQuery<Transaction> query = em.createQuery(
            "SELECT t FROM Transaction t WHERE t.loanPlan.id = :loanPlanId",
            Transaction.class);
        query.setParameter("loanPlanId", loanPlanId);
        return query.getResultList();
    }

    @Override
    public List<Transaction> findByTags(Collection<Long> tagIds, boolean matchAll, int tagCount) {
        if (tagIds == null || tagIds.isEmpty()) {
            return List.of();
        }

        String jpql = matchAll ?
            "SELECT t FROM Transaction t WHERE " +
            "SIZE(t.tags) >= :tagCount AND " +
            "(SELECT COUNT(tag) FROM t.tags tag WHERE tag.id IN :tagIds) = :tagCount" :
            "SELECT DISTINCT t FROM Transaction t JOIN t.tags tag WHERE tag.id IN :tagIds";

        TypedQuery<Transaction> query = em.createQuery(jpql, Transaction.class);
        query.setParameter("tagIds", tagIds);
        if (matchAll) {
            query.setParameter("tagCount", (long) tagCount);
        }
        return query.getResultList();
    }

    @Override
    public List<Transaction> findAllForUser(User user) {
        Set<Group> userGroups = user.getGroups();
        if (userGroups.isEmpty()) {
            TypedQuery<Transaction> query = em.createQuery(
                "SELECT t FROM Transaction t WHERE t.user = :user", Transaction.class);
            query.setParameter("user", user);
            return query.getResultList();
        }

        TypedQuery<Transaction> query = em.createQuery(
            "SELECT t FROM Transaction t WHERE t.user = :user OR EXISTS " +
            "(SELECT g FROM t.user.groups g WHERE g IN :groups)", Transaction.class);
        query.setParameter("user", user);
        query.setParameter("groups", userGroups);
        return query.getResultList();
    }

    @Override
    public List<Transaction> findWithFilters(User user, List<Long> groupIds, String searchTerm, LocalDate startDate, LocalDate endDate, List<Long> tagIds) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Transaction> cq = cb.createQuery(Transaction.class);
        Root<Transaction> transaction = cq.from(Transaction.class);
        List<Predicate> predicates = new ArrayList<>();

        // User and Group filtering
        if (user != null) {
            Predicate userPredicate = cb.equal(transaction.get("user"), user);
            if (groupIds != null && !groupIds.isEmpty()) {
                Join<Transaction, User> userJoin = transaction.join("user");
                Join<User, Group> groupJoin = userJoin.join("groups");
                Predicate groupPredicate = groupJoin.get("id").in(groupIds);
                predicates.add(cb.or(userPredicate, groupPredicate));
            } else {
                predicates.add(userPredicate);
            }
        }

        // Date range filtering
        if (startDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(transaction.get("date"), startDate));
        }
        if (endDate != null) {
            predicates.add(cb.lessThanOrEqualTo(transaction.get("date"), endDate));
        }

        // Search term filtering
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(transaction.get("description")), "%" + searchTerm.toLowerCase() + "%"));
        }

        // Tag filtering
        if (tagIds != null && !tagIds.isEmpty()) {
            Join<Transaction, Tag> tagJoin = transaction.join("tags");
            predicates.add(tagJoin.get("id").in(tagIds));
        }

        cq.where(predicates.toArray(new Predicate[0])).distinct(true);
        cq.orderBy(cb.desc(transaction.get("date")));

        return em.createQuery(cq).getResultList();
    }

    @Override
    public boolean existsByScheduledTransactionAndDate(ScheduledTransaction scheduledTransaction, LocalDate date) {
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(t) FROM Transaction t WHERE t.scheduledTransaction = :st AND t.date = :date", Long.class);
        query.setParameter("st", scheduledTransaction);
        query.setParameter("date", date);
        return query.getSingleResult() > 0;
    }

    @Override
    public List<Transaction> findByUserAndGroups(User user, Set<Long> groupIds, LocalDate startDate, LocalDate endDate, String search, List<Tag> tags) {
        // This is a complex query, redirecting to the existing flexible filter method
        List<Long> tagIds = tags.stream().map(Tag::getId).collect(Collectors.toList());
        return findWithFilters(user, new ArrayList<>(groupIds), search, startDate, endDate, tagIds);
    }

    @Override
    public List<Transaction> findByTagAndDateRange(Long tagId, LocalDate startDate, LocalDate endDate) {
        TypedQuery<Transaction> query = em.createQuery(
                "SELECT t FROM Transaction t JOIN t.tags tag WHERE tag.id = :tagId " +
                        "AND t.date >= :startDate AND t.date <= :endDate", Transaction.class);
        query.setParameter("tagId", tagId);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        return query.getResultList();
    }
}
