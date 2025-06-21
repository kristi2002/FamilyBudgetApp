package it.unicam.cs.mpgc.jbudget120002.repository;

import java.util.List;
import java.util.Optional;

public interface Repository<T, ID> {
    Optional<T> findById(ID id);
    List<T> findAll();
    void save(T entity);
    void delete(T entity);
    void deleteById(ID id);
}
