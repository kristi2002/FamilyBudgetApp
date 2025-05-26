package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.service.*;
import jakarta.persistence.EntityManager;
import it.unicam.cs.mpgc.jbudget120002.repository.*;

public class ServiceFactory {
    private final EntityManager entityManager;
    private TransactionService transactionService;
    private TagService tagService;
    private StatisticsService statisticsService;
    private ScheduledTransactionService scheduledService;
    private BudgetService budgetService;
    private UserSettingsService userSettingsService;

    public ServiceFactory(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public TransactionService getTransactionService() {
        if (transactionService == null) {
            transactionService = new TransactionServiceImpl(entityManager, getTagService());
        }
        return transactionService;
    }

    public TagService getTagService() {
        if (tagService == null) {
            tagService = new TagServiceImpl(entityManager);
        }
        return tagService;
    }

    public StatisticsService getStatisticsService() {
        if (statisticsService == null) {
            statisticsService = new StatisticsServiceImpl(entityManager, getTransactionService(), getTagService());
        }
        return statisticsService;
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

    public UserSettingsService getUserSettingsService() {
        if (userSettingsService == null) {
            userSettingsService = new UserSettingsServiceImpl(entityManager);
        }
        return userSettingsService;
    }
} 