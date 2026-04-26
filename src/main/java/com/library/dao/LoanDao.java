package com.library.dao;

import com.library.model.Loan;
import java.util.List;
import java.util.Optional;

public interface LoanDao {
    List<Loan>     findAll();
    List<Loan>     findBySearch(String query);   // шукає по назві книги або імені читача
    Optional<Loan> findById(int id);
    void           insert(Loan loan);
    void           update(Loan loan);
    void           delete(int id);
}