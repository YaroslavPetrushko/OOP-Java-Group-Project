package com.library.dao;

import com.library.model.Reader;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for the {@link Reader} entity.
 *
 * <p>All implementations must use {@link java.sql.PreparedStatement} exclusively —
 * never string concatenation — to prevent SQL injection.
 *
 * <p>Write operations ({@link #delete}) may throw a {@link RuntimeException}
 * wrapping a foreign-key violation when the reader still has loans.
 */
public interface ReaderDao {

    /**
     * Returns all readers ordered by {@code full_name} ascending.
     *
     * @return an unmodifiable snapshot; never {@code null}
     */
    List<Reader> findAll();

    /**
     * Finds a single reader by its primary key.
     *
     * @param id the reader ID to look up
     * @return an {@link Optional} containing the reader, or empty if not found
     */
    Optional<Reader> findById(int id);

    /**
     * Dynamic multi-field search with optional date-range filter.
     *
     * <ul>
     *   <li>{@code text}    — case-insensitive LIKE match against {@code full_name},
     *                         {@code email}, and {@code phone}; ignored if blank</li>
     *   <li>{@code regFrom} — {@code reg_date >= regFrom}; ignored if {@code null}</li>
     *   <li>{@code regTo}   — {@code reg_date <= regTo};   ignored if {@code null}</li>
     * </ul>
     *
     * @param text    free-text search term; may be blank
     * @param regFrom lower bound for registration date; may be {@code null}
     * @param regTo   upper bound for registration date; may be {@code null}
     * @return matching readers ordered by {@code full_name}; never {@code null}
     */
    List<Reader> search(String text, LocalDate regFrom, LocalDate regTo);

    /**
     * Inserts a new reader record. The {@code id} field is ignored;
     * the generated key is assigned by the database.
     *
     * @param reader the reader to insert (fullName must be set)
     */
    void insert(Reader reader);

    /**
     * Updates an existing reader record identified by {@code reader.getId()}.
     *
     * @param reader the reader with updated fields; {@code id} must be valid
     */
    void update(Reader reader);

    /**
     * Deletes the reader with the given {@code id}.
     *
     * @param id the primary key of the reader to delete
     * @throws RuntimeException if the reader is referenced by one or more loans
     *                          (foreign-key violation, SQL state {@code 23503})
     */
    void delete(int id);
}