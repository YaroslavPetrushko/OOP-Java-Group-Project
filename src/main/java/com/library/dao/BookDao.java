package com.library.dao;

import com.library.model.Book;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for the {@link Book} entity.
 *
 * <p>All implementations must use {@link java.sql.PreparedStatement} exclusively —
 * never string concatenation — to prevent SQL injection.
 *
 * <p>All write operations ({@link #delete}) may throw a
 * {@link RuntimeException} wrapping a foreign-key violation when the record
 * is still referenced by {@code loans}.
 */
public interface BookDao {

    /**
     * Returns all books ordered by {@code id} ascending.
     *
     * @return an unmodifiable snapshot; never {@code null}
     */
    List<Book> findAll();

    /**
     * Finds a single book by its primary key.
     *
     * @param id the book ID to look up
     * @return an {@link Optional} containing the book, or empty if not found
     */
    Optional<Book> findById(int id);

    /**
     * Dynamic multi-field search with optional filters.
     *
     * <ul>
     *   <li>{@code text}     — case-insensitive LIKE match against {@code title}
     *                          and {@code author.full_name}; ignored if blank</li>
     *   <li>{@code genre}    — exact match against {@code genre}; ignored if blank</li>
     *   <li>{@code yearFrom} — {@code pub_year >= yearFrom}; ignored if {@code null}</li>
     *   <li>{@code yearTo}   — {@code pub_year <= yearTo};   ignored if {@code null}</li>
     * </ul>
     *
     * @param text     free-text search term (title / author); may be blank
     * @param genre    exact genre filter; may be blank
     * @param yearFrom lower bound for publication year; may be {@code null}
     * @param yearTo   upper bound for publication year; may be {@code null}
     * @return matching books ordered by {@code id}; never {@code null}
     */
    List<Book> search(String text, String genre, Integer yearFrom, Integer yearTo);

    /**
     * Returns distinct genre values present in the database, sorted alphabetically.
     * Used to populate the genre filter {@link javafx.scene.control.ComboBox}.
     *
     * @return list of genre strings; never {@code null}
     */
    List<String> findAllGenres();

    /**
     * Inserts a new book record. The {@code id} field of {@code book} is ignored;
     * the generated key is assigned by the database.
     *
     * @param book the book to insert (title and authorId must be set)
     */
    void insert(Book book);

    /**
     * Updates an existing book record identified by {@code book.getId()}.
     *
     * @param book the book with updated fields; {@code id} must be valid
     */
    void update(Book book);

    /**
     * Deletes the book with the given {@code id}.
     *
     * @param id the primary key of the book to delete
     * @throws RuntimeException if the book is referenced by one or more loans
     *                          (foreign-key violation, SQL state {@code 23503})
     */
    void delete(int id);
}