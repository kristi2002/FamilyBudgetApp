package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.model.BudgetPeriod;
import it.unicam.cs.mpgc.jbudget120002.repository.BudgetPeriodRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class BudgetPeriodServiceImpl implements BudgetPeriodService {

    private final BudgetPeriodRepository repo;

    public BudgetPeriodServiceImpl(BudgetPeriodRepository repo) {
        this.repo = repo;
    }

    @Override
    public BudgetPeriod create(BudgetPeriod b) {
        repo.save(b);
        return b;
    }

    @Override
    public Optional<BudgetPeriod> findById(Long id) {
        return repo.findById(id);
    }

    @Override
    public Optional<BudgetPeriod> findByDate(LocalDate date) {
        return repo.findByDate(date);
    }

    @Override
    public List<BudgetPeriod> findAll() {
        return repo.findAll();
    }

    @Override
    public BudgetPeriod update(BudgetPeriod b) {
        repo.save(b);
        return b;
    }

    @Override
    public void delete(Long id) {
        repo.findById(id).ifPresent(repo::delete);
    }
}
