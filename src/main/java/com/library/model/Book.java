package com.library.model;

import java.util.Objects;

/**
 * Represents a book entity in the Library Management System.
 *
 * <p>Maps to the {@code books} table in the database:
 * <pre>
 *   id (PK) | title | author_id (FK) | genre | isbn | pub_year | copies
 * </pre>
 *
 * <p>Each book belongs to exactly one {@link Author}.
 * The field {@link #authorName} is a denormalized JOIN value — it is
 * populated by DAO queries and is <em>not</em> persisted directly.
 *
 * <p>Deletion of a book is restricted by the database if any active
 * {@link Loan} records reference it.
 */
public class Book {

    /** Primary key. Set by the database on INSERT. */
    private int    id;

    /** Title of the book (NOT NULL in DB). */
    private String title;

    /** Foreign key referencing {@code authors.id} (NOT NULL in DB). */
    private int    authorId;

    /**
     * Denormalized author name populated via JOIN in DAO queries.
     * Not stored in the {@code books} table; used only for display.
     */
    private String authorName;

    /** Genre of the book; nullable. */
    private String genre;

    /** ISBN code; nullable, unique in the DB. */
    private String isbn;

    /**
     * Publication year; {@code 0} is used as a sentinel for "not set"
     * (stored as NULL in the database).
     */
    private int    pubYear;

    /**
     * Number of physical copies available. Must be {@code >= 0}
     * (enforced by DB CHECK constraint).
     */
    private int    copies;

    /** No-arg constructor required for DAO result mapping. */
    public Book() {}

    /**
     * Full constructor used by {@code BookDaoImpl} when mapping a {@link java.sql.ResultSet}.
     *
     * @param id         primary key
     * @param title      book title
     * @param authorId   FK to the author
     * @param authorName denormalized author name from JOIN
     * @param genre      genre (may be {@code null})
     * @param isbn       ISBN (may be {@code null})
     * @param pubYear    publication year, or {@code 0} if unknown
     * @param copies     number of available copies
     */
    public Book(int id, String title, int authorId, String authorName,
                String genre, String isbn, Integer pubYear, int copies) {
        this.id         = id;
        this.title      = title;
        this.authorId   = authorId;
        this.authorName = authorName;
        this.genre      = genre;
        this.isbn       = isbn;
        this.pubYear    = (pubYear != null) ? pubYear : 0;
        this.copies     = copies;
    }

    // ── Getters ───────────────────────────────────────────────────

    /** @return primary key of this book */
    public int    getId()         { return id; }

    /** @return title of this book */
    public String getTitle()      { return title; }

    /** @return FK to the {@code authors} table */
    public int    getAuthorId()   { return authorId; }

    /** @return denormalized author name (populated by JOIN, not stored) */
    public String getAuthorName() { return authorName; }

    /** @return genre string, or {@code null} */
    public String getGenre()      { return genre; }

    /** @return ISBN string, or {@code null} */
    public String getIsbn()       { return isbn; }

    /**
     * @return publication year, or {@code 0} if not set
     */
    public int    getPubYear()    { return pubYear; }

    /** @return number of physical copies available ({@code >= 0}) */
    public int    getCopies()     { return copies; }

    // ── Setters ───────────────────────────────────────────────────

    /** @param id primary key (assigned after INSERT) */
    public void setId(int id)                 { this.id = id; }

    /** @param title book title (must not be blank) */
    public void setTitle(String title)        { this.title = title; }

    /** @param authorId FK to the author (must reference an existing author) */
    public void setAuthorId(int authorId)     { this.authorId = authorId; }

    /** @param authorName denormalized name used for display */
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    /** @param genre genre string; {@code null} is allowed */
    public void setGenre(String genre)        { this.genre = genre; }

    /** @param isbn ISBN string; {@code null} is allowed */
    public void setIsbn(String isbn)          { this.isbn = isbn; }

    /**
     * @param pubYear publication year; pass {@code 0} to store NULL in the database
     */
    public void setPubYear(int pubYear)       { this.pubYear = pubYear; }

    /**
     * @param copies number of copies ({@code >= 0}); the DB enforces {@code copies >= 0}
     */
    public void setCopies(int copies)         { this.copies = copies; }

    // ── Object overrides ──────────────────────────────────────────

    /**
     * Two books are equal if and only if their {@link #id} values match.
     * This is used by JavaFX ComboBox to correctly pre-select items in edit dialogs.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return id == ((Book) o).id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    /**
     * Returns a human-readable string used by JavaFX ComboBox
     * via {@code StringConverter}.
     *
     * @return {@code "Title (Author Name)"}
     */
    @Override
    public String toString() { return title + " (" + authorName + ")"; }
}