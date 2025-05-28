package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.model.Deadline;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing financial deadlines and reminders in the Family Budget App.
 * This interface defines methods for handling important due dates, payment reminders,
 * and deadline-related notifications.
 *
 * Responsibilities:
 * - Track and manage financial deadlines (bills, loan payments, etc.)
 * - Provide reminders and alerts for upcoming or overdue deadlines
 * - Integrate with notification and scheduling systems
 * - Support querying and updating deadline status
 *
 * Usage:
 * Implemented by DeadlineServiceImpl to provide deadline management functionality
 * to controllers and other services.
 */
public interface DeadlineService {
    Deadline create(Deadline d);
    Optional<Deadline> findById(Long id);
    List<Deadline> findAll();
    List<Deadline> findDueBefore(LocalDate date);
    Deadline update(Deadline d);
    void delete(Long id);
}
