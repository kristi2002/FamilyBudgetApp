package it.unicam.cs.mpgc.jbudget120002.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Deadline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;
    private LocalDate dueDate;
    private double amount;
    private boolean isPaid;
    private String category;

    @OneToOne
    private Transaction relatedTransaction;

    public Deadline() {}

    public Deadline(String description,
                    LocalDate dueDate,
                    double amount,
                    boolean isPaid,
                    Transaction relatedTransaction,
                    String category) {
        this.description = description;
        this.dueDate = dueDate;
        this.amount = amount;
        this.isPaid = isPaid;
        this.relatedTransaction = relatedTransaction;
        this.category = category;
    }

    public Long getId() { return id; }
    public String getDescription() { return description; }
    public LocalDate getDueDate() { return dueDate; }
    public double getAmount() { return amount; }
    public boolean isPaid() { return isPaid; }
    public Transaction getRelatedTransaction() { return relatedTransaction; }
    public String getCategory() { return category; }

    public void setDescription(String description) {
        this.description = description;
    }
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
    public void setAmount(double amount) {
        this.amount = amount;
    }
    public void setPaid(boolean paid) {
        isPaid = paid;
    }
    public void setRelatedTransaction(Transaction relatedTransaction) {
        this.relatedTransaction = relatedTransaction;
    }
    public void setCategory(String category) { this.category = category; }
    public void setId(Long id) { this.id = id; }
}
