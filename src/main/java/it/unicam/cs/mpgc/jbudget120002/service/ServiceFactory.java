package it.unicam.cs.mpgc.jbudget120002.service;

import jakarta.persistence.EntityManager;
import it.unicam.cs.mpgc.jbudget120002.repository.*;

public class ServiceFactory {
    private final EntityManager entityManager;
    private TagService tagService;
    private TransactionService transactionService;
    private ScheduledTransactionService scheduledService;
    private BudgetService budgetService;
    private StatisticsService statisticsService;

    public ServiceFactory(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public TagService getTagService() {
        if (tagService == null) {
            tagService = new TagServiceImpl(entityManager);
        }
        return tagService;
    }

    public TransactionService getTransactionService() {
        if (transactionService == null) {
            transactionService = new TransactionServiceImpl(entityManager, getTagService());
        }
        return transactionService;
    }

    public ScheduledTransactionService getScheduledTransactionService() {
        if (scheduledService == null) {
            scheduledService = new ScheduledTransactionServiceImpl(entityManager, getTagService());
        }
        return scheduledService;
    }

    public BudgetService getBudgetService() {
        if (budgetService == null) {
            budgetService = new BudgetServiceImpl(entityManager, getTagService(), getTransactionService());
        }
        return budgetService;
    }

    public StatisticsService getStatisticsService() {
        if (statisticsService == null) {
            statisticsService = new StatisticsServiceImpl(entityManager, getTransactionService(), getTagService());
        }
        return statisticsService;
    }
} 