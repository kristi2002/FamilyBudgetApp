package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.service.*;
import jakarta.persistence.EntityManager;
import it.unicam.cs.mpgc.jbudget120002.repository.*;

public class ServiceFactory {
    private final EntityManager entityManager;
    private TransactionService transactionService;
    private TagService tagService;
    private ExchangeRateService exchangeRateService;
    private UserSettingsService userSettingsService;
    private ScheduledTransactionService scheduledTransactionService;
    private StatisticsService statisticsService;
    private BudgetService budgetService;
    private DeadlineService deadlineService;

    public ServiceFactory(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public TransactionService getTransactionService() {
        if (transactionService == null) {
            transactionService = new TransactionServiceImpl(entityManager, getTagService(), getExchangeRateService());
        }
        return transactionService;
    }

    public TagService getTagService() {
        if (tagService == null) {
            tagService = new TagServiceImpl(entityManager);
        }
        return tagService;
    }

    public ExchangeRateService getExchangeRateService() {
        if (exchangeRateService == null) {
            exchangeRateService = new ExchangeRateServiceImpl(entityManager);
        }
        return exchangeRateService;
    }

    public UserSettingsService getUserSettingsService() {
        if (userSettingsService == null) {
            userSettingsService = new UserSettingsServiceImpl(entityManager);
        }
        return userSettingsService;
    }

    public ScheduledTransactionService getScheduledTransactionService() {
        if (scheduledTransactionService == null) {
            scheduledTransactionService = new ScheduledTransactionServiceImpl(entityManager, getTagService());
        }
        return scheduledTransactionService;
    }

    public StatisticsService getStatisticsService() {
        if (statisticsService == null) {
            statisticsService = new StatisticsServiceImpl(entityManager, getTransactionService(), getTagService());
        }
        return statisticsService;
    }

    public BudgetService getBudgetService() {
        if (budgetService == null) {
            budgetService = new BudgetServiceImpl(entityManager, getTagService(), getTransactionService());
        }
        return budgetService;
    }

    public DeadlineService getDeadlineService() {
        if (deadlineService == null) {
            deadlineService = new DeadlineServiceImpl(new DeadlineRepositoryJpa(entityManager));
        }
        return deadlineService;
    }
} 