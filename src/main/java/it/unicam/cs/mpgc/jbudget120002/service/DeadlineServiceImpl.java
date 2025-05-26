package it.unicam.cs.mpgc.jbudget120002.service;

import it.unicam.cs.mpgc.jbudget120002.model.Deadline;
import it.unicam.cs.mpgc.jbudget120002.repository.DeadlineRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class DeadlineServiceImpl implements DeadlineService {

    private final DeadlineRepository repo;

    public DeadlineServiceImpl(DeadlineRepository repo) {
        this.repo = repo;
    }

    @Override
    public Deadline create(Deadline d) {
        repo.save(d);
        return d;
    }

    @Override
    public Optional<Deadline> findById(Long id) {
        return repo.findById(id);
    }

    @Override
    public List<Deadline> findAll() {
        return repo.findAll();
    }

    @Override
    public List<Deadline> findDueBefore(LocalDate date) {
        return repo.findByDueDateBefore(date);
    }

    @Override
    public Deadline update(Deadline d) {
        repo.save(d);
        return d;
    }

    @Override
    public void delete(Long id) {
        repo.findById(id).ifPresent(repo::delete);
    }
}
