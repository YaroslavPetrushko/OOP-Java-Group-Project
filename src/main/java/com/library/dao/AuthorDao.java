package com.library.dao;

import com.library.model.Author;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for the {@link Author} entity.
 *
 * <p>All implementations must use {@link java.sql.PreparedStatement} exclusively —
 * never string concatenation — to prevent SQL injection.
 *
 * <p>All write operations ({@link #delete}) may throw a
 * {@link RuntimeException} wrapping a foreign-key violation when the record
 * is still referenced by {@code books}.
 */
public interface AuthorDao {

    /**
     * Returns all authors ordered by {@code full_name} ascending.
     *
     * @return an unmodifiable snapshot; never {@code null}
     */
    List<Author> findAll();

    /**
     * Finds a single author by its primary key.
     *
     * @param id the author ID to look up
     * @return an {@link Optional} containing the author, or empty if not found
     */
    Optional<Author> findById(int id);

    /**
     * Dynamic multi-field search with optional filters.
     *
     * <ul>
     *   <li>{@code text}     — case-insensitive LIKE match against {@code full_name},
     *                          {@code country}, and {@code birth_year}; ignored if blank</li>
     *   <li>{@code country}  — exact match against {@code country}; ignored if blank</li>
     *   <li>{@code yearFrom} — {@code birth_year >= yearFrom}; ignored if {@code null}</li>
     *   <li>{@code yearTo}   — {@code birth_year <= yearTo};   ignored if {@code null}</li>
     * </ul>
     *
     * @param text     free-text search term; may be blank
     * @param country  exact country filter; may be blank
     * @param yearFrom lower bound for birth year; may be {@code null}
     * @param yearTo   upper bound for birth year; may be {@code null}
     * @return matching authors ordered by {@code full_name}; never {@code null}
     */
    List<Author> search(String text, String country, Integer yearFrom, Integer yearTo);

    /**
     * Returns distinct country values present in the database, sorted alphabetically.
     * Used to populate the country filter {@link javafx.scene.control.ComboBox}.
     *
     * @return list of country strings; never {@code null}
     */
    List<String> findAllCountries();

    /**
     * Inserts a new author record. The {@code id} field of {@code author} is ignored;
     * the generated key is assigned by the database.
     *
     * @param author the author to insert (fullName must be set)
     */
    void insert(Author author);

    /**
     * Updates an existing author record identified by {@code author.getId()}.
     *
     * @param author the author with updated fields; {@code id} must be valid
     */
    void update(Author author);

    /**
     * Deletes the author with the given {@code id}.
     *
     * @param id the primary key of the author to delete
     * @throws RuntimeException if the author is referenced by one or more books
     *                          (foreign-key violation, SQL state {@code 23503})
     */
    void delete(int id);
}