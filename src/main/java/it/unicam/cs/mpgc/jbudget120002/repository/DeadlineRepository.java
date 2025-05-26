// src/main/java/it/unicam/cs/mpgc/jbudget120002/repository/DeadlineRepository.java
package it.unicam.cs.mpgc.jbudget120002.repository;

import it.unicam.cs.mpgc.jbudget120002.model.Deadline;
import java.time.LocalDate;
import java.util.List;

public interface DeadlineRepository
        extends Repository<Deadline, Long> {
    List<Deadline> findByDueDateBefore(LocalDate date);
}
