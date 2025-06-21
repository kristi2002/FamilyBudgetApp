package it.unicam.cs.mpgc.jbudget120002.repository;

import it.unicam.cs.mpgc.jbudget120002.model.Budget;
import it.unicam.cs.mpgc.jbudget120002.model.Group;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface BudgetRepository extends Repository<Budget, Long> {

    List<Budget> findByGroups(Set<Group> groups);

    List<Budget> findByDateRange(LocalDate start, LocalDate end);

    List<Budget> findByCategory(Long categoryId);
} 