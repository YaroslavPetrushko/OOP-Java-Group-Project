package com.library.model;

import java.time.LocalDate;

/**
 * Represents a book loan (borrowing record) in the Library Management System.
 *
 * <p>Maps to the {@code loans} table in the database:
 * <pre>
 *   id (PK) | book_id (FK) | reader_id (FK) | loan_date | due_date | status
 * </pre>
 *
 * <p>This is the associative entity between {@link Book} and {@link Reader} (M:N).
 * The fields {@link #bookTitle} and {@link #readerName} are denormalized JOIN values —
 * populated by DAO queries and <em>not</em> stored in the {@code loans} table.
 *
 * <p>The {@code status} field is constrained by the database to one of:
 * <ul>
 *   <li>{@code "active"}   — book is currently borrowed</li>
 *   <li>{@code "returned"} — book has been returned</li>
 *   <li>{@code "overdue"}  — return deadline has passed</li>
 * </ul>
 */
public class Loan {

    /** Primary key. Set by the database on INSERT. */
    private int       id;

    /** FK referencing {@code books.id}. */
    private int       bookId;

    /**
     * Denormalized book title populated via JOIN in DAO queries.
     * Not stored in the {@code loans} table; used only for display.
     */
    private String    bookTitle;

    /** FK referencing {@code readers.id}. */
    private int       readerId;

    /**
     * Denormalized reader name populated via JOIN in DAO queries.
     * Not stored in the {@code loans} table; used only for display.
     */
    private String    readerName;

    /** Date the book was lent out. Defaults to {@code CURRENT_DATE} in the database. */
    private LocalDate loanDate;

    /**
     * Deadline by which the book must be returned.
     * Must be after {@link #loanDate} (validated in the UI).
     */
    private LocalDate dueDate;

    /**
     * Borrowing status: {@code "active"}, {@code "returned"}, or {@code "overdue"}.
     * Constrained by a DB CHECK; defaults to {@code "active"}.
     */
    private String    status;

    /** No-arg constructor required for DAO result mapping. */
    public Loan() {}

    /**
     * Full constructor used by {@code LoanDaoImpl} when mapping a {@link java.sql.ResultSet}.
     *
     * @param id         primary key
     * @param bookId     FK to the book
     * @param bookTitle  denormalized book title from JOIN
     * @param readerId   FK to the reader
     * @param readerName denormalized reader name from JOIN
     * @param loanDate   date of borrowing
     * @param dueDate    return deadline
     * @param status     one of: {@code "active"}, {@code "returned"}, {@code "overdue"}
     */
    public Loan(int id, int bookId, String bookTitle, int readerId,
                String readerName, LocalDate loanDate,
                LocalDate dueDate, String status) {
        this.id         = id;
        this.bookId     = bookId;
        this.bookTitle  = bookTitle;
        this.readerId   = readerId;
        this.readerName = readerName;
        this.loanDate   = loanDate;
        this.dueDate    = dueDate;
        this.status     = status;
    }

    // ── Getters ───────────────────────────────────────────────────

    /** @return primary key of this loan */
    public int       getId()         { return id; }

    /** @return FK to the {@code books} table */
    public int       getBookId()     { return bookId; }

    /** @return denormalized book title (populated by JOIN, not stored) */
    public String    getBookTitle()  { return bookTitle; }

    /** @return FK to the {@code readers} table */
    public int       getReaderId()   { return readerId; }

    /** @return denormalized reader name (populated by JOIN, not stored) */
    public String    getReaderName() { return readerName; }

    /** @return date the book was lent out */
    public LocalDate getLoanDate()   { return loanDate; }

    /** @return return deadline */
    public LocalDate getDueDate()    { return dueDate; }

    /**
     * @return status string: {@code "active"}, {@code "returned"}, or {@code "overdue"}
     */
    public String    getStatus()     { return status; }

    // ── Setters ───────────────────────────────────────────────────

    /** @param id primary key (assigned after INSERT) */
    public void setId(int id)                { this.id = id; }

    /** @param bookId FK to the book (must reference an existing book) */
    public void setBookId(int bookId)        { this.bookId = bookId; }

    /** @param t denormalized book title used for display */
    public void setBookTitle(String t)       { this.bookTitle = t; }

    /** @param readerId FK to the reader (must reference an existing reader) */
    public void setReaderId(int readerId)    { this.readerId = readerId; }

    /** @param n denormalized reader name used for display */
    public void setReaderName(String n)      { this.readerName = n; }

    /** @param d date of borrowing */
    public void setLoanDate(LocalDate d)     { this.loanDate = d; }

    /** @param d return deadline (must be after loan date) */
    public void setDueDate(LocalDate d)      { this.dueDate = d; }

    /**
     * @param status one of: {@code "active"}, {@code "returned"}, {@code "overdue"}
     */
    public void setStatus(String status)     { this.status = status; }
}