package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.repository.*;
import jakarta.persistence.EntityManager;

public class ServiceFactory {

    private final EntityManager entityManager;

    private BudgetService budgetService;
    private DeadlineService deadlineService;
    private GroupService groupService;
    private ScheduledTransactionService scheduledTransactionService;
    private StatisticsService statisticsService;
    private TagService tagService;
    private TransactionService transactionService;
    private UserService userService;
    private UserSettingsService userSettingsService;
    private SyncService syncService;

    public ServiceFactory(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public BudgetService getBudgetService(boolean newInstance) {
        if (budgetService == null || newInstance) {
            budgetService = new BudgetServiceImpl(
                    entityManager,
                    new BudgetRepositoryJpa(entityManager),
                    getTransactionService(true),
                    getTagService(true)
            );
        }
        return budgetService;
    }

    public DeadlineService getDeadlineService(boolean newInstance) {
        if (deadlineService == null || newInstance) {
            deadlineService = new DeadlineServiceImpl(new DeadlineRepositoryJpa(entityManager));
        }
        return deadlineService;
    }

    public GroupService getGroupService(boolean newInstance) {
        if (groupService == null || newInstance) {
            groupService = new GroupServiceImpl(
                    entityManager,
                    new GroupRepositoryJpa(entityManager),
                    new UserRepositoryJpa(entityManager));
        }
        return groupService;
    }

    public ScheduledTransactionService getScheduledTransactionService(boolean newInstance) {
        if (scheduledTransactionService == null || newInstance) {
            scheduledTransactionService = new ScheduledTransactionServiceImpl(
                    entityManager,
                    new ScheduledTransactionRepositoryJpa(entityManager),
                    new TransactionRepositoryJpa(entityManager),
                    getTagService(true)
            );
        }
        return scheduledTransactionService;
    }

    public StatisticsService getStatisticsService(boolean newInstance) {
        if (statisticsService == null || newInstance) {
            statisticsService = new StatisticsServiceImpl(
                    entityManager,
                    getTransactionService(true),
                    getTagService(true)
            );
        }
        return statisticsService;
    }

    public TagService getTagService(boolean newInstance) {
        if (tagService == null || newInstance) {
            tagService = new TagServiceImpl(entityManager);
        }
        return tagService;
    }

    public TransactionService getTransactionService(boolean newInstance) {
        if (transactionService == null || newInstance) {
            transactionService = new TransactionServiceImpl(
                    entityManager,
                    new TransactionRepositoryJpa(entityManager),
                    getTagService(true)
            );
        }
        return transactionService;
    }

    public UserService getUserService(boolean newInstance) {
        if (userService == null || newInstance) {
            userService = new UserServiceImpl(
                    entityManager,
                    new UserRepositoryJpa(entityManager)
            );
        }
        return userService;
    }

    public UserSettingsService getUserSettingsService(boolean newInstance) {
        if (userSettingsService == null || newInstance) {
            userSettingsService = new UserSettingsServiceImpl(
                    entityManager,
                    new UserSettingsRepositoryJpa(entityManager)
            );
        }
        return userSettingsService;
    }

    public SyncService getSyncService(boolean newInstance) {
        if (syncService == null || newInstance) {
            syncService = new FileSyncService(
                    entityManager,
                    getTransactionService(true),
                    getScheduledTransactionService(true),
                    "sync/data.json"
            );
        }
        return syncService;
    }
} 