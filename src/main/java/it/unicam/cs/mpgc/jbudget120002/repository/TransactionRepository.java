package it.unicam.cs.mpgc.jbudget120002.repository;

import it.unicam.cs.mpgc.jbudget120002.model.Transaction;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends Repository<Transaction, Long> {
    List<Transaction> findByDateBetween(LocalDate from, LocalDate to);
    List<Transaction> findByTagName(String tagName);
}
