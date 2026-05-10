package com.library.dao;

import com.library.model.Loan;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LoanDao {
    List<Loan>     findAll();
    Optional<Loan> findById(int id);

    /**
     * text     → якщо число — точний пошук за ID позики;
     *            інакше LIKE в title книги та імені читача.
     * status   → фільтр за статусом (active / returned / overdue).
     * dateFrom → loan_date >= dateFrom.
     * dateTo   → loan_date <= dateTo.
     */
    List<Loan> search(String text, String status, LocalDate dateFrom, LocalDate dateTo);

    void insert(Loan loan);
    void update(Loan loan);
    void delete(int id);
}
