package com.library.dao;

import com.library.model.Loan;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for the {@link Loan} entity.
 *
 * <p>All implementations must use {@link java.sql.PreparedStatement} exclusively —
 * never string concatenation — to prevent SQL injection.
 */
public interface LoanDao {

    /**
     * Returns all loans ordered by {@code id} descending (newest first).
     *
     * @return an unmodifiable snapshot; never {@code null}
     */
    List<Loan> findAll();

    /**
     * Finds a single loan by its primary key.
     *
     * @param id the loan ID to look up
     * @return an {@link Optional} containing the loan, or empty if not found
     */
    Optional<Loan> findById(int id);

    /**
     * Dynamic multi-field search with optional filters.
     *
     * <ul>
     *   <li>{@code text}     — if the value is numeric, performs an exact match on
     *                          {@code loans.id}; otherwise performs a case-insensitive
     *                          LIKE match against {@code books.title} and
     *                          {@code readers.full_name}; ignored if blank</li>
     *   <li>{@code status}   — exact match against {@code status}; ignored if blank</li>
     *   <li>{@code dateFrom} — {@code loan_date >= dateFrom}; ignored if {@code null}</li>
     *   <li>{@code dateTo}   — {@code loan_date <= dateTo};   ignored if {@code null}</li>
     * </ul>
     *
     * @param text     free-text / ID search term; may be blank
     * @param status   exact status filter ({@code "active"}, {@code "returned"},
     *                 {@code "overdue"}); may be blank
     * @param dateFrom lower bound for loan date; may be {@code null}
     * @param dateTo   upper bound for loan date; may be {@code null}
     * @return matching loans ordered by {@code id} descending; never {@code null}
     */
    List<Loan> search(String text, String status, LocalDate dateFrom, LocalDate dateTo);

    /**
     * Inserts a new loan record. The {@code id} field is ignored;
     * the generated key is assigned by the database.
     *
     * @param loan the loan to insert (bookId, readerId, and dueDate must be set)
     */
    void insert(Loan loan);

    /**
     * Updates an existing loan record identified by {@code loan.getId()}.
     *
     * @param loan the loan with updated fields; {@code id} must be valid
     */
    void update(Loan loan);

    /**
     * Deletes the loan with the given {@code id}.
     *
     * @param id the primary key of the loan to delete
     * @throws RuntimeException wrapping any unexpected {@link java.sql.SQLException}
     */
    void delete(int id);

    /**
     * Sets status to {@code "overdue"} for all active loans whose
     * {@code due_date} is before today. Called once on application startup.
     *
     * @return the number of rows updated
     */
    int markOverdue();
}