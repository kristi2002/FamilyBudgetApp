package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.model.Deadline;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DeadlineService {
    Deadline create(Deadline d);
    Optional<Deadline> findById(Long id);
    List<Deadline> findAll();
    List<Deadline> findDueBefore(LocalDate date);
    Deadline update(Deadline d);
    void delete(Long id);
}
